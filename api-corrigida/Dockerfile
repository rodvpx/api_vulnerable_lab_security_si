FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
# Copy only what is needed for dependency resolution first
COPY pom.xml .
COPY src src
# Build the application using the maven image (no mvnw required)
RUN mvn -B package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]