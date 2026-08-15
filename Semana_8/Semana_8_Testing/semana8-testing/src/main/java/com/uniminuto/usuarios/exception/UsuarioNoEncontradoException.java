package com.uniminuto.usuarios.exception;

/**
 * EXCEPCIONES DE DOMINIO
 *
 * Usar excepciones propias en lugar de excepciones genéricas tiene dos ventajas:
 *  1. El código de negocio expresa su intención claramente.
 *  2. Las pruebas pueden verificar el tipo exacto de excepción lanzada,
 *     no solo el mensaje (que es frágil y puede cambiar).
 *
 * En las pruebas unitarias veremos: assertThrows(UsuarioNoEncontradoException.class, ...)
 */
public class UsuarioNoEncontradoException extends RuntimeException {

    private final Long id;

    public UsuarioNoEncontradoException(Long id) {
        super("No se encontró el usuario con id: " + id);
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
