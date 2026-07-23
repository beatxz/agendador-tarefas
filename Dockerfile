FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

COPY . .

RUN chmod +x gradlew

RUN ./gradlew build --no-daemon

FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY --from=build /app/build/libs/agendador-tarefas-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

CMD ["java","-jar","app.jar"]