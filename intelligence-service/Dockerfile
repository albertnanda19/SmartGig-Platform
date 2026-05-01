FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/intelligence-service-*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker", "app.jar"]

