FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/message-management-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8091

ENTRYPOINT ["java", "-jar", "app.jar"]