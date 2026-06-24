package com.um.umbook.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Datos de entrada para iniciar sesion (CU-2). La validacion se aplica en el controller
 * con @Valid. Espeja la clase LoginDTO del diagrama de clases.
 */
public class LoginDTO {

    @NotBlank(message = "El email es obligatorio")
    private String email;

    @NotBlank(message = "La contrasena es obligatoria")
    private String contrasena;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}
