# Multi-stage build for Spring Boot application with AWS Secrets Manager support
# Explicitly target AMD64 architecture for ECS compatibility

# Build stage - use explicit AMD64 image
FROM --platform=linux/amd64 maven:3.8.6-openjdk-11-slim AS build
WORKDIR /app

# Verify architecture
RUN echo "Build architecture: $(uname -m)" && \
  echo "Java architecture: $(java -XshowSettings:properties -version 2>&1 | grep 'os.arch')"

# Copy pom.xml and download dependencies (for better caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage - use explicit AMD64 image
FROM --platform=linux/amd64 openjdk:11-jre-slim

# Verify runtime architecture
RUN echo "Runtime architecture: $(uname -m)" && \
  echo "Java runtime architecture: $(java -XshowSettings:properties -version 2>&1 | grep 'os.arch')"

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create app directory and user
RUN groupadd -r appuser && useradd -r -g appuser appuser
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/memevote-backend-*.jar app.jar

# Change ownership to appuser
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
