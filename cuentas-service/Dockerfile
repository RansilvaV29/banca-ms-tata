# ---- Etapa 1: compilación ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Primero solo el pom: la capa de dependencias se reutiliza mientras el pom no cambie
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---- Etapa 2: imagen de ejecución (solo JRE, más liviana y con menos superficie de ataque) ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/target/cuentas-service-*.jar app.jar
USER app
EXPOSE 8082
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
