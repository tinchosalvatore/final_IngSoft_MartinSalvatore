# Plan de Pruebas
### 1. Introducción
Este documento describe el plan de pruebas al que se someterá el sistema UMBook, una red social orientada al intercambio de fotos y mensajes entre usuarios. El alcance del plan abarca las funcionalidades principales del sistema: gestión de usuarios, amistades, álbumes, fotos, comentarios, grupos, muro y notificaciones.
El proyecto será probado funcionalmente al nivel de Integración/Sistema, verificando la correcta interacción entre los componentes del sistema, y al nivel de Aceptación, validando que el sistema cumple con los requisitos relevados con el usuario.

### 2. Elementos incluidos en las pruebas
La siguiente es una lista de los elementos a ser testeados:
Gestión de Usuarios (registro, login, edición de perfil, búsqueda, deshabilitación)
Gestión de Amistades (envío, aceptación y rechazo de solicitudes, eliminación de amigos)
Gestión de Grupos (creación, asignación de miembros)
Gestión de Álbumes (creación, modificación, eliminación y gestión de permisos)
Gestión de Fotos (subida, modificación, eliminación y comentarios)
Gestión del Muro (comentarios, permisos)
Gestión de Notificaciones (notificaciones en sitio, envío de mails)
Procesamiento Batch (envío diario de mails de cumpleaños)

### 3. Funcionalidades a ser probadas
| Subsistema | Funcionalidad | Objetivo | Id |
| --- | --- | --- | --- |
| Gestión de Usuarios | Registrar usuario | Lograr un registro completo del usuario | 1.1.1 |
|  |  | Comprobar que se requieran los campos obligatorios | 1.1.2 |
|  |  | Comprobar que se impida registrar dos veces el mismo email | 1.1.3 |
|  |  | Comprobar que se impida registrar dos veces el mismo nombre de usuario | 1.1.4 |
|  | Iniciar sesión | Lograr un login exitoso con credenciales válidas | 1.2.1 |
|  |  | Comprobar que se rechace el login con credenciales inválidas | 1.2.2 |
|  |  | Comprobar que un usuario deshabilitado no pueda iniciar sesión | 1.2.3 |
|  | Buscar usuarios | Lograr una búsqueda exitosa por nombre | 1.3.1 |
|  |  | Lograr una búsqueda exitosa por apellido | 1.3.2 |
|  |  | Lograr una búsqueda exitosa por nombre y apellido | 1.3.3 |
|  |  | Comprobar que una búsqueda sin criterios muestre error | 1.3.4 |
|  |  | Comprobar que una búsqueda sin resultados muestre mensaje adecuado | 1.3.5 |
|  |  | Comprobar que un usuario deshabilitado no aparezca en los resultados | 1.3.6 |
| Gestión de Amistades | Enviar solicitud de amistad | Lograr el envío exitoso de una solicitud | 2.1.1 |
|  |  | Comprobar que no se pueda enviar solicitud a un usuario inexistente | 2.1.2 |
|  |  | Comprobar que no se pueda enviar solicitud a alguien con quien ya se es amigo | 2.1.3 |
|  |  | Comprobar que no se pueda enviar solicitud duplicada pendiente | 2.1.4 |
|  |  | Comprobar que no se pueda enviar solicitud a uno mismo |  |
|  | Aceptar/Rechazar solicitud | Lograr la aceptación exitosa de una solicitud | 2.2.1 |
|  |  | Lograr el rechazo exitoso de una solicitud | 2.2.2 |
|  |  | Comprobar que el token del mail sea válido | 2.2.3 |
|  | Eliminar amigo | Lograr la eliminación exitosa de un amigo | 2.3.1 |
|  |  | Comprobar que el amigo eliminado no pueda operar en el sitio | 2.3.2 |
| Gestión de Grupos | Crear grupo | Lograr la creación exitosa de un grupo | 3.1.1 |
|  |  | Comprobar que se requiera un nombre o integrante para el grupo | 3.1.2 |
|  | Modificar grupo | Lograr la asignación exitosa de un amigo a un grupo con un rol | 3.2.1 |
|  |  | Modificar los roles de un integrante en el grupo sin permisos | 3.2.2 |
|  |  | Modificar otras características del grupo | 3.2.3 |
|  | Eliminar grupo | Verificar que solo un administrador puede eliminar el grupo | 3.3.1 |
|  | Salir del grupo | Verificar que puedo salir del grupo correctamente sin importar mi rol | 3.4.1 |
|  | Eliminar integrante | Verificar que solo un administrador puede eliminar un integrante | 3.5.1 |
| Gestión de Álbumes | Crear álbum | Lograr la creación exitosa de un álbum | 4.1.1 |
|  |  | Comprobar que se requiera un nombre para el álbum | 4.1.2 |
|  | Modificar álbum | Lograr la modificación exitosa de nombre y descripción de un álbum | 4.2.1 |
|  |  | Lograr la modificación exitosa de permisos de visualización y comentarios | 4.2.2 |
|  |  | Comprobar que se requiera un nombre para modificar el álbum | 4.2.3 |
|  | Gestionar permisos | Comprobar que un grupo sin permiso no pueda ver el álbum | 4.3.1 |
|  |  | Comprobar que un grupo con permiso de ver pueda acceder al álbum | 4.3.2 |
|  |  | Comprobar que un grupo con permiso de comentar pueda agregar comentarios | 4.3.3 |
|  | Eliminar álbum | Lograr la eliminación exitosa de un álbum y las fotos que contenga | 4.4.1 |
|  |  | Comprobar que la eliminación pueda ser cancelada por el usuario | 4.4.2 |
| Gestión de Fotos | Subir foto | Lograr la subida exitosa de una foto | 5.1.1 |
|  |  | Comprobar que solo se acepten formatos de imagen válidos | 5.1.2 |
|  |  | Lograr la subida exitosa de múltiples fotos | 5.1.3 |
|  |  | Comprobar que se requiera seleccionar un álbum de destino | 5.1.4 |
|  |  | Comprobar que se rechacen archivos que superen el tamaño máximo permitido (10MB) | 5.1.5 |
|  |  | Comprobar que archivos inválidos sean descartados sin afectar la carga de archivos válidos | 5.1.6 |
|  | Modificar foto | Lograr la modificación exitosa de la descripción de una foto | 5.2.1 |
|  |  | Lograr el cambio exitoso de una foto a otro álbum | 5.2.2 |
|  | Comentar foto | Lograr la creación exitosa de un comentario en una foto | 5.3.1 |
|  |  | Comprobar que un usuario sin permiso no pueda comentar | 5.3.2 |
|  | Eliminar foto | Lograr la eliminación exitosa de una foto | 5.4.1 |
|  |  | Comprobar que la eliminación pueda ser cancelada por el usuario | 5.4.2 |
| Gestión del Muro | Comentar en muro | Lograr la creación exitosa de un comentario en el muro | 6.1.1 |
|  |  | Comprobar que un usuario sin permiso no pueda comentar en el muro | 6.1.2 |
| Administración | Deshabilitar usuario | Lograr la deshabilitación exitosa de un usuario | 7.1.1 |
|  |  | Comprobar que un usuario deshabilitado no pueda iniciar sesión | 7.1.2 |
|  |  | Comprobar que un usuario deshabilitado no aparezca en búsquedas | 7.1.3 |
|  |  | Comprobar que un usuario no Admin no pueda deshabilitar usuarios | 7.1.4 |
|  | Eliminar comentario | Lograr la eliminación exitosa de un comentario por el Admin | 7.2.1 |
|  |  | Comprobar que quede el mensaje de eliminación visible | 7.2.2 |
| Notificaciones | Notificación en sitio | Comprobar que se genere notificación al recibir comentario en foto | 8.1.1 |
|  |  |  |  |
|  | Notificación solicitud de amistad | Comprobar que se genere la notificación al enviarse la solicitud | 8.2.1 |
|  |  |  |  |
| Cumpleaños | Mail de cumpleaños | Comprobar que se envíe mail a usuarios con amigos con cumpleaños próximos | 9.1.1 |
|  |  | Comprobar que no se envíe mail si no hay cumpleaños próximos | 9.1.2 |
|  | Notificación de cumpleaños | Comprobar que los usuarios amigos reciben la notificación de cumpleaños | 9.2.1 |
|  |  | Comprobar que la notificación se envía en la fecha de cumpleaños correcta | 9.2.2 |
| Mensajería | Enviar mensajes | Verificar que puedo enviar mensaje a un amigo | 10.1.1 |
|  |  | Verificar que puedo enviar mensajes a un grupo | 10.1.2 |
| Buscador | Listar Usuarios Con +2 Amigos En Común | Verificar que los Usuarios listados tienen más de dos amigos en común con el Usuario | 11.1.1 |
|  |  |  | 11.1.2 |


