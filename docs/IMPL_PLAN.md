# IMPL_PLAN — UM-Book (demo CU-13/14/15)

## Context

Proyecto de fantasía para Ing. en Informática (UM), materia de Proceso Unificado de
Desarrollo. UM-Book = "facebook" de la universidad. No es producto real: es una demo
de implementación **1:1** con el diseño documentado (diagrama de clases + secuencia).
Solo se implementan 3 Casos de Uso, más andamiaje mínimo para que corran en vivo:

- **CU-13** Listar usuarios con +2 amigos en común (buscador, fullstack funcional).
- **CU-14** Notificar Solicitud de Amistad (toast en vivo, sin refrescar).
- **CU-15** Notificar Cumpleaños (toast en vivo, sin refrescar).

Más: Home solo-UI (único link real = buscador), alta simple de usuario (con cumpleaños),
y 2 "scripts" Java que disparan las notis desde el back para verlas llegar a la UI en vivo.

El stack y los nombres de clases/métodos/endpoints salen **textual** de los diagramas en
`docs/diseño/` y `docs/analisis/diag_colab/`. Respetar esos nombres es el objetivo (1:1).

## Stack (de la spec)

| Capa | Tec |
|---|---|
| Backend | Spring Boot 3.4.3 (Java 21), Maven |
| Frontend | Angular 18 (TypeScript) |
| DB | **Mix por perfiles**: H2 in-memory (default, demo arranca solo) + MySQL 8.4 (perfil `mysql`, per spec) |
| ORM | JPA / Hibernate |
| Seguridad | Spring Security + BCrypt |
| Real-time | **SSE** (SseEmitter ↔ EventSource) |
| Comunicación | REST API JSON |

Nota: el `CLAUDE.md` global habla de venv/vpip de Python — **no aplica** (proyecto Java/Angular).

## Decisiones (confirmadas con el usuario)

- Tiempo real: **SSE**. `GET /notificaciones/stream` (SseEmitter) ↔ `EventSource` en Angular.
- DB: **mix por Spring profiles**. `application.properties` → H2 (default). `application-mysql.properties` → MySQL 8.4. Mismo código JPA. Arrancar MySQL: `--spring.profiles.active=mysql`.
- Scripts: **endpoints dev + cliente Java**. `POST /dev/trigger-solicitud`, `POST /dev/trigger-cumple` + 2 clases Java standalone (`java Script.java`, Java 21 single-file) con `HttpClient`.
- Click-through: **toast + panel simple**. CU-14 click → lista solicitudes pendientes (real). CU-15 click → página/toast simple. **No** se implementa Chat completo (fuera de foco).

## Modelo de dominio (subset de los diagramas, solo lo necesario)

Entidades JPA: `Usuario`, `Amistad`, `SolicitudAmistad`, `Notificacion`.
Enums: `EstadoSolicitud {PENDIENTE, ACEPTADA, RECHAZADA}`, `TipoNotificacion {SOLICITUD_AMISTAD, CUMPLEANOS, ...}`.
Campos exactos del diagrama de clases (ej. `Usuario`: id, nombre, apellido, email, nombreUsuario,
contrasena, fechaNacimiento, activo, diasCumpleanosConfig). Chat/Mensaje/Foto/Album/Grupo/etc → **NO** se implementan.

## Capas backend (nombres 1:1 del diagrama)

- **Repositories** (Spring Data JPA): `UsuarioRepository` (incl. `findByNombreContainingOrApellidoContaining`, `findUsuariosConAmigosEnComun`), `AmistadRepository` (`findAmigosEnComun`, `findByUsuario1OrUsuario2`), `SolicitudAmistadRepository` (`findByDestinatario`, `findByTokenEmail`, `findByEstado`), `NotificacionRepository` (`findByDestinatarioAndLeida`).
- **Services**: `UsuarioService` (`registrar`, `buscarUsuarios`), `AmistadService` (`obtenerAmigosEnComun`, `crearAmistad`), `SolicitudAmistadService` (`enviarSolicitud`, `generarTokenEmail`, `obtenerPendientes`), `NotificacionService` (`crearNotificacion`, `obtenerNoLeidas`, `emitirNotificacion` vía SSE), `CumpleanosService` (`ejecutarBatchDiario`, `obtenerUsuariosConCumpleanos`).
  - `JavaMailService` → **stub** (loguea en vez de enviar; demo sin SMTP real).
