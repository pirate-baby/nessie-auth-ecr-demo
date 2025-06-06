# Build stage
FROM maven:3.9-eclipse-temurin-11 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package

# Run stage
FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=build /app/target/jwt-validator-1.0-SNAPSHOT-jar-with-dependencies.jar /app/jwt-validator.jar
COPY validate-token.sh /app/validate-token.sh
RUN chmod +x /app/validate-token.sh
ENTRYPOINT ["/app/validate-token.sh"]