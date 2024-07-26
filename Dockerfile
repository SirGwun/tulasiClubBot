# Используем базовый образ с JRE для финальной сборки
FROM openjdk:17-jdk-slim as runtime

# Рабочая директория внутри контейнера
WORKDIR /app

# Копируем JAR файл из папки target в контейнер
COPY target/tulasiClabBot-1.0-jar-with-dependencies.jar /app/tcbot.jar

# Команда для запуска JAR файла
ENTRYPOINT ["java", "-jar", "/app/tcbot.jar"]
