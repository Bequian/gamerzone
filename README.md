# GamerZone

Aplicación web para reunir y difundir eventos de la comunidad gamer (torneos, LAN parties,
ferias y charlas), desarrollada para la asignatura **Seguridad y Calidad en el Desarrollo de
Software (ISY2202)** de Duoc UC.

**Autor:** Angelo Benvenuto

---

## Tecnologías

- Java 21
- Spring Boot 4 (Spring MVC)
- Spring Security
- Thymeleaf + Thymeleaf Extras Spring Security
- Bean Validation (Jakarta Validation)
- JUnit 5 + MockMvc + Spring Security Test
- Maven
- Docker
- AWS EC2 (Ubuntu 24.04)

---

## Funcionalidades

| Funcionalidad | Ruta | Acceso |
|---|---|---|
| Inicio con **eventos destacados**, **próximos eventos** y **publicidad de marcas asociadas** | `/` y `/home` | Pública |
| **Registro** de usuarios (nombre, correo y contraseña) | `/registro` | Pública |
| Inicio de sesión | `/login` | Pública |
| **Perfil**: gamertag, avatar, juegos de interés y opciones de notificación | `/perfil` | Privada |
| **Búsqueda de eventos** por categoría, ubicación y rango de fechas | `/eventos` | Privada |
| **Detalle de evento**: descripción, dirección, fecha y hora, organizadores, servicios (estacionamiento, locomoción, comidas, alojamiento) y expositores | `/eventos/{id}` | Privada |
| Panel de administración | `/admin` | Solo rol **ADMIN** |

Además:
- Páginas de error personalizadas **403** (acceso denegado) y **404** (no encontrada).
- Diseño responsivo con tema oscuro en todas las páginas.

> ℹ️ Aún no hay base de datos: los eventos están definidos en el código, y los usuarios
> registrados y los perfiles se guardan **en memoria**, por lo que se pierden al reiniciar la aplicación.

---

## 👤 Usuarios

| Usuario | Roles | Contraseña |
|---|---|---|
| `user` | USER | Variable `GZ_USER_PASSWORD` | 
| `admin` | USER, ADMIN | Variable `GZ_ADMIN_PASSWORD` |
| `organizador` | USER, ORGANIZADOR | Variable `GZ_ORGANIZADOR_PASSWORD` |

Los usuarios que se registran desde `/registro` inician sesión con su **correo** y
reciben siempre el rol `USER`.

---

## 🔐 Medidas de seguridad implementadas

| Medida | Descripción |
|---|---|
| Protección de URLs | Solo inicio, login, registro, error y recursos estáticos son públicos; todo lo demás requiere sesión. |
| Control de acceso por roles | `/admin/**` exige rol `ADMIN` en la configuración de seguridad. |
| Defensa en profundidad | El controlador de admin además está protegido con `@PreAuthorize`. |
| Contraseñas cifradas | Se almacenan con **BCrypt**. |
| Política de contraseñas | Entre 8 y 72 caracteres, con al menos una letra y un número (72 es el límite de BCrypt). |
| Secretos fuera del código | Las contraseñas de los usuarios base se entregan por **variables de entorno**. |
| Validación en el servidor | Los formularios se validan con Bean Validation (`@Valid`), sin depender del navegador. |
| Lista blanca de valores | El avatar y los juegos del perfil se validan contra un catálogo de valores permitidos. |
| Roles asignados por el servidor | El registro siempre asigna `USER`; el formulario no puede elegir el rol. |
| Protección CSRF | Activa en todos los formularios (Thymeleaf agrega el token automáticamente). |
| Logout seguro | Se hace por `POST`, invalida la sesión y elimina la cookie `JSESSIONID`. |
| Mensajes de error genéricos | El login no revela si falló el usuario o la contraseña. |
| Prevención de XSS | Uso de `th:text`, que escapa el contenido HTML, y CSP restrictiva. |
| Cabeceras HTTP de seguridad | Content-Security-Policy, X-Frame-Options (`DENY`) y Referrer-Policy. |
| Cookies de sesión | `HttpOnly`, `SameSite=Strict` y expiración a los 15 minutos. |
| Errores sin detalles internos | No se muestran stack traces ni mensajes internos. |
| Contenedor sin root | La imagen Docker ejecuta la app con un usuario sin privilegios. |

---

## 📁 Estructura del proyecto

