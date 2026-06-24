package com.um.umbook.service;

import com.um.umbook.dto.UsuarioDTO;
import com.um.umbook.exception.CredencialesInvalidasException;
import com.um.umbook.exception.CuentaBloqueadaException;
import com.um.umbook.exception.UsuarioNotFoundException;
import com.um.umbook.exception.UsuarioYaExisteException;
import com.um.umbook.model.Usuario;
import com.um.umbook.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Logica de usuarios. Metodos 1:1 con el diagrama de clases de diseño.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final AmistadService amistadService;
    private final CumpleanosService cumpleanosService;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, AmistadService amistadService,
                          CumpleanosService cumpleanosService, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.amistadService = amistadService;
        this.cumpleanosService = cumpleanosService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Edita la fecha de nacimiento de un usuario para que su dia/mes sea HOY (conserva el año).
     * Es una accion real de perfil que la demo usa para elegir el cumpleañero antes de correr
     * el batch (CU-15). Devuelve el usuario actualizado.
     */
    public UsuarioDTO marcarCumpleanosHoy(Long id) {
        Usuario usuario = obtenerPorId(id);
        if (usuario == null) {
            throw new UsuarioNotFoundException("Usuario " + id + " no encontrado");
        }
        int anio = usuario.getFechaNacimiento() != null ? usuario.getFechaNacimiento().getYear() : 2000;
        LocalDate hoy = LocalDate.now();
        usuario.setFechaNacimiento(LocalDate.of(anio, hoy.getMonth(), hoy.getDayOfMonth()));
        return UsuarioDTO.fromEntity(usuarioRepository.save(usuario));
    }

    /**
     * Usuarios que cumplen años hoy (CU-15), con sus amigos en comun con la referencia.
     * Lo consume la tarjeta de Cumpleaños del home, que se actualiza con quienes cumplen.
     */
    public List<UsuarioDTO> cumpleanosDeHoy(Usuario referencia) {
        List<UsuarioDTO> resultado = new ArrayList<>();
        for (Usuario u : cumpleanosService.obtenerUsuariosConCumpleanos()) {
            if (u.getId().equals(referencia.getId())) {
                continue; // no me muestro a mi mismo
            }
            int comunes = amistadService.obtenerAmigosEnComun(referencia, u).size();
            resultado.add(UsuarioDTO.fromEntity(u, comunes));
        }
        return resultado;
    }

    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    /**
     * Registra un usuario nuevo. Verifica que email y nombre de usuario sean unicos
     * y guarda la contrasena hasheada con BCrypt.
     */
    public Usuario registrar(Usuario usuario) {
        if (usuarioRepository.findByEmail(usuario.getEmail()) != null) {
            throw new UsuarioYaExisteException("Ya existe un usuario con ese email");
        }
        if (usuarioRepository.findByNombreUsuario(usuario.getNombreUsuario()) != null) {
            throw new UsuarioYaExisteException("Ya existe un usuario con ese nombre de usuario");
        }
        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        usuario.setActivo(true);
        return usuarioRepository.save(usuario);
    }

    /** Intentos fallidos consecutivos antes de bloquear la cuenta (CU-2, alt "multiples intentos"). */
    private static final int MAX_INTENTOS_FALLIDOS = 3;

    /**
     * CU-2: inicia sesion validando email + contrasena (BCrypt). Devuelve el usuario si las
     * credenciales son correctas. Si la cuenta esta bloqueada (activo=false) lanza
     * {@link CuentaBloqueadaException} (400). Si las credenciales son invalidas, suma un intento
     * fallido y, al alcanzar {@link #MAX_INTENTOS_FALLIDOS}, bloquea la cuenta (activo=false) y
     * lanza {@link CuentaBloqueadaException}; en caso contrario lanza
     * {@link CredencialesInvalidasException} (401). Un inicio exitoso resetea el contador.
     */
    public Usuario iniciarSesion(String email, String contrasena) {
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null) {
            throw new CredencialesInvalidasException("Usuario inexistente o contrasena incorrecta");
        }
        if (!usuario.isActivo()) {
            throw new CuentaBloqueadaException("La cuenta esta bloqueada por multiples intentos fallidos");
        }
        if (!passwordEncoder.matches(contrasena, usuario.getContrasena())) {
            usuario.setIntentosFallidos(usuario.getIntentosFallidos() + 1);
            if (usuario.getIntentosFallidos() >= MAX_INTENTOS_FALLIDOS) {
                usuario.setActivo(false);
                usuarioRepository.save(usuario);
                throw new CuentaBloqueadaException("La cuenta quedo bloqueada por multiples intentos fallidos");
            }
            usuarioRepository.save(usuario);
            throw new CredencialesInvalidasException("Usuario inexistente o contrasena incorrecta");
        }
        if (usuario.getIntentosFallidos() != 0) {
            usuario.setIntentosFallidos(0);
            usuarioRepository.save(usuario);
        }
        return usuario;
    }

    /** Cantidad de sugerencias que devuelve cada recarga (de a dos). */
    private static final int EXTRAS_POR_RECARGA = 2;

    /**
     * Pool de extras que el boton "recargar sugerencias" va creando. Cada recarga toma el
     * siguiente lote de {@link #EXTRAS_POR_RECARGA} que aun no existan. Cuando no quedan
     * suficientes para completar un lote, no hay mas para sugerir.
     * {nombre, apellido, email, nombreUsuario, mesNac, diaNac}.
     */
    private static final String[][] EXTRAS_DEMO = {
            {"Lucas", "Pereyra", "lucas@um.edu.ar", "lucas", "6", "15"},
            {"Sofia", "Romero", "sofia@um.edu.ar", "sofia", "10", "3"},
            {"Mateo", "Funes", "mateo@um.edu.ar", "mateo", "2", "21"},
            {"Valentina", "Costa", "valentina@um.edu.ar", "valentina", "8", "9"},
    };

    /**
     * Demo del boton "recargar sugerencias" (CU-13): agrega un lote de DOS usuarios nuevos con
     * +2 amigos en comun con la referencia (enlazados a 2 amigos directos suyos). Devuelve solo
     * el lote nuevo, asi el front reemplaza la lista y deja de mostrar los previos. Cuando ya
     * no quedan extras por crear, lanza {@link UsuarioNotFoundException} (alt "Lista vacia"
     * del diagrama) que el front muestra como notificacion de excepcion.
     */
    public List<UsuarioDTO> agregarSugerenciaExtra(Usuario referencia) {
        List<String[]> lote = new ArrayList<>();
        for (String[] candidato : EXTRAS_DEMO) {
            if (usuarioRepository.findByNombreUsuario(candidato[3]) == null) {
                lote.add(candidato);
                if (lote.size() == EXTRAS_POR_RECARGA) {
                    break;
                }
            }
        }
        if (lote.size() < EXTRAS_POR_RECARGA) {
            throw new UsuarioNotFoundException("No hay mas usuarios con +2 amigos en comun para sugerir");
        }

        List<Usuario> amigosReferencia = amistadService.obtenerAmigos(referencia);
        if (amigosReferencia.size() < 2) {
            throw new UsuarioNotFoundException("La referencia no tiene suficientes amigos para generar la sugerencia");
        }

        List<UsuarioDTO> resultado = new ArrayList<>();
        for (String[] datos : lote) {
            LocalDate nacimiento = LocalDate.of(2000, Integer.parseInt(datos[4]), Integer.parseInt(datos[5]));
            Usuario extra = new Usuario(datos[0], datos[1], datos[2], datos[3],
                    passwordEncoder.encode("demo1234"), nacimiento);
            extra.setActivo(true);
            extra = usuarioRepository.save(extra);

            // Lo hago amigo de 2 amigos directos de la referencia => 2 amigos en comun.
            amistadService.crearAmistad(extra, amigosReferencia.get(0));
            amistadService.crearAmistad(extra, amigosReferencia.get(1));

            int comunes = amistadService.obtenerAmigosEnComun(referencia, extra).size();
            resultado.add(UsuarioDTO.fromEntity(extra, comunes));
        }
        return resultado;
    }

    /** Busqueda por texto (nombre o apellido) usada por la searchbar. */
    public List<Usuario> buscarUsuarios(String nombre, String apellido) {
        return usuarioRepository.findByNombreContainingOrApellidoContaining(nombre, apellido);
    }

    /**
     * CU-7: busqueda por nombre o apellido (delega 1:1 en {@link #buscarUsuarios(String, String)}),
     * excluyendo al propio usuario de referencia. Cada resultado incluye cuantos amigos en comun
     * tiene con la referencia (campo extra de la tarjeta, ver docs/EXTRAS.md).
     */
    public List<UsuarioDTO> buscarPorTexto(Usuario referencia, String nombre, String apellido) {
        List<UsuarioDTO> resultado = new ArrayList<>();
        for (Usuario candidato : buscarUsuarios(nombre, apellido)) {
            if (candidato.getId().equals(referencia.getId())) {
                continue; // no me muestro a mi mismo
            }
            int comunes = amistadService.obtenerAmigosEnComun(referencia, candidato).size();
            resultado.add(UsuarioDTO.fromEntity(candidato, comunes));
        }
        return resultado;
    }

    /**
     * CU-13: lista los usuarios con al menos {@code minAmigos} amigos en comun con la referencia,
     * excluyendo al propio usuario y a sus amigos directos ("personas que quizas conozcas").
     * El filtrado pesado (≥2 + exclusiones) lo hace el repositorio
     * ({@link UsuarioRepository#findUsuariosConAmigosEnComun}, 1:1 con el diagrama); aca solo se
     * arma el DTO con el conteo de comunes y, si {@code minAmigos>2}, se aplica el umbral extra.
     */
    public List<UsuarioDTO> listarConAmigosEnComun(Usuario referencia, int minAmigos) {
        List<UsuarioDTO> resultado = new ArrayList<>();
        for (Usuario candidato : usuarioRepository.findUsuariosConAmigosEnComun(referencia)) {
            int comunes = amistadService.obtenerAmigosEnComun(referencia, candidato).size();
            if (comunes >= minAmigos) {
                resultado.add(UsuarioDTO.fromEntity(candidato, comunes));
            }
        }
        return resultado;
    }
}
