FROM gradle:8.5.0-jdk21 AS builder

WORKDIR /home/gradle/src

COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle
RUN gradle dependencies --no-daemon || true

COPY . .

RUN gradle clean bootJar --no-daemon -x test

FROM eclipse-temurin:21-jdk-jammy AS runtime

WORKDIR /app

COPY --from=builder /home/gradle/src/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]