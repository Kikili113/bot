# Этап 1: Сборка проекта с использованием Maven
FROM maven:3.8.6-openjdk-17-slim AS build
WORKDIR /app
COPY . .
RUN mvn clean package

# Этап 2: Запуск приложения из собранного jar-файла
FROM openjdk:17-jdk-slim
WORKDIR /app
# Обратите внимание: если имя jar-файла другое, отредактируйте его ниже
COPY --from=build /app/target/mathBot-1.0-SNAPSHOT.jar /app/mathBot.jar
EXPOSE 8080
CMD ["java", "-jar", "mathBot.jar"]
