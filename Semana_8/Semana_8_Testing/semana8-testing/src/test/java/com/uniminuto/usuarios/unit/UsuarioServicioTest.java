package com.uniminuto.usuarios.unit;

import com.uniminuto.usuarios.exception.UsuarioNoEncontradoException;
import com.uniminuto.usuarios.model.Usuario;
import com.uniminuto.usuarios.repository.UsuarioRepositorio;
import com.uniminuto.usuarios.service.UsuarioServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║             SUITE DE PRUEBAS UNITARIAS                          ║
 * ╠══════════════════════════════════════════════════════════════════╣
 * ║  Propósito:   Verificar la LÓGICA DE NEGOCIO en completo        ║
 * ║               aislamiento. No hay BD, no hay red, no hay FS.    ║
 * ║                                                                  ║
 * ║  Herramientas:                                                   ║
 * ║   · JUnit 5    → estructura y aserción de pruebas               ║
 * ║   · Mockito    → simulación de dependencias (repositorio)        ║
 * ║   · AssertJ    → aserciones fluidas y legibles                  ║
 * ║                                                                  ║
 * ║  Ejecución:   mvn test -Dtest=UsuarioServicioTest               ║
 * ║               o clic derecho → Run en IntelliJ IDEA             ║
 * ║                                                                  ║
 * ║  Velocidad:   < 100 ms. Sin I/O, sin dependencias externas.     ║
 * ╚══════════════════════════════════════════════════════════════════╝
 *
 * ANATOMÍA DE CADA PRUEBA — patrón AAA (Arrange / Act / Assert):
 *   Arrange → preparar el escenario y configurar los mocks
 *   Act     → invocar el método que se está probando (SUT: System Under Test)
 *   Assert  → verificar el resultado esperado
 */
@ExtendWith(MockitoExtension.class)       // Activa la integración JUnit 5 + Mockito
@DisplayName("UsuarioServicio - Pruebas Unitarias")
class UsuarioServicioTest {

    /**
     * @Mock crea un DOBLE DE PRUEBA del repositorio.
     * No toca ninguna BD: devuelve lo que configuremos con when(...).thenReturn(...)
     * Mockito registra cada llamada para que podamos verificarlas con verify(...)
     */
    @Mock
    private UsuarioRepositorio repositorioMock;

    /**
     * @InjectMocks crea la instancia real de UsuarioServicio
     * e inyecta automáticamente el mock anterior por constructor.
     * Esto es lo que queremos probar: el servicio REAL con dependencias FALSAS.
     */
    @InjectMocks
    private UsuarioServicio servicio;

    // Datos de prueba reutilizables
    private Usuario usuarioValido;

    @BeforeEach
    void configurarDatosDePrueba() {
        // Builder de Lombok: legible y sin setters encadenados
        usuarioValido = Usuario.builder()
                .nombre("Carlos Pérez")
                .email("carlos@ejemplo.com")
                .edad(30)
                .build();
    }

    // ================================================================
    // CASO 1: Registro exitoso de un usuario nuevo
    // ================================================================
    @Nested
    @DisplayName("Registro de usuarios")
    class RegistroUsuarios {

        @Test
        @DisplayName("✅ CASO 1: Registra correctamente un usuario con datos válidos")
        void registrar_conDatosValidos_debeGuardarYRetornarUsuario() {
            // ARRANGE
            // Configuramos el mock: cuando pregunte si el email existe → false
            when(repositorioMock.existePorEmail("carlos@ejemplo.com"))
                    .thenReturn(false);

            // Cuando llame a guardar, devuelve el usuario con id asignado
            Usuario usuarioGuardado = Usuario.builder()
                    .id(1L)
                    .nombre("Carlos Pérez")
                    .email("carlos@ejemplo.com")
                    .edad(30)
                    .activo(true)
                    .build();
            when(repositorioMock.guardar(any(Usuario.class)))
                    .thenReturn(usuarioGuardado);

            // ACT
            Usuario resultado = servicio.registrar(usuarioValido);

            // ASSERT — AssertJ: sintaxis fluida, mensajes de error descriptivos
            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.isActivo()).isTrue();
            assertThat(resultado.getNombre()).isEqualTo("Carlos Pérez");

            // VERIFICACIÓN DE INTERACCIÓN: ¿el servicio invocó guardar exactamente 1 vez?
            // Esto detecta bugs donde el servicio olvida persistir el objeto.
            verify(repositorioMock, times(1)).guardar(any(Usuario.class));
        }

