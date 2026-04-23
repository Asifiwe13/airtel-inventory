# Build stage - compile Java code
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Install fonts for QR code generation
RUN apk add --no-cache \
    fontconfig \
    freetype \
    ttf-dejavu \
    wget

WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/inventory-*.jar app.jar

# Create directories
RUN mkdir -p /tmp/qrcodes /tmp/reports

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", \
  "-Dspring.profiles.active=render", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
