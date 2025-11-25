FROM eclipse-temurin:21
CMD ["java", "-jar", "/opt/app/notification-service-1.0.0-SNAPSHOT.jar"]
COPY target/notification-service-1.0.0-SNAPSHOT.jar /opt/app
RUN mkdir /opt/app

