Pedidos360 — Backend

Plataforma logística que centraliza la gestión de pedidos, catálogo de productos, notificaciones, auditoría de eventos de negocio y reportería operacional, mediante una arquitectura de microservicios Spring Boot con autenticación corporativa federada (Azure AD) y mensajería asíncrona dual (Kafka + RabbitMQ).

Este repositorio contiene el backend completo. El frontend (Angular 18 + MSAL) vive en un repositorio separado: pedidos360-frontend.

Arquitectura
Servicio	Puerto	Responsabilidad
ms-pedidos360-bff	8080	Entrada HTTP para el cliente, propaga el JWT entrante y reenvía hacia los microservicios internos vía RestTemplate.
ms-pedidos360-orders	8081	CRUD de pedidos, máquina de estados, coordinación de descuento de stock contra catalog, integración de pagos con Transbank Webpay Plus, productor de eventos Kafka y comandos RabbitMQ.
ms-pedidos360-catalog	8082	CRUD de productos, control de stock, expone el endpoint interno de descuento usado por orders.
ms-pedidos360-notify	—	Consumidor de RabbitMQ para el envío de notificaciones (sin base de datos propia).
ms-pedidos360-audit	8084	Consume eventos de Kafka y los persiste como registro inmutable de auditoría en audit_logs.
ms-pedidos360-report	8085	Consume eventos de Kafka, los persiste en report_logs y calcula KPIs (ventas, pedidos activos, lead time) a partir del histórico.
Oracle XE	1521	Base de datos, una instancia por servicio que la requiere (aislamiento de dominio).
Kafka + Zookeeper	9092 / 29092 interno	Event streaming de eventos de negocio.
RabbitMQ	5672 (AMQP) / 15672 (consola)	Mensajería de comandos de trabajo.
Flujo principal de un pedido
El cliente llama a ms-pedidos360-bff con un token JWT emitido por Azure AD.
El BFF reenvía la petición hacia ms-pedidos360-orders, propagando el header Authorization.
OrderController.crearPedido(...) recibe el request y delega en OrderService.createOrder(...).
createOrder(...) persiste el pedido en estado CREADO, publica el evento correspondiente y encola una notificación.
Al cambiar de estado vía PUT /{id}/status, OrderController.cambiarEstado(...) valida la transición (no se puede DESPACHADO sin pasar antes por ACEPTADO/EN_PREPARACION) y delega en OrderService.updateOrderStatus(...).
Si el nuevo estado es ACEPTADO, coordinarDescuentoStock(...) llama de forma síncrona a ms-pedidos360-catalog (PUT /api/catalog/products/{id}/reduce-stock), propagando el JWT original extraído del HttpServletRequest.
Cada cambio de estado dispara publicarEventoKafka(...), que publica el pedido serializado en dos tópicos: orders.events y audit.timeline.
ms-pedidos360-report y ms-pedidos360-audit consumen orders.events de forma independiente, cada uno con su propio group-id (report-group, audit-group), por lo que ambos reciben la copia completa del evento sin competir entre sí.

Las transiciones de estado válidas son: CREADO → ACEPTADO → EN_PREPARACION → DESPACHADO → ENTREGADO / CANCELADO.

Kafka

Kafka se usa para publicar hechos de negocio ya ocurridos (un pedido fue creado, un pedido cambió de estado), que son consumidos de forma independiente por auditoría y reportería sin acoplar al productor con sus consumidores.

Tópicos
Tópico	Productor	Consumidor	Uso
orders.events	ms-pedidos360-orders	ms-pedidos360-report (report-group)	Fuente para el cálculo de KPIs: ventas totales, pedidos activos, lead time promedio.
audit.timeline	ms-pedidos360-orders	ms-pedidos360-audit (audit-group)	Timeline inmutable de trazabilidad de pedidos.
Publicación

El método publicarEventoKafka(Order order) en OrderService.java serializa el pedido completo con ObjectMapper y lo envía a ambos tópicos en la misma operación:

java
kafkaTemplate.send("orders.events", eventPayload);
kafkaTemplate.send("audit.timeline", eventPayload);

Se invoca desde createOrder(...) (al crear un pedido) y desde updateOrderStatus(...) (en cada transición de estado).

