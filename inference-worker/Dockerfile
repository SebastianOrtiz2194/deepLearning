FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY common/pom.xml common/
COPY common/src common/src/
COPY inference-worker/pom.xml inference-worker/
COPY inference-worker/src inference-worker/src/
RUN mvn -f pom.xml install -N -DskipTests -q && \
    mvn -f common/pom.xml install -DskipTests -q && \
    mvn -f inference-worker/pom.xml clean package -DskipTests -q

FROM eclipse-temurin:17-jre
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
RUN groupadd appgroup && useradd -g appgroup appuser
WORKDIR /app
COPY --from=build /build/inference-worker/target/*.jar app.jar
HEALTHCHECK --interval=15s --timeout=5s --retries=3 --start-period=60s \
  CMD curl -sf http://localhost:8081/actuator/health || exit 1
USER appuser
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
