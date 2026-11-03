-- Se ejecuta solo la primera vez que se crea el volumen de datos.
-- La base de datos "gamerzone" y su usuario los crea la imagen oficial de MySQL
-- a partir de las variables MYSQL_DATABASE, MYSQL_USER y MYSQL_PASSWORD.
-- Las tablas las crea la aplicación (JPA / Hibernate) al arrancar.

ALTER DATABASE gamerzone CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;