Consumo
ms-pedidos360-audit — AuditKafkaConsumer.consumeOrderEvent(...) escucha orders.events con group-id: audit-group, construye un AuditLog con eventType = "ORDER_EVENT" y el payload crudo, y lo persiste vía AuditLogRepository.
ms-pedidos360-report — ReportMetricsService.consumeOrderEvent(...) escucha el mismo tópico con group-id: report-group. Persiste cada evento como OrderEventLog en report_logs y actualiza en memoria los contadores agregados mediante aplicarEvento(...):
CREADO → incrementa pedidosActivos.
ENTREGADO → decrementa pedidosActivos, suma a ventasTotales y calcula el lead time (Duration entre el timestamp de creación y el de entrega).
CANCELADO → decrementa pedidosActivos.
Al arrancar, @PostConstruct reconstruirEstadoDesdeBaseDeDatos() relee todo report_logs y reproduce los eventos en orden, para que las métricas sobrevivan un reinicio del contenedor.
Configuración
yaml
spring:
  kafka:
    bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:kafka-pedidos360:29092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
    consumer:
      group-id: report-group   # o audit-group, según el servicio
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
bash
docker compose up -d zookeeper-pedidos360 kafka-pedidos360
RabbitMQ

RabbitMQ transporta comandos dirigidos a una acción concreta (por ejemplo, "enviar este email"), a diferencia de Kafka, que transporta hechos de dominio que varios consumidores pueden leer de forma independiente.

Topología declarada en el caso
Exchange	Cola principal	Routing key	DLQ
cmd.direct	q.cmd.email	email.send	q.cmd.email.dlq
cmd.direct	q.cmd.kitchen	kitchen.ticket	q.cmd.kitchen.dlq
cmd.direct	q.cmd.invoice	invoice.gen	q.cmd.invoice.dlq
Implementación actual

El método enviarNotificacionRabbitMQ(...) en OrderService.java publica únicamente comandos de tipo EMAIL_NOTIFICATION, usando el exchange cmd.direct y la routing key email.send:

java
rabbitTemplate.convertAndSend("cmd.direct", "email.send", envelope);

Se invoca al crear un pedido y en las transiciones a ACEPTADO y DESPACHADO. El envelope sigue el formato estándar:

json
{
  "type": "EMAIL_NOTIFICATION",
  "eventId": "uuid",
  "timestamp": "2026-09-13T18:24:18",
  "traceId": "uuid",
  "correlationId": "123",
  "payload": { "mensaje": "El estado de su pedido #123 cambió a: ACEPTADO" }
}

ms-pedidos360-notify consume q.cmd.email y ejecuta el envío.

Pagos — Transbank Webpay Plus

WebpayController.java en ms-pedidos360-orders integra el ambiente de integración (sandbox) de Webpay Plus REST:

Endpoint	Método	Autenticación	Descripción
/api/payments/create	POST	JWT (usuario autenticado)	Inicia la transacción contra Transbank y devuelve url + token_ws.
/api/payments/commit	POST	Pública, sin JWT	Recibida directamente desde el navegador tras el formulario de retorno de Transbank; confirma la transacción y, si fue AUTHORIZED, invoca OrderService.updateOrderStatus(orderId, ACEPTADO).

/payments/commit debe quedar exenta de autenticación en la configuración de seguridad y, si el backend corre detrás de AWS API Gateway, también debe existir como ruta específica sin JWT Authorizer — el POST de retorno de Transbank nunca lleva un header Authorization.

