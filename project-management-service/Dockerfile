FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/project-management-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8083

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Add health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8083/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]