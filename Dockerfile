# Stage 1: Build
FROM eclipse-temurin:21 AS builder
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY .mvn/ .mvn/
COPY mvnw mvnw.cmd pom.xml ./

# Download dependencies (cached if pom.xml doesn't change)
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
RUN ./mvnw dependency:go-offline || true

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre
WORKDIR /opt/app

# Copy jar from build stage
COPY --from=builder /app/target/notification-service-1.0.0-SNAPSHOT.jar ./app.jar

# Expose port
EXPOSE 8082

# Run the application
CMD ["java", "-jar", "app.jar"]
