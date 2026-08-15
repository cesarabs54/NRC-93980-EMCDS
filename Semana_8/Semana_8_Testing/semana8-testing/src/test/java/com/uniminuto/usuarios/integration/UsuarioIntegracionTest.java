package com.uniminuto.usuarios.integration;

import com.uniminuto.usuarios.exception.UsuarioNoEncontradoException;
import com.uniminuto.usuarios.model.Usuario;
import com.uniminuto.usuarios.repository.UsuarioRepositorioH2;
import com.uniminuto.usuarios.service.UsuarioServicio;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║             SUITE DE PRUEBAS DE INTEGRACIÓN                     ║
 * ╠══════════════════════════════════════════════════════════════════╣
 * ║  Propósito:   Verificar que SERVICIO + REPOSITORIO + BD         ║
 * ║               funcionan correctamente juntos.                   ║
 * ║               No hay mocks: todo es real.                       ║
 * ║                                                                  ║
 * ║  Herramientas:                                                   ║
 * ║   · JUnit 5      → estructura de pruebas                        ║
 * ║   · H2 Database  → BD SQL en memoria (sin servidor externo)     ║
 * ║   · JDBC puro    → interacción real con la BD                   ║
 * ║   · AssertJ      → aserciones fluidas                           ║
 * ║                                                                  ║
 * ║  Ejecución:   mvn test -Dtest=UsuarioIntegracionTest            ║
 * ║                                                                  ║
 * ║  Velocidad:   200-500 ms. Hay I/O (BD en memoria).             ║
 * ║                                                                  ║
 * ║  ¿Qué detectan que las unitarias NO pueden?                     ║
 * ║   · Errores en el SQL (typos, JOIN incorrectos)                 ║
 * ║   · Mapeo incorrecto de columnas a atributos                    ║
 * ║   · Constraints de BD (UNIQUE, NOT NULL, FK)                    ║
 * ║   · Transacciones y commits                                     ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
@DisplayName("Integración: UsuarioServicio + UsuarioRepositorioH2")
class UsuarioIntegracionTest {

    // Componentes REALES — sin mocks
    private Connection conexion;
    private UsuarioRepositorioH2 repositorio;
    private UsuarioServicio servicio;

    /**
     * @BeforeEach: se ejecuta ANTES DE CADA prueba.
     * Cada test recibe una BD vacía y limpia → independencia total entre tests.
     * Si un test deja datos sucios, no afecta al siguiente.
     */
    @BeforeEach
    void configurar() throws SQLException {
        // H2 en modo "mem": vive solo mientras dure la conexión
        // "DB_CLOSE_DELAY=-1": mantiene la BD mientras la JVM siga corriendo
        conexion = DriverManager.getConnection(
            "jdbc:h2:mem:testdb_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1", 
            "sa", ""
        );
        repositorio = new UsuarioRepositorioH2(conexion);  // Crea tabla automáticamente
        servicio = new UsuarioServicio(repositorio);        // Servicio REAL
    }

    /**
     * @AfterEach: limpieza después de cada test.
     * Cierra la conexión → H2 destruye la BD en memoria.
     */
    @AfterEach
    void limpiar() throws SQLException {
        if (conexion != null && !conexion.isClosed()) {
            try (Statement stmt = conexion.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS usuarios");
            }
            conexion.close();
        }
    }

    // ================================================================
    // INTEGRACIÓN 1: Flujo completo de registro y recuperación
    // ================================================================

    @Test
    @DisplayName("✅ INTEGRACIÓN 1: Registrar usuario y recuperarlo por ID")
    void flujoRegistroYBusqueda_debePersistitrYRecuperarCorrectamente() {
        // ARRANGE
        Usuario nuevo = Usuario.builder()
                .nombre("Laura Rodríguez")
                .email("laura@ejemplo.com")
                .edad(25)
                .build();

        // ACT — Operación 1: registrar (persiste en H2)
        Usuario registrado = servicio.registrar(nuevo);

        // ACT — Operación 2: recuperar por el id que asignó la BD
        Usuario recuperado = servicio.buscarPorId(registrado.getId());

        // ASSERT
        // Verificamos que lo que salió de la BD es lo mismo que guardamos
        assertThat(recuperado.getId()).isNotNull();
        assertThat(recuperado.getNombre()).isEqualTo("Laura Rodríguez");
        assertThat(recuperado.getEmail()).isEqualTo("laura@ejemplo.com");
        assertThat(recuperado.getEdad()).isEqualTo(25);
        assertThat(recuperado.isActivo()).isTrue(); // el servicio pone activo=true
    }

