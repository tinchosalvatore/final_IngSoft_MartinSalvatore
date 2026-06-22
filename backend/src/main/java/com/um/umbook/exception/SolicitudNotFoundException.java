package com.um.umbook.exception;

/**
 * Se lanza al aceptar/rechazar una solicitud de amistad inexistente.
 */
public class SolicitudNotFoundException extends RuntimeException {

    public SolicitudNotFoundException(String mensaje) {
        super(mensaje);
    }
}
