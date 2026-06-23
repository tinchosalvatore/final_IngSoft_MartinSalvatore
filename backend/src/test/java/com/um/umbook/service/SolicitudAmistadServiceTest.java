package com.um.umbook.service;

import com.um.umbook.event.SolicitudAmistadCreadaEvent;
import com.um.umbook.model.EstadoSolicitud;
import com.um.umbook.model.SolicitudAmistad;
import com.um.umbook.model.Usuario;
import com.um.umbook.repository.SolicitudAmistadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests de SolicitudAmistadService. Tras el refactor a eventos, enviar una solicitud la
 * persiste y publica el evento de dominio (la notificacion/email los hace el listener).
 * Cubre CP 8.2.1 (publica el evento al enviarse) y el aceptar/rechazar.
 */
@ExtendWith(MockitoExtension.class)
class SolicitudAmistadServiceTest {

    @Mock
    private SolicitudAmistadRepository solicitudRepository;
    @Mock
    private AmistadService amistadService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SolicitudAmistadService solicitudService;

    private Usuario usuario(Long id, String nombre, String apellido) {
        Usuario u = new Usuario(nombre, apellido, nombre + "@um.edu.ar", nombre, "x", LocalDate.of(2000, 1, 1));
        u.setId(id);
        return u;
    }

    // ---------- CP 8.2.1 ----------
    @Test
    void enviarSolicitud_persisteYPublicaEvento() {
        Usuario remitente = usuario(1L, "Juan", "Sanchez");
        Usuario destinatario = usuario(2L, "Juan", "Perez");
        when(solicitudRepository.save(any(SolicitudAmistad.class))).thenAnswer(inv -> {
            SolicitudAmistad s = inv.getArgument(0);
            s.setId(99L);
            return s;
        });

        solicitudService.enviarSolicitud(remitente, destinatario);

        // Se publica el evento de dominio con la solicitud persistida (el listener notifica).
        ArgumentCaptor<SolicitudAmistadCreadaEvent> captor =
                ArgumentCaptor.forClass(SolicitudAmistadCreadaEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        SolicitudAmistad solicitud = captor.getValue().getSolicitud();
        assertThat(solicitud.getId()).isEqualTo(99L);
        assertThat(solicitud.getDestinatario()).isEqualTo(destinatario);
    }

    @Test
    void aceptarSolicitud_marcaAceptadaYCreaAmistad() {
        Usuario remitente = usuario(1L, "Juan", "Sanchez");
        Usuario destinatario = usuario(2L, "Juan", "Perez");
        SolicitudAmistad solicitud = new SolicitudAmistad(remitente, destinatario, "tok");
        solicitud.setId(99L);
        when(solicitudRepository.findById(99L)).thenReturn(java.util.Optional.of(solicitud));

        solicitudService.aceptarSolicitud(99L);

        assertThat(solicitud.getEstado()).isEqualTo(EstadoSolicitud.ACEPTADA);
        verify(solicitudRepository).save(solicitud);
        verify(amistadService).crearAmistad(remitente, destinatario);
    }

    @Test
    void rechazarSolicitud_marcaRechazada() {
        Usuario remitente = usuario(1L, "Juan", "Sanchez");
        Usuario destinatario = usuario(2L, "Juan", "Perez");
        SolicitudAmistad solicitud = new SolicitudAmistad(remitente, destinatario, "tok");
        solicitud.setId(99L);
        when(solicitudRepository.findById(99L)).thenReturn(java.util.Optional.of(solicitud));
        when(solicitudRepository.save(solicitud)).thenReturn(solicitud);

        solicitudService.rechazarSolicitud(99L);

        assertThat(solicitud.getEstado()).isEqualTo(EstadoSolicitud.RECHAZADA);
    }
}
