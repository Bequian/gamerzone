# 🎮 GamerZone

Aplicación web para reunir y difundir eventos de la comunidad gamer (torneos, LAN parties,
ferias y charlas), desarrollada para la asignatura **Seguridad y Calidad en el Desarrollo de
Software (ISY2202)** de Duoc UC.

**Autor:** Angelo Benvenuto

---

## Tecnologías

- Java 21
- Spring Boot 4 (Spring MVC)
- Spring Security
- Spring Data JPA (Hibernate)
- MySQL 8.4
- JWT (biblioteca JJWT 0.12)
- Thymeleaf + Thymeleaf Extras Spring Security
- Bean Validation (Jakarta Validation)
- JUnit 5 + MockMvc + Spring Security Test + H2 (base de datos en memoria para tests)
- Maven
- Docker y Docker Compose
- Apache HTTP Server (proxy inverso con HTTPS)
- AWS EC2 (Ubuntu 24.04)

---

## Arquitectura

```
                  ┌──────────────────────── EC2 (docker compose) ─────────────────────────┐
Navegador ─HTTPS─►│ proxy (Apache, 443) ──red "frontend"──► app (Spring Boot, 8080)       │
  :443            │   └ certificado SSL                         │                         │
  :80 → HTTPS     │                                    red "backend" (interna)            │
                  │                                             ▼                         │
                  │                                      db (MySQL, 3306) ← conexión TLS  │
                  └───────────────────────────────────────────────────────────────────────┘
```

- Hacia internet solo quedan expuestos los puertos **80** (que redirige a HTTPS) y **443** del proxy.
- La aplicación y MySQL no son accesibles desde fuera de la VM.
- La red `backend` es interna: MySQL no tiene salida a internet.

---

## Funcionalidades del sitio web

| Funcionalidad | Ruta | Acceso |
|---|---|---|
| Inicio con **eventos destacados**, **próximos eventos** y **publicidad de marcas asociadas** | `/` y `/home` | Pública |
| **Registro** de usuarios (nombre, correo y contraseña) | `/registro` | Pública |
| Inicio de sesión | `/login` | Pública |
| **Perfil**: gamertag, avatar, juegos de interés y opciones de notificación | `/perfil` | Privada |
| **Búsqueda de eventos** por categoría, ubicación y rango de fechas | `/eventos` | Pública |
| **Detalle de evento**: descripción, dirección, fecha y hora, organizadores, servicios (estacionamiento, locomoción, comidas, alojamiento) y expositores | `/eventos/{id}` | Privada |
| Panel de administración | `/admin` | Solo rol **ADMIN** |

Además:
- `http://IP/eventos` abre la búsqueda pública de eventos (redirigiendo automáticamente a HTTPS).
- Páginas de error personalizadas **403** (acceso denegado) y **404** (no encontrada).
- Diseño responsivo con tema oscuro en todas las páginas.
- Los usuarios, perfiles y eventos se guardan en **MySQL**, así que se conservan al reiniciar la aplicación.

---

## 🔌 API REST

La API usa autenticación **JWT**: primero se obtiene un token con `POST /api/auth/login`,
y luego se envía en cada petición privada con la cabecera:

```
Authorization: Bearer <token>
```

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `POST` | `/api/auth/login` | Pública | Recibe usuario y contraseña; devuelve un token JWT |
| `GET` | `/api/eventos` | Pública | Lista y busca eventos |
| `GET` | `/api/eventos/{id}` | Privada (cualquier usuario autenticado) | Detalle de un evento |
| `POST` | `/api/eventos` | Rol **ORGANIZADOR** o **ADMIN** | Crea un evento |
| `PUT` | `/api/eventos/{id}` | Rol **ORGANIZADOR** o **ADMIN** | Edita un evento |
| `DELETE` | `/api/eventos/{id}` | Rol **ORGANIZADOR** o **ADMIN** | Elimina un evento |

### Parámetros

