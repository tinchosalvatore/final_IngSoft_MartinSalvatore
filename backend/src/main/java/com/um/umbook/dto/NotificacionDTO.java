package com.um.umbook.dto;

import com.um.umbook.model.Notificacion;
import com.um.umbook.model.TipoNotificacion;

import java.time.LocalDateTime;

/**
 * DTO de notificacion que viaja por SSE al frontend para mostrar el toast.
 * Incluye un mensaje ya armado para mostrar directamente.
 */
public class NotificacionDTO {

    private Long id;
    private TipoNotificacion tipo;
    private String mensaje;
    private Long referenciaId;
    private boolean leida;
    private LocalDateTime fechaCreacion;

    public NotificacionDTO() {
    }

    public NotificacionDTO(Long id, TipoNotificacion tipo, String mensaje, Long referenciaId,
                           boolean leida, LocalDateTime fechaCreacion) {
        this.id = id;
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.referenciaId = referenciaId;
        this.leida = leida;
        this.fechaCreacion = fechaCreacion;
    }

    public static NotificacionDTO fromEntity(Notificacion n, String mensaje) {
        return new NotificacionDTO(n.getId(), n.getTipo(), mensaje, n.getReferenciaId(),
                n.isLeida(), n.getFechaCreacion());
    }

    public Long getId() {
        return id;
    }

    public TipoNotificacion getTipo() {
        return tipo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public Long getReferenciaId() {
        return referenciaId;
    }

    public boolean isLeida() {
        return leida;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}
