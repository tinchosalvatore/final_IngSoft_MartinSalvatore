package com.um.umbook.service;

import com.um.umbook.model.Amistad;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests de SolicitudAmistadService. Tras el refactor a llamada directa (1:1 con el diagrama),
 * enviar una solicitud la persiste y notifica al destinatario llamando directo a mailService y
 * notificacionService. Cubre CP 8.2.1 (se crea la notificacion al enviarse) y aceptar/rechazar
 * por token.
 */
@ExtendWith(MockitoExtension.class)
class SolicitudAmistadServiceTest {

    @Mock
    private SolicitudAmistadRepository solicitudRepository;
    @Mock
    private AmistadService amistadService;
    @Mock
    private NotificacionService notificacionService;
    @Mock
    private JavaMailService mailService;

    @InjectMocks
    private SolicitudAmistadService solicitudService;

    private Usuario usuario(Long id, String nombre, String apellido) {
        Usuario u = new Usuario(nombre, apellido, nombre + "@um.edu.ar", nombre, "x", LocalDate.of(2000, 1, 1));
        u.setId(id);
        return u;
    }

    // ---------- CP 8.2.1 ----------
    @Test
    void enviarSolicitud_persisteYNotificaAlDestinatario() {
        Usuario remitente = usuario(1L, "Juan", "Sanchez");
        Usuario destinatario = usuario(2L, "Juan", "Perez");
        when(solicitudRepository.save(any(SolicitudAmistad.class))).thenAnswer(inv -> {
            SolicitudAmistad s = inv.getArgument(0);
            s.setId(99L);
            return s;
        });

        solicitudService.enviarSolicitud(remitente, destinatario);

        // Email + notificacion en vivo por llamada directa (sin eventos).
        verify(mailService).enviarEmailSolicitudAmistad(any(SolicitudAmistad.class));
        verify(notificacionService).crearNotificacion(eq(destinatario),
                eq(TipoNotificacion.SOLICITUD_AMISTAD), eq(99L), any(String.class));
    }

    @Test
    void aceptarSolicitud_marcaAceptadaCreaAmistadYNotificaAlRemitente() {
        Usuario remitente = usuario(1L, "Juan", "Sanchez");
        Usuario destinatario = usuario(2L, "Juan", "Perez");
        SolicitudAmistad solicitud = new SolicitudAmistad(remitente, destinatario, "tok");
        solicitud.setId(99L);
        when(solicitudRepository.findByTokenEmail("tok")).thenReturn(solicitud);
        Amistad amistad = new Amistad(remitente, destinatario);
        amistad.setId(7L);
        when(amistadService.crearAmistad(remitente, destinatario)).thenReturn(amistad);

        solicitudService.aceptarSolicitud("tok");

        assertThat(solicitud.getEstado()).isEqualTo(EstadoSolicitud.ACEPTADA);
        verify(solicitudRepository).save(solicitud);
        verify(amistadService).crearAmistad(remitente, destinatario);
        // CU-18 alt 2.1: se notifica al remitente que la solicitud fue aceptada.
        verify(notificacionService).crearNotificacion(eq(remitente),
                eq(TipoNotificacion.SOLICITUD_ACEPTADA), eq(7L), any(String.class));
    }

    @Test
    void rechazarSolicitud_eliminaLaSolicitud() {
        Usuario remitente = usuario(1L, "Juan", "Sanchez");
        Usuario destinatario = usuario(2L, "Juan", "Perez");
        SolicitudAmistad solicitud = new SolicitudAmistad(remitente, destinatario, "tok");
        solicitud.setId(99L);
        when(solicitudRepository.findByTokenEmail("tok")).thenReturn(solicitud);

        solicitudService.rechazarSolicitud("tok");

        // CU-18 alt 3.1: rechazar = Delete del diagrama.
        verify(solicitudRepository).delete(solicitud);
    }
}
