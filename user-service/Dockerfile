FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY common-lib/pom.xml common-lib/
COPY user-service/pom.xml user-service/
RUN mvn -pl common-lib,user-service dependency:go-offline -B -q 2>/dev/null || true
COPY common-lib/src common-lib/src
COPY user-service/src user-service/src
RUN mvn -pl common-lib install -DskipTests -q && mvn -pl user-service package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S smartgig && adduser -S smartgig -G smartgig
WORKDIR /app
COPY --from=builder /app/user-service/target/user-service-*.jar app.jar
RUN chown smartgig:smartgig app.jar
USER smartgig
EXPOSE 8082
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 CMD wget -qO- http://localhost:8082/actuator/health || exit 1
ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75.0","-Djava.security.egd=file:/dev/./urandom","-Dspring.profiles.active=docker","-jar","app.jar"]

