package com.um.umbook.controller;

import com.um.umbook.dto.SolicitudAmistadDTO;
import com.um.umbook.exception.UsuarioNotFoundException;
import com.um.umbook.model.Usuario;
import com.um.umbook.service.SolicitudAmistadService;
import com.um.umbook.service.UsuarioService;
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
 * Endpoints de solicitudes de amistad. CU-14 (click en notificacion):
 * GET /solicitudes/pendientes.
 */
@RestController
@RequestMapping("/solicitudes")
public class SolicitudAmistadController {

    private static final Long DEMO_USUARIO_ID = 1L;

    private final SolicitudAmistadService solicitudService;
    private final UsuarioService usuarioService;

    public SolicitudAmistadController(SolicitudAmistadService solicitudService, UsuarioService usuarioService) {
        this.solicitudService = solicitudService;
        this.usuarioService = usuarioService;
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
