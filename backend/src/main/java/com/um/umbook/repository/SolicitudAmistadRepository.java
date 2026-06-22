package com.um.umbook.repository;

import com.um.umbook.model.EstadoSolicitud;
import com.um.umbook.model.SolicitudAmistad;
import com.um.umbook.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de solicitudes de amistad. Metodos 1:1 con el diagrama de clases.
 */
@Repository
public interface SolicitudAmistadRepository extends JpaRepository<SolicitudAmistad, Long> {

    List<SolicitudAmistad> findByDestinatario(Usuario destinatario);

    SolicitudAmistad findByRemitenteAndDestinatario(Usuario remitente, Usuario destinatario);

    SolicitudAmistad findByTokenEmail(String tokenEmail);

    List<SolicitudAmistad> findByEstado(EstadoSolicitud estado);

    List<SolicitudAmistad> findByDestinatarioAndEstado(Usuario destinatario, EstadoSolicitud estado);
}
