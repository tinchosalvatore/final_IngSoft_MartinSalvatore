package com.um.umbook.controller;

import com.um.umbook.dto.RegistroDTO;
import com.um.umbook.dto.UsuarioDTO;
import com.um.umbook.exception.UsuarioNotFoundException;
import com.um.umbook.model.Usuario;
import com.um.umbook.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "usuarioId", required = false) Long usuarioId) {

        Long refId = (usuarioId != null) ? usuarioId : DEMO_USUARIO_ID;
        Usuario referencia = usuarioService.obtenerPorId(refId);
        if (referencia == null) {
            throw new UsuarioNotFoundException("Usuario de referencia no encontrado");
        }

        // Searchbar: si viene texto, busca usuarios por nombre/apellido (lista vacia => 200 []).
        if (q != null && !q.isBlank()) {
            return ResponseEntity.ok(usuarioService.buscarPorTexto(referencia, q.trim()));
        }

        // CU-13: sin texto, sugiere usuarios con +N amigos en comun (vacio => 404).
        List<UsuarioDTO> usuarios = usuarioService.listarConAmigosEnComun(referencia, amigosEnComun);
        if (usuarios.isEmpty()) {
            throw new UsuarioNotFoundException("No se encontraron usuarios");
        }
        return ResponseEntity.ok(usuarios);
    }

    /**
     * Edita la fecha de nacimiento de un usuario para que cumpla HOY (accion real de perfil).
     * La demo de CU-15 la usa para elegir el cumpleañero antes de correr el batch.
     */
    @PutMapping("/{id}/cumpleanos")
    public ResponseEntity<UsuarioDTO> marcarCumpleanosHoy(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.marcarCumpleanosHoy(id));
    }

    /** CU-15: usuarios que cumplen años hoy. Alimenta la tarjeta de Cumpleaños del home. */
    @GetMapping("/cumpleanos")
    public ResponseEntity<List<UsuarioDTO>> cumpleanosDeHoy(
            @RequestParam(name = "usuarioId", required = false) Long usuarioId) {

        Long refId = (usuarioId != null) ? usuarioId : DEMO_USUARIO_ID;
        Usuario referencia = usuarioService.obtenerPorId(refId);
        if (referencia == null) {
            throw new UsuarioNotFoundException("Usuario de referencia no encontrado");
        }
        return ResponseEntity.ok(usuarioService.cumpleanosDeHoy(referencia));
    }

    /**
     * Demo del boton "recargar sugerencias" del buscador (CU-13): agrega un lote de dos usuarios
     * nuevos con +2 amigos en comun. Si ya no quedan por sugerir, el service lanza 404 (alt "Lista vacia").
     */
    @PostMapping("/sugerencia-extra")
    public ResponseEntity<List<UsuarioDTO>> agregarSugerenciaExtra(
            @RequestParam(name = "usuarioId", required = false) Long usuarioId) {

        Long refId = (usuarioId != null) ? usuarioId : DEMO_USUARIO_ID;
        Usuario referencia = usuarioService.obtenerPorId(refId);
        if (referencia == null) {
            throw new UsuarioNotFoundException("Usuario de referencia no encontrado");
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuarioService.agregarSugerenciaExtra(referencia));
    }

    @PostMapping
    public ResponseEntity<UsuarioDTO> registrar(@Valid @RequestBody RegistroDTO datos) {
        Usuario usuario = new Usuario(
                datos.getNombre(),
                datos.getApellido(),
                datos.getEmail(),
                datos.getNombreUsuario(),
                datos.getContrasena(),
                datos.getFechaNacimiento());
        Usuario creado = usuarioService.registrar(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioDTO.fromEntity(creado));
    }
}
