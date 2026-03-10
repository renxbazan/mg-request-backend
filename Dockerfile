# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY src src

# Build (skip tests for faster builds; run tests in CI separately)
RUN apk add --no-cache maven && \
    mvn package -DskipTests -q

# Stage 2: Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -g 1000 appgroup && adduser -u 1000 -G appgroup -D appuser
USER appuser

# Copy JAR from build stage
COPY --from=build /app/target/request.jar app.jar

# JVM options for containers (lightweight, AWS-friendly)
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
