package com.um.umbook.event;

import com.um.umbook.model.SolicitudAmistad;

/**
 * Evento de dominio: se creo una solicitud de amistad (CU-14). Lo publica
 * {@code SolicitudAmistadService} y reacciona {@code NotificacionEventListener}.
 * Asi el envio de la solicitud no conoce al subsistema de notificaciones (observer).
 */
public class SolicitudAmistadCreadaEvent {

    private final SolicitudAmistad solicitud;

    public SolicitudAmistadCreadaEvent(SolicitudAmistad solicitud) {
        this.solicitud = solicitud;
    }

    public SolicitudAmistad getSolicitud() {
        return solicitud;
    }
}
