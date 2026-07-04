FROM eclipse-temurin:21
LABEL maintainer="devchrisnam@gmail.com"

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
