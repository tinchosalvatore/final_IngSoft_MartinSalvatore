package com.um.umbook.service;

import com.um.umbook.dto.UsuarioDTO;
import com.um.umbook.exception.UsuarioNotFoundException;
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
import static org.mockito.Mockito.when;

/**
 * Tests de UsuarioService. Cubre CU-7 (buscar usuarios, CP 1.3.1 / 1.3.2 / 1.3.3 / 1.3.5)
 * y CU-13 (listar usuarios con +2 amigos en comun, CP 11.1.1 / 11.1.2).
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

    private Usuario usuario(Long id, String nombre, String apellido) {
        Usuario u = new Usuario(nombre, apellido, nombre + "@um.edu.ar", nombre, "x", LocalDate.of(2000, 1, 1));
        u.setId(id);
        return u;
    }

    // ==================== CU-7: Buscar Usuarios ====================

    // ---------- CP 1.3.1 / 1.3.2 / 1.3.3 ----------
    @Test
    void buscarUsuarios_devuelveLasCoincidenciasDelRepositorio() {
        Usuario ana = usuario(2L, "Ana", "Gomez");
        // La searchbar manda el mismo texto como nombre y apellido (busqueda por nombre o apellido).
        when(usuarioRepository.findByNombreContainingOrApellidoContaining("gomez", "gomez"))
                .thenReturn(List.of(ana));

        List<Usuario> resultado = usuarioService.buscarUsuarios("gomez", "gomez");

        assertThat(resultado).containsExactly(ana);
    }

    // ---------- CP 1.3.5 ----------
    @Test
    void buscarUsuarios_sinResultados_devuelveListaVacia() {
        when(usuarioRepository.findByNombreContainingOrApellidoContaining("zzzz", "zzzz"))
                .thenReturn(List.of());

        List<Usuario> resultado = usuarioService.buscarUsuarios("zzzz", "zzzz");

        assertThat(resultado).isEmpty();
    }

    // ==================== CU-13: Listar +2 amigos en comun ====================

    // ---------- CP 11.1.1 ----------
    @Test
    void listarUsuarios_listaUsuariosConDosOMasAmigosEnComun() {
        Usuario ref = usuario(1L, "jperez", "Perez");
        Usuario candidatoA = usuario(3L, "candidatoA", "Ap"); // 2 en comun -> aparece

        // El filtrado pesado (≥2 + exclusiones) lo hace el repositorio (1:1 con el diagrama).
        when(usuarioRepository.findUsuariosConAmigosEnComun(ref)).thenReturn(List.of(candidatoA));
        when(amistadService.obtenerAmigosEnComun(ref, candidatoA))
                .thenReturn(List.of(usuario(8L, "x", "Ap"), usuario(9L, "y", "Ap")));

        List<UsuarioDTO> resultado = usuarioService.listarUsuarios(ref, 2);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombreUsuario()).isEqualTo("candidatoA");
        assertThat(resultado.get(0).getAmigosEnComun()).isEqualTo(2);
    }

    // ---------- CP 11.1.2 ----------
    @Test
    void listarUsuarios_sinCandidatos_lanzaUsuarioNotFound() {
        Usuario ref = usuario(1L, "jperez", "Perez");

        // El repositorio no devuelve candidatos con +2 amigos en comun.
        when(usuarioRepository.findUsuariosConAmigosEnComun(ref)).thenReturn(List.of());

        assertThrows(UsuarioNotFoundException.class, () -> usuarioService.listarUsuarios(ref, 2));
    }
}
