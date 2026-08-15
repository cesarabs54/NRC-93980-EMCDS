package com.uniminuto.usuarios.service;

import com.uniminuto.usuarios.exception.UsuarioNoEncontradoException;
import com.uniminuto.usuarios.model.Usuario;
import com.uniminuto.usuarios.repository.UsuarioRepositorio;

import java.util.List;

/**
 * CAPA DE SERVICIO: Lógica de negocio de gestión de usuarios
 *
 * Esta clase es el OBJETIVO PRINCIPAL de las pruebas unitarias.
 * Contiene las reglas de negocio que no dependen de la tecnología:
 *  - Validaciones
 *  - Orquestación de operaciones
 *  - Decisiones de dominio
 *
 * DISEÑO PARA TESTEABILIDAD:
 *  El repositorio se INYECTA por constructor (no se instancia internamente).
 *  Esto permite que en pruebas unitarias se pase un mock, y en integración
 *  se pase la implementación real. Este patrón es Inyección de Dependencias.
 *
 * ¿Qué NO va aquí? Detalles de HTTP (status codes), SQL, o formateo de respuestas.
 * Esas son responsabilidades de otras capas.
 */
public class UsuarioServicio {

    private final UsuarioRepositorio repositorio;

    // Constructor injection: la única forma de crear el servicio
    public UsuarioServicio(UsuarioRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * REGLA DE NEGOCIO #1: No se pueden registrar emails duplicados.
     * Esta validación protege la integridad del sistema independientemente
     * de si la BD tiene o no un constraint UNIQUE.
     */
    public Usuario registrar(Usuario usuario) {
        validarCamposObligatorios(usuario);

        if (repositorio.existePorEmail(usuario.getEmail())) {
            throw new IllegalArgumentException(
                "Ya existe un usuario con el email: " + usuario.getEmail()
            );
        }

        usuario.setActivo(true); // Por defecto, todo usuario nuevo está activo
        return repositorio.guardar(usuario);
    }

    /**
     * REGLA DE NEGOCIO #2: Buscar un usuario inexistente es un error explícito,
     * no un "null silencioso". El llamador sabe qué pasó.
     */
    public Usuario buscarPorId(Long id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));
    }

    public List<Usuario> listarTodos() {
        return repositorio.listarTodos();
    }

    /**
     * REGLA DE NEGOCIO #3: No se puede actualizar un usuario que no existe.
     * REGLA DE NEGOCIO #4: No se puede cambiar el email por uno ya registrado.
     */
    public Usuario actualizar(Long id, Usuario datosNuevos) {
        Usuario existente = buscarPorId(id); // Lanza excepción si no existe

        // Verificar conflicto de email solo si está cambiando
        if (!existente.getEmail().equals(datosNuevos.getEmail())
                && repositorio.existePorEmail(datosNuevos.getEmail())) {
            throw new IllegalArgumentException(
                "El email " + datosNuevos.getEmail() + " ya está en uso"
            );
        }

        existente.setNombre(datosNuevos.getNombre());
        existente.setEmail(datosNuevos.getEmail());
        existente.setEdad(datosNuevos.getEdad());

        return repositorio.guardar(existente);
    }

    /**
     * REGLA DE NEGOCIO #5: Eliminación lógica preferida sobre física.
     * Aquí implementamos desactivación (soft delete) en lugar de borrado.
     *
     * NOTA DIDÁCTICA: Esta decisión tiene impacto en las pruebas de aceptación:
     * un usuario "eliminado" sigue existiendo en BD pero no debe aparecer
     * en las consultas de usuarios activos.
     */
    public void desactivar(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.setActivo(false);
        repositorio.guardar(usuario);
    }

    /** Eliminar físicamente (para casos que lo requieran explícitamente) */
    public void eliminar(Long id) {
        buscarPorId(id); // Valida que existe antes de eliminar
        repositorio.eliminar(id);
    }

    // ----------------------------------------------------------------
    // Métodos de validación privados
    // ----------------------------------------------------------------

    private void validarCamposObligatorios(Usuario usuario) {
        if (usuario.getNombre() == null || usuario.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (usuario.getEmail() == null || !usuario.getEmail().contains("@")) {
            throw new IllegalArgumentException("El email no es válido");
        }
        if (usuario.getEdad() < 0 || usuario.getEdad() > 150) {
            throw new IllegalArgumentException("La edad no es válida: " + usuario.getEdad());
        }
    }
}
