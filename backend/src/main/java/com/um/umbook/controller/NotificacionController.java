package com.um.umbook.controller;

import com.um.umbook.dto.NotificacionDTO;
import com.um.umbook.exception.UsuarioNotFoundException;
import com.um.umbook.model.TipoNotificacion;
import com.um.umbook.model.Usuario;
import com.um.umbook.service.NotificacionService;
import com.um.umbook.service.UsuarioService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * Endpoints de notificaciones. Stream SSE para los toasts en vivo (CU-14 y CU-15).
 */
@RestController
@RequestMapping("/notificaciones")
public class NotificacionController {

    private static final Long DEMO_USUARIO_ID = 1L;

    private final NotificacionService notificacionService;
    private final UsuarioService usuarioService;

    public NotificacionController(NotificacionService notificacionService, UsuarioService usuarioService) {
        this.notificacionService = notificacionService;
        this.usuarioService = usuarioService;
    }

    /** Canal SSE: el frontend abre un EventSource aca y recibe los toasts en vivo. */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam(name = "usuarioId", required = false) Long usuarioId) {
        return notificacionService.suscribir(usuarioId != null ? usuarioId : DEMO_USUARIO_ID);
    }

    @GetMapping("/no-leidas")
    public ResponseEntity<List<NotificacionDTO>> noLeidas(
            @RequestParam(name = "usuarioId", required = false) Long usuarioId) {
        Usuario usuario = usuarioService.obtenerPorId(usuarioId != null ? usuarioId : DEMO_USUARIO_ID);
        if (usuario == null) {
            throw new UsuarioNotFoundException("Usuario no encontrado");
        }
        List<NotificacionDTO> dtos = notificacionService.obtenerNoLeidas(usuario).stream()
                .map(n -> NotificacionDTO.fromEntity(n, mensajeGenerico(n.getTipo())))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /** Mensaje generico para la lista de no leidas (el toast en vivo lleva el detallado). */
    private String mensajeGenerico(TipoNotificacion tipo) {
        return switch (tipo) {
            case SOLICITUD_AMISTAD -> "Tenes una solicitud de amistad pendiente";
            case CUMPLEANOS -> "Un amigo cumple años hoy";
            default -> "Nueva notificacion";
        };
    }

    @PutMapping("/{id}/leida")
    public ResponseEntity<Void> marcarLeida(@PathVariable Long id) {
        notificacionService.marcarComoLeida(id);
        return ResponseEntity.noContent().build();
    }
}
