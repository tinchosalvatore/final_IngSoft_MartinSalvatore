package com.um.umbook.exception;

/**
 * Se lanza cuando una busqueda de usuarios no arroja resultados (CU-13, alt "Lista vacia").
 */
public class UsuarioNotFoundException extends RuntimeException {

    public UsuarioNotFoundException(String mensaje) {
        super(mensaje);
    }
}
