package com.um.umbook.event;

import com.um.umbook.model.SolicitudAmistad;
import com.um.umbook.model.TipoNotificacion;
import com.um.umbook.model.Usuario;
import com.um.umbook.service.AmistadService;
import com.um.umbook.service.JavaMailService;
import com.um.umbook.service.NotificacionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests del observador de notificaciones. Verifica que, al reaccionar a los eventos de
 * dominio, se cree+emita la notificacion y se mande el email. Mantiene la cobertura de
 * CP 8.2.1 (solicitud -> notifica destinatario) y CP 9.2.1 (cumple -> notifica amigos).
 */
@ExtendWith(MockitoExtension.class)
class NotificacionEventListenerTest {

    @Mock
    private NotificacionService notificacionService;
    @Mock
    private JavaMailService mailService;
    @Mock
    private AmistadService amistadService;

    @InjectMocks
    private NotificacionEventListener listener;

    private Usuario usuario(Long id, String nombre, String apellido) {
        Usuario u = new Usuario(nombre, apellido, nombre + "@um.edu.ar", nombre, "x", LocalDate.of(2000, 1, 1));
        u.setId(id);
        return u;
    }

    // ---------- CP 8.2.1 ----------
    @Test
    void alCrearseSolicitud_notificaDestinatarioYManadaEmail() {
        Usuario remitente = usuario(1L, "Juan", "Sanchez");
        Usuario destinatario = usuario(2L, "Juan", "Perez");
        SolicitudAmistad solicitud = new SolicitudAmistad(remitente, destinatario, "tok");
        solicitud.setId(99L);

        listener.alCrearseSolicitud(new SolicitudAmistadCreadaEvent(solicitud));

        verify(mailService).enviarEmailSolicitudAmistad(solicitud);
        verify(notificacionService).crearNotificacion(
                eq(destinatario), eq(TipoNotificacion.SOLICITUD_AMISTAD), eq(99L), contains("Juan Sanchez"));
    }

    // ---------- CP 9.2.1 ----------
    @Test
    void alDetectarCumpleanos_notificaAmigosYManadaEmail() {
        Usuario cumpleanero = usuario(1L, "jgomez", "Gomez");
        Usuario amigo = usuario(2L, "jperez", "Perez");
        when(amistadService.obtenerAmigos(cumpleanero)).thenReturn(List.of(amigo));

        listener.alDetectarCumpleanos(new CumpleanosEvent(cumpleanero));

        verify(notificacionService).crearNotificacion(
                eq(amigo), eq(TipoNotificacion.CUMPLEANOS), eq(1L), contains("jgomez"));
        verify(mailService).enviarEmailCumpleanos(amigo, cumpleanero);
    }
}
