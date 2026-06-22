package com.um.umbook.repository;

import com.um.umbook.model.Notificacion;
import com.um.umbook.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de notificaciones. Metodos 1:1 con el diagrama de clases.
 */
@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByDestinatario(Usuario destinatario);

    List<Notificacion> findByDestinatarioAndLeida(Usuario destinatario, boolean leida);
}
