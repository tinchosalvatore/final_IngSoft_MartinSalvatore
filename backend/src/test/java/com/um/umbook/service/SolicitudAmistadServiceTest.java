package com.um.umbook.service;

import com.um.umbook.model.EstadoSolicitud;
import com.um.umbook.model.SolicitudAmistad;
import com.um.umbook.model.TipoNotificacion;
import com.um.umbook.model.Usuario;
import com.um.umbook.repository.SolicitudAmistadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests de SolicitudAmistadService. Cubre CP 8.2.1 (al enviarse una solicitud se genera
 * la notificacion) y el aceptar/rechazar.
 */
@ExtendWith(MockitoExtension.class)
class SolicitudAmistadServiceTest {

    @Mock
    private SolicitudAmistadRepository solicitudRepository;
    @Mock
    private NotificacionService notificacionService;
    @Mock
    private JavaMailService mailService;
    @Mock
    private AmistadService amistadService;

    @InjectMocks
    private SolicitudAmistadService solicitudService;

    private Usuario usuario(Long id, String nombre, String apellido) {
        Usuario u = new Usuario(nombre, apellido, nombre + "@um.edu.ar", nombre, "x", LocalDate.of(2000, 1, 1));
        u.setId(id);
        return u;
    }

    // ---------- CP 8.2.1 ----------
    @Test
    void enviarSolicitud_generaNotificacionYEmail() {
        Usuario remitente = usuario(1L, "Juan", "Sanchez");   // jsanchez
        Usuario destinatario = usuario(2L, "Juan", "Perez");  // jperez
        when(solicitudRepository.save(any(SolicitudAmistad.class))).thenAnswer(inv -> {
            SolicitudAmistad s = inv.getArgument(0);
            s.setId(99L);
            return s;
        });

        solicitudService.enviarSolicitud(remitente, destinatario);

        // Se manda el email (stub) y se crea+emite la notificacion para el destinatario.
        verify(mailService).enviarEmailSolicitudAmistad(any(SolicitudAmistad.class));
        verify(notificacionService).crearNotificacion(
                eq(destinatario), eq(TipoNotificacion.SOLICITUD_AMISTAD), eq(99L), contains("Juan Sanchez"));
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
