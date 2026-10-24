# ===== Etapa 1: compilar con Maven =====
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Primero solo el pom para aprovechar la caché de dependencias
COPY pom.xml .
RUN mvn -q dependency:go-offline

COPY src ./src
RUN mvn -q package -DskipTests

# ===== Etapa 2: imagen final liviana (solo JRE) =====
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Seguridad: no ejecutar la app como root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=build /app/target/*.jar app.jar

# Dentro del contenedor usamos 8080 (un usuario no-root no puede usar el puerto 80)
ENV SERVER_PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]