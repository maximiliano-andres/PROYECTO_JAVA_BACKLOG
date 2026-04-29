# ==============================================================================
# STAGE 1: Build the application
# ==============================================================================
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the pom.xml and download dependencies first (caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# ==============================================================================
# STAGE 2: Run the application
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install curl for health checks
RUN apk add --no-cache curl

# Create a non-root user for security (enterprise standard)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the JAR from the build stage
# Using a glob pattern to handle versioning automatically
COPY --from=build /app/target/*.jar app.jar

# Environment configuration
# Using the port defined in application.yaml
ENV SERVER_PORT=2025
EXPOSE 2025

# Performance tuning and metadata
LABEL maintainer="maximiliano-andres"
LABEL version="1.0"
LABEL description="Professional Docker image for ExtractorInfoDB"

# Healthcheck to ensure the container is running correctly
# Uses Spring Boot Actuator endpoint added previously
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:2025/actuator/health || exit 1

# Entry point with optimized JVM flags for container environments
# -XX:+UseParallelGC: Efficient for batch/backend workloads
# -XX:MaxRAMPercentage: Allows Java to adapt to Docker memory limits
ENTRYPOINT ["java", \
            "-XX:+UseParallelGC", \
            "-XX:MaxRAMPercentage=75.0", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", \
            "app.jar"]
