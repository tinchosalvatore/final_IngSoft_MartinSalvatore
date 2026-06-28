# **Mecanismos Genéricos de Diseño — UMBook**
## **1. Persistencia**
**Problemática:** Las entidades del sistema necesitan ser almacenadas, recuperadas y modificadas de forma persistente entre sesiones.
**Solución:** Se utiliza **Hibernate** como ORM (Object-Relational Mapping) junto con **JPA** (Java Persistence API). Cada entidad del dominio se anota con @Entity y se mapea a una tabla en **MySQL**. Para el acceso a datos, cada entidad tiene una interfaz Repository que extiende JpaRepository<Entidad, Long>, y Spring genera la implementación automáticamente en tiempo de ejecución.
**Ejemplo:** UsuarioRepository extends JpaRepository<Usuario, Long> permite guardar, buscar y eliminar usuarios sin escribir SQL manualmente.

## **2. Seguridad y Autenticación**
**Problemática:** El sistema debe garantizar que solo usuarios autenticados puedan operar, y que cada usuario solo acceda a los recursos para los que tiene permiso.
**Solución:** Se utiliza **Spring Security** para la autenticación y autorización. El acceso a los endpoints está protegido mediante tokens de sesión. Adicionalmente, la lógica de permisos sobre recursos (álbumes, muros) se gestiona a través de la entidad Permiso y la interfaz RecursoConPermisos, cuyo método verificarPermiso(usuario, accion) es invocado por los Services antes de operar sobre un recurso.
**Ejemplo:** Antes de agregar un comentario a una foto, AlbumService llama a PermisoService.verificarAcceso(album, usuario) para confirmar que el grupo del usuario tiene permiso de comentar.

## **3. Distribución Transparente**
**Problemática:** El sistema está distribuido en múltiples nodos (Browser, Servidor Web, Servidor de Aplicación, Base de Datos, Servidor de Mail, Sistema de Archivos) y las capas deben comunicarse de forma transparente.
**Solución:** La comunicación entre el frontend (**Angular**) y el backend (**Spring MVC**) se realiza mediante una **API REST** con HTTP/HTTPS. Spring MVC gestiona el ruteo de requests y la serialización/deserialización de objetos Java a JSON automáticamente. La comunicación entre el Servidor de Aplicación y la Base de Datos se realiza mediante **JDBC/Hibernate** sobre **TCP/IP**.
**Ejemplo:** Cuando Angular llama a GET /usuarios/buscar?nombre=X, Spring MVC rutea el request a UsuarioController y serializa el List<Usuario> de respuesta a JSON automáticamente.

## **4. Manejo de Errores**
**Problemática:** El sistema debe manejar situaciones excepcionales de forma controlada, informando al usuario de manera clara sin exponer detalles internos del sistema.
**Solución:** Se utilizan **excepciones personalizadas** para cada situación de error (ej: UsuarioNotFoundException, SolicitudPendienteException). Spring provee @ControllerAdvice como mecanismo centralizado para capturar estas excepciones y transformarlas en respuestas HTTP con el código de estado correspondiente (404 Not Found, 400 Bad Request, etc.).
**Ejemplo:** Si al enviar una solicitud de amistad el destinatario no existe, SolicitudAmistadService lanza UsuarioNotFoundException, que es capturada por el @ControllerAdvice y devuelta al cliente como ResponseEntity(404, "Usuario no encontrado").

## **5. Transacciones**
**Problemática:** Ciertas operaciones involucran múltiples cambios en la base de datos que deben ejecutarse de forma atómica — si una falla, todas deben revertirse.
**Solución:** Se utiliza la anotación @Transactional de Spring en los métodos de Service que involucran múltiples operaciones de persistencia. Si ocurre una excepción durante la transacción, Spring revierte automáticamente todos los cambios realizados hasta ese punto.
**Ejemplo:** Al aceptar una solicitud de amistad, SolicitudAmistadService.aceptarSolicitud() debe actualizar el estado de la SolicitudAmistad a ACEPTADA y crear una nueva Amistad. Ambas operaciones van dentro de una misma transacción — si la creación de la Amistad falla, el estado de la solicitud no se actualiza.

## **6. Almacenamiento de Archivos**
**Problemática:** Las fotos subidas por los usuarios no pueden almacenarse en la base de datos relacional — necesitan un sistema de almacenamiento de archivos dedicado.
**Solución:** Se utiliza un **Sistema de Archivos** dedicado gestionado por ArchivoService. Cuando un usuario sube una foto, FotoService delega en ArchivoService.guardarArchivo(archivo), que persiste el archivo y devuelve una URL. Esa URL es la que se almacena en el atributo url de la entidad Foto en la base de datos.
**Ejemplo:** El usuario sube una foto desde Angular → FotoController recibe el MultipartFile → FotoService llama a ArchivoService.guardarArchivo() → se almacena el archivo y se guarda la URL en la entidad Foto.

## **7. Procesamiento Batch**
**Problemática:** Ciertas tareas del sistema deben ejecutarse automáticamente en intervalos regulares, sin intervención del usuario.
**Solución:** Se utiliza @Scheduled de Spring para definir tareas programadas. La clase CumpleanosScheduler se ejecuta una vez por día automáticamente, consulta los usuarios con cumpleaños próximos a través de UsuarioRepository y delega el envío de notificaciones a NotificacionService.
**Ejemplo:** Una vez al día, CumpleanosScheduler.ejecutarBatchDiario() obtiene los usuarios con cumpleaños en los próximos N días y llama a NotificacionService.enviarEmailCumpleanos() por cada uno.

## **8. Logging**
**Problemática:** El sistema necesita registrar eventos relevantes para poder diagnosticar errores, auditar acciones administrativas y monitorear el comportamiento en producción.
**Solución:** Se utiliza **SLF4J** con **Logback** (incluido por defecto en Spring Boot) como framework de logging. Cada clase Service y Controller tiene un logger propio que registra eventos en distintos niveles:
- INFO — operaciones exitosas relevantes (usuario registrado, solicitud enviada)
- WARN — situaciones anómalas no críticas (intento de enviar solicitud duplicada)
- ERROR — excepciones y fallos del sistema (error al enviar mail, fallo de persistencia)
Los logs se almacenan en archivos rotativos para no consumir disco indefinidamente.
**Ejemplo:** Cuando el Admin deshabilita un usuario, AdminService registra:
INFO: Usuario id=X deshabilitado por Admin id=Y en fecha Z
Cuando falla el envío de un mail de cumpleaños, JavaMailService registra:
ERROR: Fallo al enviar email de cumpleaños a usuario id=X - [detalle del error]
