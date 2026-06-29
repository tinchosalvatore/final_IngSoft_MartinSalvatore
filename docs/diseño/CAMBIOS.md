# CAMBIOS — dónde el código se despega de los diagramas originales (CU-7, 13, 14, 15)

Este documento registra **dónde la implementación se aparta de los diagramas de diseño**
(`docs/diseño/diag_sec/` y `docs/diseño/diagrama_clases.svg`) para los **4 CU que se mantienen
1:1 estricto: CU-7, CU-13, CU-14, CU-15**. Sirve de insumo para **corregir los diagramas de
secuencia y el diagrama de clases** solo con lo realmente 1:1.

Regla aplicada: el profesor toma el 1:1 sobre el **diagrama de clases**. Cuando una **secuencia**
contradice al diagrama de clases y el **código ya cumple las clases**, mandó el código y se marca
la secuencia para redibujar. Donde el código necesita algo que el diseño no previó (extra
funcional), se documenta acá para incorporarlo al diagrama nuevo.

> Nota: `MODIFICACIONES.md` (de la entrega previa) cubría desviaciones menores de CU-1/2/6/7/18.
> Este archivo lo reemplaza como referencia para los 4 CU vigentes; CU-1/CU-2 fueron eliminados.

---

## 0. Alcance y decisiones globales

- **Solo CU-7, CU-13, CU-14, CU-15 son 1:1.** El resto del sistema es andamiaje de demo.
- **CU-1 (registrarse) y CU-2 (iniciar sesión): ELIMINADOS** por completo (front y back). Se quitó
  la cadena de autenticación (login, registro, bloqueo de cuenta) porque no son objetivo y su
  maquinaria sacaba a los 4 CU de los diagramas.
- **CU-6 (enviar solicitud) y CU-18 (gestionar solicitudes): se mantienen como SOPORTE** funcional,
  fuera del redibujo. CU-14 los necesita: `SolicitudAmistadService.enviarSolicitud` dispara la
  notificación, y el click del toast aterriza en `obtenerSolicitudes` (lista de pendientes).
- **Usuario actual sin sesión:** no hay login ni `SesionService`. El "usuario actual" es un
  **demo fijo = martin (id=1)**, resuelto internamente en el backend. Se eliminó el parámetro
  `usuarioId` de todos los endpoints (las secuencias y el diagrama de clases **no** lo modelaban;
  era un extra que rompía las firmas). El frontend ya no envía `usuarioId`; el SSE se abre como
  `GET /notificaciones/stream` sin parámetro.
- **Campos de `Usuario`:** se eliminó `intentosFallidos` (extra del bloqueo de cuenta, no estaba
  en el diagrama de clases). Se mantiene `activo` (sí está en el diagrama, aunque ahora sin uso).
- **Borrados concretos (back):** `dto/LoginDTO`, `dto/RegistroDTO`,
  `exception/{CredencialesInvalidas,CuentaBloqueada,UsuarioYaExiste}Exception` + sus handlers en
  `GlobalExceptionHandler`; métodos `UsuarioService.iniciarSesion/registrar`; endpoints
  `POST /usuarios/login` y `POST /usuarios`.
- **Borrados concretos (front):** componentes `inicio` (login) y `registro`, `guards/sesion.guard`,
  `services/sesion.service`; rutas `login`/`registro` y todos los `canActivate`.
- **Se mantiene** en `SecurityConfig`: CORS para `localhost:4200`, CSRF off, headers de H2,
  stateless, `permitAll` y el bean `PasswordEncoder` (lo usan `DataSeeder` y la demo de CU-13).

---

## CU-7 — Buscar Usuarios

**1:1 con secuencia y clases.** Ajustes hechos al código para lograrlo:

