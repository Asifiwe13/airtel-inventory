FROM openjdk:17-jdk-slim

# Install fonts for QR code generation
RUN apt-get update && apt-get install -y \
    fontconfig \
    libfreetype6 \
    wget \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy the jar file
COPY target/inventory-1.0.0.jar app.jar

# Create directories for QR codes and reports
RUN mkdir -p /tmp/qrcodes /tmp/reports

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application with render profile
ENTRYPOINT ["java", \
  "-Dspring.profiles.active=render", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
