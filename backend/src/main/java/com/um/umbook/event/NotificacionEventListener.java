package com.um.umbook.event;

import com.um.umbook.model.SolicitudAmistad;
import com.um.umbook.model.TipoNotificacion;
import com.um.umbook.model.Usuario;
import com.um.umbook.service.AmistadService;
import com.um.umbook.service.JavaMailService;
import com.um.umbook.service.NotificacionService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Observador del subsistema de notificaciones: "esta atento" a los eventos de dominio y,
 * cuando ocurren, crea+emite la notificacion (toast SSE en vivo) y manda el email (stub).
 *
 * De esta forma el dominio (enviar solicitud, batch de cumpleaños) solo publica QUE paso;
 * no sabe que existe NotificacionService. Es el patron observer del diagrama: las
 * notificaciones reaccionan a hechos reales, no se disparan a mano.
 */
@Component
public class NotificacionEventListener {

    private final NotificacionService notificacionService;
    private final JavaMailService mailService;
    private final AmistadService amistadService;

    public NotificacionEventListener(NotificacionService notificacionService,
                                     JavaMailService mailService,
                                     AmistadService amistadService) {
        this.notificacionService = notificacionService;
        this.mailService = mailService;
        this.amistadService = amistadService;
    }

    /** CU-14: al crearse una solicitud, notifica al destinatario y manda el email. */
    @EventListener
    public void alCrearseSolicitud(SolicitudAmistadCreadaEvent evento) {
        SolicitudAmistad solicitud = evento.getSolicitud();
        Usuario remitente = solicitud.getRemitente();

        mailService.enviarEmailSolicitudAmistad(solicitud);

        String mensaje = remitente.getNombre() + " " + remitente.getApellido()
                + " te envio una solicitud de amistad";
        notificacionService.crearNotificacion(solicitud.getDestinatario(),
                TipoNotificacion.SOLICITUD_AMISTAD, solicitud.getId(), mensaje);
    }

    /** CU-15: por cada cumpleañero detectado, notifica a cada uno de sus amigos. */
    @EventListener
    public void alDetectarCumpleanos(CumpleanosEvent evento) {
        Usuario cumpleanero = evento.getCumpleanero();
        String mensaje = cumpleanero.getNombre() + " " + cumpleanero.getApellido()
                + " cumple años hoy";

        for (Usuario amigo : amistadService.obtenerAmigos(cumpleanero)) {
            notificacionService.crearNotificacion(amigo, TipoNotificacion.CUMPLEANOS,
                    cumpleanero.getId(), mensaje);
            mailService.enviarEmailCumpleanos(amigo, cumpleanero);
        }
    }
}
