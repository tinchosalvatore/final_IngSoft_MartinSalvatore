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
 * Logica de cumpleaños (CU-15). El batch diario busca usuarios que cumplen hoy y,
 * por cada uno, notifica (toast en vivo + email stub) a sus amigos.
 * Metodos 1:1 con el diagrama de clases.
 */
@Service
public class CumpleanosService {

    private static final Logger log = LoggerFactory.getLogger(CumpleanosService.class);

    private final UsuarioRepository usuarioRepository;
    private final AmistadService amistadService;
    private final NotificacionService notificacionService;
    private final JavaMailService mailService;

    public CumpleanosService(UsuarioRepository usuarioRepository, AmistadService amistadService,
                             NotificacionService notificacionService, JavaMailService mailService) {
        this.usuarioRepository = usuarioRepository;
        this.amistadService = amistadService;
        this.notificacionService = notificacionService;
        this.mailService = mailService;
    }

    /** Usuarios cuyo dia y mes de nacimiento coinciden con hoy. */
    public List<Usuario> obtenerUsuariosConCumpleanos() {
        MonthDay hoy = MonthDay.from(LocalDate.now());
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getFechaNacimiento() != null
                        && MonthDay.from(u.getFechaNacimiento()).equals(hoy))
                .collect(Collectors.toList());
    }

    /**
     * CU-15: por cada cumpleañero de hoy, notifica a cada uno de sus amigos
     * (crea+emite la notificacion -> toast en vivo) y manda el email stub.
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
                mailService.enviarEmailCumpleanos(amigo, cumpleanero);
            }
        }
    }
}
