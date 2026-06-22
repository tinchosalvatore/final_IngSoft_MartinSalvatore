package com.um.umbook.service;

import com.um.umbook.model.TipoNotificacion;
import com.um.umbook.model.Usuario;
import com.um.umbook.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests de CumpleanosService. Cubre CP 9.2.1 (notifica a los amigos cuando el cumpleaños
 * es hoy) y CP 9.2.2 (no notifica cuando la fecha no coincide con hoy).
 */
@ExtendWith(MockitoExtension.class)
class CumpleanosServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private AmistadService amistadService;
    @Mock
    private NotificacionService notificacionService;
    @Mock
    private JavaMailService mailService;

    @InjectMocks
    private CumpleanosService cumpleanosService;

    private Usuario usuario(Long id, String nombre, LocalDate fechaNacimiento) {
        Usuario u = new Usuario(nombre, "Gomez", nombre + "@um.edu.ar", nombre, "x", fechaNacimiento);
        u.setId(id);
        return u;
    }

    // ---------- CP 9.2.1 ----------
    @Test
    void ejecutarBatchDiario_notificaAmigosCuandoCumpleHoy() {
        LocalDate hoy = LocalDate.now();
        Usuario cumpleanero = usuario(1L, "jgomez", LocalDate.of(2000, hoy.getMonthValue(), hoy.getDayOfMonth()));
        Usuario amigo = usuario(2L, "jperez", LocalDate.of(1999, 5, 5));

        when(usuarioRepository.findAll()).thenReturn(List.of(cumpleanero, amigo));
        when(amistadService.obtenerAmigos(cumpleanero)).thenReturn(List.of(amigo));

        cumpleanosService.ejecutarBatchDiario();

        verify(notificacionService).crearNotificacion(
                eq(amigo), eq(TipoNotificacion.CUMPLEANOS), eq(1L), contains("jgomez"));
        verify(mailService).enviarEmailCumpleanos(amigo, cumpleanero);
    }

    // ---------- CP 9.2.2 ----------
    @Test
    void ejecutarBatchDiario_noNotificaCuandoNoEsHoy() {
        LocalDate manana = LocalDate.now().plusDays(1);
        Usuario jgomez = usuario(1L, "jgomez", LocalDate.of(2000, manana.getMonthValue(), manana.getDayOfMonth()));
        Usuario jperez = usuario(2L, "jperez", LocalDate.of(1999, 5, 5));

        when(usuarioRepository.findAll()).thenReturn(List.of(jgomez, jperez));

        cumpleanosService.ejecutarBatchDiario();

        verifyNoInteractions(notificacionService);
    }
}
