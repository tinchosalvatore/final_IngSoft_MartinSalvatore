package com.um.umbook.service;

import com.um.umbook.dto.NotificacionDTO;
import com.um.umbook.model.Notificacion;
import com.um.umbook.model.TipoNotificacion;
import com.um.umbook.model.Usuario;
import com.um.umbook.repository.NotificacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Notificaciones de UM-Book. Persiste las notificaciones y las empuja en vivo al
 * frontend via SSE (Server-Sent Events). Metodos 1:1 con el diagrama de clases.
 *
 * Para la demo, el broadcast SSE va a todos los clientes conectados (hay un unico
 * usuario observando, 'martin').
 */
@Service
public class NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);
    private static final long SSE_TIMEOUT = 60 * 60 * 1000L; // 1 hora

    private final NotificacionRepository notificacionRepository;
    private final JavaMailService mailService;

    /** Emisores SSE conectados, ruteados por id de usuario destinatario (thread-safe). */
    private final Map<Long, List<SseEmitter>> emittersPorUsuario = new ConcurrentHashMap<>();

    public NotificacionService(NotificacionRepository notificacionRepository, JavaMailService mailService) {
        this.notificacionRepository = notificacionRepository;
        this.mailService = mailService;
    }

    /** Registra un nuevo cliente SSE para un usuario (GET /notificaciones/stream?usuarioId=). */
    public SseEmitter suscribir(Long usuarioId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        List<SseEmitter> lista = emittersPorUsuario.computeIfAbsent(usuarioId,
                k -> new CopyOnWriteArrayList<>());
        lista.add(emitter);
        emitter.onCompletion(() -> lista.remove(emitter));
        emitter.onTimeout(() -> lista.remove(emitter));
        emitter.onError(e -> lista.remove(emitter));
        log.info("Cliente SSE suscripto (usuario {}). Conectados de ese usuario: {}", usuarioId, lista.size());
        return emitter;
    }

    /**
     * Persiste una notificacion y la emite en vivo por SSE al destinatario.
     * Firma 1:1 con el diagrama de clases: el mensaje se arma internamente segun el tipo.
     */
    public void crearNotificacion(Usuario destinatario, TipoNotificacion tipo, Long referenciaId) {
        crearNotificacion(destinatario, tipo, referenciaId, mensajeGenerico(tipo));
    }

    /**
     * SSE overload con mensaje detallado para que
     * el toast en vivo muestre texto especifico (ej. "Fede te envio una solicitud") en lugar del
     * generico. Persiste la notificacion y la emite por SSE al destinatario.
     */
    public void crearNotificacion(Usuario destinatario, TipoNotificacion tipo,
                                  Long referenciaId, String mensaje) {
        Notificacion notificacion = notificacionRepository.save(
                new Notificacion(destinatario, tipo, referenciaId));
        emitirNotificacion(destinatario.getId(), NotificacionDTO.fromEntity(notificacion, mensaje)); // SSE al front
    }

    /**
     * Envia el email de cumpleaños al amigo (delega en el servicio de mail). Firma 1:1 con el
     * diagrama de clases (un solo parametro): el cuerpo es generico porque la firma no transporta
     * al cumpleañero (ver docs/EXTRAS.md).
     */
    public void enviarEmailCumpleanos(Usuario usuario) {
        mailService.enviarEmail(usuario.getEmail(), "Cumpleaños de un amigo",
                "Un amigo cumple años hoy!");
    }

    /** Empuja una notificacion a los clientes SSE del usuario destinatario. */
    public void emitirNotificacion(Long usuarioId, NotificacionDTO dto) {
        List<SseEmitter> lista = emittersPorUsuario.getOrDefault(usuarioId, List.of());
        for (SseEmitter emitter : lista) {
            try {
                emitter.send(SseEmitter.event().name("notificacion").data(dto));
            } catch (IOException e) {
                lista.remove(emitter);
            }
        }
        log.info("Notificacion emitida [{}] al usuario {} ({} cliente/s): {}",
                dto.getTipo(), usuarioId, lista.size(), dto.getMensaje());
    }

    /** Notificaciones no leidas del usuario. Devuelve entidades (1:1 con el diagrama). */
    public List<Notificacion> obtenerNoLeidas(Usuario usuario) {
        return notificacionRepository.findByDestinatarioAndLeida(usuario, false);
    }

    public void marcarComoLeida(Long id) {
        notificacionRepository.findById(id).ifPresent(n -> {
            n.setLeida(true);
            notificacionRepository.save(n);
        });
    }

    /** Mensaje generico para la lista de no leidas (el toast en vivo lleva el mensaje detallado). */
    private String mensajeGenerico(TipoNotificacion tipo) {
        return switch (tipo) {
            case SOLICITUD_AMISTAD -> "Tenes una solicitud de amistad pendiente";
            case CUMPLEANOS -> "Un amigo cumple años hoy";
            default -> "Nueva notificacion";
        };
    }
}
