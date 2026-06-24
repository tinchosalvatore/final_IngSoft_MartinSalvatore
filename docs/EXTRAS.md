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
| `UsuarioService.buscarPorTexto(...)` + param `q` en `GET /usuarios` | idem | Searchbar por nombre/apellido. El diagrama tiene `buscarUsuarios(nombre, apellido)` en el service, pero el endpoint con `q` es agregado. |
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
Angular (`buscador`, `solicitudes`, `chat`, `home`, `toast`, `registro`, `perfil`) mapean a las
clases de borde de los **diagramas de colaboración** (`UISearchbar`, `UISolicitudes`, `UIChat`,
`UIhome`, etc.), no al diagrama de clases. El campo `tokenEmail` agregado a `SolicitudAmistad`
(modelo TS) espeja el DTO para poder aceptar/rechazar por token.