API expuesta por el BFF
Método	Ruta	Rol requerido	Descripción
GET	/api/orders	Admin	Listado global de pedidos.
GET	/api/orders/pending	Admin, Operador	Pedidos en curso y pendientes (sin estado final).
GET	/api/orders/customer?email=	Admin, Operador, Cliente	Pedidos del usuario autenticado.
POST	/api/orders	Admin, Operador, Cliente	Crear un pedido.
PUT	/api/orders/{id}/status	Admin, Operador	Cambiar el estado de un pedido.
GET	/api/audit	Admin, Operador	Timeline de auditoría (solo lectura).
GET	/api/reports/summary	Admin	KPIs agregados en vivo.
GET	/api/reports/ventas-por-hora	Admin	Serie de ventas agregadas por hora.
GET	/api/reports/lead-time-trend	Admin	Lead time por pedido entregado.
POST	/api/payments/create	Usuario autenticado	Inicia un pago Webpay.
POST	/api/payments/commit	Pública	Confirmación de pago (llamada por Transbank).
Seguridad
El frontend se autentica contra Azure AD vía MSAL y adjunta el JWT como Authorization: Bearer <token>.
En despliegue cloud, AWS API Gateway (HTTP API) valida issuer y audience con un JWT Authorizer antes de enrutar.
Cada microservicio vuelve a validar el token de forma independiente (spring.security.oauth2.resourceserver.jwt.issuer-uri), y aplica autorización por rol con @PreAuthorize("hasAnyRole(...)").
El claim roles del JWT se mapea a authorities con prefijo ROLE_. Los roles usados en todo el sistema son, en español: Admin, Operador, Cliente.
Requisitos
Docker y Docker Compose.
Para ejecutar un servicio fuera de Docker: JDK 21 y Maven Wrapper.
App Registration en Azure AD con los scopes/roles configurados (Admin, Operador, Cliente).
Credenciales de prueba de Transbank (ambiente de integración, ya embebidas en WebpayController para sandbox).
Configuración

Variables relevantes por servicio (application.yml):

yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@oracle-pedidos360:1521/XEPDB1
  jpa:
    hibernate:
      ddl-auto: update   # genera el esquema si la tabla no existe aún
  kafka:
    bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:kafka-pedidos360:29092}
  rabbitmq:
    host: ${SPRING_RABBITMQ_HOST:rabbitmq-pedidos360}
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://login.microsoftonline.com/<TENANT_ID>/v2.0
          audiences: <API_CLIENT_ID>
Ejecución con Docker Compose
bash
git clone https://github.com/meninaaa/pedidos360-backend.git
cd pedidos360-backend/infra
docker compose up -d --build

Swagger de cada servicio disponible en http://localhost:<puerto>/swagger-ui.html.

URLs útiles:

BFF: http://localhost:8080
RabbitMQ Management: http://localhost:15672 (guest / guest)
Despliegue en AWS
EC2 (recomendado t3.medium, con Elastic IP asociada para evitar que la integración del API Gateway se rompa en cada reinicio) corriendo el stack completo vía Docker Compose.
AWS API Gateway (HTTP API) con:
Ruta proxy ANY /{proxy+} hacia la EC2, protegida por un JWT Authorizer (issuer y audience de Azure AD).
Ruta específica POST /api/bff/payments/commit, sin Authorizer, para el retorno de Transbank.
CORS configurado nativamente en la API (no como ruta OPTIONS manual), ya que el preflight del navegador nunca lleva JWT y el Authorizer lo rechazaría.
Estructura del repositorio
.
├── infra/
│   └── docker-compose.yml
├── ms-pedidos360-bff/
├── ms-pedidos360-orders/
├── ms-pedidos360-catalog/
├── ms-pedidos360-notify/
├── ms-pedidos360-audit/
└── ms-pedidos360-report/
Observaciones del estado actual
La topología de RabbitMQ definida en el caso contempla tres flujos de comando (email, kitchen, invoice) con sus respectivas DLQ; actualmente solo el flujo de email tiene productor y consumidor activos en el código. kitchen e invoice están documentados en el diseño original pero no implementados aún.
ms-pedidos360-orders publica a audit.timeline directamente desde el mismo método que publica a orders.events, en vez de que audit derive ese tópico a partir de orders.events. Funcionalmente es equivalente para el alcance actual, pero difiere del diagrama original del caso, que mostraba a audit y report consumiendo ambos del mismo tópico único.
El cálculo de lead time en report-service usa los timestamps reales de los eventos recibidos; en datos de prueba generados sin demora entre transiciones de estado, el resultado puede mostrarse como 0.0 horas — es un artefacto de los datos sintéticos, no un error de cálculo.
La identificación del "dueño" de un pedido para el endpoint /orders/customer se hace comparando el claim name/email del JWT contra el campo customerId (texto libre), no contra un userId estable de Azure AD. Es suficiente para el alcance del caso, pero no es robusto ante usuarios con el mismo nombre.
