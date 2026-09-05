# Pedidos360 - Backend & BFF (Spring Boot)

Arquitectura de microservicios y Backend For Frontend (BFF) desarrollada en **Spring Boot**, diseñada para gestionar la lógica de negocios, la persistencia de datos y la integración con la infraestructura en la nube.

## Arquitectura y Componentes
- **Framework:** Spring Boot (Java).
- **Capa de Datos:** Base de datos H2 (en memoria) integrada para el almacenamiento y sincronización de registros logísticos.
- **Seguridad y Enrutamiento:** Endpoints expuestos de forma segura para la recepción de solicitudes desde el API Gateway.
- **Contenedores y Cloud:** Preparado para ejecución en instancias **AWS EC2**.

## Prerrequisitos
- Java Development Kit (JDK 17 o superior).
- Maven (incluye wrapper `./mvnw`).

## Configuración y Ejecución Local

1. Clona el repositorio:
   ```bash
   git clone https://github.com/meninaaa/pedidos360-backend.git
   cd pedidos360-backend
   ```

2. Compila el proyecto con Maven:
   ```bash
   ./mvnw clean install
   ```

3. Ejecuta la aplicación Spring Boot:
   ```bash
   ./mvnw spring-boot:run
   ```
   El servicio estará disponible en el puerto `8080`.

## Despliegue en AWS EC2 y API Gateway
1. **Empaquetado:** Genera el archivo ejecutable `.jar`:
   ```bash
   ./mvnw package
   ```
2. **Despliegue en EC2:** Transfiere el archivo empaquetado a tu instancia de AWS EC2 y ejecútalo mediante Java:
   ```bash
   nohup java -jar target/pedidos360-backend.jar &
   ```
3. **AWS API Gateway:** Configura un API Gateway HTTP apuntando a la IP pública de la instancia EC2 utilizando la ruta de reenvío con comodín `/{proxy+}` para garantizar que todas las peticiones lleguen correctamente al microservicio.
