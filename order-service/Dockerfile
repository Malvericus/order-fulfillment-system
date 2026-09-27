# Stage 1: build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY common-events/pom.xml common-events/pom.xml
COPY order-service/pom.xml order-service/pom.xml
COPY inventory-service/pom.xml inventory-service/pom.xml
COPY fulfillment-service/pom.xml fulfillment-service/pom.xml
COPY alert-service/pom.xml alert-service/pom.xml
COPY insight-service/pom.xml insight-service/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
RUN mvn -pl order-service -am -q dependency:go-offline
COPY common-events/src common-events/src
COPY order-service/src order-service/src
RUN mvn -pl order-service -am -q -DskipTests package

# Stage 2: runtime
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=build /build/order-service/target/order-service-0.1.0.jar app.jar
USER appuser
EXPOSE 8081
HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=5 \
  CMD wget -qO- http://localhost:8081/actuator/health | grep -q '"status":"UP"' || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
