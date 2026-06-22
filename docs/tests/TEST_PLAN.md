CU-8.2: Notificación Solicitud de Amistad - Martin Salvatore

Casos de Prueba

| Id | Objetivo | Estado Inicial | Datos de Prueba | Resultado Esperado |
|---|---|---|---|---|
| CP 8.2.1 | Comprobar que se genera la notificación al recibirse una solicitud de amistad | Los usuarios "jsanchez" y "jperez" existen en el sistema y no son amigos. "jperez" está logueado | Solicitud enviada por: jsanchez. Receptor: jperez | "jperez" recibe un toast de notificación indicando que "jsanchez" le envió una solicitud de amistad |


CU-9.2: Notificación de Cumpleaños - Martin Salvatore

Casos de Prueba

| Id | Objetivo | Estado Inicial | Datos de Prueba | Resultado Esperado |
|---|---|---|---|---|
| CP 9.2.1 | Comprobar que los usuarios amigos reciben la notificación en la fecha de cumpleaños | El usuario "jgomez" tiene fecha de nacimiento igual a la fecha actual del sistema y tiene al menos un amigo ("jperez") | Fecha del sistema: fecha de nacimiento de jgomez. Usuario amigo logueado: jperez | "jperez" recibe un toast de notificación de cumpleaños de "jgomez" |
| CP 9.2.2 | Comprobar que la notificación se envía únicamente en la fecha correcta de cumpleaños | El usuario "jgomez" tiene fecha de nacimiento distinta a la fecha actual del sistema | Fecha del sistema: fecha distinta a la de nacimiento de jgomez | "jperez" NO recibe ninguna notificación de cumpleaños de "jgomez" |


CU-11: Listar Usuarios Con +2 Amigos En Común - Martin Salvatore

| Id | Objetivo | Estado Inicial | Datos de Prueba | Resultado Esperado |
|---|---|---|---|---|
| CP 11.1.1 | Verificar que los usuarios listados tienen más de 2 amigos en común con el usuario logueado | Existen en el sistema usuarios que comparten más de 2 amigos con el usuario "jperez" | Usuario logueado: jperez | El sistema muestra la lista de usuarios que tienen más de 2 amigos en común con jperez |
| CP 11.1.2 | Verificar que cuando no existen usuarios con +2 amigos en común se muestra un mensaje adecuado | No existe ningún usuario en el sistema que comparta más de 2 amigos con el usuario logueado | Usuario logueado: jperez | El sistema muestra el mensaje "No se encontraron usuarios" |



--Procedimiento de Prueba - Martin Salvatore
CP 8.2.1 — Notificación de solicitud de amistad
Iniciar sesión con el usuario "jperez" (receptor).
En una sesión separada, iniciar sesión con el usuario "jsanchez" (remitente).
Desde la sesión de "jsanchez", buscar al usuario "jperez" y enviar una solicitud de amistad.
Verificar que el sistema dispara triggerNotificacion(solicitudId) hacia "jperez".
Comprobar que "jperez" recibe el toast de notificación con los datos de "jsanchez".
Hacer click en la notificación desde la sesión de "jperez".
Verificar que el sistema realiza GET /solicitudes/pendientes.
Comprobar que el sistema redirige a la pantalla de solicitudes pendientes y que la solicitud de "jsanchez" aparece en la lista.


Procedimiento de Prueba - Martin Salvatore
CP 9.2.1 — Notificación enviada en fecha correcta
Configurar el usuario "jgomez" con fecha de nacimiento igual a la fecha actual del sistema.
Verificar que "jgomez" tiene al menos un amigo en el sistema ("jperez").
Iniciar sesión con el usuario "jperez".
Disparar el proceso batch verificarCumpleañosDiarios().
Verificar que el sistema ejecuta findByFechaNacimiento(hoy) y recupera a "jgomez".
Comprobar que se invoca crearNotificacionCumpleaños(usuarios) con "jgomez" en la lista.
Verificar que "jperez" recibe el toast de cumpleaños en pantalla.
Hacer click en la notificación y comprobar que el sistema redirige a la pantalla de chat con "jgomez".
CP 9.2.2 — Notificación no enviada fuera de fecha
Configurar el usuario "jgomez" con una fecha de nacimiento distinta a la fecha actual.
Iniciar sesión con el usuario amigo "jperez".
Disparar el proceso batch verificarCumpleañosDiarios().
Verificar que findByFechaNacimiento(hoy) no retorna a "jgomez".
Comprobar que NO aparece ningún toast de cumpleaños para "jperez".


Procedimiento de Prueba - Martin Salvatore
CP 11.1.1 — Listar usuarios exitosamente
Iniciar sesión con el usuario "jperez".
Verificar en la base de datos que existen usuarios que comparten más de 2 amigos con "jperez".
Dirigirse a la sección de buscador de usuarios con amigos en común.
Confirmar que el sistema realiza la petición GET /usuarios?amigosEnComun=2.
Verificar que el sistema muestra la lista de usuarios recuperada.
Comprobar que todos los usuarios de la lista tienen efectivamente más de 2 amigos en común con "jperez".
CP 11.1.2 — Sin resultados
Iniciar sesión con un usuario que no tenga otros usuarios con más de 2 amigos en común.
Dirigirse a la sección de buscador de usuarios con amigos en común.
Confirmar que el sistema realiza la petición GET /usuarios?amigosEnComun=2.
Verificar que el sistema lanza UsuarioNotFoundException y retorna 404.
Comprobar que se muestra el mensaje de error "No se encontraron usuarios".
