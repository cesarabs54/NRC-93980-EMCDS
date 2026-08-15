package com.uniminuto.usuarios.acceptance.steps;

import com.uniminuto.usuarios.model.Usuario;
import com.uniminuto.usuarios.repository.UsuarioRepositorioH2;
import com.uniminuto.usuarios.service.UsuarioServicio;
import io.cucumber.java.es.*;
import io.cucumber.java.Before;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.*;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║              DEFINICIONES DE PASOS (Step Definitions)           ║
 * ╠══════════════════════════════════════════════════════════════════╣
 * ║  Conecta el lenguaje natural del .feature con código Java.      ║
 * ║                                                                  ║
 * ║  Cada anotación @Dado/@Cuando/@Entonces mapea una línea del     ║
 * ║  escenario a un método Java.                                    ║
 * ║                                                                  ║
 * ║  Las pruebas de aceptación usan componentes REALES (no mocks)   ║
 * ║  porque validan el comportamiento observable del sistema,        ║
 * ║  no los detalles internos de implementación.                    ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
public class GestionUsuariosSteps {

    // El contexto compartido entre pasos del mismo escenario
    private UsuarioServicio servicio;
    private Connection conexion;
    private Usuario ultimoRegistrado;
    private Exception ultimaExcepcion;
    private boolean resultadoElegibilidad;

    /**
     * @Before de Cucumber: se ejecuta antes de CADA escenario.
     * Garantiza BD limpia y componentes frescos por escenario.
     */
    @Before
    public void inicializar() throws Exception {
        conexion = DriverManager.getConnection(
            "jdbc:h2:mem:acceptance_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1",
            "sa", ""
        );
        UsuarioRepositorioH2 repositorio = new UsuarioRepositorioH2(conexion);
        servicio = new UsuarioServicio(repositorio);
        ultimoRegistrado = null;
        ultimaExcepcion = null;
    }

    // ================================================================
    // PASOS "DADO" — Configuración del estado inicial
    // ================================================================

    @Dado("que el sistema no tiene ningún usuario registrado con email {string}")
    public void dadoSinUsuarioConEmail(String email) {
        // La BD está vacía por el @Before, pero verificamos explícitamente
        // para que el escenario sea auto-documentado y robusto
        assertThat(servicio.listarTodos()).isEmpty();
    }

    @Dado("que ya existe un usuario registrado con email {string}")
    public void dadoExisteUsuarioConEmail(String email) {
        // Registramos un usuario previo para crear la condición del escenario
        servicio.registrar(Usuario.builder()
                .nombre("Usuario Previo")
                .email(email)
                .edad(30)
                .build());
    }

    @Dado("que existe un usuario activo con email {string}")
    public void dadoExisteUsuarioActivoConEmail(String email) {
        ultimoRegistrado = servicio.registrar(Usuario.builder()
                .nombre("Usuario Activo")
                .email(email)
                .edad(25)
                .build());
    }

    @Dado("que tengo un usuario activo con {int} años de edad")
    public void dadoUsuarioActivoConEdad(int edad) {
        // Creamos el usuario en memoria con los parámetros del escenario
        ultimoRegistrado = Usuario.builder()
                .nombre("Usuario Test")
                .email("test" + edad + "@sistema.com")
                .edad(edad)
                .activo(true)
                .build();
        // Lo registramos para que tenga estado real
        ultimoRegistrado = servicio.registrar(ultimoRegistrado);
    }

    // ================================================================
    // PASOS "CUANDO" — Acciones del usuario sobre el sistema
    // ================================================================

    @Cuando("registro un usuario con nombre {string}, email {string} y edad {int}")
    public void cuandoRegistroUsuario(String nombre, String email, int edad) {
        try {
            ultimoRegistrado = servicio.registrar(Usuario.builder()
                    .nombre(nombre)
                    .email(email)
                    .edad(edad)
                    .build());
        } catch (Exception e) {
            ultimaExcepcion = e;
        }
    }

    @Cuando("intento registrar otro usuario con el mismo email {string}")
    public void cuandoIntentoDuplicarEmail(String email) {
        try {
            servicio.registrar(Usuario.builder()
                    .nombre("Intento Duplicado")
                    .email(email)
                    .edad(25)
                    .build());
        } catch (Exception e) {
            // Capturamos la excepción para verificarla en los pasos "Entonces"
            ultimaExcepcion = e;
        }
    }

    @Cuando("desactivo al usuario con email {string}")
    public void cuandoDesactivoUsuario(String email) {
        // Buscamos el id del usuario por email para poder desactivarlo
        servicio.listarTodos().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .ifPresent(u -> servicio.desactivar(u.getId()));
    }

    @Cuando("consulto si el usuario es elegible para servicios premium")
    public void cuandoConsultoElegibilidad() {
        resultadoElegibilidad = ultimoRegistrado.esElegibleParaPremium();
    }

    // ================================================================
    // PASOS "ENTONCES" — Verificación de resultados
    // ================================================================

    @Entonces("el sistema confirma el registro con un identificador único")
    public void entoncesRegistroConfirmado() {
        assertThat(ultimaExcepcion)
                .as("No debe haber excepción en un registro válido")
                .isNull();
        assertThat(ultimoRegistrado.getId())
                .as("El sistema debe asignar un ID único")
                .isNotNull()
                .isPositive();
    }

    @Y("el usuario aparece como activo en el sistema")
    public void yUsuarioActivo() {
        assertThat(ultimoRegistrado.isActivo())
                .as("Todo usuario nuevo debe estar activo")
                .isTrue();
    }

    @Entonces("el sistema rechaza el registro")
    public void entoncesRegistroRechazado() {
        assertThat(ultimaExcepcion)
                .as("El sistema debió lanzar una excepción")
                .isNotNull();
    }

    @Y("el sistema informa que el email ya está en uso")
    public void yMensajeEmailDuplicado() {
        assertThat(ultimaExcepcion.getMessage())
                .as("El mensaje debe indicar el problema del email")
                .containsIgnoringCase("ya existe");
    }

    @Entonces("el usuario con email {string} aparece como inactivo")
    public void entoncesUsuarioInactivo(String email) {
        boolean inactivo = servicio.listarTodos().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .map(u -> !u.isActivo())
                .orElse(false);

        assertThat(inactivo)
                .as("El usuario debe aparecer como inactivo tras la desactivación")
                .isTrue();
    }

    @Y("el usuario sigue existiendo en el sistema \\(no fue eliminado)")
    public void yUsuarioSigueExistiendo() {
        // Verifica que el soft delete no eliminó el registro
        long count = servicio.listarTodos().stream()
                .filter(u -> u.getEmail().equals("temporal@sistema.com"))
                .count();
        assertThat(count)
                .as("El usuario debe seguir en BD aunque esté inactivo")
                .isEqualTo(1);
    }

    @Entonces("el sistema confirma que el usuario es elegible para premium")
    public void entoncesElegibleParaPremium() {
        assertThat(resultadoElegibilidad)
                .as("El usuario debería ser elegible para premium")
                .isTrue();
    }

    @Entonces("el sistema confirma que el usuario NO es elegible para premium")
    public void entoncesNoElegibleParaPremium() {
        assertThat(resultadoElegibilidad)
                .as("El usuario no debería ser elegible para premium")
                .isFalse();
    }
}
