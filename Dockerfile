FROM gradle:8.5.0-jdk21 AS builder

WORKDIR /home/gradle/src

# Accept GitHub credentials as build args
ARG USERNAME
ARG TOKEN
ARG GITHUB_USERNAME
ARG GITHUB_TOKEN

ENV GITHUB_ACTOR=${USERNAME}
ENV GITHUB_TOKEN=${TOKEN}

ENV USERNAME=${USERNAME}
ENV TOKEN=${TOKEN}

COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle
RUN gradle dependencies --no-daemon || true

COPY . .

RUN gradle clean bootJar --no-daemon -x test

FROM eclipse-temurin:21-jdk-jammy AS runtime
WORKDIR /app
RUN apt-get update && apt-get install -y curl && \
    curl -O https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip && \
    apt-get install -y unzip && \
    unzip newrelic-java.zip && \
    rm newrelic-java.zip && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
COPY --from=builder /home/gradle/src/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-javaagent:/app/newrelic/newrelic.jar", "-jar", "app.jar"]
