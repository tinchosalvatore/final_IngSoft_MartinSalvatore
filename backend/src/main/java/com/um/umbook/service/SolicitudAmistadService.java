package com.um.umbook.service;

import com.um.umbook.dto.SolicitudAmistadDTO;
import com.um.umbook.exception.NoHaySolicitudesException;
import com.um.umbook.exception.SolicitudInvalidaException;
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
 * Logica de solicitudes de amistad (CU-6 enviar, CU-14 notificar, CU-18 gestionar). Al enviar una
 * solicitud la persiste y, segun el diagrama de clases, llama directamente al servicio de
 * notificaciones (toast en vivo) y al de mail (email stub). Campos y metodos 1:1 con el diagrama.
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
     * CU-6 / CU-14: envia una solicitud de amistad, la persiste y notifica al destinatario por
     * llamada directa: email (stub) + notificacion en vivo (toast SSE). Valida las alternativas
     * del diagrama CU-6: no se puede si ya son amigos ni si ya hay una solicitud pendiente.
     */
    public SolicitudAmistad enviarSolicitud(Usuario remitente, Usuario destinatario) {
        if (amistadService.sonAmigos(remitente, destinatario)) {
            throw new SolicitudInvalidaException("Ya son amigos");
        }
        SolicitudAmistad existente = solicitudRepository.findByRemitenteAndDestinatario(remitente, destinatario);
        if (existente != null && existente.getEstado() == EstadoSolicitud.PENDIENTE) {
            throw new SolicitudInvalidaException("Ya existe una solicitud de amistad pendiente");
        }

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
     * CU-18: solicitudes de amistad pendientes del usuario (pantalla "gestionar solicitudes").
     * Si no hay ninguna, lanza {@link NoHaySolicitudesException} (alt 4.1 del diagrama).
     */
    public List<SolicitudAmistadDTO> obtenerSolicitudes(Usuario usuario) {
        List<SolicitudAmistadDTO> pendientes = solicitudRepository
                .findByDestinatarioAndEstado(usuario, EstadoSolicitud.PENDIENTE).stream()
                .map(SolicitudAmistadDTO::fromEntity)
                .collect(Collectors.toList());
        if (pendientes.isEmpty()) {
            throw new NoHaySolicitudesException("No hay solicitudes pendientes");
        }
        return pendientes;
    }

    /**
     * CU-18 (alt 2.1): acepta una solicitud por su token de email: la marca ACEPTADA, crea la
     * amistad y notifica al remitente que fue aceptada. Firma 1:1 con el diagrama (token -> Amistad).
     */
    public Amistad aceptarSolicitud(String token) {
        SolicitudAmistad solicitud = buscarPorToken(token);
        solicitud.setEstado(EstadoSolicitud.ACEPTADA);
        solicitudRepository.save(solicitud);

        Amistad amistad = amistadService.crearAmistad(solicitud.getRemitente(), solicitud.getDestinatario());

        Usuario destinatario = solicitud.getDestinatario();
        String mensaje = destinatario.getNombre() + " " + destinatario.getApellido()
                + " acepto tu solicitud de amistad";
        notificacionService.crearNotificacion(solicitud.getRemitente(),
                TipoNotificacion.SOLICITUD_ACEPTADA, amistad.getId(), mensaje);

        return amistad;
    }

    /**
     * CU-18 (alt 3.1): rechaza una solicitud por su token de email eliminandola (Delete del
     * diagrama de secuencia). 1:1 con el diagrama.
     */
    public void rechazarSolicitud(String token) {
        SolicitudAmistad solicitud = buscarPorToken(token);
        solicitudRepository.delete(solicitud);
    }

    private SolicitudAmistad buscarPorToken(String token) {
        SolicitudAmistad solicitud = solicitudRepository.findByTokenEmail(token);
        if (solicitud == null) {
            throw new SolicitudNotFoundException("Solicitud no encontrada");
        }
        return solicitud;
    }
}