- El controller `UsuarioController.buscarUsuarios(nombre, apellido)` ahora llama directo a
  `UsuarioService.buscarUsuarios(nombre, apellido) : List<Usuario>` (firma 1:1 con el diagrama de
  clases), en vez del wrapper `buscarPorTexto`. Se **eliminó** `buscarPorTexto` (era un extra que
  agregaba `usuarioId`, auto-exclusión y conteo de amigos en común).
- Se **quitó `amigosEnComun`** de la búsqueda (no está en el diagrama; era adorno de la tarjeta) y
  la auto-exclusión del propio usuario.
- Repo `findByNombreContainingOrApellidoContaining` ya era 1:1.

**Pendiente menor (para el diagrama nuevo):** la búsqueda devuelve `List<UsuarioDTO>` (formato de
wire, sin exponer contraseña) en lugar de `List<Usuario>`; el DTO arrastra el campo `amigosEnComun`
en 0 (lo usa CU-13). Es ruido inocuo para CU-7.

---

## CU-13 — Listar Usuarios con +2 amigos en común

**El código ya cumplía el diagrama de clases; la SECUENCIA estaba mal.**

- **Secuencia a redibujar:** la llamada al repo figura como `buscarUsuario(+2amigos)`, pero el
  diagrama de clases (y el código) usan **`findUsuariosConAmigosEnComun(usuario)`**. Redibujar la
  secuencia con ese nombre.
- **Renombre de código:** el método de servicio `listarConAmigosEnComun` se renombró a
  **`listarUsuarios`** (nombre que usa la secuencia CU-13). El diagrama de clases no declaraba este
  método de servicio → agregarlo al diagrama nuevo.
- **Throw de "lista vacía":** la secuencia lo muestra lanzado por el **servicio**; se movió el
  `throw UsuarioNotFoundException` del controller al servicio (`listarUsuarios`) para casar.
- **Extra funcional necesario:** el conteo de amigos en común (vía `AmistadService`) es **núcleo**
  del CU-13 (la tarjeta lo muestra). El diagrama de clases no lista `amistadService` como campo de
  `UsuarioService` → agregarlo al diagrama nuevo.

---

## CU-14 — Notificar Solicitud de Amistad

**El núcleo ya es 1:1 con el diagrama de clases; la SECUENCIA modela un diseño viejo (eventos).**
Sin cambios de código salvo el `usuarioId` global.

- **Secuencia parte 1 a redibujar (contradice el diagrama de clases):** muestra
  `Sistema → NotificacionService.triggerNotificacion(solicitudId)`,
  `NotificacionService → UsuarioService.findById(...)` y
  `NotificacionService → NotificacionComponent.emitirNotificacion(UsuarioDTO)`. **Nada de eso
  existe en el diagrama de clases** (`NotificacionService` no depende de `UsuarioService`, no tiene
  `triggerNotificacion` ni `emitirNotificacion(UsuarioDTO)`). El flujo real (y 1:1 con clases) es
  por **llamada directa**:
  `SolicitudAmistadController.enviarSolicitud → SolicitudAmistadService.enviarSolicitud →
  NotificacionService.crearNotificacion(destinatario, tipo, referenciaId) + JavaMailService`.
  Redibujar la secuencia con esa forma.
- **Secuencia parte 2 (ref [Click en Notificación]):** muestra `obtenerPendientes()` con retorno
  `List<SolicitudAmistad>`. En código es **`obtenerSolicitudes()`** y devuelve
  `List<SolicitudAmistadDTO>`. Ajustar la secuencia.
- **Extra de demo:** `SolicitudAmistadController.enviarSolicitud` recibe `remitenteId` y
  `destinatarioId` (el diagrama de clases declara solo `enviarSolicitud(destinatarioId)`). Son
  parámetros que usan los scripts de demo (`scripts/TriggerSolicitud.java`). En la demo, el
  remitente es otro usuario y el destinatario es martin (id=1).

---

## CU-15 — Notificar Cumpleaños

