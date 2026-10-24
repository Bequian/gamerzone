# 🎮 GamerZone

Aplicación web de eventos y noticias del mundo gamer, desarrollada para la asignatura
**Seguridad y Calidad en Desarrollo de Software** (Duoc UC).

**Autor:** Angelo Benvenuto

---

## 🛠️ Tecnologías

- Java 21
- Spring Boot 4
- Spring Security
- Thymeleaf + Thymeleaf Extras Spring Security
- Maven
- Docker
- AWS EC2 (Ubuntu 24.04)

---

## ✨ Funcionalidades

- Página de inicio con listado de eventos y noticias.
- Inicio y cierre de sesión con formulario propio.
- Panel de administración (`/admin`) visible solo para usuarios con rol **ADMIN**.
- Página personalizada de **acceso denegado (403)**.
- Diseño responsivo con tema oscuro.

---

## 🔐 Medidas de seguridad implementadas

| Medida | Descripción |
|---|---|
| Contraseñas cifradas | Se almacenan con **BCrypt**. |
| Secretos fuera del código | Las contraseñas se entregan por **variables de entorno**. Si faltan, la app no arranca (*fail-fast*). |
| Control de acceso por roles | `/admin/**` exige rol `ADMIN` en la configuración de seguridad. |
| Defensa en profundidad | Además, el controlador de admin está protegido con `@PreAuthorize`. |
| Protección CSRF | Activa en todos los formularios (Thymeleaf agrega el token automáticamente). |
| Logout seguro | Se hace por `POST`, invalida la sesión y elimina la cookie `JSESSIONID`. |
| Mensajes de error genéricos | El login no revela si falló el usuario o la contraseña. |
| Prevención de XSS | Uso de `th:text`, que escapa el contenido HTML. |
| Cabeceras HTTP de seguridad | Content-Security-Policy, X-Frame-Options (`DENY`) y Referrer-Policy. |
| Cookies de sesión | `HttpOnly`, `SameSite=Strict` y expiración a los 15 minutos. |
| Errores sin detalles internos | No se muestran stack traces ni mensajes internos. |
| Contenedor sin root | La imagen Docker ejecuta la app con un usuario sin privilegios. |

---

## 📁 Estructura del proyecto

```
src/main/java/com/bequian/gamerzone/
├── controller/      → HomeController, LoginController, AdminController
├── model/           → Evento (record)
├── service/         → EventoService
└── security/        → WebSecurityConfig

src/main/resources/
├── static/css/      → styles.css, login.css
└── templates/
    ├── fragments/   → comunes.html (navbar y footer reutilizables)
    ├── admin/       → panel.html
    ├── error/       → 403.html
    ├── home.html
    └── login.html
```