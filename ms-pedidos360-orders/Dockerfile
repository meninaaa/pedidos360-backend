# Etapa de construcción (Build)
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
# Copiar wrappers de Maven y el pom
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
# Descargar dependencias (mejora el caché de Docker)
RUN ./mvnw dependency:go-offline
# Copiar código fuente y compilar
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Etapa de ejecución (Run)
FROM eclipse-temurin:21-jre
WORKDIR /app
# Copiar solo el .jar generado en la etapa anterior
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]