        @Test
        @DisplayName("❌ CASO 2: Lanza excepción si el email ya está registrado")
        void registrar_conEmailDuplicado_debeLanzarExcepcion() {
            // ARRANGE
            // Simulamos que el email YA existe en el sistema
            when(repositorioMock.existePorEmail("carlos@ejemplo.com"))
                    .thenReturn(true);

            // ACT + ASSERT
            // assertThatThrownBy captura la excepción y permite verificar
            // tipo, mensaje y otras propiedades — todo en una sola expresión.
            assertThatThrownBy(() -> servicio.registrar(usuarioValido))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Ya existe un usuario");

            // VERIFICACIÓN NEGATIVA: guardar NUNCA debe llamarse si el email ya existe
            // Un fallo aquí indica que el servicio tiene un bug de guard clause
            verify(repositorioMock, never()).guardar(any());
        }

        @Test
        @DisplayName("❌ CASO 3: Lanza excepción si el nombre está vacío")
        void registrar_conNombreVacio_debeLanzarExcepcion() {
            // ARRANGE
            Usuario usuarioSinNombre = Usuario.builder()
                    .nombre("  ")    // solo espacios → isBlank() = true
                    .email("test@ejemplo.com")
                    .edad(25)
                    .build();

            // ACT + ASSERT
            assertThatThrownBy(() -> servicio.registrar(usuarioSinNombre))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("nombre es obligatorio");

            // El repositorio no debe ser consultado si la validación falla primero
            verifyNoInteractions(repositorioMock);
        }
    }

    // ================================================================
    // CASO 4: Búsqueda de usuario por ID
    // ================================================================
    @Nested
    @DisplayName("Búsqueda de usuarios")
    class BusquedaUsuarios {

        @Test
        @DisplayName("✅ CASO 4: Retorna el usuario cuando el ID existe")
        void buscarPorId_cuandoExiste_debeRetornarUsuario() {
            // ARRANGE
            Usuario esperado = Usuario.builder()
                    .id(42L)
                    .nombre("María García")
                    .email("maria@ejemplo.com")
                    .edad(28)
                    .activo(true)
                    .build();

            // Optional.of simula un registro encontrado en BD
            when(repositorioMock.buscarPorId(42L))
                    .thenReturn(Optional.of(esperado));

            // ACT
            Usuario resultado = servicio.buscarPorId(42L);

            // ASSERT
            assertThat(resultado)
                    .isNotNull()
                    .extracting(Usuario::getId, Usuario::getNombre)
                    .containsExactly(42L, "María García");
        }

        @Test
        @DisplayName("❌ CASO 5: Lanza UsuarioNoEncontradoException si el ID no existe")
        void buscarPorId_cuandoNoExiste_debeLanzarExcepcionDeDominio() {
            // ARRANGE
            // Optional.empty() simula que no hay registro en BD para ese id
            when(repositorioMock.buscarPorId(99L))
                    .thenReturn(Optional.empty());

            // ACT + ASSERT
            // Verificamos la excepción de DOMINIO (no una genérica).
            // También verificamos que la excepción contiene el id que falló.
            assertThatThrownBy(() -> servicio.buscarPorId(99L))
                    .isInstanceOf(UsuarioNoEncontradoException.class)
                    .hasMessageContaining("99");
        }
    }

    // ================================================================
    // CASO 6: Elegibilidad para premium (lógica en el modelo)
    // ================================================================
    @Nested
    @DisplayName("Reglas de negocio en el modelo")
    class ReglasDeNegocio {

        @Test
        @DisplayName("✅ CASO 6: Usuario activo mayor de edad es elegible para premium")
        void esElegibleParaPremium_activo18Anos_debeRetornarTrue() {
            // NOTA: Esta prueba NO usa mock porque está probando lógica
            // del modelo (Usuario), no del servicio. No hay dependencias externas.
            Usuario usuario = Usuario.builder()
                    .activo(true)
                    .edad(18)
                    .build();

            assertThat(usuario.esElegibleParaPremium()).isTrue();
        }

        @Test
        @DisplayName("❌ CASO 7: Usuario inactivo NO es elegible aunque tenga 18+")
        void esElegibleParaPremium_inactivo_debeRetornarFalse() {
            Usuario usuario = Usuario.builder()
                    .activo(false)
                    .edad(30)
                    .build();

            assertThat(usuario.esElegibleParaPremium()).isFalse();
        }

        @Test
        @DisplayName("❌ CASO 8: Menor de edad NO es elegible aunque esté activo")
        void esElegibleParaPremium_menorDeEdad_debeRetornarFalse() {
            Usuario usuario = Usuario.builder()
                    .activo(true)
                    .edad(17)
                    .build();

            assertThat(usuario.esElegibleParaPremium()).isFalse();
        }
    }
}
