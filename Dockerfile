# Multi-stage build for optimal image size
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy POM first for better caching
COPY pom.xml .

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B || mvn dependency:resolve -B

# Copy source code
COPY src ./src

# Build application with retry logic
RUN mvn clean package -DskipTests -B -e || \
    (echo "First attempt failed, retrying..." && mvn clean package -DskipTests -B -e)

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy JAR from builder
COPY --from=builder /build/target/*.jar app.jar

# Change ownership
RUN chown appuser:appgroup app.jar

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
