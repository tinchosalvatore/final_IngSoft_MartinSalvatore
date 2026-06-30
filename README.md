# UM-Book — Implementación

Examen final de Ingeniería de Software (Proceso Unificado de Desarrollo). UM-Book es un
"facebook" de la Universidad de Mendoza, **fullstack** (Spring Boot + Angular).

> **Importante para la corrección:** solo **4 casos de uso** están implementados **1:1 estricto**
> con el diseño (clases, métodos, parámetros y retornos — front y back). El resto del sistema es
> **andamiaje de demo** para que la app no se sienta vacía y **no fue codificada 1:1** por cuestiones de tiempo. El
> detalle está en la sección [Alcance](#alcance--qué-es-11-y-qué-no) de acá abajo.

---

## Alcance — qué es 1:1 y qué no

| Categoría | Qué incluye | Cómo evaluarlo |
|---|---|---|
| ✅ **1:1 estricto** | **CU-7** Buscar Usuarios · **CU-13** Listar Usuarios con +2 Amigos en Común · **CU-14** Notificar Solicitud de Amistad · **CU-15** Notificar Cumpleaños | Implementados **fullstack 1:1** (front + back) con el Diseño. |
| 🔧 **Soporte funcional** | **CU-6** Enviar Solicitud · **CU-18** Gestionar Solicitudes (aceptar/rechazar) | Funcionan y son coherentes con el diseño, pero **no cumplen al 100% con el 1:1**. CU-14 los necesita: enviar dispara la notificación y el click del toast aterriza en la lista de pendientes. |
| 🎨 **Decoración (NO 1:1)** | **Home** (feed y contactos mockeados) · **Perfil** (placeholder) · **Chat** (demo solo-front, respuesta precargada) | UI sin lógica de negocio real, solo para dar vida a la demo. |


**Estado de Demo**

- **Usuario**:  Usuario demo **fijo: `martin` (id=1)**
- **Seed**: La data es  sembrada por `DataSeeder` y resuelta internamente en el backend.

---

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Backend | Spring Boot 3.4.3 (Java 21) |
| Frontend | Angular 18 (TypeScript) |
| Base de datos | H2 en memoria (default) · MySQL 8.4 (perfil `mysql`) |
| ORM | JPA / Hibernate |
| Seguridad | Spring Security **solo para CORS** (sin login). BCrypt se usa únicamente al sembrar datos |
| Tiempo real | SSE (Server-Sent Events) |
| Comunicación | REST API (JSON) |

---

## Estructura del proyecto

```
final_IngSoft_MartinSalvatore/
├── backend/                  <- Spring Boot
│   └── src/main/java/com/um/umbook/
│       ├── config/           <- SecurityConfig (CORS + bean BCrypt; sin login)
│       ├── controller/       <- Usuario, SolicitudAmistad, Notificacion, Cumpleanos
│       ├── dto/              <- UsuarioDTO, NotificacionDTO, SolicitudAmistadDTO
│       ├── exception/        <- Excepciones de dominio + GlobalExceptionHandler
│       ├── model/            <- Usuario, Amistad, SolicitudAmistad, Notificacion (+ enums)
│       ├── repository/       <- Repositorios JPA (+ JPQL de amigos en común)
│       ├── service/          <- Usuario, Amistad, SolicitudAmistad, Notificacion, Cumpleanos, JavaMail
│       └── seed/             <- DataSeeder (9 usuarios + amistades; usuario demo = martin id=1)
│
├── frontend/                 <- Angular 18 (entra directo como martin, sin login)
│   └── src/app/
│       ├── buscador/         <- CU-7 (buscar) + CU-13 (sugerencias +2 amigos)
│       ├── solicitudes/      <- CU-14 (click): pendientes + aceptar/rechazar
│       ├── toast/            <- ToastComponent global (SSE) — CU-14/15
│       ├── home/             <- decoración (feed/contactos mock) + tarjeta de cumpleaños (CU-15)
│       ├── perfil/           <- decoración (placeholder) + re-muestra sugerencias (CU-13)
│       ├── chat/             <- decoración (chat demo solo-front) — destino del click de CU-15
│       ├── services/         <- UsuarioService, NotificacionService (EventSource/SSE)
│       └── models/           <- Interfaces (Usuario, Notificacion, SolicitudAmistad)
│
├── scripts/                  <- TriggerSolicitud.java, TriggerCumple.java (disparan las notis)
├── docs/                     <- diseño/ (diagramas + CAMBIOS), pruebas/, analisis/, requerimientos/
└── boot.sh                   <- levanta backend + frontend de una sola vez
```

---

## Correr el proyecto

Requisitos: **Java 21** y **Node 18+**. No requiere Docker ni MySQL (la demo usa H2 en memoria).

### Inicio rápido (script)

```bash
./boot.sh          # backend con H2 (la demo arranca sola) + frontend
./boot.sh mysql    # backend con perfil MySQL 8.4
```

El script fuerza Java 21, instala las dependencias del frontend si faltan, y con un solo
`Ctrl+C` frena ambos procesos. Para arrancar a mano:

### 1. Backend (puerto 8080)

```bash
cd backend
./mvnw spring-boot:run                                     # H2
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql    # MySQL 8.4
```

Arranca con H2 en memoria y siembra usuarios + amistades. El **usuario observado de la demo es
`martin` (id=1)**; la UI entra directo como él (no hay login).

### 2. Frontend (puerto 4200)

```bash
cd frontend
npm install
npx ng serve
```

Abrir `http://localhost:4200` (entra directo como `martin`).

### 3. Disparar las notificaciones en vivo

Con backend + frontend corriendo y la UI abierta (como `martin`):

```bash
java scripts/TriggerSolicitud.java   # CU-14 — toast de solicitud (fede=7 -> martin=1)
java scripts/TriggerCumple.java      # CU-15 — toast de cumpleaños (Ana cumple hoy -> notifica a martin)
```

> Los scripts apuntan por defecto al usuario demo (para mas info del funcionamiento interno de los scripts ver `scripts/README.md`).

---

## Endpoints REST

| Método | Endpoint | Caso de uso | Descripción |
|---|---|---|---|
| `GET` | `/usuarios/buscar?nombre=&apellido=` | **CU-7** | Busca usuarios por nombre o apellido |
| `GET` | `/usuarios?amigosEnComun=2` | **CU-13** | Usuarios con +N amigos en común (vacío → 404) |
| `POST` | `/usuarios/sugerencia-extra` | CU-13 (demo) | Agrega un lote de sugerencias ("recargar") |
| `GET` | `/usuarios/cumpleanos` | CU-15 (tarjeta) | Usuarios que cumplen años hoy |
| `PUT` | `/usuarios/{id}/cumpleanos` | demo | Setea el cumpleaños de un usuario a hoy |
| `POST` | `/solicitudes?remitenteId=&destinatarioId=` | **CU-6 / CU-14** | Envía la solicitud y notifica al destinatario |
| `GET` | `/solicitudes/pendientes` | **CU-14** (click) / CU-18 | Solicitudes pendientes del usuario actual |
| `POST` | `/solicitudes/aceptar?token=` | CU-18 | Acepta (crea la amistad, notifica al remitente) |
| `POST` | `/solicitudes/rechazar?token=` | CU-18 | Rechaza (elimina la solicitud) |
| `POST` | `/cumpleanos/ejecutar-batch` | **CU-15** | Corre el batch de cumpleaños del día |
| `GET` | `/notificaciones/stream` | **CU-14/15** | Canal SSE de notificaciones en vivo |
| `GET` | `/notificaciones/no-leidas` | CU-14/15 | Notificaciones no leídas |
| `PUT` | `/notificaciones/{id}/leida` | — | Marca una notificación como leída |

---

## Flujo de demo

```
1. http://localhost:4200/             ->  Home (decoración) como martin
2. "Buscar usuarios"                  ->  CU-7: buscar por nombre/apellido
                                          CU-13: "personas que quizás conozcas" (+2 amigos)
3. java scripts/TriggerSolicitud.java ->  CU-14: toast de solicitud en vivo (SSE)
4. Click en el toast / la campana     ->  pantalla de solicitudes (aceptar/rechazar)
5. java scripts/TriggerCumple.java    ->  CU-15: toast de cumpleaños en vivo (SSE)
6. Click en el toast de cumpleaños    ->  chat con el cumpleañero (demo solo-front)
```

---

## Reglas de negocio implementadas (4 CU)

- **Usuario actual fijo:** `martin` (id=1), sin login ni sesión por estar fuera de alcance de CU.
- **CU-7:** la búsqueda matchea por **nombre o apellido** (coincidencia parcial, insensible a
  mayúsculas). Sin resultados → lista vacía.
- **CU-13:** lista usuarios con **al menos N amigos en común** (N=2 por defecto), excluyendo al
  propio usuario y a sus amigos directos ("personas que quizás conozcas"). Si no hay → **404**
  "No se encontraron usuarios".
- **CU-14:** enviar una solicitud la **persiste y notifica** al destinatario por llamada directa
  (toast SSE + email stub). Guardas: si ya son amigos o si ya hay una pendiente → **409**.
- **CU-15:** el batch notifica a **los amigos del cumpleañero**, solo si la fecha coincide con hoy.
- **Notificaciones ruteadas por usuario** vía SSE: cada cliente recibe solo las suyas.

---

## Tests

Tests unitarios de la capa de servicios con **JUnit 5 + Mockito** (no requieren base de datos):

```bash
cd backend
./mvnw test
```

| Test | CP | CU |
|---|---|---|
| `UsuarioServiceTest` — buscar (coincidencias / sin resultados) | CP 1.3.1–1.3.3 / 1.3.5 | **CU-7** |
| `UsuarioServiceTest` — listar +2 amigos (≥2 / vacío → 404) | CP 11.1.1 / 11.1.2 | **CU-13** |
| `SolicitudAmistadServiceTest` — notifica al enviar (+ aceptar / rechazar) | CP 8.2.1 | **CU-14** |
| `CumpleanosServiceTest` — notifica hoy / no fuera de fecha | CP 9.2.1 / 9.2.2 | **CU-15** |

Estado: **10 tests, 0 fallas.** Los casos de prueba y procedimientos están en `docs/pruebas/`
(`CP-*.md`, `PP-*.md`, `Plan de Prueba.md`).

---

## Documentación de diseño

Todo en `docs/`:

- **Diagramas de secuencia** (front + back, por CU): `docs/diseño/diag_sec/diag_sec-CU-{7,13,14,15}`.
- **Diagramas de clases** (separados): `docs/diseño/diag_clases/diagrama_clases_back` y `…_front`.
- **Diagramas de estado** (por CU): `docs/diseño/diag_estados/diag_estados-CU-{7,13,14,15}`.
- **Pruebas:** `docs/pruebas/` · **Análisis y requerimientos:** `docs/analisis/`, `docs/requerimientos/`.
