package com.um.umbook.exception;

/**
 * CU-2 (alt 3.1, multiples intentos): la cuenta quedo bloqueada (activo=false) tras superar
 * el umbral de intentos fallidos de inicio de sesion. Se traduce a HTTP 400.
 */
public class CuentaBloqueadaException extends RuntimeException {

    public CuentaBloqueadaException(String mensaje) {
        super(mensaje);
    }
}
