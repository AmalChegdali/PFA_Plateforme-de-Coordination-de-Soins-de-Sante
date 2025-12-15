FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/medicalrecord-service-*.jar app.jar
EXPOSE 8083
CMD ["java", "-jar", "app.jar"]

