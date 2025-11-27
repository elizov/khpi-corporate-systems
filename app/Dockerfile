# Build stage
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml mvnw mvnw.cmd .
COPY .mvn .mvn
COPY src src
RUN ./mvnw -DskipTests package

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/app-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 3199
ENTRYPOINT ["java","-jar","/app/app.jar"]
