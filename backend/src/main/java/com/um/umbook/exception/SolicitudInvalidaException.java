package com.um.umbook.exception;

/**
 * CU-6 (alternativas): no se puede enviar la solicitud porque ya son amigos o ya existe una
 * solicitud pendiente entre ambos. Se traduce a HTTP 409.
 */
public class SolicitudInvalidaException extends RuntimeException {

    public SolicitudInvalidaException(String mensaje) {
        super(mensaje);
    }
}
