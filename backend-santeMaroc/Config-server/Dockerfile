FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/config-*.jar app.jar
EXPOSE 8888
CMD ["java", "-jar", "app.jar"]