### 4. Estrategias de Prueba
**Niveles de Prueba**
Las pruebas para este proyecto se realizarán a nivel de Integración/Sistema y de Aceptación.
Las pruebas de Integración/Sistema serán realizadas por el equipo de desarrollo, verificando la correcta interacción entre los componentes del sistema (Controllers, Services, Repositories) y con los sistemas externos (base de datos MySQL, servidor de mail JavaMail, sistema de archivos).
Las pruebas de Aceptación serán realizadas simulando el rol del usuario final, validando que el sistema cumple con los requisitos funcionales relevados en el enunciado del proyecto UMBook.
**Herramientas de Prueba JUnit** —> framework principal para la escritura y ejecución de pruebas unitarias e integración en Java.
**Mockito** —> para simular dependencias (mocks) en pruebas unitarias de Services, evitando acceso real a la base de datos.
**Spring Boot Test** —> para pruebas de integración que levantan el contexto completo de Spring.
**Postman** —> para pruebas manuales de los endpoints REST de los Controllers.

### 5. Criterios de Éxito
El proceso de prueba se completará una vez aplicados todos los casos de prueba definidos y resueltos todos los errores importantes (blocker, critical y major) descubiertos durante el proceso. Se considerará exitoso cuando el 100% de los casos de prueba definidos hayan sido ejecutados y el 95% hayan pasado sin errores críticos.

### 6. Entregables
La siguiente es una lista de los entregables generados durante el proceso de pruebas:
Reportes de defectos e incidencias
Resultados de ejecución de casos de prueba (pass/fail por cada Id)
Informe final de pruebas con resumen de cobertura

### 7. Ambiente de prueba necesario
Los siguientes elementos son necesarios para poder llevar adelante el proceso de prueba:
JDK 25 con JUnit instalado
MySQL 8 para base de datos de prueba (entorno separado del productivo)
Servidor de mail de prueba (MailHog o similar) para verificar envío de mails sin afectar usuarios reales
Navegador con Angular compilado para pruebas de aceptación
Sistema operativo: Linux Debian o Windows 11

