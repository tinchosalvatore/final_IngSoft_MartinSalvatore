# EXTRAS — código fuera del diagrama de clases

Este documento lista lo que el **código** de los CU-13/14/15 tiene de más respecto al
**diagrama de clases de diseño** (`docs/diseño/diagrama_clases.svg`). Son agregados intencionales
que **dan vida a la demo** (UI en vivo, datos de prueba, SSE). El diagrama de clases sigue siendo
la fuente 1:1; esto es lo que se permitió por encima.

> Si en el futuro hay que volver "puro" al diagrama, estos son los puntos a revisar.

## Backend

| Elemento | Dónde | Por qué es extra |
|---|---|---|
| `UsuarioService.agregarSugerenciaExtra(...)` + `POST /usuarios/sugerencia-extra` | `service/UsuarioService.java`, `controller/UsuarioController.java` | Botón "recargar sugerencias" del buscador (CU-13): crea un lote de usuarios demo con +2 amigos en común. No está en el diagrama. |
| `UsuarioService.marcarCumpleanosHoy(...)` + `PUT /usuarios/{id}/cumpleanos` | idem | Permite elegir el cumpleañero antes de correr el batch (demo CU-15). |
| `UsuarioService.cumpleanosDeHoy(...)` + `GET /usuarios/cumpleanos` | idem | Alimenta la tarjeta de "Cumpleaños" del home. |
| `UsuarioService.buscarPorTexto(...)` (campo `amigosEnComun` en el resultado de CU-7) | `service/UsuarioService.java` | CU-7 ya es 1:1: endpoint `GET /usuarios/buscar?nombre=&apellido=` + `buscarUsuarios(nombre, apellido)`. El `amigosEnComun` que devuelve la tarjeta es extra sobre el `List<Usuario>` del diagrama (ver `docs/diseño/diag_sec/MODIFICACIONES.md`). |
| `UsuarioService.obtenerPorId(...)` | `service/UsuarioService.java` | Helper para resolver el "usuario actual" de la demo (no hay login). |
| `AmistadService.sonAmigos(...)` | `service/AmistadService.java` | Helper de conveniencia, no está en el diagrama. |
| `SolicitudAmistadService.obtenerPendientes(...)` + `GET /solicitudes/pendientes` + `SolicitudAmistadRepository.findByDestinatarioAndEstado(...)` | `service/`, `controller/`, `repository/` | Subflujo "click en la notificación" de CU-14 (aparece en los diag. de secuencia, no en el de clases). |
| Param `remitenteId` en `POST /solicitudes` (`enviarSolicitud`) | `controller/SolicitudAmistadController.java` | El diagrama sólo recibe `destinatarioId`; `remitenteId` es para elegir quién manda la solicitud en la demo. |
| Endpoints id-based originales eliminados → ahora **token** | `controller/SolicitudAmistadController.java` | `aceptar`/`rechazar` ahora van por token (1:1 con el diagrama). El front manda `tokenEmail` expuesto en `SolicitudAmistadDTO`. |
| `NotificacionService.suscribir(...)` + `emitirNotificacion(...)` + `GET /notificaciones/stream` (SSE) | `service/NotificacionService.java`, `controller/NotificacionController.java` | Infraestructura de notificaciones en vivo (Server-Sent Events). `emitirNotificacion` aparece en los diag. de secuencia; `suscribir`/el stream son detalle de implementación. |
| Overload `NotificacionService.crearNotificacion(dest, tipo, refId, mensaje)` | `service/NotificacionService.java` | El diagrama tiene `crearNotificacion(dest, tipo, refId)`. El overload con `mensaje` deja que el toast muestre texto detallado (ej. "Fede te envió una solicitud") en vez del genérico. El método canónico de 3 args existe y delega en éste. |
| Dependencia `amistadService` en `CumpleanosService` | `service/CumpleanosService.java` | El diagrama le da sólo `notificacionService` + `usuarioRepository`, pero el fan-out a los amigos del cumpleañero necesita resolver amistades. |
| `CumpleanosController` + `POST /cumpleanos/ejecutar-batch` | `controller/CumpleanosController.java` | Dispara el batch on-demand para la demo (normalmente correría agendado). |
| `DataSeeder` (`seed/`) | `seed/DataSeeder.java` | Siembra usuarios + amistades + cumpleaños al arrancar. Andamiaje de demo. |

### Simplificación documentada

- `NotificacionService.enviarEmailCumpleanos(Usuario)` respeta la firma del diagrama (un solo
  parámetro), por lo que el cuerpo del email es **genérico** ("Un amigo cumple años hoy"): la firma
  no transporta al cumpleañero. El método rico `JavaMailService.enviarEmailCumpleanos(usuario, amigo)`
  queda disponible pero no se usa en el fan-out.

## Frontend

Todo el frontend es "extra" respecto al diagrama de clases (que modela el backend). Los componentes
Angular (`inicio`, `buscador`, `solicitudes`, `chat`, `home`, `toast`, `registro`, `perfil`) mapean
a las clases de borde de los **diagramas de colaboración** (`UISearchbar`, `UISolicitudes`, `UIChat`,
`UIhome`, etc.), no al diagrama de clases. El campo `tokenEmail` agregado a `SolicitudAmistad`
(modelo TS) espeja el DTO para poder aceptar/rechazar por token.

CU-2 trae infraestructura de sesión del lado del front (no modelada en el diagrama de clases, que
es del backend): `SesionService` (guarda el usuario logueado en `localStorage` y expone su id) y
`sesionGuard` (redirige a `/login` si no hay sesión). El backend agrega el contador
`Usuario.intentosFallidos` para el bloqueo de cuenta del CU-2 (ver
`docs/diseño/diag_sec/MODIFICACIONES.md`).
