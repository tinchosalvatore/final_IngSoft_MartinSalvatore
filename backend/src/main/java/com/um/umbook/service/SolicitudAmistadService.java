package com.um.umbook.service;

import com.um.umbook.dto.SolicitudAmistadDTO;
import com.um.umbook.exception.SolicitudNotFoundException;
import com.um.umbook.model.Amistad;
import com.um.umbook.model.EstadoSolicitud;
import com.um.umbook.model.SolicitudAmistad;
import com.um.umbook.model.TipoNotificacion;
import com.um.umbook.model.Usuario;
import com.um.umbook.repository.SolicitudAmistadRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Logica de solicitudes de amistad (CU-14). Al enviar una solicitud la persiste y, segun el
 * diagrama de clases, llama directamente al servicio de notificaciones (toast en vivo) y al de
 * mail (email stub). Campos y metodos 1:1 con el diagrama de clases.
 */
@Service
public class SolicitudAmistadService {

    private final SolicitudAmistadRepository solicitudRepository;
    private final AmistadService amistadService;
    private final NotificacionService notificacionService;
    private final JavaMailService mailService;

    public SolicitudAmistadService(SolicitudAmistadRepository solicitudRepository,
                                   AmistadService amistadService,
                                   NotificacionService notificacionService,
                                   JavaMailService mailService) {
        this.solicitudRepository = solicitudRepository;
        this.amistadService = amistadService;
        this.notificacionService = notificacionService;
        this.mailService = mailService;
    }

    public String generarTokenEmail() {
        return UUID.randomUUID().toString();
    }

    /**
     * CU-14: envia una solicitud de amistad, la persiste y notifica al destinatario por
     * llamada directa: email (stub) + notificacion en vivo (toast SSE).
     */
    public SolicitudAmistad enviarSolicitud(Usuario remitente, Usuario destinatario) {
        SolicitudAmistad solicitud = new SolicitudAmistad(remitente, destinatario, generarTokenEmail());
        solicitud = solicitudRepository.save(solicitud);

        mailService.enviarEmailSolicitudAmistad(solicitud);

        String mensaje = remitente.getNombre() + " " + remitente.getApellido()
                + " te envio una solicitud de amistad";
        notificacionService.crearNotificacion(destinatario, TipoNotificacion.SOLICITUD_AMISTAD,
                solicitud.getId(), mensaje);

        return solicitud;
    }

    /**
     * EXTRA (no esta en el diagrama, ver docs/EXTRAS.md): solicitudes pendientes del usuario,
     * para la pantalla del click en la notificacion (subflujo CU-14 de los diag. de secuencia).
     */
    public List<SolicitudAmistadDTO> obtenerPendientes(Usuario usuario) {
        return solicitudRepository.findByDestinatarioAndEstado(usuario, EstadoSolicitud.PENDIENTE).stream()
                .map(SolicitudAmistadDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Acepta una solicitud por su token de email: la marca ACEPTADA y crea la amistad.
     * Firma 1:1 con el diagrama de clases (token -> Amistad).
     */
    public Amistad aceptarSolicitud(String token) {
        SolicitudAmistad solicitud = buscarPorToken(token);
        solicitud.setEstado(EstadoSolicitud.ACEPTADA);
        solicitudRepository.save(solicitud);
        return amistadService.crearAmistad(solicitud.getRemitente(), solicitud.getDestinatario());
    }

    /** Rechaza una solicitud por su token de email: la marca RECHAZADA. 1:1 con el diagrama. */
    public void rechazarSolicitud(String token) {
        SolicitudAmistad solicitud = buscarPorToken(token);
        solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        solicitudRepository.save(solicitud);
    }

    private SolicitudAmistad buscarPorToken(String token) {
        SolicitudAmistad solicitud = solicitudRepository.findByTokenEmail(token);
        if (solicitud == null) {
            throw new SolicitudNotFoundException("Solicitud no encontrada");
        }
        return solicitud;
    }
}
