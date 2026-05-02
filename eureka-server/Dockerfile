FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY eureka-server/pom.xml eureka-server/pom.xml
RUN mvn -f eureka-server/pom.xml dependency:go-offline -B -q 2>/dev/null || true
COPY eureka-server/src eureka-server/src
RUN mvn -f eureka-server/pom.xml package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S smartgig && adduser -S smartgig -G smartgig
WORKDIR /app
COPY --from=builder /app/eureka-server/target/eureka-server-*.jar app.jar
RUN chown smartgig:smartgig app.jar
USER smartgig
EXPOSE 8761
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 CMD wget -qO- http://localhost:8761/actuator/health || exit 1
ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75.0","-Djava.security.egd=file:/dev/./urandom","-Dspring.profiles.active=docker","-jar","app.jar"]

