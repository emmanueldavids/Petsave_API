# Use a base image with Java and Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the project files
COPY . .

# Package the application
RUN mvn clean package -DskipTests

# Use a lightweight image for the final container
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copy the jar from the build image
COPY --from=build /app/target/*.jar app.jar

# Copy .env if needed
COPY .env .env

# Expose port
EXPOSE 9001

# Run the jar file
CMD ["java", "-jar", "app.jar"]