```
src/main/java/com/bequian/gamerzone/
├── controller/   → HomeController, LoginController, RegistroController,
│                   PerfilController, EventoController, AdminController
├── model/        → Evento, ServiciosEvento, Marca, Perfil, Catalogo,
│                   RegistroForm, PerfilForm
├── service/      → EventoService, MarcaService, UsuarioService
└── security/     → WebSecurityConfig

src/main/resources/
├── static/css/   → styles.css, login.css
└── templates/
    ├── fragments/ → comunes.html (navbar y footer reutilizables)
    ├── admin/     → panel.html
    ├── eventos/   → lista.html, detalle.html
    ├── error/     → 403.html, 404.html
    ├── home.html
    ├── login.html
    ├── registro.html
    └── perfil.html

src/test/java/com/bequian/gamerzone/
├── GamerzoneApplicationTests.java
└── security/     → SeguridadTest, AdminSecurityTest
```

---

## 💻 Ejecutar en local

1. Definir las contraseñas de los usuarios base. Opciones:
   - Variables de entorno en PowerShell:
     ```powershell
     $env:GZ_USER_PASSWORD="clave_user"
     $env:GZ_ADMIN_PASSWORD="clave_admin"
     $env:GZ_ORGANIZADOR_PASSWORD="clave_organizador"
     ```
   - O un archivo `config/application.properties` en la raíz del proyecto
     (excluido de Git y Docker).

2. Ejecutar:
   ```powershell
   .\mvnw spring-boot:run
   ```

3. Abrir `http://localhost:8080`

### Ejecutar los tests

```powershell
.\mvnw test
```

Los tests verifican, entre otras cosas, que:
- las páginas públicas (`/`, `/registro`) responden sin sesión,
- las páginas privadas (`/eventos`, `/perfil`) redirigen al login,
- un usuario con rol `USER` recibe **403** en `/admin` y un `ADMIN` puede entrar,
- los 3 usuarios pueden iniciar sesión y una contraseña incorrecta es rechazada,
- el registro crea un usuario que luego puede iniciar sesión,
- el registro rechaza contraseñas débiles y solicitudes sin token CSRF,
- un evento inexistente devuelve **404**.

---

## Docker

La imagen se construye en dos etapas: primero compila con Maven y luego deja una imagen
liviana que solo contiene el JRE y el `.jar`.

```powershell
# Construir
docker build -t bequian/gamerzone:1.3 .

# Probar en local
docker run --rm -p 80:8080 `
  -e GZ_USER_PASSWORD="clave_user" `
  -e GZ_ADMIN_PASSWORD="clave_admin" `
  -e GZ_ORGANIZADOR_PASSWORD="clave_organizador" `
  bequian/gamerzone:1.3

# Publicar en Docker Hub
docker push bequian/gamerzone:1.3
```

---

## Despliegue en AWS EC2

1. Crear una instancia **Ubuntu 24.04** (`t3.micro`) en el AWS Learner Lab.
2. Security Group:
   - **SSH (22)** solo desde mi IP.
   - **HTTP (80)** desde cualquier origen.
3. Conectarse por SSH:
   ```powershell
   ssh -i labsuser.pem ubuntu@IP_PUBLICA
   ```
4. Instalar Docker:
   ```bash
   sudo apt update
   sudo apt install -y docker.io
   sudo systemctl enable --now docker
   sudo usermod -aG docker ubuntu
   ```
5. Descargar y ejecutar la imagen:
   ```bash
   docker pull bequian/gamerzone:1.3
   docker run -d --name gamerzone --restart unless-stopped \
     -p 80:8080 \
     -e GZ_USER_PASSWORD='clave_user' \
     -e GZ_ADMIN_PASSWORD='clave_admin' \
     -e GZ_ORGANIZADOR_PASSWORD='clave_organizador' \
     bequian/gamerzone:1.3
   ```
6. Acceder desde el navegador a `http://IP_PUBLICA`

> **Nota:** la IP pública cambia cada vez que la instancia se detiene y se vuelve a iniciar.

---

## Pendientes / mejoras futuras

- [ ] Configurar **HTTPS** (hoy el login viaja por HTTP sin cifrar).
- [ ] Conectar una base de datos para usuarios, perfiles y eventos.
- [ ] Gestión de eventos (crear, editar y eliminar) para los roles `ORGANIZADOR` y `ADMIN`.
- [ ] Subida de avatar propio (hoy se elige de una lista predefinida).
- [ ] Límite de intentos de login (protección contra fuerza bruta).
- [ ] Evitar la enumeración de usuarios en el registro (confirmación por correo).
- [ ] Quitar los valores por defecto de las contraseñas en `application.properties`
      para que la app no arranque sin variables de entorno (*fail-fast*).