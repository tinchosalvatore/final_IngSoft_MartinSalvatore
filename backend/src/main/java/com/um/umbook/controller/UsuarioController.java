package com.um.umbook.controller;

import com.um.umbook.dto.UsuarioDTO;
import com.um.umbook.exception.UsuarioNotFoundException;
import com.um.umbook.model.Usuario;
import com.um.umbook.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de usuarios. CU-13: GET /usuarios?amigosEnComun=2 (diagrama de secuencia).
 *
 * Como la demo no tiene sesion/login, el "usuario actual" se toma del parametro
 * usuarioId (por defecto 1 = 'martin', el usuario de referencia sembrado).
 */
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    /** Usuario de referencia por defecto para la demo (martin, sembrado con id=1). */
    private static final Long DEMO_USUARIO_ID = 1L;

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> buscarUsuarios(
            @RequestParam(name = "amigosEnComun", defaultValue = "2") int amigosEnComun,
            @RequestParam(name = "usuarioId", required = false) Long usuarioId) {

        Long refId = (usuarioId != null) ? usuarioId : DEMO_USUARIO_ID;
        Usuario referencia = usuarioService.obtenerPorId(refId);
        if (referencia == null) {
            throw new UsuarioNotFoundException("Usuario de referencia no encontrado");
        }

        List<UsuarioDTO> usuarios = usuarioService.listarConAmigosEnComun(referencia, amigosEnComun);
        if (usuarios.isEmpty()) {
            throw new UsuarioNotFoundException("No se encontraron usuarios");
        }
        return ResponseEntity.ok(usuarios);
    }
}
