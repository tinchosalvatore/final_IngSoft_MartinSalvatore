package com.um.umbook.service;

import com.um.umbook.dto.UsuarioDTO;
import com.um.umbook.model.Usuario;
import com.um.umbook.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Logica de usuarios. Metodos 1:1 con el diagrama de clases de diseño.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final AmistadService amistadService;

    public UsuarioService(UsuarioRepository usuarioRepository, AmistadService amistadService) {
        this.usuarioRepository = usuarioRepository;
        this.amistadService = amistadService;
    }

    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    /** Busqueda por texto (nombre o apellido) usada por la searchbar. */
    public List<Usuario> buscarUsuarios(String nombre, String apellido) {
        return usuarioRepository
                .findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(nombre, apellido);
    }

    /**
     * CU-13: lista los usuarios que tienen al menos {@code minAmigos} amigos en comun con
     * el usuario de referencia, excluyendo al propio usuario y a sus amigos directos
     * (sugerencias del tipo "personas que quizas conozcas").
     */
    public List<UsuarioDTO> listarConAmigosEnComun(Usuario referencia, int minAmigos) {
        Set<Long> idsAmigosDirectos = amistadService.obtenerAmigos(referencia).stream()
                .map(Usuario::getId)
                .collect(Collectors.toSet());

        List<UsuarioDTO> resultado = new ArrayList<>();
        for (Usuario candidato : usuarioRepository.findAll()) {
            if (candidato.getId().equals(referencia.getId())) {
                continue; // no me sugiero a mi mismo
            }
            if (idsAmigosDirectos.contains(candidato.getId())) {
                continue; // ya somos amigos
            }
            int comunes = amistadService.obtenerAmigosEnComun(referencia, candidato).size();
            if (comunes >= minAmigos) {
                resultado.add(UsuarioDTO.fromEntity(candidato, comunes));
            }
        }
        return resultado;
    }
}
