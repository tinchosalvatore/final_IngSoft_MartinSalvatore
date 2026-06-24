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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests de CumpleanosService. Tras el refactor a llamada directa (1:1 con el diagrama), el batch
 * detecta los cumpleaños de hoy y, por cada amigo del cumpleañero, llama directo a
 * notificacionService.crearNotificacion. Cubre CP 9.2.1 (notifica cuando cumple hoy) y CP 9.2.2
 * (no notifica si no es hoy).
 */
@ExtendWith(MockitoExtension.class)
class CumpleanosServiceTest {

    @Mock
    private NotificacionService notificacionService;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private AmistadService amistadService;

    @InjectMocks
    private CumpleanosService cumpleanosService;

    private Usuario usuario(Long id, String nombre, LocalDate fechaNacimiento) {
        Usuario u = new Usuario(nombre, "Gomez", nombre + "@um.edu.ar", nombre, "x", fechaNacimiento);
        u.setId(id);
        return u;
    }

    // ---------- CP 9.2.1 ----------
    @Test
    void ejecutarBatchDiario_notificaAlosAmigosCuandoCumpleHoy() {
        LocalDate hoy = LocalDate.now();
        Usuario cumpleanero = usuario(1L, "jgomez", LocalDate.of(2000, hoy.getMonthValue(), hoy.getDayOfMonth()));
        Usuario otro = usuario(2L, "jperez", LocalDate.of(1999, 5, 5));
        Usuario amigo = usuario(3L, "amigo", LocalDate.of(1998, 3, 3));

        when(usuarioRepository.findAll()).thenReturn(List.of(cumpleanero, otro));
        when(amistadService.obtenerAmigos(cumpleanero)).thenReturn(List.of(amigo));

        cumpleanosService.ejecutarBatchDiario();

        verify(notificacionService).crearNotificacion(eq(amigo),
                eq(TipoNotificacion.CUMPLEANOS), eq(1L), any(String.class));
        verify(notificacionService).enviarEmailCumpleanos(amigo);
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
