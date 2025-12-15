FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/provider-service-*.jar app.jar
EXPOSE 8082
CMD ["java", "-jar", "app.jar"]

