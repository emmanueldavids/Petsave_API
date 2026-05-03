# Use a base image with Java and Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the rest of the project files
COPY . .

# Package the application
RUN mvn clean package -DskipTests -B

# Use a lightweight image for the final container
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the jar from the build image
COPY --from=build /app/target/*.jar app.jar

# Create a simple startup script for debugging
RUN echo '#!/bin/sh' > /app/start.sh && \
    echo 'echo "Starting PetSave API..."' >> /app/start.sh && \
    echo 'echo "Java version: $(java -version 2>&1 | head -n 1)"' >> /app/start.sh && \
    echo 'echo "Working directory: $(pwd)"' >> /app/start.sh && \
    echo 'echo "Files in app: $(ls -la)"' >> /app/start.sh && \
    echo 'exec java -Djava.security.egd=file:/dev/./urandom -jar app.jar --spring.profiles.active=railway --server.port=$PORT' >> /app/start.sh && \
    chmod +x /app/start.sh

# Expose port (Railway uses PORT variable)
EXPOSE 8080

# Health check for Docker
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/test/health || exit 1

# Run the startup script
CMD ["/app/start.sh"]
