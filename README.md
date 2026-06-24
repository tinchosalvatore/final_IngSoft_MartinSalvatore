# UM-Book — Implementación

Implementación del examen final de Ingeniería de Software (Proceso Unificado de Desarrollo).
UM-Book es un "facebook" de la Universidad de Mendoza. La implementación es 1:1 con el diseño
documentado (diagrama de clases y de secuencia) y cubre los casos de uso:

- **CU-1 Registrarse** (alta de usuario, fullstack).
- **CU-2 Iniciar Sesión** (login por email o usuario, con bloqueo de cuenta tras 3 intentos fallidos).
- **CU-6 Enviar Solicitud de Amistad** (con guardas: ya son amigos / solicitud ya pendiente).
- **CU-7 Buscar Usuarios** (búsqueda por nombre/apellido).
- **CU-13 Listar Usuarios con +2 Amigos en Común** (buscador, fullstack).
- **CU-14 Notificar Solicitud de Amistad** (toast en vivo, sin refrescar).
- **CU-15 Notificar Cumpleaños** (toast en vivo, sin refrescar).
- **CU-18 Gestionar Solicitudes** (listar pendientes, aceptar → notifica al remitente, rechazar → elimina).

Más: Home (solo UI), chat (solo UI), y 2 scripts Java que disparan las notificaciones
desde el back para verlas llegar en vivo a la UI.

---

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Backend | Spring Boot 3.4.3 (Java 21) |
| Frontend | Angular 18 (TypeScript) |
| Base de datos | H2 en memoria (default) · MySQL 8.4 (perfil `mysql`) |
| ORM | JPA / Hibernate |
| Seguridad | Spring Security + BCrypt |
| Tiempo real | SSE (Server-Sent Events) |
| Comunicación | REST API (JSON) |

---

## Estructura del proyecto

```
final_IngSoft_MartinSalvatore/
├── backend/                  <- Spring Boot
│   └── src/main/java/com/um/umbook/
│       ├── config/           <- SecurityConfig (CORS + BCrypt)
│       ├── controller/       <- Usuario, SolicitudAmistad, Notificacion, Dev
│       ├── dto/              <- UsuarioDTO, RegistroDTO, NotificacionDTO, SolicitudAmistadDTO
│       ├── exception/        <- Excepciones + GlobalExceptionHandler
│       ├── model/            <- Usuario, Amistad, SolicitudAmistad, Notificacion (+ enums)
│       ├── repository/       <- Repositorios JPA (+ JPQL amigos en común)
│       ├── service/          <- Usuario, Amistad, SolicitudAmistad, Notificacion, Cumpleanos, JavaMail
│       └── seed/             <- DataSeeder (usuarios + amistades + cumpleaños)
│
├── frontend/                 <- Angular 18
│   └── src/app/
│       ├── home/             <- Home (solo UI), link real al buscador
│       ├── buscador/         <- CU-13: buscar usuarios con +2 amigos en común
│       ├── registro/         <- Alta de usuario
│       ├── solicitudes/      <- CU-14 (click): solicitudes pendientes + aceptar/rechazar
│       ├── chat/             <- Chat (solo UI), destino del click de cumpleaños (CU-15)
│       ├── toast/            <- ToastComponent global (SSE)
│       ├── services/         <- UsuarioService, NotificacionService (EventSource)
│       └── models/           <- Interfaces (Usuario, Notificacion, ...)
│
├── scripts/                  <- TriggerSolicitud.java, TriggerCumple.java (disparan las notis)
├── docs/                     <- IMPL_PLAN.md, diseño/, analisis/, tests/
└── boot.sh                   <- levanta backend + frontend de una sola vez
```

---

## Correr el proyecto

Requisitos: Java 21 y Node 18+. No requiere Docker ni MySQL (la demo usa H2 en memoria).

### Inicio rápido (script)

Para levantar **backend + frontend** de una sola vez:

```bash
./boot.sh          # backend con H2 (la demo arranca sola)
./boot.sh mysql    # backend con perfil MySQL 8.4
```

El script fuerza Java 21, instala las dependencias del frontend si faltan, y con un
solo `Ctrl+C` frena ambos procesos. Si preferís arrancar cada parte a mano, seguí los
pasos de abajo.

### 1. Backend (puerto 8080)

```bash
cd backend
./mvnw spring-boot:run
```

