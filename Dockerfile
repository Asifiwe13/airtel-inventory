FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache \
    fontconfig \
    freetype \
    ttf-dejavu \
    wget

WORKDIR /app

COPY target/inventory-1.0.0.jar app.jar

RUN mkdir -p /tmp/qrcodes /tmp/reports

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-Dspring.profiles.active=render", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
