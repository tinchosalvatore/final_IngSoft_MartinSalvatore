package com.um.umbook.controller;

import com.um.umbook.dto.UsuarioDTO;
import com.um.umbook.exception.UsuarioNotFoundException;
import com.um.umbook.model.Usuario;
import com.um.umbook.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de usuarios. CU-13: GET /usuarios?amigosEnComun=2; CU-7: GET /usuarios/buscar.
 *
 * el "Usuario actual" es el usuario de referencia de la demo (martin, id=1), resuelto internamente.
 */
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    /** Usuario actual de la demo (martin, sembrado con id=1). */
    private static final Long USUARIO_ACTUAL_ID = 1L;

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /** CU-13 (secuencia: listarUsuarios): usuarios con +N amigos en comun ("personas que quizas conozcas"). */
    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listarUsuarios(
            @RequestParam(name = "amigosEnComun", defaultValue = "2") int amigosEnComun) {

        Usuario referencia = usuarioActual();
        return ResponseEntity.ok(usuarioService.listarUsuarios(referencia, amigosEnComun));
    }

    /**
     * CU-7: busca usuarios por nombre o apellido. La searchbar (un solo campo) manda el mismo
     * texto en ambos params. Lista vacia => 200 [].
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<UsuarioDTO>> buscarUsuarios(
            @RequestParam(name = "nombre", required = false, defaultValue = "") String nombre,
            @RequestParam(name = "apellido", required = false, defaultValue = "") String apellido) {

        List<UsuarioDTO> resultado = usuarioService.buscarUsuarios(nombre.trim(), apellido.trim()).stream()
                .map(UsuarioDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(resultado);
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
    public ResponseEntity<List<UsuarioDTO>> cumpleanosDeHoy() {
        return ResponseEntity.ok(usuarioService.cumpleanosDeHoy(usuarioActual()));
    }

    /**
     * Demo del boton "recargar sugerencias" del buscador (CU-13): agrega un lote de dos usuarios
     * nuevos con +2 amigos en comun. Si ya no quedan por sugerir, el service lanza 404 (alt "Lista vacia").
     */
    @PostMapping("/sugerencia-extra")
    public ResponseEntity<List<UsuarioDTO>> agregarSugerenciaExtra() {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuarioService.agregarSugerenciaExtra(usuarioActual()));
    }

    private Usuario usuarioActual() {
        Usuario referencia = usuarioService.obtenerPorId(USUARIO_ACTUAL_ID);
        if (referencia == null) {
            throw new UsuarioNotFoundException("Usuario de referencia no encontrado");
        }
        return referencia;
    }
}
