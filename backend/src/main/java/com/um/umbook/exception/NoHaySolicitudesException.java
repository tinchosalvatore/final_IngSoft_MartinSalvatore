package com.um.umbook.exception;

/**
 * CU-18 (alt 4.1): el usuario no tiene solicitudes de amistad pendientes. Se traduce a HTTP 404.
 */
public class NoHaySolicitudesException extends RuntimeException {

    public NoHaySolicitudesException(String mensaje) {
        super(mensaje);
    }
}
