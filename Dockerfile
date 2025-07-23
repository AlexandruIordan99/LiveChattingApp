FROM gradle:8.5-jdk21 AS build
WORKDIR /app
COPY *.gradle* ./
COPY src ./src
RUN gradle build -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8088
ENV SPRING_PROFILES_ACTIVE=dev
ENTRYPOINT ["java", "-jar", "app.jar"]
