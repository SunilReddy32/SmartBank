# ─────────────────────────────────────────────
# Stage 1: BUILD
# Use Maven + Java 21 to compile and package the jar
# ─────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# Set working directory inside the container
WORKDIR /app

# Copy pom.xml first — Docker caches this layer
# so Maven dependencies are only re-downloaded when pom.xml changes
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# ─────────────────────────────────────────────
# Stage 2: RUN
# Use a slim Java 21 image — no Maven needed at runtime
# This keeps the final image small (~200MB vs ~600MB)
# ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

# Create a non-root user for security
# Running as root inside containers is a security risk
RUN addgroup -S smartbank && adduser -S smartbank -G smartbank

WORKDIR /app

# Copy only the built jar from the builder stage
COPY --from=builder /app/target/smartbank-1.0.0.jar app.jar

# Change ownership to the non-root user
RUN chown smartbank:smartbank app.jar

# Switch to non-root user
USER smartbank

# Expose the port Spring Boot runs on
EXPOSE 9090

# Health check — Docker will ping this every 30s
# If it fails 3 times, Docker marks the container as unhealthy
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:9090/actuator/health || exit 1

# Start the application
# -XX:+UseContainerSupport — JVM respects Docker memory limits
# -XX:MaxRAMPercentage=75  — JVM uses max 75% of container RAM
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]