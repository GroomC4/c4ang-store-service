# Runtime stage only - JAR is pre-built by GitHub Actions
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Create log directory
RUN mkdir -p /app/logs && chown -R spring:spring /app/logs

USER spring:spring

# Copy pre-built JAR from GitHub Actions build
ARG JAR_FILE=store-api/build/libs/*.jar
COPY ${JAR_FILE} app.jar

# Application metadata
LABEL maintainer="c4ang-store-service"
LABEL description="C4ang Store Service - 스토어 관리 마이크로서비스"

# Expose application port
EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
