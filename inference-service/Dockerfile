FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY common/pom.xml common/
COPY common/src common/src/
COPY inference-service/pom.xml inference-service/
COPY inference-service/src inference-service/src/
RUN mvn -f pom.xml install -N -DskipTests -q && \
    mvn -f common/pom.xml install -DskipTests -q && \
    mvn -f inference-service/pom.xml clean package -DskipTests -q

FROM eclipse-temurin:17-jre
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
RUN groupadd appgroup && useradd -g appgroup appuser
WORKDIR /app
COPY --from=build /build/inference-service/target/*.jar app.jar
HEALTHCHECK --interval=15s --timeout=5s --retries=3 --start-period=30s \
  CMD curl -sf http://localhost:8080/actuator/health || exit 1
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
