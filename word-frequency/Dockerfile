FROM maven:3.9.16-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/word-frequency.jar /app/word-frequency.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/word-frequency.jar"]