**`POST /api/auth/login`**: cuerpo JSON:
```json
{ "username": "organizador", "password": "********" }
```
Respuesta `200`:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "expiraEnSegundos": 3600,
  "username": "organizador",
  "roles": ["ROLE_USER", "ROLE_ORGANIZADOR"]
}
```

**`GET /api/eventos`**: filtros opcionales en la URL:

| Parámetro | Tipo | Ejemplo |
|---|---|---|
| `categoria` | texto | `Torneo` |
| `ubicacion` | texto | `Santiago` |
| `desde` | fecha (`AAAA-MM-DD`) | `2026-11-01` |
| `hasta` | fecha (`AAAA-MM-DD`) | `2026-12-31` |

**`POST /api/eventos`** y **`PUT /api/eventos/{id}`**: cuerpo JSON:
```json
{
  "titulo": "Torneo LoL Primavera",
  "categoria": "Torneo",
  "descripcion": "Torneo 5v5 de League of Legends",
  "fecha": "2026-12-28",
  "hora": "17:00",
  "ubicacion": "Santiago",
  "direccion": "Av. Siempre Viva 742",
  "organizadores": ["GamerZone"],
  "expositores": [],
  "servicios": { "estacionamiento": true, "locomocion": true, "comida": true, "alojamiento": false },
  "destacado": false
}
```

### Códigos de respuesta

| Código | Significado |
|---|---|
| `200` / `201` / `204` | Operación exitosa (consulta, creación, eliminación) |
| `400` | Datos inválidos (incluye el detalle de cada campo con error) |
| `401` | Token ausente, inválido o expirado, o credenciales incorrectas en el login |
| `403` | El usuario no tiene el rol necesario |
| `404` | El evento no existe |

---

## 👤 Usuarios

| Usuario | Roles | Contraseña |
|---|---|---|
| `user` | USER | Variable `GZ_USER_PASSWORD` |
| `admin` | USER, ADMIN | Variable `GZ_ADMIN_PASSWORD` |
| `organizador` | USER, ORGANIZADOR | Variable `GZ_ORGANIZADOR_PASSWORD` |

- Los 3 usuarios base se crean (o se actualiza su contraseña) en la BD cada vez que arranca la aplicación.
- Los usuarios que se registran desde `/registro` inician sesión con su **correo** y reciben siempre el rol `USER`.
- Los mismos usuarios sirven para el sitio web y para la API.

---

## 🔐 Medidas de seguridad implementadas

| Medida | Descripción |
|---|---|
| **HTTPS** | Proxy Apache con certificado SSL; solo se permiten **TLS 1.2 y 1.3**. |
| Redirección a HTTPS | Todo el tráfico por HTTP (puerto 80) se redirige a HTTPS. |
| HSTS | El navegador queda obligado a usar siempre HTTPS con el sitio. La cabecera la envía **solo Spring Security** (`max-age=31536000 ; includeSubDomains`); se quitó del proxy porque ZAP detectó cabeceras duplicadas. |
| **Autenticación JWT** | Tokens firmados con HMAC-SHA256, con emisor y expiración (60 minutos). |
| Validación del token en cada petición | Un filtro verifica firma, emisor y expiración, y consulta el usuario en la BD. |
| API sin estado | La API no usa sesión en el servidor (`STATELESS`). |
| Protección de URLs | Rutas públicas y privadas definidas tanto para el sitio web como para la API. |
| Control de acceso por roles | `/admin/**` exige `ADMIN`; crear, editar y eliminar eventos exige `ORGANIZADOR` o `ADMIN`. |
| Defensa en profundidad | Además de las reglas por URL, los controladores usan `@PreAuthorize`. |
| Contraseñas cifradas | Se almacenan en la BD con **BCrypt**. |
| Política de contraseñas | Entre 8 y 72 caracteres, con al menos una letra y un número (72 es el límite de BCrypt). |
| Secretos fuera del código | Contraseñas, credenciales de la BD y clave JWT se entregan con **variables de entorno** (archivo `.env`, excluido de Git). |
| MySQL no expuesto | La BD está en una red interna de Docker y no publica puertos. |
| Conexión cifrada a la BD | La aplicación se conecta a MySQL con TLS (`sslMode=REQUIRED`). |
| Mínimo privilegio en la BD | La aplicación usa el usuario `gamerzone`, no `root`. |
| Validación en el servidor | Formularios y APIs se validan con Bean Validation (`@Valid`). |
| Lista blanca de valores | El avatar y los juegos del perfil se validan contra un catálogo de valores permitidos. |
| Roles asignados por el servidor | El registro siempre asigna `USER`; el formulario no puede elegir el rol. |
| Protección CSRF | Activa en todos los formularios web. En la API no aplica, porque el token viaja en una cabecera y no en una cookie. |
| Logout seguro | Se hace por `POST`, invalida la sesión y elimina la cookie `JSESSIONID`. |
| Mensajes de error genéricos | Ni el login web ni el de la API revelan si falló el usuario o la contraseña. |
| Prevención de XSS | Uso de `th:text`, que escapa el contenido HTML, y CSP restrictiva. |
| Cabeceras HTTP de seguridad | Content-Security-Policy, X-Frame-Options (`DENY`), Referrer-Policy y X-Content-Type-Options. |
| Cookies de sesión | `HttpOnly`, `Secure` (en el servidor), `SameSite=Strict` y expiración a los 15 minutos. |
| Errores sin detalles internos | No se muestran stack traces; la API responde errores en un JSON controlado. |
| Versión del servidor oculta | Apache no revela su versión (`ServerTokens Prod`). |
| Contenedor sin root | La imagen de la aplicación se ejecuta con un usuario sin privilegios. |

---

## 📁 Estructura del proyecto

```
GamerZone/
├── db/                 → Dockerfile, my.cnf, init.sql, gamerzone_script.sql (creación e inserción de datos)
├── proxy/              → Dockerfile, gamerzone.conf (Apache con HTTPS)
├── Informes ZAP/       → reportes de ZAP antes y después de las correcciones
├── docker-compose.yml  → orquesta db + app + proxy
├── .env.example        → plantilla de variables de entorno (sin secretos)
├── Dockerfile          → imagen de la aplicación Spring Boot
└── src/

src/main/java/com/bequian/gamerzone/
├── api/          → AuthController, EventoApiController, ApiExceptionHandler
│   └── dto/      → LoginRequest, LoginResponse, EventoRequest, EventoResponse, ErrorResponse
├── config/       → DataInitializer (usuarios base y eventos iniciales)
├── controller/   → HomeController, LoginController, RegistroController,
│                   PerfilController, EventoController, AdminController
├── model/        → Usuario, Evento (entidades JPA), ServiciosEvento, Marca, Perfil,
│                   Catalogo, RegistroForm, PerfilForm
├── repository/   → UsuarioRepository, EventoRepository
├── security/     → WebSecurityConfig, JwtService, JwtAuthenticationFilter,
│                   JpaUserDetailsService
└── service/      → EventoService, MarcaService, UsuarioService

src/main/resources/
├── static/css/   → styles.css, login.css
└── templates/    → home, login, registro, perfil, eventos/, admin/, error/, fragments/

src/test/
├── java/com/bequian/gamerzone/
│   ├── GamerzoneApplicationTests.java
│   ├── api/       → ApiSeguridadTest
│   └── security/  → SeguridadTest, AdminSecurityTest
└── resources/config/application.properties  → configuración de tests (H2)
```

---

## ⚙️ Variables de entorno

| Variable | Descripción |
|---|---|
| `MYSQL_ROOT_PASSWORD` | Contraseña del usuario `root` de MySQL |
| `DB_PASSWORD` | Contraseña del usuario `gamerzone` de MySQL (el que usa la aplicación) |
| `DB_HOST` | Host de MySQL (`localhost` en desarrollo, `db` en docker compose) |
| `GZ_USER_PASSWORD` | Contraseña del usuario `user` |
| `GZ_ADMIN_PASSWORD` | Contraseña del usuario `admin` |
| `GZ_ORGANIZADOR_PASSWORD` | Contraseña del usuario `organizador` |
| `GZ_JWT_SECRET` | Clave para firmar los JWT (Base64, 32 bytes: `openssl rand -base64 32`) |

### Conexión a la base de datos

```properties
spring.datasource.url=jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:gamerzone}?sslMode=REQUIRED
spring.datasource.username=${DB_USER:gamerzone}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
```

Las tablas (`usuario`, `usuario_rol`, `usuario_juego`, `evento`, `evento_organizador`,
`evento_expositor`) las crea Hibernate a partir de las entidades JPA.

El archivo `db/gamerzone_script.sql` contiene el script de **creación de tablas e inserción de los
eventos** (generado con `mysqldump`). No incluye usuarios: los 3 usuarios base los crea la aplicación
al arrancar, así no se publican hashes de contraseñas. Para cargarlo en una base vacía:

```bash
docker exec -i gamerzone-db-1 sh -c 'mysql -u gamerzone -p"$MYSQL_PASSWORD" gamerzone' < db/gamerzone_script.sql
```

---

## 💻 Ejecutar en local

### Opción 1: MySQL en Docker + aplicación con Maven

```powershell
# Base de datos
docker build -t gamerzone-db ./db
docker run -d --name gamerzone-db -p 127.0.0.1:3306:3306 `
  -e MYSQL_ROOT_PASSWORD="RootLocal#1" `
  -e MYSQL_DATABASE=gamerzone `
  -e MYSQL_USER=gamerzone `
  -e MYSQL_PASSWORD="GamerZoneLocal#1" `
  -v gamerzone-data:/var/lib/mysql `
  gamerzone-db

# Aplicación
.\mvnw spring-boot:run
```

Abrir `http://localhost:8080`

### Opción 2: todo con docker compose (igual que en el servidor)

1. Copiar `.env.example` como `.env` y completar los valores.
2. Generar un certificado de prueba:
   ```powershell
   docker run --rm -v "${PWD}/certs:/certs" alpine/openssl req -x509 -nodes -newkey rsa:2048 -days 365 `
     -keyout /certs/gamerzone.key -out /certs/gamerzone.crt `
     -subj "/CN=localhost" -addext "subjectAltName=DNS:localhost,IP:127.0.0.1"
   ```
3. Levantar:
   ```powershell
   docker compose up -d
   ```
4. Abrir `https://localhost` (el navegador advierte que el certificado es autofirmado).

### Ejecutar los tests

```powershell
.\mvnw test
```

Los tests usan H2 en memoria, así que **no necesitan Docker**. Verifican, entre otras cosas, que:
- las páginas públicas (`/`, `/registro`, `/eventos`) responden sin sesión y las privadas (`/eventos/{id}`, `/perfil`) redirigen al login,
- un usuario con rol `USER` recibe **403** en `/admin` y un `ADMIN` puede entrar,
- los 3 usuarios pueden iniciar sesión y una contraseña incorrecta es rechazada,
- el registro crea un usuario y rechaza contraseñas débiles y solicitudes sin token CSRF,
- el login de la API devuelve un token JWT válido y rechaza credenciales incorrectas,
- `GET /api/eventos` es público y `GET /api/eventos/{id}` exige token (**401** sin token o con un token inválido),
- un `USER` no puede crear eventos (**403**) y un `ORGANIZADOR` puede crearlos y eliminarlos,
- los datos inválidos devuelven **400** con el detalle de cada campo.

---

## 🐳 Imágenes Docker

| Imagen | Contenido |
|---|---|
| `bequian/gamerzone:2.1` | Aplicación Spring Boot (compilada en dos etapas, se ejecuta sin root) |
| `bequian/gamerzone-db:1.0` | MySQL 8.4 con configuración UTF-8 |
| `bequian/gamerzone-proxy:1.2` | Apache con HTTPS y proxy inverso (sin HSTS duplicado) |

Ninguna imagen contiene contraseñas ni certificados: se entregan al ejecutar.

```powershell
docker compose build
docker compose push
```

---

## ☁️ Despliegue en AWS EC2

1. Crear una instancia **Ubuntu 24.04** (`t3.micro` o `t3.small`) en el AWS Learner Lab.
2. Security Group:
   - **SSH (22)** solo desde mi IP.
   - **HTTP (80)** y **HTTPS (443)** desde cualquier origen.
3. Instalar Docker, docker compose y memoria de intercambio:
   ```bash
   sudo apt update
   sudo apt install -y docker.io docker-compose-v2
   sudo systemctl enable --now docker
   sudo usermod -aG docker ubuntu
   sudo fallocate -l 1G /swapfile && sudo chmod 600 /swapfile && sudo mkswap /swapfile && sudo swapon /swapfile
   echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
   ```
4. Copiar `docker-compose.yml` a `~/gamerzone/` en la VM y crear el archivo `.env`:
   ```bash
   cd ~/gamerzone
   cat > .env <<EOF
   MYSQL_ROOT_PASSWORD='$(openssl rand -base64 24)'
   DB_PASSWORD='$(openssl rand -base64 24)'
   GZ_USER_PASSWORD='...'
   GZ_ADMIN_PASSWORD='...'
   GZ_ORGANIZADOR_PASSWORD='...'
   GZ_JWT_SECRET='$(openssl rand -base64 32)'
   EOF
   chmod 600 .env
   ```
5. Generar el certificado con la IP pública de la VM:
   ```bash
   mkdir -p certs
   IP=$(curl -s https://checkip.amazonaws.com)
   openssl req -x509 -nodes -newkey rsa:2048 -days 365 \
     -keyout certs/gamerzone.key -out certs/gamerzone.crt \
     -subj "/CN=GamerZone" -addext "subjectAltName=IP:$IP"
   chmod 600 certs/gamerzone.key
   ```
6. Levantar:
   ```bash
   docker compose pull
   docker compose up -d --no-build
   ```
7. Acceder a `https://IP_PUBLICA`

> **Notas:**
> - La IP pública cambia cada vez que la instancia se detiene y se vuelve a iniciar. Con una IP nueva hay que regenerar el certificado (paso 5) y ejecutar `docker compose restart proxy`.
> - MySQL solo lee `MYSQL_ROOT_PASSWORD` y `DB_PASSWORD` al crear su volumen. Si se cambian después, hay que recrearlo con `docker compose down -v`, lo que **borra los datos**.

---

## 🛡️ Análisis de seguridad con ZAP (OWASP Top 10)

Se analizó la aplicación desplegada en AWS con **ZAP 2.17** y la política *Default Policy*:

- **Frontend:** spider tradicional, exploración manual con sesión iniciada y escaneo activo.
- **Backend (API):** rutas registradas con `curl` a través del proxy de ZAP y escaneo activo
  autenticado (regla de Reemplazo que agrega `Authorization: Bearer <token>` con el usuario `user`).

| Riesgo | Antes | Después |
|---|---|---|
| Alto | 0 | 0 |
| Medio | 0 | 0 |
| Bajo | 1 | 0 |
| Informativo | 4 | 4 |

**Vulnerabilidad corregida:** *Strict-Transport-Security Múltiples Cabeceras* (OWASP **A05**, configuración
incorrecta). Apache y Spring Security enviaban la cabecera HSTS al mismo tiempo. Se eliminó la directiva de
`proxy/gamerzone.conf` y se dejó solo la de Spring Security (imagen `gamerzone-proxy:1.2`).

**Alertas informativas:** el aviso de *XSS potencial* en `/eventos` y `/registro` es un falso positivo
(Thymeleaf escapa los valores y la CSP bloquea scripts en línea); las demás solo indican que ZAP
identificó el login, la cookie de sesión y probó distintos *User-Agent*.

Los reportes completos están en `Informes ZAP/ZAP-GamerZone_antes.html` y `Informes ZAP/ZAP-GamerZone_despues.html`.

---

## Pendientes / mejoras futuras

- [x] ~~Configurar HTTPS~~
- [x] ~~Conectar una base de datos para usuarios, perfiles y eventos~~
- [x] ~~Gestión de eventos (crear, editar y eliminar) para los roles `ORGANIZADOR` y `ADMIN`~~ (vía API)
- [ ] Certificado emitido por una autoridad reconocida (dominio propio + Let's Encrypt) para eliminar la advertencia del navegador.
- [ ] Pantallas web para que los organizadores gestionen eventos (hoy solo se puede por API).
- [ ] Cifrar también el tráfico interno entre el proxy y la aplicación.
- [ ] Revocación de tokens JWT y *refresh tokens*.
- [ ] Límite de intentos de login (protección contra fuerza bruta).
- [ ] Evitar la enumeración de usuarios en el registro (confirmación por correo).
- [ ] Subida de avatar propio (hoy se elige de una lista predefinida).
- [ ] Quitar los valores por defecto de contraseñas y clave JWT en `application.properties`
      para que la app no arranque sin variables de entorno (*fail-fast*).