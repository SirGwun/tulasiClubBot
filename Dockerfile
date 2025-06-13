FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline
COPY ./data ./data
COPY ./src ./src
RUN ./mvnw clean package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/tcbot.jar /tcbot.jar
COPY --from=build /app/data /app/data
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/tcbot.jar"]