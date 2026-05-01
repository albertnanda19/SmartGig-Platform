FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/project-service-*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker", "app.jar"]

