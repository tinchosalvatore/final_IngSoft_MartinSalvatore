package com.um.umbook.service;

import com.um.umbook.dto.UsuarioDTO;
import com.um.umbook.exception.UsuarioYaExisteException;
import com.um.umbook.model.Usuario;
import com.um.umbook.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests de UsuarioService. Cubre CP 11.1.1 / CP 11.1.2 (listar usuarios con +2 amigos en comun)
 * y el alta de usuario (registro exitoso / duplicados).
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private AmistadService amistadService;
    @Mock
    private CumpleanosService cumpleanosService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario(Long id, String nombre) {
        Usuario u = new Usuario(nombre, "Ap", nombre + "@um.edu.ar", nombre, "x", LocalDate.of(2000, 1, 1));
        u.setId(id);
        return u;
    }

    // ---------- CP 11.1.1 ----------
    @Test
    void listarConAmigosEnComun_listaUsuariosConDosOMasAmigosEnComun() {
        Usuario ref = usuario(1L, "jperez");
        Usuario amigoDirecto = usuario(2L, "directo");
        Usuario candidatoA = usuario(3L, "candidatoA"); // 2 en comun -> aparece
        Usuario candidatoB = usuario(4L, "candidatoB"); // 1 en comun -> NO aparece

        when(amistadService.obtenerAmigos(ref)).thenReturn(List.of(amigoDirecto));
        when(usuarioRepository.findAll()).thenReturn(List.of(ref, amigoDirecto, candidatoA, candidatoB));
        when(amistadService.obtenerAmigosEnComun(ref, candidatoA))
                .thenReturn(List.of(usuario(8L, "x"), usuario(9L, "y")));
        when(amistadService.obtenerAmigosEnComun(ref, candidatoB))
                .thenReturn(List.of(usuario(8L, "x")));

        List<UsuarioDTO> resultado = usuarioService.listarConAmigosEnComun(ref, 2);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombreUsuario()).isEqualTo("candidatoA");
        assertThat(resultado.get(0).getAmigosEnComun()).isEqualTo(2);
    }

    // ---------- CP 11.1.2 ----------
    @Test
    void listarConAmigosEnComun_devuelveVacioCuandoNadieCalifica() {
        Usuario ref = usuario(1L, "jperez");
        Usuario candidato = usuario(3L, "candidato"); // solo 1 en comun

        when(amistadService.obtenerAmigos(ref)).thenReturn(List.of());
        when(usuarioRepository.findAll()).thenReturn(List.of(ref, candidato));
        when(amistadService.obtenerAmigosEnComun(ref, candidato)).thenReturn(List.of(usuario(8L, "x")));

        List<UsuarioDTO> resultado = usuarioService.listarConAmigosEnComun(ref, 2);

        assertThat(resultado).isEmpty();
    }

    // ---------- Alta de usuario ----------
    @Test
    void registrar_exitoso_hasheaContrasenaYGuarda() {
        Usuario nuevo = usuario(null, "nuevo");
        nuevo.setContrasena("secreta123");
        when(usuarioRepository.findByEmail(nuevo.getEmail())).thenReturn(null);
        when(usuarioRepository.findByNombreUsuario(nuevo.getNombreUsuario())).thenReturn(null);
        when(passwordEncoder.encode("secreta123")).thenReturn("HASH");
        when(usuarioRepository.save(nuevo)).thenReturn(nuevo);

        Usuario guardado = usuarioService.registrar(nuevo);

        assertThat(guardado.getContrasena()).isEqualTo("HASH");
        assertThat(guardado.isActivo()).isTrue();
        verify(usuarioRepository).save(nuevo);
    }

    @Test
    void registrar_emailDuplicado_lanzaExcepcion() {
        Usuario nuevo = usuario(null, "nuevo");
        when(usuarioRepository.findByEmail(nuevo.getEmail())).thenReturn(usuario(5L, "existente"));

        assertThrows(UsuarioYaExisteException.class, () -> usuarioService.registrar(nuevo));
        verify(usuarioRepository, never()).save(eq(nuevo));
    }

    @Test
    void registrar_nombreUsuarioDuplicado_lanzaExcepcion() {
        Usuario nuevo = usuario(null, "nuevo");
        when(usuarioRepository.findByEmail(nuevo.getEmail())).thenReturn(null);
        when(usuarioRepository.findByNombreUsuario(nuevo.getNombreUsuario())).thenReturn(usuario(5L, "existente"));

        assertThrows(UsuarioYaExisteException.class, () -> usuarioService.registrar(nuevo));
        verify(usuarioRepository, never()).save(eq(nuevo));
    }
}
