# Scripts de demo

Disparan las notificaciones desde el back para verlas llegar en vivo a la UI.
Son archivos Java de un solo fichero (Java 21+), no requieren compilar.

Requisitos: backend corriendo en `http://localhost:8080` y la UI abierta en
`http://localhost:4200` (logueado/observando como `martin`, id=1).

## CU-14 — Solicitud de amistad

```bash
java scripts/TriggerSolicitud.java
# o indicando remitente y destinatario:
java scripts/TriggerSolicitud.java 7 1
```

Default: `fede` (id=7) envia solicitud a `martin` (id=1) → toast en vivo.

## CU-15 — Cumpleaños

```bash
java scripts/TriggerCumple.java
# o indicando el usuario cumpleañero:
java scripts/TriggerCumple.java 3
```

Default: setea el cumpleaños de `beto` (id=3) a HOY y corre el batch → toast en vivo
para sus amigos (martin entre ellos).
