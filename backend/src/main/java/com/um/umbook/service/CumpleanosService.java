package com.um.umbook.service;

import com.um.umbook.model.TipoNotificacion;
import com.um.umbook.model.Usuario;
import com.um.umbook.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Logica de cumpleaños (CU-15). El batch diario detecta quienes cumplen hoy.
 * Campos del diagrama: notificacionService + usuarioRepository. amistadService es una dependencia
 */
@Service
public class CumpleanosService {

    private static final Logger log = LoggerFactory.getLogger(CumpleanosService.class);

    private final NotificacionService notificacionService;
    private final UsuarioRepository usuarioRepository;
    private final AmistadService amistadService;

    public CumpleanosService(NotificacionService notificacionService,
                             UsuarioRepository usuarioRepository,
                             AmistadService amistadService) {
        this.notificacionService = notificacionService;
        this.usuarioRepository = usuarioRepository;
        this.amistadService = amistadService;
    }

    /** Busca Usuarios cuyo dia y mes de nacimiento coinciden con hoy. Parte de la funcion BatchDiario del CU-15 */
    public List<Usuario> obtenerUsuariosConCumpleanos() {
        MonthDay hoy = MonthDay.from(LocalDate.now());
        // pide todos los Usuarios al Repository y depsues compara fechas para detectar cumpleaños
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getFechaNacimiento() != null
                        && MonthDay.from(u.getFechaNacimiento()).equals(hoy))
                .collect(Collectors.toList());
    }

    /**
     * CU-15: detecta los cumpleañeros de hoy y, por cada uno, notifica en vivo a cada amigo
     * (toast SSE) por llamada directa. Luego dispara los emails. 1:1 con el diagrama (void).
     */
    public void ejecutarBatchDiario() {
        List<Usuario> cumpleaneros = obtenerUsuariosConCumpleanos();
        log.info("Batch cumpleaños: {} cumpleañero(s) hoy", cumpleaneros.size());

        for (Usuario cumpleanero : cumpleaneros) {
            String mensaje = cumpleanero.getNombre() + " " + cumpleanero.getApellido()
                    + " cumple años hoy";
            for (Usuario amigo : amistadService.obtenerAmigos(cumpleanero)) {
                notificacionService.crearNotificacion(amigo, TipoNotificacion.CUMPLEANOS,
                        cumpleanero.getId(), mensaje);
            }
        }

        enviarEmailsCumpleanos();
    }

    /**
     * Envia el email de cumpleaños a los amigos de cada cumpleañero de hoy, delegando en el
     * servicio de notificaciones (que a su vez usa el mail). Parte de la funcion BatchDiario del CU-15
     */
    public void enviarEmailsCumpleanos() {
        for (Usuario cumpleanero : obtenerUsuariosConCumpleanos()) {
            for (Usuario amigo : amistadService.obtenerAmigos(cumpleanero)) {
                notificacionService.enviarEmailCumpleanos(amigo);
            }
        }
    }
}
