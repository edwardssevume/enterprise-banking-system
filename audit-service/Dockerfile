FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/audit-service-1.0.0.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]