- **Controllers**: `UsuarioController` (`registrar`, `buscarUsuarios` → `GET /usuarios?amigosEnComun=2`), `SolicitudAmistadController` (`obtenerPendientes` → `GET /solicitudes/pendientes`), `NotificacionController` (stream SSE + `GET /notificaciones/no-leidas`), `DevController` (triggers, solo perfil dev).
- **DTOs**: `UsuarioDTO`, `SolicitudAmistadDTO`, `NotificacionDTO`, `LoginDTO` (per diagrama secuencia).
- **Seguridad**: `SecurityConfig` con BCrypt `PasswordEncoder`; para demo, CORS abierto a `localhost:4200` y endpoints públicos (sin login real obligatorio — alcance demo).
- **Seed**: `data.sql` o `CommandLineRunner` que inserta ~6-8 usuarios con amistades cruzadas (para que CU-13 devuelva resultados con ≥2 amigos en común) y fechas de nacimiento (una = hoy para CU-15). **Nada hardcodeado en código de negocio** — todo en DB.

## Frontend Angular (alcance demo)

- `HomeComponent` — solo UI (mock de feed), único link real → buscador. Barra de notis (toasts) global.
- `BuscadorComponent` — input + `GET /usuarios?amigosEnComun=2`, lista `UsuarioDTO`, maneja alt vacío ("No se encontraron usuarios").
- `RegistroComponent` — alta simple: nombre, apellido, email, usuario, contraseña, fechaNacimiento → `POST /usuarios`.
- `SolicitudesComponent` — panel simple, lista pendientes (click del toast CU-14).
- `NotificacionService` (Angular) — `EventSource` a `/notificaciones/stream`; emite a `ToastComponent`.
- `ToastComponent` — muestra toast solicitud/cumpleaños en vivo.

## Estructura de objetivos (commits separados — el usuario pushea de a uno)

1. **Scaffold + IMPL_PLAN.md** — copiar este plan a `docs/IMPL_PLAN.md`; init proyecto Maven Spring Boot (`backend/`) + Angular (`frontend/`); perfiles H2/MySQL; `SecurityConfig` + BCrypt; arranque verificado.
2. **Dominio + seed** — entidades JPA, enums, repositories; seed de usuarios+amistades+cumple. Verif: H2 console / query.
3. **CU-13 buscador (fullstack)** — `UsuarioService.buscarUsuarios` + amigos en común, `UsuarioController`, DTO; Angular `BuscadorComponent`. Verif: buscar devuelve usuarios con ≥2 amigos en común; caso vacío.
4. **Registro de usuario** — `UsuarioService.registrar` + endpoint + `RegistroComponent`. Verif: alta y aparece en buscador.
5. **Home UI** — `HomeComponent` mockeado + link real al buscador + barra de notis SSE conectada.
6. **CU-14 + CU-15 notis (SSE)** — `NotificacionService` con `SseEmitter`, `CumpleanosService`, `SolicitudAmistadService.enviarSolicitud`; Angular `EventSource` + `ToastComponent` + `SolicitudesComponent`.
7. **Scripts Java de disparo** — `DevController` triggers + 2 clases single-file Java (`scripts/TriggerSolicitud.java`, `scripts/TriggerCumple.java`). Verif: correr script → toast aparece en UI en vivo.

## Verificación end-to-end

- Backend: `cd backend && ./mvnw spring-boot:run` (perfil default H2). MySQL: `--spring.profiles.active=mysql`.
- Frontend: `cd frontend && npm install && ng serve` → `http://localhost:4200`.
- CU-13: Home → link buscador → buscar → lista usuarios con +2 amigos; probar caso vacío.
- Registro: alta usuario con cumpleaños → reaparece en buscador.
- CU-14: `java scripts/TriggerSolicitud.java` (Java 21) → toast "solicitud de amistad" en vivo → click → lista pendientes.
- CU-15: `java scripts/TriggerCumple.java` → setea fecha de cumple de un usuario a hoy + batch → toast "cumpleaños" en vivo.

## Fuera de alcance (explícito)

Chat/Mensaje, Foto/Album/Grupo/Permiso/Muro/Comentario, Admin, login/sesión completos,
envío real de emails (JavaMailService = stub), aceptar/rechazar solicitud (solo notificar+listar).
