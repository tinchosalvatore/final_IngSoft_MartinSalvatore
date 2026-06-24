package com.um.umbook.exception;

/**
 * CU-2 (alt 3.1): credenciales invalidas al iniciar sesion (email inexistente o
 * contrasena incorrecta). Se traduce a HTTP 401.
 */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
