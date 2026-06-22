package com.um.umbook.exception;

/**
 * Se lanza al registrar un usuario cuyo email o nombre de usuario ya existe.
 */
public class UsuarioYaExisteException extends RuntimeException {

    public UsuarioYaExisteException(String mensaje) {
        super(mensaje);
    }
}
