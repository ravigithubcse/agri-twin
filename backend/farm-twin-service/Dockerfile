FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml .
COPY user-service/pom.xml user-service/pom.xml
COPY farm-twin-service/pom.xml farm-twin-service/pom.xml
RUN mvn -q -pl farm-twin-service -am dependency:go-offline

COPY farm-twin-service/src farm-twin-service/src
RUN mvn -q -pl farm-twin-service -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S agritwin && adduser -S agritwin -G agritwin
COPY --from=build /build/farm-twin-service/target/*.jar app.jar
USER agritwin
EXPOSE 8082
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget -q --spider http://localhost:8082/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
