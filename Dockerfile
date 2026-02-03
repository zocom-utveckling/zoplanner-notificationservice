# Stage 1: Build
FROM eclipse-temurin:21 AS builder
WORKDIR /app

# Copy Maven wrapper and pom
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy source
COPY src ./src

# Build
RUN ./mvnw clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre
WORKDIR /opt/app

COPY --from=builder /app/target/notification-service-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8082
CMD ["java", "-jar", "app.jar"]
