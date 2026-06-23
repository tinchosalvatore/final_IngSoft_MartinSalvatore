package com.um.umbook.service;

import com.um.umbook.event.CumpleanosEvent;
import com.um.umbook.model.Usuario;
import com.um.umbook.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests de CumpleanosService. Tras el refactor a eventos, el batch solo DETECTA los
 * cumpleaños de hoy y publica un evento por cada uno (la notificacion la hace el listener).
 * Cubre CP 9.2.1 (publica evento cuando cumple hoy) y CP 9.2.2 (no publica si no es hoy).
 */
@ExtendWith(MockitoExtension.class)
class CumpleanosServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CumpleanosService cumpleanosService;

    private Usuario usuario(Long id, String nombre, LocalDate fechaNacimiento) {
        Usuario u = new Usuario(nombre, "Gomez", nombre + "@um.edu.ar", nombre, "x", fechaNacimiento);
        u.setId(id);
        return u;
    }

    // ---------- CP 9.2.1 ----------
    @Test
    void ejecutarBatchDiario_publicaEventoCuandoCumpleHoy() {
        LocalDate hoy = LocalDate.now();
        Usuario cumpleanero = usuario(1L, "jgomez", LocalDate.of(2000, hoy.getMonthValue(), hoy.getDayOfMonth()));
        Usuario otro = usuario(2L, "jperez", LocalDate.of(1999, 5, 5));

        when(usuarioRepository.findAll()).thenReturn(List.of(cumpleanero, otro));

        int cantidad = cumpleanosService.ejecutarBatchDiario();

        assertThat(cantidad).isEqualTo(1);
        ArgumentCaptor<CumpleanosEvent> captor = ArgumentCaptor.forClass(CumpleanosEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getCumpleanero()).isEqualTo(cumpleanero);
    }

    // ---------- CP 9.2.2 ----------
    @Test
    void ejecutarBatchDiario_noPublicaCuandoNoEsHoy() {
        LocalDate manana = LocalDate.now().plusDays(1);
        Usuario jgomez = usuario(1L, "jgomez", LocalDate.of(2000, manana.getMonthValue(), manana.getDayOfMonth()));
        Usuario jperez = usuario(2L, "jperez", LocalDate.of(1999, 5, 5));

        when(usuarioRepository.findAll()).thenReturn(List.of(jgomez, jperez));

        int cantidad = cumpleanosService.ejecutarBatchDiario();

        assertThat(cantidad).isZero();
        verifyNoInteractions(eventPublisher);
    }
}
