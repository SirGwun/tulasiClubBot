FROM maven:3.6.3-jdk-21 AS build
COPY . /app
WORKDIR /app
RUN mvn clean package

FROM openjdk:11-jre-slim
COPY --from=build /app/target/tulasiClabBot-1.0-jar-with-dependencies.jar /tcbot.jar
CMD ["java", "-jar", "/tcbot.jar"]