package com.um.umbook.controller;

import com.um.umbook.service.CumpleanosService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint del batch de cumpleaños (CU-15). Es la operacion real que normalmente correria
 * agendada todos los dias: detecta quienes cumplen hoy y publica los eventos de dominio,
 * a los que reacciona el subsistema de notificaciones. La demo lo invoca on-demand.
 */
@RestController
@RequestMapping("/cumpleanos")
public class CumpleanosController {

    private final CumpleanosService cumpleanosService;

    public CumpleanosController(CumpleanosService cumpleanosService) {
        this.cumpleanosService = cumpleanosService;
    }

    @PostMapping("/ejecutar-batch")
    public ResponseEntity<Map<String, Object>> ejecutarBatch() {
        int cumpleaneros = cumpleanosService.ejecutarBatchDiario();
        return ResponseEntity.ok(Map.of(
                "cumpleaneros", cumpleaneros,
                "mensaje", "Batch de cumpleaños ejecutado: " + cumpleaneros + " cumpleañero(s) hoy"));
    }
}
