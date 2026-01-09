# Sistema de Mensajería Asíncrona

Sistema de mensajería en tiempo real construido con Spring Boot, que procesa mensajes de forma asíncrona utilizando RabbitMQ como middleware de mensajería, MySQL para validación de líneas autorizadas y MongoDB para persistencia de mensajes procesados.

## Tabla de Contenidos

- [Arquitectura](#arquitectura)
- [Requisitos Previos](#requisitos-previos)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Instalación](#instalación)
- [Configuración](#configuración)
- [Ejecución](#ejecución)
- [Uso](#uso)
- [Endpoints](#endpoints)
- [Tecnologías](#tecnologías)

## Arquitectura

El sistema está compuesto por dos microservicios:

```
┌─────────────┐      ┌──────────────┐      ┌──────────────────┐
│   Cliente   │─────▶│ API Gateway  │─────▶│    RabbitMQ      │
└─────────────┘      │  (Port 8080) │      │   (messages.q)   │
                     │              │      └──────────────────┘
                     │  - REST API  │              │
                     │  - Security  │              │
                     │  - MySQL     │              ▼
                     └──────────────┘      ┌──────────────────┐
                                           │ Message Processor│
                                           │  (Port 8081)     │
                                           │                  │
                                           │  - Consumer      │
                                           │  - Business Logic│
                                           │  - MongoDB       │
                                           └──────────────────┘
```

### Flujo de Procesamiento

1. **API Gateway** recibe peticiones REST con autenticación por API Key
2. Valida que la línea de origen esté registrada en **MySQL**
3. Publica el mensaje en cola de **RabbitMQ** con timestamp
4. **Message Processor** consume la cola asíncronamente
5. Valida regla de negocio (máximo 3 mensajes por destinatario en 24h)
6. Persiste el mensaje en **MongoDB** con tiempo de procesamiento
7. API de consulta permite obtener mensajes por destinatario

## Requisitos Previos

- **Java 17** o superior
- **Maven 3.6+**
- **Docker** y **Docker Compose v2**
- **Git**

### Verificar Instalaciones

```bash
java -version    # Debe mostrar Java 17+
mvn -version     # Debe mostrar Maven 3.6+
docker --version # Debe mostrar Docker
docker compose version # Debe mostrar v2.x
```

## Estructura del Proyecto

```
messaging-system/
├── docker/
│   └── docker-compose.yml      # Infraestructura (MySQL, MongoDB, RabbitMQ)
├── api-gateway/                # Microservicio 1: API REST
│   ├── src/
│   ├── pom.xml
│   └── README.md
├── message-processor/          # Microservicio 2: Procesador
│   ├── src/
│   ├── pom.xml
│   └── README.md
└── README.md                   # Este archivo
```

## Instalación

### 1. Clonar el Repositorio o descomprimir archivo con el proyecto

```bash
git clone https://github.com/BravorAndres/messaging-system.git
cd messaging-system
```

### 2. Levantar Infraestructura

```bash
cd docker
docker compose up -d
```

Esto iniciará:
- **MySQL** en puerto `3306`
- **MongoDB** en puerto `27017`
- **RabbitMQ** en puerto `5672` (Management UI en `15672`)

## Configuración

### Inicializar Base de Datos MySQL

Conectarse a MySQL:

```bash
mysql -h 127.0.0.1 -u root -proot
```

Ejecutar script de inicialización:

```sql
USE messaging_db;

CREATE TABLE IF NOT EXISTS authorized_lines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    line_number VARCHAR(50) UNIQUE NOT NULL,
    active BOOLEAN DEFAULT TRUE
);

INSERT INTO authorized_lines (line_number, active) VALUES
('1234567890', true),
('0987654321', true),
('1111111111', true),
('2222222222', true),
('3333333333', true);
```

Verificar:

```sql
SELECT * FROM authorized_lines;
```

### Verificar RabbitMQ

Acceder a la UI de administración:
- URL: http://localhost:15672
- Usuario: `admin`
- Password: `admin`

## Ejecución

### Terminal 1: API Gateway

```bash
cd api-gateway
mvn clean install
mvn spring-boot:run
```

Espera hasta ver:
```
Started ApiGatewayApplication in X.XXX seconds (JVM running for Y.YYY)
```

### Terminal 2: Message Processor

```bash
cd message-processor
mvn clean install
mvn spring-boot:run
```

Espera hasta ver:
```
Started MessageProcessorApplication in X.XXX seconds (JVM running for Y.YYY)
Listening on queue: messages.queue
```

## Uso

### Enviar Mensaje

```bash
curl -X POST http://localhost:8080/api/messages \
  -H "X-API-Key: my-super-secret-key-123" \
  -H "Content-Type: application/json" \
  -d '{
    "origin": "1234567890",
    "destination": "9999999999",
    "messageType": "TEXTO",
    "content": "Hola mundo"
  }'
```

**Respuesta exitosa:**
```json
{
  "status": "Message queued for processing"
}
```

### Consultar Mensajes por Destinatario

```bash
curl http://localhost:8081/api/messages/destination/9999999999 \
  -H "X-API-Key: my-second-super-secret-key-123"
```

**Respuesta:**
```json
{
  "destination": "9999999999",
  "totalMessages": 1,
  "messages": [
    {
      "id": "67...",
      "origin": "1234567890",
      "destination": "9999999999",
      "messageType": "TEXTO",
      "content": "Hola mundo",
      "processingTime": 45,
      "createdDate": "2026-01-08T20:15:30.123",
      "error": null
    }
  ]
}
```

### Consultar Todos los Mensajes

```bash
curl http://localhost:8081/api/messages \
-H "X-API-Key: my-second-super-secret-key-123"
```

## Endpoints

### API Gateway (Puerto 8080)

| Método | Endpoint | Headers | Descripción |
|--------|----------|---------|-------------|
| POST | `/api/messages` | `X-API-Key: my-super-secret-key-123` | Enviar mensaje |

#### Request Body
```json
{
  "origin": "string",
  "destination": "string",
  "messageType": "TEXTO|IMAGEN|VIDEO|DOCUMENTO",
  "content": "string"
}
```

#### Códigos de Respuesta
- `202 Accepted`: Mensaje encolado correctamente
- `401 Unauthorized`: API Key inválida o faltante
- `403 Forbidden`: Línea de origen no autorizada
- `400 Bad Request`: Datos inválidos

### Message Processor (Puerto 8081)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/messages/destination/{destination}` | Obtener mensajes por destinatario |
| GET | `/api/messages` | Obtener todos los mensajes |

#### Response Body
```json
{
  "id": "string",
  "origin": "string",
  "destination": "string",
  "messageType": "TEXTO|IMAGEN|VIDEO|DOCUMENTO",
  "content": "string",
  "processingTime": 45,
  "createdDate": "2026-01-08T20:15:30.123",
  "error": "string|null"
}
```

## Pruebas de Validación

### 1. Validar Línea No Autorizada

```bash
curl -X POST http://localhost:8080/api/messages \
  -H "X-API-Key: my-super-secret-key-123" \
  -H "Content-Type: application/json" \
  -d '{
    "origin": "9999999999",
    "destination": "1234567890",
    "messageType": "TEXTO",
    "content": "Este debería fallar"
  }'
```

**Resultado esperado:** `403 Forbidden`

### 2. Validar API Key Inválida

```bash
curl -X POST http://localhost:8080/api/messages \
  -H "X-API-Key: clave-incorrecta" \
  -H "Content-Type: application/json" \
  -d '{
    "origin": "1234567890",
    "destination": "9999999999",
    "messageType": "TEXTO",
    "content": "Test"
  }'
```

**Resultado esperado:** `401 Unauthorized`

### 3. Validar Límite de 3 Mensajes en 24h

Enviar 4 mensajes consecutivos al mismo destinatario:

```bash
for i in {1..4}; do
  curl -X POST http://localhost:8080/api/messages \
    -H "X-API-Key: my-super-secret-key-123" \
    -H "Content-Type: application/json" \
    -d "{
      \"origin\": \"1234567890\",
      \"destination\": \"5555555555\",
      \"messageType\": \"TEXTO\",
      \"content\": \"Mensaje $i\"
    }"
  sleep 1
done
```

Consultar mensajes:

```bash
curl http://localhost:8081/api/messages/destination/5555555555
```

**Resultado esperado:** 
- Primeros 3 mensajes: `error: null`
- Cuarto mensaje: `error: "Destination 5555555555 has reached maximum messages (3) in 24h..."`

## Tecnologías

### Backend
- **Java 17**
- **Spring Boot 3.2.x**
- **Spring Data JPA** (MySQL)
- **Spring Data MongoDB**
- **Spring AMQP** (RabbitMQ)
- **Spring Security**
- **Lombok**
- **Maven**

### Infraestructura
- **MySQL 8.0** - Validación de líneas autorizadas
- **MongoDB 7.0** - Persistencia de mensajes procesados
- **RabbitMQ 3.x** - Cola de mensajes asíncrona
- **Docker & Docker Compose** - Contenedores

## 🔍 Monitoreo

### Logs del API Gateway
```bash
cd api-gateway
mvn spring-boot:run
```

### Logs del Message Processor
```bash
cd message-processor
mvn spring-boot:run
```

### RabbitMQ Management UI
- URL: http://localhost:15672
- Monitorea colas, exchanges, mensajes pendientes y throughput

### MongoDB
```bash
mongosh mongodb://localhost:27017/messages_db
> db.processed_messages.find().pretty()
```

### MySQL
```bash
mysql -h 127.0.0.1 -u root -proot messaging_db
mysql> SELECT * FROM authorized_lines;
```

## Troubleshooting

### Error: "Failed to configure a DataSource"
**Solución:** Verifica que MySQL esté corriendo
```bash
docker compose ps mysql
```

### Error: "Connection refused" en RabbitMQ
**Solución:** Verifica que RabbitMQ esté corriendo
```bash
docker compose ps rabbitmq
```

### Error: Mensajes no llegan a MongoDB
**Solución:** Verifica logs del message-processor y que la cola exista en RabbitMQ UI

### Puerto ya en uso
**Solución:** Detén servicios existentes o cambia puertos en `application.yml`

## Detener el Sistema

### Detener Microservicios
Presiona `Ctrl+C` en cada terminal

### Detener Infraestructura
```bash
cd docker
docker compose down
```

### Limpiar Datos (Opcional)
```bash
docker compose down -v  # Elimina volúmenes con datos
```



##  Autor: Andres Bravo

Desarrollado como prueba técnica para Ingeniero de Desarrollo

##  Licencia

Este proyecto es de uso educativo y evaluativo.
