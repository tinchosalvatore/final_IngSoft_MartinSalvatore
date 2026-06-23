package com.um.umbook.service;

import com.um.umbook.event.CumpleanosEvent;
import com.um.umbook.model.Usuario;
import com.um.umbook.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Logica de cumpleaños (CU-15). El batch diario detecta quienes cumplen hoy y publica un
 * evento de dominio por cada uno; el subsistema de notificaciones reacciona avisando a sus
 * amigos. El batch no conoce a NotificacionService. Metodos 1:1 con el diagrama de clases.
 */
@Service
public class CumpleanosService {

    private static final Logger log = LoggerFactory.getLogger(CumpleanosService.class);

    private final UsuarioRepository usuarioRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CumpleanosService(UsuarioRepository usuarioRepository,
                             ApplicationEventPublisher eventPublisher) {
        this.usuarioRepository = usuarioRepository;
        this.eventPublisher = eventPublisher;
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
     * CU-15: detecta los cumpleañeros de hoy y publica un evento por cada uno. El listener
     * de notificaciones reacciona avisando a los amigos. Devuelve cuantos cumplen hoy.
     */
    public int ejecutarBatchDiario() {
        List<Usuario> cumpleaneros = obtenerUsuariosConCumpleanos();
        log.info("Batch cumpleaños: {} cumpleañero(s) hoy", cumpleaneros.size());

        for (Usuario cumpleanero : cumpleaneros) {
            eventPublisher.publishEvent(new CumpleanosEvent(cumpleanero));
        }
        return cumpleaneros.size();
    }
}
