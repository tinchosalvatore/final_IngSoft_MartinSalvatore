package com.um.umbook.event;

import com.um.umbook.model.Usuario;

/**
 * Evento de dominio: se detecto un cumpleaños de hoy (CU-15). Lo publica el batch de
 * {@code CumpleanosService} (uno por cumpleañero) y reacciona {@code NotificacionEventListener},
 * que avisa a los amigos. El batch no conoce al subsistema de notificaciones (observer).
 */
public class CumpleanosEvent {

    private final Usuario cumpleanero;

    public CumpleanosEvent(Usuario cumpleanero) {
        this.cumpleanero = cumpleanero;
    }

    public Usuario getCumpleanero() {
        return cumpleanero;
    }
}
