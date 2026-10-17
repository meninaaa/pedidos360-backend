FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos el pom.xml y el settings.xml para la resolución de dependencias
COPY pom.xml .
COPY settings.xml /root/.m2/settings.xml

# Descargamos dependencias en modo offline para aprovechar el caché
RUN mvn -q -DskipTests dependency:go-offline

# Copiamos el código fuente y compilamos
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]