    // ================================================================
    // INTEGRACIÓN 2: Restricción UNIQUE del email en la BD real
    // ================================================================

    @Test
    @DisplayName("❌ INTEGRACIÓN 2: No permite registrar dos usuarios con el mismo email")
    void registrar_emailDuplicado_debeLanzarExcepcionEnCualquierCapa() {
        // ARRANGE — registramos el primero exitosamente
        Usuario primero = Usuario.builder()
                .nombre("Ana Torres")
                .email("ana@ejemplo.com")
                .edad(30)
                .build();
        servicio.registrar(primero);

        // ACT — intentamos registrar otro con el mismo email
        Usuario segundo = Usuario.builder()
                .nombre("Otro Ana")
                .email("ana@ejemplo.com")    // mismo email → debe fallar
                .edad(22)
                .build();

        // ASSERT
        // La validación la atrapa el SERVICIO antes de llegar a la BD.
        // Pero si la validación fallara, la BD también lo rechazaría (UNIQUE).
        // Esta prueba confirma que al menos UNA de las dos capas protege el sistema.
        assertThatThrownBy(() -> servicio.registrar(segundo))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ================================================================
    // INTEGRACIÓN 3: Actualización y persistencia de cambios
    // ================================================================

    @Test
    @DisplayName("✅ INTEGRACIÓN 3: Actualizar nombre y verificar persistencia")
    void actualizar_debeReflejarCambiosEnBD() {
        // ARRANGE
        Usuario usuario = servicio.registrar(
            Usuario.builder()
                .nombre("Pedro Gómez")
                .email("pedro@ejemplo.com")
                .edad(40)
                .build()
        );

        // ACT
        Usuario datosActualizados = Usuario.builder()
                .nombre("Pedro Alberto Gómez")   // nombre ampliado
                .email("pedro@ejemplo.com")       // mismo email
                .edad(41)                          // cumpleaños
                .build();

        servicio.actualizar(usuario.getId(), datosActualizados);

        // ASSERT — recuperamos de nuevo para confirmar persistencia real
        Usuario verificado = servicio.buscarPorId(usuario.getId());
        assertThat(verificado.getNombre()).isEqualTo("Pedro Alberto Gómez");
        assertThat(verificado.getEdad()).isEqualTo(41);
    }

    // ================================================================
    // INTEGRACIÓN 4: Desactivación (soft delete) y listado
    // ================================================================

    @Test
    @DisplayName("✅ INTEGRACIÓN 4: Desactivar usuario actualiza estado en BD")
    void desactivar_debeMarcarUsuarioComoInactivo() {
        // ARRANGE
        Usuario usuario = servicio.registrar(
            Usuario.builder()
                .nombre("Sofía Herrera")
                .email("sofia@ejemplo.com")
                .edad(28)
                .build()
        );
        assertThat(usuario.isActivo()).isTrue(); // Precondición

        // ACT
        servicio.desactivar(usuario.getId());

        // ASSERT — verificar en BD que el cambio fue real, no solo en memoria
        Usuario desactivado = servicio.buscarPorId(usuario.getId());
        assertThat(desactivado.isActivo()).isFalse();
    }

    // ================================================================
    // INTEGRACIÓN 5: Eliminar un ID inexistente
    // ================================================================

    @Test
    @DisplayName("❌ INTEGRACIÓN 5: Eliminar ID inexistente lanza excepción")
    void eliminar_idInexistente_debeLanzarExcepcion() {
        // Esta prueba verifica que la interacción servicio→repositorio
        // propaga correctamente el error de "no encontrado"
        assertThatThrownBy(() -> servicio.eliminar(9999L))
                .isInstanceOf(UsuarioNoEncontradoException.class);
    }

    // ================================================================
    // INTEGRACIÓN 6: Listar múltiples registros
    // ================================================================

    @Test
    @DisplayName("✅ INTEGRACIÓN 6: Listar todos retorna todos los registros persistidos")
    void listarTodos_conVariosUsuarios_debeRetornarListaCompleta() {
        // ARRANGE — guardamos 3 usuarios
        servicio.registrar(Usuario.builder().nombre("U1").email("u1@e.com").edad(20).build());
        servicio.registrar(Usuario.builder().nombre("U2").email("u2@e.com").edad(21).build());
        servicio.registrar(Usuario.builder().nombre("U3").email("u3@e.com").edad(22).build());

        // ACT
        List<Usuario> lista = servicio.listarTodos();

        // ASSERT
        assertThat(lista).hasSize(3);
        assertThat(lista).extracting(Usuario::getNombre)
                .containsExactlyInAnyOrder("U1", "U2", "U3");
    }
}
