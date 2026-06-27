# Multi-stage build: compile with Maven + JDK, run on a slim JRE.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copy parent POM and this module's POM first to leverage Docker layer caching
COPY pom.xml .
COPY user-service/pom.xml user-service/pom.xml
COPY farm-twin-service/pom.xml farm-twin-service/pom.xml
RUN mvn -q -pl user-service -am dependency:go-offline

COPY user-service/src user-service/src
RUN mvn -q -pl user-service -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S agritwin && adduser -S agritwin -G agritwin
COPY --from=build /build/user-service/target/*.jar app.jar
USER agritwin
EXPOSE 8081
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget -q --spider http://localhost:8081/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