Arranca con H2 en memoria y siembra usuarios + amistades automáticamente.
El usuario observado en la demo es `martin` (id=1).

Para usar MySQL 8.4 en lugar de H2 (requiere el server corriendo):

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

### 2. Frontend (puerto 4200)

```bash
cd frontend
npm install
npx ng serve
```

Abrir `http://localhost:4200`.

### 3. Disparar las notificaciones en vivo

Con backend + frontend corriendo y la UI abierta como `martin`:

```bash
java scripts/TriggerSolicitud.java   # CU-14 -> toast de solicitud de amistad
java scripts/TriggerCumple.java      # CU-15 -> toast de cumpleaños
```

Ver `scripts/README.md` para opciones (ids de remitente/destinatario/cumpleañero).

---

## Endpoints REST

| Método | Endpoint | Caso de uso | Descripción |
|---|---|---|---|
| `GET` | `/usuarios?amigosEnComun=2` | CU-13 | Usuarios con +2 amigos en común con el de referencia |
| `POST` | `/usuarios` | Alta | Registra un usuario nuevo |
| `GET` | `/notificaciones/stream` | CU-14/15 | Canal SSE de notificaciones en vivo |
| `GET` | `/notificaciones/no-leidas` | CU-14/15 | Notificaciones no leídas |
| `GET` | `/solicitudes/pendientes` | CU-14 (click) | Solicitudes de amistad pendientes |
| `POST` | `/solicitudes/{id}/aceptar` | — | Acepta una solicitud (crea la amistad) |
| `POST` | `/solicitudes/{id}/rechazar` | — | Rechaza una solicitud |
| `POST` | `/dev/trigger-solicitud` | Demo CU-14 | Dispara una solicitud (usado por el script) |
| `POST` | `/dev/trigger-cumple` | Demo CU-15 | Setea cumpleaños a hoy y corre el batch |

---

## Flujo de demo

```
1. Ir a http://localhost:4200/        ->  Home (solo UI)
2. Click en "Buscar usuarios"         ->  CU-13: lista usuarios con +2 amigos en común
3. "Crear cuenta nueva"               ->  alta de usuario con cumpleaños
4. java scripts/TriggerSolicitud.java ->  CU-14: aparece toast de solicitud en vivo
5. Click en el toast / la campana     ->  pantalla de solicitudes pendientes (aceptar/rechazar)
6. java scripts/TriggerCumple.java    ->  CU-15: aparece toast de cumpleaños en vivo
7. Click en el toast de cumpleaños    ->  pantalla de chat con el cumpleañero
```

---

## Reglas de negocio implementadas

- Las contraseñas se almacenan **hasheadas con BCrypt** — nunca en texto plano.
- El email y el nombre de usuario deben ser **únicos** (registro rechaza duplicados con 409).
- La contraseña debe tener **mínimo 6 caracteres** (validado en frontend y backend).
- El buscador lista usuarios con **al menos N amigos en común** (N=2 por defecto), excluyendo
  al propio usuario y a sus amigos directos ("personas que quizás conozcas").
- Si la búsqueda no arroja resultados, el backend responde **404** con el mensaje
  "No se encontraron usuarios".
- Las notificaciones se **rutean por usuario** vía SSE: cada cliente recibe sólo las suyas.
- El batch de cumpleaños notifica a **los amigos** del cumpleañero, sólo si la fecha coincide con hoy.

---

## Tests

Tests unitarios de la capa de servicios con **JUnit 5 + Mockito** (no requieren MySQL).

```bash
cd backend
./mvnw test
```

Cubren los casos de prueba documentados en `docs/tests/TEST_PLAN.md`:

| Test | CP documentado |
|---|---|
| Notificación de solicitud de amistad generada | CP 8.2.1 |
| Notificación de cumpleaños en fecha correcta | CP 9.2.1 |
| Sin notificación de cumpleaños fuera de fecha | CP 9.2.2 |
| Listar usuarios con +2 amigos en común | CP 11.1.1 |
| Búsqueda sin resultados (lista vacía → 404) | CP 11.1.2 |
| Registro exitoso / email duplicado / usuario duplicado | — |

---

## Documentación

- Plan de implementación: `docs/IMPL_PLAN.md`
- Plan de pruebas: `docs/tests/TEST_PLAN.md`
- Diagramas de diseño y análisis: `docs/diseño/`, `docs/analisis/`
