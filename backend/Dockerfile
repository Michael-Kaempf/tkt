# Build stage
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Copy gradle files
COPY build.gradle settings.gradle ./

# Copy source code
COPY src ./src

# Build application
RUN ./gradlew build -x test

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Install common tools and create non-root user
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd -m javauser

# Copy JAR from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Use non-root user
USER javauser

# Set entrypoint
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1