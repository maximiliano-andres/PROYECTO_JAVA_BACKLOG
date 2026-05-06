# =========================
# STAGE 1: BUILD
# =========================
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos solo el pom primero (mejora cache)
COPY pom.xml .
RUN mvn -q -B -DskipTests dependency:go-offline

# Copiamos código y compilamos
COPY src ./src
RUN mvn -q -DskipTests package

# =========================
# STAGE 2: RUNTIME
# =========================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuario seguro (best practice)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiamos el jar compilado
COPY --from=build /app/target/*.jar app.jar

# Configuración
ENV SERVER_PORT=2025
EXPOSE 2025

# Healthcheck liviano (sin curl)
HEALTHCHECK CMD wget -qO- http://localhost:2025/actuator/health || exit 1

# JVM optimizada para contenedores
ENTRYPOINT ["java","-XX:+UseParallelGC","-XX:MaxRAMPercentage=75.0","-Djava.security.egd=file:/dev/./urandom","-jar","app.jar"]