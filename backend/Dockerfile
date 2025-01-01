# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Kopiere das bereits gebaute JAR aus dem Build-Prozess
COPY build/libs/*.jar app.jar

EXPOSE 8080

# JVM-Optimierungen für Container
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]