package com.um.umbook.controller;

import com.um.umbook.dto.LoginDTO;
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
 * Endpoints de usuarios. CU-13: GET /usuarios?amigosEnComun=2; CU-7: GET /usuarios/buscar;
 * CU-2: POST /usuarios/login; CU-1: POST /usuarios.
 *
 * El "usuario actual" lo aporta el login (CU-2): el front guarda el id del usuario logueado
 * y lo manda como usuarioId. Si no viene, se cae al usuario de referencia de la demo (martin).
 */
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    /** Usuario de referencia por defecto cuando no llega usuarioId (martin, sembrado con id=1). */
    private static final Long DEMO_USUARIO_ID = 1L;

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /** CU-13 (secuencia: listarUsuarios): usuarios con +N amigos en comun ("personas que quizas conozcas"). */
    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listarUsuarios(
            @RequestParam(name = "amigosEnComun", defaultValue = "2") int amigosEnComun,
            @RequestParam(name = "usuarioId", required = false) Long usuarioId) {

        Usuario referencia = resolverReferencia(usuarioId);

        List<UsuarioDTO> usuarios = usuarioService.listarConAmigosEnComun(referencia, amigosEnComun);
        if (usuarios.isEmpty()) {
            throw new UsuarioNotFoundException("No se encontraron usuarios");
        }
        return ResponseEntity.ok(usuarios);
    }

    /**
     * CU-7: busca usuarios por nombre o apellido. La searchbar (un solo campo) manda el mismo
     * texto en ambos params. Lista vacia => 200 [].
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<UsuarioDTO>> buscarUsuarios(
            @RequestParam(name = "nombre", required = false, defaultValue = "") String nombre,
            @RequestParam(name = "apellido", required = false, defaultValue = "") String apellido,
            @RequestParam(name = "usuarioId", required = false) Long usuarioId) {

        Usuario referencia = resolverReferencia(usuarioId);
        return ResponseEntity.ok(usuarioService.buscarPorTexto(referencia, nombre.trim(), apellido.trim()));
    }

    /** CU-2: inicia sesion. 200 + usuario si las credenciales son validas; 401/400 en error. */
    @PostMapping("/login")
    public ResponseEntity<UsuarioDTO> login(@Valid @RequestBody LoginDTO datos) {
        Usuario usuario = usuarioService.iniciarSesion(datos.getEmail(), datos.getContrasena());
        return ResponseEntity.ok(UsuarioDTO.fromEntity(usuario));
    }

    private Usuario resolverReferencia(Long usuarioId) {
        Long refId = (usuarioId != null) ? usuarioId : DEMO_USUARIO_ID;
        Usuario referencia = usuarioService.obtenerPorId(refId);
        if (referencia == null) {
            throw new UsuarioNotFoundException("Usuario de referencia no encontrado");
        }
        return referencia;
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
