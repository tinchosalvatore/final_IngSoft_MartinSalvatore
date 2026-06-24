# FALTA — clases del diagrama de diseño fuera de CU-13/14/15

El diagrama de clases (`docs/diseño/diagrama_clases.svg`) modela el **sistema completo** de UM-Book.
Esta implementación cubre sólo **CU-13, CU-14 y CU-15**, así que muchas clases del diagrama **no
están implementadas**. Este documento las lista por si en el futuro se implementa el sistema entero.

> **Importante:** acá NO se toca código. Es sólo el inventario de lo pendiente del diagrama.
> Lo ya implementado y alineado 1:1 está en el backend; los agregados de demo, en `EXTRAS.md`.

## Subsistemas enteros no implementados

Por cada uno faltan entidad + repository + service + controller (salvo donde se aclare):

| Subsistema | Clases del diagrama | Métodos clave (firma del diagrama) |
|---|---|---|
| **Grupos** | `Grupo`, `GrupoRepository`, `GrupoService`, `GrupoController` | `crearGrupo`, `eliminarGrupo`, `agregarMiembro`, `eliminarMiembro`, `obtenerGruposDePropietario` |
| **Comentarios** | `Comentario`, `ComentarioRepository`, `ComentarioService`, `ComentarioController` | `crear`, `editar`, `eliminar`, `eliminarPorAdmin` |
| **Fotos** | `Foto`, `FotoRepository`, `FotoService`, `FotoController` | `subirFoto`, `eliminarFoto`, `obtenerFotosPorAlbum` |
| **Álbumes** | `Album`, `AlbumRepository`, `AlbumService`, `AlbumController` | `crearAlbum`, `eliminarAlbum`, `agregarFoto`, `eliminarFoto`, `verificarAcceso` |
| **Muro** | `Muro`, `MuroRepository`, `MuroService`, `MuroController` | `obtenerMuro`, `verificarAcceso` |
| **Permisos** | `Permiso`, `PermisoRepository`, `PermisoService` | `darPermiso`, `quitarPermiso`, `modificarPermiso`, `verificarAcceso` |
| **Chat / Mensajería** | `Chat`, `Mensaje`, `ChatRepository`, `MensajeRepository`, `ChatService`, `MensajeService`, `ChatController` | `crearChat`, `obtenerChats`, `obtenerMensajes`, `eliminarChat`, `enviar`, `marcarComoLeido`, `obtenerNoLeidos` |
| **Admin** | `Admin`, `AdminRepository`, `AdminService`, `AdminController` | `deshabilitarUsuario`, `eliminarComentario` |
| **Archivos** | `ArchivoService` | `guardarArchivo`, `eliminarArchivo`, `obtenerArchivo` |

> Nota CU-15: el subflujo "click en la notificación" de cumpleaños abre un chat (`ChatController.
> obtenerOCrearChat`, en el diag. de secuencia). Hoy el chat es **sólo UI** en el frontend; el
> backend de Chat/Mensaje pertenece a este pendiente.

## Interfaces de diseño no implementadas

- `Recurso` `<<interface>>`
- `RecursoComentable` `<<interface>>` — `agregarComentario`, `eliminarComentario`
- `RecursoConPermisos` `<<interface>>` — `verificarPermiso(usuario, accion)`

(Las implementarían `Muro`, `Album`, `Foto` según el diagrama.)

## Métodos de clases YA implementadas pero fuera de alcance

Estas clases existen (las usan CU-13/14/15), pero el diagrama les define métodos que **no** se
implementaron por ser de otros casos de uso (login, edición de perfil, baja):

| Clase | Método del diagrama | No implementado porque |
|---|---|---|
| `UsuarioController` | `login(credenciales: LoginDTO): ResponseEntity` | No hay autenticación (demo). Falta también `LoginDTO`. |
| `UsuarioController` | `editarPerfil(id, datos): ResponseEntity` | Fuera de CU-13/14/15. |
| `UsuarioService` | `iniciarSesion(email, contrasena): Usuario` | Sin login en la demo. |
| `UsuarioService` | `editarPerfil(id, datos): Usuario` | Fuera de alcance. |
| `UsuarioService` | `deshabilitarUsuario(id): void` | Es del subsistema Admin. |

## DTOs faltantes

- `LoginDTO` (lo usa `UsuarioController.login`).
