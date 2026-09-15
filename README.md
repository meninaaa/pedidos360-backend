# Pedidos360 - Backend & BFF (Spring Boot)

Sistema central y arquitectura de microservicios desarrollada en **Spring Boot**. Este repositorio contiene la lógica de negocio modularizada, la persistencia de datos y el **BFF (Backend For Frontend)**, el cual actúa como orquestador y punto de entrada seguro para la aplicación cliente, facilitando la integración con la infraestructura cloud.

---

## Características Principales

* **Arquitectura Backend For Frontend (BFF):** Centraliza y optimiza las peticiones del cliente (Angular), reduciendo el acoplamiento y manejando la comunicación interna con los microservicios subyacentes (Orders, Catalog, Audit, Reports).
* **Seguridad y Procesamiento JWT:** Implementación de decodificación y validación de tokens JWT en la capa de controladores del BFF. Extracción segura de claims y roles corporativos provenientes de Azure AD/MSAL.
* **Enrutamiento Basado en Roles (RBAC):** Resolución dinámica de endpoints dependiendo de los privilegios del usuario autenticado:
    * **Admin:** Acceso a `/api/orders` (Gestión total).
    * **Operator:** Acceso a `/api/orders/pending` (Gestión operativa).
    * **Customer:** Acceso a `/api/orders/me` (Aislamiento de datos por cliente).
* **Persistencia y Sincronización:** Uso de base de datos relacional H2 (en memoria) para el almacenamiento eficiente de registros logísticos, ideal para entornos de desarrollo y pruebas de concepto rápidas.

---

## Arquitectura y Componentes Técnicos

* **Framework Principal:** Spring Boot (Java).
* **Gestión de Dependencias y Build:** Maven (Wrapper incluido).
* **Contenedores y Cloud:** Preparado para ejecución nativa en instancias de **AWS EC2** y exposición a través de **AWS API Gateway**.
* **Documentación de API:** Integración nativa con `springdoc-openapi` para la generación de contratos Swagger.

---

## Documentación de API y Swagger

El BFF expone los contratos de comunicación mediante **Swagger / OpenAPI**. Esta documentación interactiva permite a los desarrolladores del frontend y a los integradores visualizar los endpoints disponibles, los esquemas de petición/respuesta y los requisitos de autorización (Bearer Token).

Para acceder a la consola interactiva de Swagger:

> **Entorno Local:** `http://localhost:8080/swagger-ui.html`
> 
> **Entorno de Producción (AWS EC2):** `https://3lgyldt561.execute-api.us-east-1.amazonaws.com/swagger-ui/index.html`

*Nota de infraestructura:* Asegúrate de que el Security Group de la instancia EC2 tenga habilitado el tráfico de entrada (Inbound Rules) en el puerto TCP `8080` para permitir la visualización pública de esta interfaz.

---

## Prerrequisitos de Entorno

Para compilar y ejecutar este proyecto de forma local, se requiere:

* **Java Development Kit (JDK):** Versión 17 o superior.
* **Git:** Para el control de versiones.
* *(Nota: No es necesario tener Maven instalado globalmente, el proyecto incluye el wrapper `./mvnw`)*.

---

## Configuración y Ejecución Local

1. **Clonar el repositorio:**
   ```bash
   git clone [https://github.com/meninaaa/pedidos360-backend.git](https://github.com/meninaaa/pedidos360-backend.git)
   cd pedidos360-backend
Compilar el proyecto y descargar dependencias:

Bash
./mvnw clean install
Ejecutar la aplicación Spring Boot:

Bash
./mvnw spring-boot:run
El servicio iniciará y estará disponible para recibir peticiones en el puerto 8080.

Despliegue en AWS (EC2 y API Gateway)
El sistema está diseñado para integrarse fácilmente en el ecosistema de Amazon Web Services. Sigue estos pasos para un despliegue estándar en producción:

1. Empaquetado de Producción
Genera el archivo ejecutable unificado (.jar) que contiene el servidor web embebido:

Bash
./mvnw clean package -DskipTests
2. Despliegue en AWS EC2
Transfiere el archivo empaquetado (ubicado en la carpeta target/) a tu instancia de AWS EC2. Para mantener el servicio en ejecución en segundo plano incluso al cerrar la sesión SSH, conéctate a tu instancia y ejecuta:

Bash
nohup java -jar target/pedidos360-backend.jar > app.log 2>&1 &
