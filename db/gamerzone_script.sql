-- GamerZone - Script de creación e inserción de datos (MySQL 8.4)
-- Los usuarios base (user, admin, organizador) no se incluyen: los crea la aplicación al arrancar
-- con las contraseñas de las variables de entorno (así no se publican hashes de contraseñas).

-- MySQL dump 10.13  Distrib 8.4.13, for Linux (x86_64)
--
-- Host: localhost    Database: gamerzone
-- ------------------------------------------------------
-- Server version	8.4.13

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `evento`
--

DROP TABLE IF EXISTS `evento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `evento` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `categoria` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL,
  `descripcion` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `destacado` bit(1) NOT NULL,
  `direccion` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `fecha` date NOT NULL,
  `hora` time NOT NULL,
  `estacionamiento` bit(1) DEFAULT NULL,
  `locomocion` bit(1) DEFAULT NULL,
  `comida` bit(1) DEFAULT NULL,
  `alojamiento` bit(1) DEFAULT NULL,
  `titulo` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `ubicacion` varchar(60) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `evento`
--

LOCK TABLES `evento` WRITE;
/*!40000 ALTER TABLE `evento` DISABLE KEYS */;
INSERT INTO `evento` VALUES (1,'Torneo','Arma tu equipo y compite por el primer lugar en nuestro torneo mensual. Fase de grupos y playoffs al mejor de tres.',_binary '','Av. Providencia 1234, Providencia','2026-11-15','15:00:00',_binary '',_binary '',_binary '',_binary '\0','Torneo Valorant 5v5','Santiago'),(2,'LAN Party','Trae tu equipo y juega toda la noche con la comunidad GamerZone. Mesas, energía y red cableada para 200 jugadores.',_binary '','Centro de Eventos Matucana 456, Estación Central','2026-11-22','18:00:00',_binary '',_binary '',_binary '',_binary '\0','LAN Party Santiago','Santiago'),(3,'Online','¿Eres el más rápido? Demuéstralo en nuestro desafío de speedrun transmitido en vivo.',_binary '\0','Transmisión en vivo desde el canal oficial de GamerZone','2026-11-30','20:00:00',_binary '\0',_binary '\0',_binary '\0',_binary '\0','Speedrun Challenge','Online'),(4,'Feria','Consolas clásicas, arcades, coleccionables y zona de juego libre para toda la familia.',_binary '','Av. Brasil 2950, Valparaíso','2026-12-05','11:00:00',_binary '',_binary '',_binary '',_binary '','Feria Retro Gaming','Valparaíso'),(5,'Torneo','Torneo 1 vs 1 de fútbol virtual con premios para los tres primeros lugares.',_binary '\0','Calle O\'Higgins 780, Concepción','2026-12-12','16:00:00',_binary '\0',_binary '',_binary '',_binary '','Torneo FC Clausura','Concepción'),(6,'Charla','Desarrolladores independientes muestran sus juegos y cuentan cómo los crearon. Incluye ronda de preguntas.',_binary '\0','Av. Libertador Bernardo O\'Higgins 3363, Estación Central','2026-12-19','10:30:00',_binary '',_binary '',_binary '\0',_binary '\0','Encuentro Indie Devs','Santiago'),(7,'Torneo','Torneo 5v5 de League of Legends',_binary '\0','Av. Siempre Viva 742','2026-12-28','17:00:00',_binary '',_binary '',_binary '',_binary '\0','Torneo LoL Primavera','Santiago');
/*!40000 ALTER TABLE `evento` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `evento_expositor`
--

DROP TABLE IF EXISTS `evento_expositor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `evento_expositor` (
  `evento_id` bigint NOT NULL,
  `nombre` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `orden` int NOT NULL,
  PRIMARY KEY (`evento_id`,`orden`),
  CONSTRAINT `FKda830126tgfhnimy01hm10o97` FOREIGN KEY (`evento_id`) REFERENCES `evento` (`id`),
  CONSTRAINT `evento_expositor_chk_1` CHECK ((`orden` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `evento_expositor`
--

LOCK TABLES `evento_expositor` WRITE;
/*!40000 ALTER TABLE `evento_expositor` DISABLE KEYS */;
INSERT INTO `evento_expositor` VALUES (1,'Equipo Andes Esports',0),(1,'Caster Sofía \"Nova\" Rojas',1),(2,'Técnicos de HexaGear',0),(2,'Streamer Diego \"Pixel\" Fuentes',1),(3,'Comunidad Speedrun Chile',0),(4,'Coleccionistas de la V Región',0),(4,'Museo del Videojuego (muestra itinerante)',1),(5,'Jugador profesional Matías Soto',0),(6,'Estudio Lumen Games',0),(6,'Estudio Pájaro Azul',1);
/*!40000 ALTER TABLE `evento_expositor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `evento_organizador`
--

DROP TABLE IF EXISTS `evento_organizador`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `evento_organizador` (
  `evento_id` bigint NOT NULL,
  `nombre` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `orden` int NOT NULL,
  PRIMARY KEY (`evento_id`,`orden`),
  CONSTRAINT `FKb6mwner45mn1byd1tsgl4vk52` FOREIGN KEY (`evento_id`) REFERENCES `evento` (`id`),
  CONSTRAINT `evento_organizador_chk_1` CHECK ((`orden` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `evento_organizador`
--

LOCK TABLES `evento_organizador` WRITE;
/*!40000 ALTER TABLE `evento_organizador` DISABLE KEYS */;
INSERT INTO `evento_organizador` VALUES (1,'GamerZone',0),(1,'Liga Andina de Esports',1),(2,'GamerZone',0),(2,'Club LAN Chile',1),(3,'GamerZone',0),(4,'Asociación Retro Porteña',0),(4,'GamerZone',1),(5,'Liga Sur Gaming',0),(6,'GamerZone',0),(6,'Colectivo Indie Chile',1),(7,'GamerZone',0);
/*!40000 ALTER TABLE `evento_organizador` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `avatar` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `gamertag` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `nombre` varchar(60) COLLATE utf8mb4_unicode_ci NOT NULL,
  `notif_email` bit(1) NOT NULL,
  `notif_nuevos_eventos` bit(1) NOT NULL,
  `notif_recordatorios` bit(1) NOT NULL,
  `password` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `username` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK863n1y3x0jalatoir4325ehal` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario`
--

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
/*!40000 ALTER TABLE `usuario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario_juego`
--

DROP TABLE IF EXISTS `usuario_juego`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario_juego` (
  `usuario_id` bigint NOT NULL,
  `juego` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL,
  `orden` int NOT NULL,
  PRIMARY KEY (`usuario_id`,`orden`),
  CONSTRAINT `FKebytgagdhnsd675as63sdhqmn` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `usuario_juego_chk_1` CHECK ((`orden` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario_juego`
--

LOCK TABLES `usuario_juego` WRITE;
/*!40000 ALTER TABLE `usuario_juego` DISABLE KEYS */;
/*!40000 ALTER TABLE `usuario_juego` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario_rol`
--

DROP TABLE IF EXISTS `usuario_rol`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario_rol` (
  `usuario_id` bigint NOT NULL,
  `rol` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`usuario_id`,`rol`),
  CONSTRAINT `FKbyfgloj439r9wr9smrms9u33r` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario_rol`
--

LOCK TABLES `usuario_rol` WRITE;
/*!40000 ALTER TABLE `usuario_rol` DISABLE KEYS */;
/*!40000 ALTER TABLE `usuario_rol` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-03 21:50:00
