package com.uniminuto.usuarios.repository;

import com.uniminuto.usuarios.model.Usuario;

import java.util.List;
import java.util.Optional;

/**
 * REPOSITORIO: Contrato de acceso a datos
 *
 * Definir el repositorio como INTERFAZ es clave para la testeabilidad:
 *
 *   → En pruebas UNITARIAS: se usa Mockito para crear un mock de esta interfaz.
 *     El servicio cree que habla con la BD, pero en realidad habla con un objeto
 *     controlado por el test. Velocidad: milisegundos.
 *
 *   → En pruebas de INTEGRACIÓN: se usa una implementación real (UsuarioRepositorioH2)
 *     conectada a H2 en memoria. Valida que SQL y mapeo funcionan correctamente.
 *
 * Este patrón se llama "Inversión de Dependencias" (la D de SOLID).
 */
public interface UsuarioRepositorio {

    Usuario guardar(Usuario usuario);

    Optional<Usuario> buscarPorId(Long id);

    Optional<Usuario> buscarPorEmail(String email);

    List<Usuario> listarTodos();

    void eliminar(Long id);

    boolean existePorEmail(String email);
}
