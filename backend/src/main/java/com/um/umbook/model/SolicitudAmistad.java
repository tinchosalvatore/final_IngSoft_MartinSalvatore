package com.um.umbook.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Solicitud de amistad enviada de un remitente a un destinatario.
 * Campos 1:1 con el diagrama de clases.
 */
@Entity
@Table(name = "solicitud_amistad")
public class SolicitudAmistad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Usuario remitente;

    @ManyToOne
    private Usuario destinatario;

    @Enumerated(EnumType.STRING)
    private EstadoSolicitud estado;

    private LocalDateTime fechaEnvio;

    /** Token enviado por email para aceptar/rechazar la solicitud. */
    private String tokenEmail;

    public SolicitudAmistad() {
    }

    public SolicitudAmistad(Usuario remitente, Usuario destinatario, String tokenEmail) {
        this.remitente = remitente;
        this.destinatario = destinatario;
        this.estado = EstadoSolicitud.PENDIENTE;
        this.fechaEnvio = LocalDateTime.now();
        this.tokenEmail = tokenEmail;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getRemitente() {
        return remitente;
    }

    public void setRemitente(Usuario remitente) {
        this.remitente = remitente;
    }

    public Usuario getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(Usuario destinatario) {
        this.destinatario = destinatario;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public String getTokenEmail() {
        return tokenEmail;
    }

    public void setTokenEmail(String tokenEmail) {
        this.tokenEmail = tokenEmail;
    }
}
