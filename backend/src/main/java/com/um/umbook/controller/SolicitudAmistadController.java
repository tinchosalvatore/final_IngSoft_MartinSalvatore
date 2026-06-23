package com.um.umbook.controller;

import com.um.umbook.dto.SolicitudAmistadDTO;
import com.um.umbook.exception.UsuarioNotFoundException;
import com.um.umbook.model.SolicitudAmistad;
import com.um.umbook.model.Usuario;
import com.um.umbook.service.SolicitudAmistadService;
import com.um.umbook.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Endpoints de solicitudes de amistad (CU-14). El envio real (POST /solicitudes) dispara la
 * notificacion en vivo via evento de dominio; el click en la notificacion lista las pendientes.
 */
@RestController
@RequestMapping("/solicitudes")
public class SolicitudAmistadController {

    private static final Long DEMO_USUARIO_ID = 1L;
    /** Remitente por defecto: 'fede' (id=7), que NO es amigo de martin. */
    private static final Long DEMO_REMITENTE_ID = 7L;

    private final SolicitudAmistadService solicitudService;
    private final UsuarioService usuarioService;

    public SolicitudAmistadController(SolicitudAmistadService solicitudService, UsuarioService usuarioService) {
        this.solicitudService = solicitudService;
        this.usuarioService = usuarioService;
    }

    /**
     * CU-14: envia una solicitud de amistad de verdad. Persiste la solicitud y publica el
     * evento de dominio -> el listener de notificaciones emite el toast en vivo al destinatario.
     */
    @PostMapping
    public ResponseEntity<SolicitudAmistadDTO> enviar(
            @RequestParam(name = "remitenteId", required = false) Long remitenteId,
            @RequestParam(name = "destinatarioId", required = false) Long destinatarioId) {

        Usuario remitente = buscar(remitenteId != null ? remitenteId : DEMO_REMITENTE_ID);
        Usuario destinatario = buscar(destinatarioId != null ? destinatarioId : DEMO_USUARIO_ID);

        SolicitudAmistad solicitud = solicitudService.enviarSolicitud(remitente, destinatario);
        return ResponseEntity.status(HttpStatus.CREATED).body(SolicitudAmistadDTO.fromEntity(solicitud));
    }

    private Usuario buscar(Long id) {
        Usuario u = usuarioService.obtenerPorId(id);
        if (u == null) {
            throw new UsuarioNotFoundException("Usuario " + id + " no encontrado");
        }
        return u;
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<SolicitudAmistadDTO>> pendientes(
            @RequestParam(name = "usuarioId", required = false) Long usuarioId) {
        Usuario usuario = usuarioService.obtenerPorId(usuarioId != null ? usuarioId : DEMO_USUARIO_ID);
        if (usuario == null) {
            throw new UsuarioNotFoundException("Usuario no encontrado");
        }
        return ResponseEntity.ok(solicitudService.obtenerPendientes(usuario));
    }

    @PostMapping("/{id}/aceptar")
    public ResponseEntity<Map<String, String>> aceptar(@PathVariable Long id) {
        solicitudService.aceptarSolicitud(id);
        return ResponseEntity.ok(Map.of("mensaje", "Solicitud aceptada"));
    }

    @PostMapping("/{id}/rechazar")
    public ResponseEntity<Map<String, String>> rechazar(@PathVariable Long id) {
        solicitudService.rechazarSolicitud(id);
        return ResponseEntity.ok(Map.of("mensaje", "Solicitud rechazada"));
    }
}
