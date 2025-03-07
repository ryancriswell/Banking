# Build stage
FROM maven:3.8-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies separately to leverage Docker cache
RUN mvn dependency:go-offline
COPY src/ ./src/
RUN mvn package -DskipTests

# Runtime stage
FROM bellsoft/liberica-openjdk-alpine:17
WORKDIR /app
# Copy the JAR from the build stage
COPY --from=build /app/target/*.jar app.jar
# Add OpenTelemetry agent
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.31.0/opentelemetry-javaagent.jar /opt/opentelemetry-agent.jar
EXPOSE 8080
ENTRYPOINT ["java", "-javaagent:/opt/opentelemetry-agent.jar", \
    "-Dotel.resource.attributes=service.instance.id=$HOSTNAME,container=app", \
    "-Dotel.instrumentation.logback-appender.experimental-log-attributes=true", \
    "-Dotel.instrumentation.common.default-enabled=true", \
    "-Dotel.sdk.disabled=false", \
    "-Dotel.logs.exporter=otlp", \
    "-Dotel.traces.exporter=otlp", \
    "-Dotel.metrics.exporter=otlp", \
    "-jar", "/app/app.jar"]