FROM eclipse-temurin:21
RUN mkdir /opt/app
COPY target/notification-service-1.0.0-SNAPSHOT.jar /opt/app
CMD ["java", "-jar", "/opt/app/notification-service-1.0.0-SNAPSHOT.jar"]