**El núcleo ya es 1:1 con el diagrama de clases; la SECUENCIA está mal en nombres, y la parte 2
(Chat) no está implementada en backend.** Sin cambios de código en el núcleo.

- **Secuencia parte 1 a redibujar (nombres que contradicen el diagrama de clases):**
  - `verificarCumpleañosDiarios()` → en código y clases es **`ejecutarBatchDiario()`**.
  - `UsuarioRepository.findByFechaNacimiento(hoy)` → **no existe** ese método; el código resuelve
    con `findAll().filter(...)` dentro de `obtenerUsuariosConCumpleanos()`.
  - `NotificacionService.crearNotificacionCumpleaños(usuarios)` → **no existe**; el código itera
    los amigos y llama `crearNotificacion(amigo, CUMPLEANOS, cumpleañeroId, mensaje)` por cada uno.
  - `emitirNotificacion(CumpleañeroDTO)` → no está en clases (ver "Extras" abajo).
- **Secuencia parte 2 (ref [Click] → Chat): NO implementada en backend.** El click del toast abre
  una **pantalla de chat demo solo-front** (`chat/chat.component`, respuesta precargada, sin
  llamadas HTTP). La secuencia dibuja
  `ChatController.GET /chat/conversacion → ChatService.obtenerOCrearChat(usuarioId, amigoId) :
  ChatDTO`, que **además contradice el diagrama de clases** (su `ChatService` declara
  `crearChat/obtenerChats/obtenerMensajes/eliminarChat`, no `obtenerOCrearChat`, y no hay `ChatDTO`).
  **Al redibujar:** marcar la parte 2 como **no implementada / solo-front**; no dejar las lifelines
  `ChatController`/`ChatService` como una interacción real de backend.

---

## Extras funcionales necesarios (incorporar al diagrama de clases nuevo)

Cosas que el código necesita para funcionar y que el diseño original no previó. No son "innovación
gratis": son necesarias y deben quedar reflejadas en el diagrama reconstruido.

- **SSE (notificaciones en vivo):** `NotificacionService` agrega `suscribir(usuarioId)`,
  `emitirNotificacion(usuarioId, NotificacionDTO)` y el mapa `emittersPorUsuario`
  (`ConcurrentHashMap<Long, List<SseEmitter>>`). Es el mecanismo real del toast en vivo de CU-14/15.
  El `NotificacionController` expone `GET /notificaciones/stream` (canal SSE).
- **Overload `crearNotificacion(destinatario, tipo, referenciaId, mensaje)`:** además del de 3
  parámetros del diagrama, hay un overload con `mensaje` para que el toast muestre texto específico
  (ej. "Fede te envió una solicitud", "Ana cumple años hoy") con datos del remitente/cumpleañero
  que la firma de 3 parámetros no transporta.
- **`AmistadService` como dependencia:** en `CumpleanosService` (fan-out a los amigos del
  cumpleañero) y en `UsuarioService` (conteo de amigos en común de CU-13).
- **`PasswordEncoder` en `UsuarioService`:** lo usa la demo de CU-13 (`agregarSugerenciaExtra`) y
  `DataSeeder` para sembrar contraseñas BCrypt (cosmético: sin login no se verifican).

### Helpers de demo (no son CU, dan vida a la demo)

- `POST /usuarios/sugerencia-extra` (`agregarSugerenciaExtra`): botón "recargar sugerencias" de
  CU-13; agrega de a 2 usuarios con +2 amigos en común; 404 cuando no quedan.
- `GET /usuarios/cumpleanos` (`cumpleanosDeHoy`) + `PUT /usuarios/{id}/cumpleanos`
  (`marcarCumpleanosHoy`): alimentan la tarjeta de cumpleaños del home y permiten elegir el
  cumpleañero antes de correr el batch (lo usa `scripts/TriggerCumple.java`).
- `POST /cumpleanos/ejecutar-batch` (`CumpleanosController`): dispara el batch de CU-15 on-demand
  para la demo.
