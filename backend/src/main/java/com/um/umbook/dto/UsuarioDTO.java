package com.um.umbook.dto;

import com.um.umbook.model.Usuario;

import java.time.LocalDate;

/**
 * DTO de salida de un usuario (sin exponer la contrasena).
 * Incluye amigosEnComun para el CU-13 (listar usuarios con +2 amigos en comun).
 */
public class UsuarioDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String nombreUsuario;
    private LocalDate fechaNacimiento;
    private int amigosEnComun;

    public UsuarioDTO() {
    }

    public static UsuarioDTO fromEntity(Usuario u, int amigosEnComun) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.id = u.getId();
        dto.nombre = u.getNombre();
        dto.apellido = u.getApellido();
        dto.email = u.getEmail();
        dto.nombreUsuario = u.getNombreUsuario();
        dto.fechaNacimiento = u.getFechaNacimiento();
        dto.amigosEnComun = amigosEnComun;
        return dto;
    }

    public static UsuarioDTO fromEntity(Usuario u) {
        return fromEntity(u, 0);
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getEmail() {
        return email;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public int getAmigosEnComun() {
        return amigosEnComun;
    }
}
