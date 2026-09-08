# Pedidos360 - Microservices Architecture (Spring Boot)

Arquitectura completa de microservicios desarrollada en **Spring Boot 3.3**, diseñada para gestionar un sistema distribuido de comercio electrónico que incluye procesamiento de pedidos, control de inventario, notificaciones asíncronas, auditoría, reportes y una capa de Backend For Frontend (BFF).

## Arquitectura y Componentes del Sistema

El ecosistema está compuesto por los siguientes microservicios desacoplados que interactúan mediante HTTP síncrono, eventos de dominio (Kafka) y comandos asíncronos (RabbitMQ):

*   **`ms-pedidos360-bff` (Backend For Frontend):** Actúa como puerta de enlace y fachada para unificar y enrutar las peticiones provenientes de las interfaces de cliente hacia los microservicios internos de forma segura.
*   **`ms-pedidos360-orders` (Puerto 8081):** Gestiona el ciclo de vida y la máquina de estados de los pedidos. Aplica reglas de negocio estrictas (como el bloqueo de despacho sin aceptación previa), se comunica síncronamente con el catálogo mediante `WebClient`, publica eventos de dominio en **Kafka** (`orders.events`) y emite comandos de notificación a **RabbitMQ** (`cmd.direct`).
*   **`ms-pedidos360-catalog` (Puerto 8082):** Administra el inventario de productos y el control de stock de forma transaccional. Expone endpoints protegidos para consultas y descuentos de inventario requeridos por el servicio de órdenes.
*   **`ms-pedidos360-notify` (Puerto 8083):** Servicio tipo *worker* orientado a eventos. Consume comandos desde la cola de RabbitMQ (`q.cmd.email`) implementando un sistema robusto con manejo de reintentos, confirmación manual (`ACK`/`NACK`) y Dead Letter Queue (`DLX`/`DLQ`) para garantizar tolerancia a fallos.
*   **`ms-pedidos360-audit`:** Módulo especializado en la captura, registro y persistencia de eventos de auditoría y traza generados por las transacciones del sistema.
*   **`ms-pedidos360-report`:** Servicio analítico enfocado en la generación de reportes operativos mediante la suscripción a los streams de eventos de Kafka.

## Infraestructura y Persistencia
*   **Base de Datos:** Oracle Database (almacenamiento relacional persistente para órdenes, productos y registros mediante Spring Data JPA / Hibernate).
*   **Mensajería Asíncrona:** RabbitMQ (intercambio de comandos directos, enrutamiento y gestión de colas muertas).
*   **Event Streaming:** Apache Kafka (publicación y consumo de eventos de dominio a gran escala).

## Seguridad (Zero Trust)
*   **Autenticación y Autorización:** Integración con **Azure AD** mediante OAuth2 Resource Server con validación de tokens JWT.
*   **Control de Accesos (RBAC):** Mapeo de roles personalizados (`ROLE_ADMIN`, `ROLE_OPERATOR`, `ROLE_CLIENT`) aplicados a nivel de método mediante `@PreAuthorize`.

## Prerrequisitos
*   Java Development Kit (JDK 21 o superior).
*   Maven (incluye wrapper `./mvnw` en cada microservicio).
*   Docker y Docker Compose (para levantar la infraestructura de Oracle, RabbitMQ y Kafka).

## Configuración y Ejecución Local

1. Clona el repositorio y cámbiate a la rama de desarrollo:
   ```bash
   git clone [https://github.com/meninaaa/pedidos360-backend.git](https://github.com/meninaaa/pedidos360-backend.git)
   cd pedidos360-backend
   git checkout feature-branch
Levanta la infraestructura base utilizando Docker Compose en las carpetas correspondientes de infra/.

Compila y ejecuta cada microservicio de forma independiente utilizando su respectivo Maven Wrapper:

Bash
cd ms-pedidos360-orders
./mvnw spring-boot:run
Documentación API (OpenAPI / Swagger)
Los microservicios con exposición HTTP cuentan con documentación interactiva integrada mediante SpringDoc OpenAPI:

Orders API: http://localhost:8081/swagger-ui/index.html

Catalog API: http://localhost:8082/swagger-ui/index.html

Contenedorización y Despliegue (Docker & AWS EC2)
Cada microservicio incluye un Dockerfile optimizado de múltiples etapas (multi-stage build) basado en Eclipse Temurin JDK/JRE 21 para generar imágenes ligeras listas para producción:

Genera la imagen Docker por microservicio:

Bash
docker build -t pedidos360/orders-service:v1 .
Despliegue en instancias AWS EC2 combinando la ejecución de contenedores y enrutamiento mediante un AWS API Gateway configurado con rutas de reenvío con comodín /{proxy+}.
