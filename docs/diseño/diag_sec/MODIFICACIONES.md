# MODIFICACIONES — desviaciones pequeñas respecto a los diagramas de secuencia

Este documento registra las **desviaciones menores** entre los diagramas de secuencia de diseño
(`docs/diseño/diag_sec/`) y la implementación 1:1 de **CU-1, CU-2, CU-6, CU-7, CU-18**. Son
ajustes acotados (nada estructural); el resto es 1:1 con el diagrama. Lo que el código agrega
por fuera del diagrama de clases sigue catalogado en `docs/EXTRAS.md`.

## Clase de borde "Angular"

Donde un diagrama nombra la clase de borde (UI) como `Angular`, se la trató como nombre inválido
y se mapeó al **componente Angular real** con nombre semántico:

| Diagrama | Clase de borde usada | Componente real |
|---|---|---|
| CU-1 | `RegistroUI` | `registro/registro.component` |
| CU-2 | `InicioUI` | `inicio/inicio.component` |
| CU-7 | `BuscadorUI` | `buscador/buscador.component` |
| CU-18 | `SolicitudesUI` | `solicitudes/solicitudes.component` |

## CU-1 Registrarse

- El `validarDatos()` (self-call del `UsuarioService` en la secuencia) se realiza con
  **Bean Validation `@Valid`** sobre `RegistroDTO` (en el controller) + chequeo de unicidad
  (email / nombre de usuario) en el service. No se agregó un método público `validarDatos`
  porque el diagrama de clases no lo define.
- Datos inválidos → **400** (vía `@Valid`); duplicado → **409** (`UsuarioYaExisteException`).
  El diagrama muestra un 400 genérico para el alternativo "datos inválidos".

## CU-2 Iniciar Sesión

- **Umbral de bloqueo:** la secuencia muestra `save(usuario, activo:false)` en "múltiples
  intentos" pero no fija el número. Se eligió **3 intentos fallidos consecutivos** antes de
  bloquear (`UsuarioService.MAX_INTENTOS_FALLIDOS`).
- Se agregó el campo `Usuario.intentosFallidos` (contador) para soportar el bloqueo; un login
  exitoso lo resetea. Una cuenta con `activo=false` no puede iniciar sesión (400).
- Códigos: credenciales inválidas → **401** (`CredencialesInvalidasException`); cuenta
  bloqueada → **400** (`CuentaBloqueadaException`).
- **Login por email o nombre de usuario (UX):** el campo del login acepta email **o** nombre de
  usuario. La firma se mantiene 1:1 (`iniciarSesion(email, contrasena)`) y el lookup de
  autenticación sigue siendo `findByEmail` (= secuencia CU-2): si lo ingresado no es un email
  registrado, se resuelve a su email vía `findByNombreUsuario` **antes** de autenticar. Es una
  desviación "retorcida" (la secuencia solo modela `findByEmail`).
- **Usuario actual:** el diseño asume que el usuario actual proviene del login (CU-2). Se quitó
  el `DEMO_USUARIO_ID = 1` hardcodeado del front; ahora el id sale de la sesión
  (`SesionService`) y se propaga como `usuarioId` al resto (SSE, CU-7, CU-13, CU-18). Si no hay
  sesión, el backend cae al usuario de referencia de la demo (martin) para no romper scripts.

## CU-7 Buscar Usuarios

- Endpoint nuevo dedicado **`GET /usuarios/buscar?nombre=&apellido=`** (1:1 con la secuencia).
  CU-13 conserva `GET /usuarios?amigosEnComun=`. Se quitó el parámetro `q` que antes vivía en
  `GET /usuarios` (era un extra).
- El método del controller se llama **`buscarUsuarios(nombre, apellido)`** (1:1 con el diagrama de
  clases). Para liberar ese nombre, el handler de CU-13 (`GET /usuarios`) se renombró a
  **`listarUsuarios`** (nombre que usa la secuencia CU-13). Solo cambian nombres de método Java;
  los endpoints/paths no cambian.
- La searchbar del front es **un solo campo**: manda el mismo texto en `nombre` y `apellido`.
- La secuencia devuelve `List<Usuario>`; el endpoint devuelve `List<UsuarioDTO>` con el campo
  **`amigosEnComun`** (extra de la tarjeta, ver `docs/EXTRAS.md`).

## CU-6 Enviar Solicitud

- Se agregaron las guardas de las alternativas del diagrama antes de persistir: si **ya son
  amigos** o si **ya existe una solicitud pendiente** → **409** (`SolicitudInvalidaException`).
- El parámetro `remitenteId` del endpoint sigue siendo un extra de demo (ver `docs/EXTRAS.md`).

## CU-18 Gestionar Solicitudes

- `obtenerPendientes` se renombró a **`obtenerSolicitudes(usuario)`** (nombre de la secuencia),
  tanto en el service como en el método del controller; devuelve solo las **PENDIENTE**. Si no
  hay ninguna → **404** (`NoHaySolicitudesException`, alt 4.1). Se conserva el path REST
  `GET /solicitudes/pendientes`.
- **Aceptar:** además de crear la amistad, notifica al **remitente** que fue aceptada con un
  tipo nuevo `TipoNotificacion.SOLICITUD_ACEPTADA` (la secuencia muestra
  `crearNotificacion(remitente, Amistad)`).
- **Rechazar:** ahora **elimina** la solicitud (`repository.delete`, = `Delete` de la alt 3.1),
  en vez de marcarla `RECHAZADA`. Como consecuencia, el valor `EstadoSolicitud.RECHAZADA` queda
  sin uso, pero se conserva en el enum porque el diagrama de clases lo declara.

## 1:1 nominal (auditoría)

Para que el código quede literal al **diagrama de clases**, se ajustaron estos nombres/campos aun
cuando no cambian la funcionalidad:

- `AmistadService` incorpora el campo `- notificacionService : NotificacionService` que declara el
  diagrama de clases, aunque por ahora **no se usa** (ningún método de amistad emite notificación
  en el alcance actual).
- Renombres de método ya descritos arriba: `UsuarioController.buscarUsuarios` (CU-7) +
  `listarUsuarios` (CU-13); `SolicitudAmistadController.obtenerSolicitudes` (CU-18).
