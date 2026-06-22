package com.um.umbook.controller;

import com.um.umbook.exception.UsuarioNotFoundException;
import com.um.umbook.model.Usuario;
import com.um.umbook.repository.UsuarioRepository;
import com.um.umbook.service.CumpleanosService;
import com.um.umbook.service.SolicitudAmistadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

/**
 * Endpoints de DEMO/DEV para disparar las notificaciones en vivo desde el back.
 * Los "scripts en Java" (objetivo 7) golpean estos endpoints.
 *
 * NO es codigo de produccion: existe solo para la demostracion de CU-14 y CU-15.
 */
@RestController
@RequestMapping("/dev")
public class DevController {

    /** Por defecto la solicitud la recibe el usuario observado en la UI (martin, id=1). */
    private static final Long DEMO_DESTINATARIO_ID = 1L;
    /** Por defecto el remitente es 'fede' (id=7), que NO es amigo de martin. */
    private static final Long DEMO_REMITENTE_ID = 7L;
    /** Por defecto el cumpleañero es 'beto' (id=3), amigo de martin. */
    private static final Long DEMO_CUMPLEANERO_ID = 3L;

    private final UsuarioRepository usuarioRepository;
    private final SolicitudAmistadService solicitudService;
    private final CumpleanosService cumpleanosService;

    public DevController(UsuarioRepository usuarioRepository, SolicitudAmistadService solicitudService,
                         CumpleanosService cumpleanosService) {
        this.usuarioRepository = usuarioRepository;
        this.solicitudService = solicitudService;
        this.cumpleanosService = cumpleanosService;
    }

    /** CU-14: dispara una solicitud de amistad -> toast en vivo para el destinatario. */
    @PostMapping("/trigger-solicitud")
    public ResponseEntity<Map<String, String>> triggerSolicitud(
            @RequestParam(name = "remitenteId", required = false) Long remitenteId,
            @RequestParam(name = "destinatarioId", required = false) Long destinatarioId) {

        Usuario remitente = buscar(remitenteId != null ? remitenteId : DEMO_REMITENTE_ID);
        Usuario destinatario = buscar(destinatarioId != null ? destinatarioId : DEMO_DESTINATARIO_ID);

        solicitudService.enviarSolicitud(remitente, destinatario);
        return ResponseEntity.ok(Map.of("mensaje",
                remitente.getNombre() + " envio una solicitud de amistad a " + destinatario.getNombre()));
    }

    /**
     * CU-15: setea el cumpleaños del usuario indicado a HOY y corre el batch
     * -> toast en vivo para sus amigos.
     */
    @PostMapping("/trigger-cumple")
    public ResponseEntity<Map<String, String>> triggerCumple(
            @RequestParam(name = "usuarioId", required = false) Long usuarioId) {

        Usuario cumpleanero = buscar(usuarioId != null ? usuarioId : DEMO_CUMPLEANERO_ID);

        int anioNacimiento = cumpleanero.getFechaNacimiento() != null
                ? cumpleanero.getFechaNacimiento().getYear() : 2000;
        LocalDate hoy = LocalDate.now();
        cumpleanero.setFechaNacimiento(LocalDate.of(anioNacimiento, hoy.getMonth(), hoy.getDayOfMonth()));
        usuarioRepository.save(cumpleanero);

        cumpleanosService.ejecutarBatchDiario();
        return ResponseEntity.ok(Map.of("mensaje",
                "Cumpleaños de " + cumpleanero.getNombre() + " seteado a hoy y batch ejecutado"));
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario " + id + " no encontrado"));
    }
}
