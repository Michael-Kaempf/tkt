# Verwende ein Java 21-Basisimage
FROM eclipse-temurin:21-jdk-alpine

# Setze das Arbeitsverzeichnis im Container
WORKDIR /app

# Kopiere das erstellte JAR-File in das Image
COPY build/libs/*.jar app.jar

# Definiere den Startbefehl
ENTRYPOINT ["java", "-jar", "app.jar"]
