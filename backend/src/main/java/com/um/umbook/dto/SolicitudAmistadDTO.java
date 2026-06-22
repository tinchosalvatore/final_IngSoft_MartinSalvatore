package com.um.umbook.dto;

import com.um.umbook.model.EstadoSolicitud;
import com.um.umbook.model.SolicitudAmistad;

import java.time.LocalDateTime;

/**
 * DTO de salida de una solicitud de amistad (CU-14, lista de pendientes).
 */
public class SolicitudAmistadDTO {

    private Long id;
    private String remitenteNombre;
    private String remitenteUsuario;
    private EstadoSolicitud estado;
    private LocalDateTime fechaEnvio;

    public SolicitudAmistadDTO() {
    }

    public static SolicitudAmistadDTO fromEntity(SolicitudAmistad s) {
        SolicitudAmistadDTO dto = new SolicitudAmistadDTO();
        dto.id = s.getId();
        dto.remitenteNombre = s.getRemitente().getNombre() + " " + s.getRemitente().getApellido();
        dto.remitenteUsuario = s.getRemitente().getNombreUsuario();
        dto.estado = s.getEstado();
        dto.fechaEnvio = s.getFechaEnvio();
        return dto;
    }

    public Long getId() {
        return id;
    }

    public String getRemitenteNombre() {
        return remitenteNombre;
    }

    public String getRemitenteUsuario() {
        return remitenteUsuario;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }
}
