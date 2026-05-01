FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY ../pom.xml /workspace/pom.xml
COPY ../common-lib /workspace/common-lib
COPY . /workspace/auth-service
RUN mvn -q -pl auth-service -am test package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/auth-service/target/*.jar /app/app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]

