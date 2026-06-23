package com.um.umbook.service;

import com.um.umbook.dto.SolicitudAmistadDTO;
import com.um.umbook.event.SolicitudAmistadCreadaEvent;
import com.um.umbook.exception.SolicitudNotFoundException;
import com.um.umbook.model.EstadoSolicitud;
import com.um.umbook.model.SolicitudAmistad;
import com.um.umbook.model.Usuario;
import com.um.umbook.repository.SolicitudAmistadRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Logica de solicitudes de amistad (CU-14). Al enviar una solicitud la persiste y publica
 * un evento de dominio; el subsistema de notificaciones reacciona (toast + email stub) sin
 * que este servicio lo conozca. Metodos 1:1 con el diagrama.
 */
@Service
public class SolicitudAmistadService {

    private final SolicitudAmistadRepository solicitudRepository;
    private final AmistadService amistadService;
    private final ApplicationEventPublisher eventPublisher;

    public SolicitudAmistadService(SolicitudAmistadRepository solicitudRepository,
                                   AmistadService amistadService,
                                   ApplicationEventPublisher eventPublisher) {
        this.solicitudRepository = solicitudRepository;
        this.amistadService = amistadService;
        this.eventPublisher = eventPublisher;
    }

    public String generarTokenEmail() {
        return UUID.randomUUID().toString();
    }

    /**
     * CU-14: envia una solicitud de amistad, la persiste y publica el evento de dominio.
     * La notificacion en vivo y el email los dispara el listener que escucha el evento.
     */
    public SolicitudAmistad enviarSolicitud(Usuario remitente, Usuario destinatario) {
        SolicitudAmistad solicitud = new SolicitudAmistad(remitente, destinatario, generarTokenEmail());
        solicitud = solicitudRepository.save(solicitud);

        eventPublisher.publishEvent(new SolicitudAmistadCreadaEvent(solicitud));

        return solicitud;
    }

    /** CU-14 (click en notificacion): solicitudes pendientes del usuario. */
    public List<SolicitudAmistadDTO> obtenerPendientes(Usuario usuario) {
        return solicitudRepository.findByDestinatarioAndEstado(usuario, EstadoSolicitud.PENDIENTE).stream()
                .map(SolicitudAmistadDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /** Acepta una solicitud pendiente: la marca ACEPTADA y crea la amistad. */
    public SolicitudAmistad aceptarSolicitud(Long solicitudId) {
        SolicitudAmistad solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new SolicitudNotFoundException("Solicitud no encontrada"));
        solicitud.setEstado(EstadoSolicitud.ACEPTADA);
        solicitudRepository.save(solicitud);
        amistadService.crearAmistad(solicitud.getRemitente(), solicitud.getDestinatario());
        return solicitud;
    }

    /** Rechaza una solicitud pendiente: la marca RECHAZADA. */
    public SolicitudAmistad rechazarSolicitud(Long solicitudId) {
        SolicitudAmistad solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new SolicitudNotFoundException("Solicitud no encontrada"));
        solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        return solicitudRepository.save(solicitud);
    }
}
