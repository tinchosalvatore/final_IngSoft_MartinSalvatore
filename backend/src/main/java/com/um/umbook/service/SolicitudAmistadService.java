package com.um.umbook.service;

import com.um.umbook.dto.SolicitudAmistadDTO;
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
 * Logica de solicitudes de amistad (CU-14). Al enviar una solicitud, dispara la
 * notificacion (toast en vivo) y el email (stub). Metodos 1:1 con el diagrama.
 */
@Service
public class SolicitudAmistadService {

    private final SolicitudAmistadRepository solicitudRepository;
    private final NotificacionService notificacionService;
    private final JavaMailService mailService;

    public SolicitudAmistadService(SolicitudAmistadRepository solicitudRepository,
                                   NotificacionService notificacionService,
                                   JavaMailService mailService) {
        this.solicitudRepository = solicitudRepository;
        this.notificacionService = notificacionService;
        this.mailService = mailService;
    }

    public String generarTokenEmail() {
        return UUID.randomUUID().toString();
    }

    /**
     * CU-14: envia una solicitud de amistad, la persiste, manda el email (stub) y
     * crea+emite la notificacion para el destinatario (toast en vivo).
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

    /** CU-14 (click en notificacion): solicitudes pendientes del usuario. */
    public List<SolicitudAmistadDTO> obtenerPendientes(Usuario usuario) {
        return solicitudRepository.findByDestinatarioAndEstado(usuario, EstadoSolicitud.PENDIENTE).stream()
                .map(SolicitudAmistadDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
