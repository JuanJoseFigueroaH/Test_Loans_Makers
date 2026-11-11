# ── Stage 1: Dependencies ──
FROM maven:3.9.9-eclipse-temurin-21-alpine AS deps
WORKDIR /app
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# ── Stage 2: Build ──
FROM deps AS build
COPY src ./src
RUN mvn clean package -DskipTests -B -q

# ── Stage 3: Runtime ──
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=build /app/target/*.jar app.jar

RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
