# Use a base image with Java and Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy project files
COPY . .

# Build application
RUN mvn clean package -DskipTests -B

# Runtime Stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Install curl for healthcheck
RUN apk add --no-cache curl

# Copy built jar
COPY --from=build /app/target/*.jar app.jar

# Create startup script
RUN echo '#!/bin/sh' > /app/start.sh && \
    echo 'echo "Starting PetSave API..."' >> /app/start.sh && \
    echo 'echo "Java version: $(java -version 2>&1 | head -n 1)"' >> /app/start.sh && \
    echo 'echo "Working directory: $(pwd)"' >> /app/start.sh && \
    echo 'echo "Files in app:"' >> /app/start.sh && \
    echo 'ls -la' >> /app/start.sh && \
    echo 'echo "PORT: ${PORT:-8080}"' >> /app/start.sh && \
    echo 'exec java -Djava.security.egd=file:/dev/./urandom -jar app.jar --spring.profiles.active=railway --server.port=${PORT:-8080}' >> /app/start.sh && \
    chmod +x /app/start.sh

# Railway provides PORT dynamically
EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:${PORT:-8080}/api/test/health || exit 1

# Start app
CMD ["/app/start.sh"]