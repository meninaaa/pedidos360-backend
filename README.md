# Pedidos360 - Backend & BFF/microservicios (Spring Boot)

## 1. Resumen Ejecutivo
El backend de **Pedidos360** es un ecosistema de microservicios distribuidos desarrollados en **Spring Boot (Java)**. Está diseñado bajo los principios de alta cohesión, bajo acoplamiento y escalabilidad horizontal. Cuenta con un **BFF (Backend For Frontend)** como puerta de enlace lógica y utiliza una **Arquitectura Orientada a Eventos (EDA)** apoyada en Message Brokers (RabbitMQ / Kafka) para garantizar la resiliencia y el procesamiento asíncrono de grandes volúmenes de datos logísticos.

## 2. Topología de Microservicios

El sistema está fragmentado en dominios de negocio específicos (Domain-Driven Design):

1.  **BFF (Backend For Frontend):** Es el orquestador principal. Recibe la petición del frontend (Angular), decodifica y audita el token JWT de Azure AD, extrae los claims (roles, email) de forma segura a través de `ObjectMapper`, y enruta la petición al microservicio correspondiente con las cabeceras inyectadas.
2.  **Orders Service (Pedidos):** Núcleo transaccional. Maneja la máquina de estados de los envíos (Creado, Aceptado, En Preparación, Despachado).
3.  **Catalog Service (Catálogo):** Gestiona el inventario, precios y disponibilidad de SKUs.
4.  **Audit Service (Auditoría):** Microservicio pasivo que registra una traza inmutable de quién hizo qué y cuándo.
5.  **Reports Service (Reportería):** Agregador de datos que calcula KPIs, tendencias de Lead Time y top de ventas.

## 3. Arquitectura Orientada a Eventos: Kafka y RabbitMQ

Para evitar cuellos de botella y acoplamiento sincrónico, el ecosistema utiliza un **sistema de mensajería asíncrona**. 

### 3.1. ¿Por qué se utilizan Message Brokers?
Cuando una orden cambia de estado (ej. un Operador presiona "Despachar"), el servicio de *Orders* no hace una petición HTTP directa a *Audit* o *Reports*. En su lugar, el flujo es el siguiente:
1.  *Orders* guarda el estado en su base de datos.
2.  *Orders* actúa como **Productor** y dispara un evento (ej. `OrderStatusChangedEvent`) al Broker de mensajería.
3.  Inmediatamente, *Orders* responde un `200 OK` al frontend. El usuario no sufre tiempos de espera.

### 3.2. Roles de RabbitMQ y Kafka en el Ecosistema
*   **RabbitMQ (Task/Work Queues):** Ideal para tareas de enrutamiento exacto y procesamiento garantizado. Se utiliza para el **Audit Service**. Cuando ocurre una acción crítica, se encola en RabbitMQ. El microservicio de auditoría actúa como **Consumidor**, toma el mensaje de la cola, lo procesa y lo guarda en la base de datos de trazas. Si el servicio de auditoría se cae, RabbitMQ retiene los mensajes; al volver a encenderse, procesa todo el historial pendiente asegurando que **ningún log se pierda** (Tolerancia a fallos).
*   **Kafka (Event Streaming):** Utilizado para el procesamiento masivo de datos en tiempo real (High Throughput). El **Reports Service** se suscribe a los Tópicos de Kafka (Topics) para ir construyendo proyecciones de datos (CQRS). A medida que Kafka emite flujos ininterrumpidos de ventas, el servicio de reportería va recalculando los "Top Productos" y "Ventas por Hora" en memoria y guardándolos en base de datos, lo que permite que el Dashboard del frontend cargue en milisegundos.

## 4. Persistencia de Datos
Actualmente, el sistema utiliza **H2 Database** (bases de datos relacionales en memoria) por cada microservicio. Esto garantiza el aislamiento de datos (Data Sovereignty) exigido por el patrón microservicios y permite un despliegue ágil en entornos de prueba y desarrollo.

## 5. Despliegue en Infraestructura AWS (Cloud)

El proyecto está diseñado para funcionar nativamente en la nube de Amazon Web Services (AWS):

### 5.1. Construcción del Artefacto
Generar el empaquetado `.jar` autoejecutable (Fat JAR) prescindiendo de los tests de integración:
```bash
./mvnw clean package -DskipTests
5.2. Despliegue en Instancias EC2
Una vez transferido el artefacto a la instancia Linux/Ubuntu, se levanta el servicio en background para aislarlo de la sesión SSH del terminal:

Bash
nohup java -jar target/pedidos360-backend.jar > application-logs.log 2>&1 &
Regla de Seguridad: El Security Group de la EC2 debe tener habilitado el Inbound Port TCP 8080.

5.3. Exposición mediante AWS API Gateway

El comodín {proxy+} asegura que todas las rutas internas de los microservicios (/api/bff/orders, /api/bff/catalog) se resuelvan dinámicamente, actuando como un puente transparente entre Angular y el servidor EC2.
