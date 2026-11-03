# Imagen de la base de datos de GamerZone
FROM mysql:8.4

# Configuración del servidor: UTF-8 completo (tildes, ñ y emojis)
COPY my.cnf /etc/mysql/conf.d/gamerzone.cnf

# Scripts que MySQL ejecuta automáticamente la PRIMERA vez que se crea el volumen de datos
COPY init.sql /docker-entrypoint-initdb.d/01-init.sql

# Las contraseñas NO van aquí: se entregan al ejecutar el contenedor con variables de entorno
# (MYSQL_ROOT_PASSWORD, MYSQL_DATABASE, MYSQL_USER y MYSQL_PASSWORD)
EXPOSE 3306