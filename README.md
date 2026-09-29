# Pedidos360 - Backend & Microservicios (Spring Boot)

## 1. Resumen Ejecutivo
El backend de **Pedidos360** es un ecosistema de microservicios distribuidos desarrollados en **Spring Boot (Java)**. Está diseñado bajo los principios de alta cohesión, bajo acoplamiento y escalabilidad horizontal. Cuenta con un **BFF (Backend For Frontend)** como puerta de enlace lógica y utiliza una **Arquitectura Asíncrona (Patrón de Comandos y Eventos)** apoyada en Message Brokers (RabbitMQ / Kafka) para garantizar la resiliencia operativa y tiempos de respuesta mínimos.

## 2. Topología de Microservicios

El sistema está fragmentado en dominios de negocio específicos (Domain-Driven Design):

1. **BFF (Backend For Frontend):** Es el orquestador principal. Recibe la petición del frontend (Angular), decodifica y audita el token JWT de Azure AD, extrae los claims (roles, email) de forma segura a través de `ObjectMapper`, y enruta la petición al microservicio correspondiente.
2. **Orders Service (Pedidos):** Núcleo transaccional. Maneja la máquina de estados de los envíos (Creado, Aceptado, En Preparación, Despachado). Actúa como el productor principal de comandos hacia el resto del sistema.
3. **Catalog Service (Catálogo):** Gestiona el inventario, precios y disponibilidad de SKUs.
4. **Notify Service (Notificaciones):** Microservicio reactivo que escucha las colas de comandos para enviar correos electrónicos de forma silenciosa.
5. **Audit Service (Auditoría):** Microservicio pasivo que registra una traza inmutable de quién hizo qué y cuándo.
6. **Reports Service (Reportería):** Agregador de datos que calcula KPIs, tendencias y top de ventas.

## 3. Arquitectura Asíncrona: RabbitMQ y Kafka

Para evitar cuellos de botella y acoplamiento sincrónico, el ecosistema delega las tareas pesadas a un sistema de mensajería. 

### 3.1. El Flujo de Comandos en RabbitMQ
RabbitMQ no solo audita, sino que orquesta físicamente las operaciones del negocio a través de **Task Queues**. Cuando una orden cambia de estado (ej. de Creado a Aceptado), el flujo es el siguiente:

1. **El Productor (orders-pedidos360):** 
   El servicio de órdenes hace su trabajo principal (actualizar la base de datos) e inmediatamente dispara *comandos específicos* a las colas de RabbitMQ (`q.cmd.email`, `q.cmd.invoice`, `q.cmd.kitchen`). Una vez enviados, *Orders* responde `200 OK` al frontend de inmediato, sin hacer esperar al usuario.

2. **Los Consumidores (ej. ms-pedidos360-notify):** 
   Microservicios especializados están escuchando estas colas en segundo plano. 
   * La cola `q.cmd.email` es consumida para avisarle al cliente por correo.
   * La cola `q.cmd.invoice` gatilla la facturación del pedido.
   * La cola `q.cmd.kitchen` notifica a bodega/cocina para que empiecen a preparar.

3. **Tolerancia a Fallos de Grado Empresarial (DLQ):**
   El sistema está diseñado para no perder datos. Cada cola cuenta con su respectiva **Dead Letter Queue** (ej. `q.cmd.email.dlq`). Si el servidor de correos se cae y el servicio de notificaciones falla, RabbitMQ intercepta el error y mueve el mensaje a la cola `.dlq`. Ninguna instrucción se pierde; todo queda respaldado para su reprocesamiento automático cuando la red se estabilice.

### 3.2. Kafka (Event Streaming)
Mientras RabbitMQ ejecuta "órdenes directas", Kafka maneja el contexto global. El **Reports Service** y **Audit Service** pueden consumir tópicos de Kafka (ej. `Pedido_Actualizado`) para recalcular métricas en tiempo real (Ventas Totales, Top Productos) y registrar movimientos sin sobrecargar a la base de datos principal de transacciones.

## 4. Persistencia de Datos
Actualmente, el sistema utiliza **H2 Database** (base de datos relacional en memoria) por cada microservicio. Esto garantiza el aislamiento de datos (Data Sovereignty) exigido por el patrón microservicios, asegurando que cada dominio controle sus propias tablas y permite un despliegue súper ágil.

## 5. Despliegue en Infraestructura AWS (Cloud)

El proyecto está diseñado para funcionar nativamente en la nube de Amazon Web Services (AWS):

### 5.1. Despliegue en Instancias EC2
Una vez transferido el artefacto `.jar` a la instancia Linux/Ubuntu, se levantó el servicio en background para aislarlo de la sesión SSH del terminal.

*Regla de Seguridad:* El Security Group de la EC2 debe tener habilitado el Inbound Port TCP `8080`.

### 5.2. Exposición mediante AWS API Gateway
El comodín `{proxy+}` asegura que todas las rutas internas de los microservicios (`/api/bff/orders`, `/api/bff/catalog`) se resuelvan dinámicamente, actuando como un puente transparente entre el frontend en Angular y el backend alojado en el servidor EC2.
