# Stage 1: Build
FROM eclipse-temurin:21 AS builder
WORKDIR /app

# Kopiera Maven wrapper och pom.xml
COPY .mvn/ .mvn/
COPY mvnw mvnw.cmd pom.xml ./

# Ladda ner dependencies (cachas om pom.xml inte ändras)
RUN chmod +x ./mvnw || true
RUN ./mvnw dependency:go-offline || true

# Kopiera source code
COPY src ./src

# Bygg applikationen
RUN ./mvnw clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre
WORKDIR /opt/app

# Kopiera jar från build stage
COPY --from=builder /app/target/webapi-0.0.1-SNAPSHOT.jar ./app.jar

# Exponera port
EXPOSE 8080

# Kör applikationen
CMD ["java", "-jar", "app.jar"]