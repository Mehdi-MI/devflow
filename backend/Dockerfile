# ============================================
# Stage 1: Build
# ============================================
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy Gradle configuration first
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Make Gradle wrapper executable
RUN chmod +x gradlew

# Download dependencies
RUN ./gradlew dependencies --no-daemon

# Copy application source
COPY src src

# Build the Spring Boot application
RUN ./gradlew clean bootJar --no-daemon


# ============================================
# Stage 2: Runtime
# ============================================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy only the generated JAR
COPY --from=builder /app/build/libs/*.jar app.jar

# DevFlow Spring Boot port
EXPOSE 8081

# Start application
ENTRYPOINT ["java", "-jar", "app.jar"]
