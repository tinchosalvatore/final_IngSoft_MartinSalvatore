# Scripts de demo

Provocan los **hechos reales** del dominio contra los endpoints de la app; la notificacion
en vivo aparece como **reaccion** a esos hechos (por llamada directa → toast SSE), no como un
disparo directo de notificacion. Son archivos Java de un solo fichero (Java 21+), no requieren
compilar.

Requisitos: backend corriendo en `http://localhost:8080` y la UI abierta en
`http://localhost:4200`. No hay login: la UI observa siempre al usuario de la demo `martin`
(id=1), por lo que los scripts notifican a `martin` por defecto.

## CU-6 / CU-14 — Solicitud de amistad

```bash
java scripts/TriggerSolicitud.java
# o indicando remitente y destinatario:
java scripts/TriggerSolicitud.java 7 1
```

Hace `POST /solicitudes` (envio real). Default: `fede` (id=7) envia solicitud a `martin`
(id=1). `fede` no es amigo de `martin`, asi que pasa las guardas de CU-6. El backend la
persiste y, por llamada directa, notifica a `martin` → toast en vivo.

## CU-15 — Cumpleaños

```bash
java scripts/TriggerCumple.java
# o eligiendo el cumpleañero (edita su cumple a hoy y corre el batch):
java scripts/TriggerCumple.java 3
```

Hace `POST /cumpleanos/ejecutar-batch` (el batch diario real). El batch detecta quienes
cumplen hoy y, por llamada directa, avisa a sus amigos. Sin argumentos notifica por quienes
ya cumplen hoy: el seed deja a `ana` (id=2) cumpliendo hoy, y como `ana` es amiga de
`martin`, el toast le llega. Para elegir otro cumpleañero que notifique a `martin`, pasale
un amigo suyo: `2` (ana), `3` (beto) o `4` (carla); el script hace `PUT /usuarios/{id}/cumpleanos`
(edita el cumple a hoy, accion real de perfil) y despues corre el batch.
