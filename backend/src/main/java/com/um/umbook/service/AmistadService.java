package com.um.umbook.service;

import com.um.umbook.model.Amistad;
import com.um.umbook.model.Usuario;
import com.um.umbook.repository.AmistadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Logica de amistades. Campos y metodos 1:1 con el diagrama de clases de diseño.
 */
@Service
public class AmistadService {

    private final AmistadRepository amistadRepository;
    /** Declarado por el diagrama de clases (AmistadService -> NotificacionService). Aun sin uso. */
    private final NotificacionService notificacionService;

    public AmistadService(AmistadRepository amistadRepository, NotificacionService notificacionService) {
        this.amistadRepository = amistadRepository;
        this.notificacionService = notificacionService;
    }

    /** Amigos de un usuario (el otro extremo de cada amistad, sin importar el orden del par). */
    public List<Usuario> obtenerAmigos(Usuario usuario) {
        List<Usuario> amigos = new ArrayList<>();
        for (Amistad a : amistadRepository.findByUsuario1OrUsuario2(usuario, usuario)) {
            Usuario otro = a.getUsuario1().getId().equals(usuario.getId())
                    ? a.getUsuario2() : a.getUsuario1();
            amigos.add(otro);
        }
        return amigos;
    }

    /** Amigos en comun entre dos usuarios (interseccion). */
    public List<Usuario> obtenerAmigosEnComun(Usuario u1, Usuario u2) {
        return amistadRepository.findAmigosEnComun(u1, u2);
    }

    public boolean sonAmigos(Usuario u1, Usuario u2) {
        return amistadRepository.findByUsuario1AndUsuario2(u1, u2) != null
                || amistadRepository.findByUsuario1AndUsuario2(u2, u1) != null;
    }

    public Amistad crearAmistad(Usuario u1, Usuario u2) {
        return amistadRepository.save(new Amistad(u1, u2));
    }

    /**
     * Elimina la amistad entre dos usuarios. El par es no ordenado, asi que borra ambos sentidos.
     * Metodo 1:1 con el diagrama de clases.
     */
    @Transactional
    public void eliminarAmistad(Usuario u1, Usuario u2) {
        amistadRepository.deleteByUsuario1AndUsuario2(u1, u2);
        amistadRepository.deleteByUsuario1AndUsuario2(u2, u1);
    }
}
