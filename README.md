# MSLA Challenge - Exchange Rate API

API RESTful para conversión de monedas con autenticación básica, historial de transacciones y funcionalidades de resumen.

## 📋 Descripción

Este proyecto implementa un microservicio para conversión de monedas que:

- Convierte cantidades entre diferentes monedas usando tipos de cambio externos
- Mantiene un historial de todas las conversiones por usuario
- Proporciona resúmenes de conversiones agrupadas por moneda destino
- Incluye autenticación básica con Spring Security
- Utiliza arquitectura en N capas con Spring Boot
- Implementa contract-first con OpenAPI 3.0

## 🏗️ Arquitectura

### Tecnologías utilizadas

- **Java 17**
- **Spring Boot 3.2.8**
- **Spring Cloud 2023.0.2**
- **Spring Security** (Autenticación básica)
- **Spring Data JPA** (Persistencia)
- **Spring Cloud OpenFeign** (Cliente HTTP)
- **PostgreSQL 15** (Base de datos)
- **Docker & Docker Compose** (Containerización)
- **OpenAPI 3.0** (Documentación de API)
- **Maven** (Gestión de dependencias)
- **H2** (Base de datos en memoria para tests)

### Estructura del proyecto

```
src/
├── main/
│   ├── java/msla/challenge/mslachallenge/
│   │   ├── client/          # Feign clients
│   │   ├── config/          # Configuraciones
│   │   ├── controller/      # Controladores REST
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── entity/         # Entidades JPA
│   │   ├── repository/     # Repositorios
│   │   └── service/        # Servicios de negocio
│   └── resources/
│       ├── api/            # Especificaciones OpenAPI
│       └── application.properties
└── test/                   # Tests unitarios
    └── resources/
        └── application-test.properties  # Configuración H2 para tests
```

## 🚀 Instalación y ejecución

### Prerrequisitos

- Java 17+
- Maven 3.6+
- Docker & Docker Compose
- Git

### 1. Clonar el repositorio

```bash
git clone <repository-url>
cd msla-challenge
```

### 2. Ejecutar con Docker Compose

```bash
# Construir y ejecutar todos los servicios
docker-compose up --build

# Ejecutar en segundo plano
docker-compose up -d --build
```

### 3. Verificar la instalación

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- PostgreSQL: localhost:5432
- Health Check: http://localhost:8080/actuator/health

### 4. Ejecutar solo localmente (desarrollo)

```bash
# Ejecutar solo PostgreSQL
docker-compose up postgresql -d

# Configurar credenciales locales en application.properties
# spring.datasource.username=postgres
# spring.datasource.password=password

# Ejecutar la aplicación localmente
mvn spring-boot:run
```

## 🔑 Autenticación

El API utiliza autenticación básica HTTP. Credenciales por defecto:

- **Usuario**: `admin`
- **Contraseña**: `password`

## 📚 Endpoints del API

### Base URL: `http://localhost:8080/api/v1/exchange`

### 1. Convertir Moneda

```http
POST /convert
Content-Type: application/json
Authorization: Basic YWRtaW46cGFzc3dvcmQ=

{
    "from": "USD",
    "to": "PEN",
    "amount": 100.00,
    "userId": 1
}
```

**Respuesta:**

```json
{
  "id": 1,
  "date": "2025-01-15T10:30:00Z",
  "from": "USD",
  "to": "PEN",
  "originalAmount": 100.0,
  "convertedAmount": 373.92,
  "exchangeRate": 3.739165,
  "userId": 1
}
```

### 2. Obtener Historial de Conversiones

```http
GET /history?userId=1&startDate=2025-01-01&endDate=2025-12-31
Authorization: Basic YWRtaW46cGFzc3dvcmQ=
```

### 3. Obtener Resumen por Moneda

```http
GET /summary?userId=1&startDate=2025-01-01&endDate=2025-12-31
Authorization: Basic YWRtaW46cGFzc3dvcmQ=
```

## 🧪 Pruebas

### Ejecutar tests unitarios

```bash
mvn test
```

Los tests utilizan H2 en memoria configurado en `application-test.properties` para evitar dependencia de PostgreSQL.

### Colección de Postman

Importar el archivo `postman/MSLA_Challenge_Collection.json` en Postman para realizar pruebas completas del API con tests automatizados.

### Ejemplos de uso con curl

#### Convertir USD a PEN

```bash
curl -X POST "http://localhost:8080/api/v1/exchange/convert" \
  -H "Content-Type: application/json" \
  -u admin:password \
  -d '{
    "from": "USD",
    "to": "PEN",
    "amount": 100.00,
    "userId": 1
  }'
```

#### Obtener historial

```bash
curl -X GET "http://localhost:8080/api/v1/exchange/history?userId=1" \
  -u admin:password
```

## 🗄️ Base de Datos

### Configuración PostgreSQL

**Docker Compose:**

- Base de datos: `msla-challenge`
- Usuario: `msla_user`
- Contraseña: `msla_password`
- Puerto: `5432`

**Desarrollo local:**

- Usuario: `postgres`
- Contraseña: `password`

### Esquema: `exchange`

#### Tabla: `users`

- `id` (BIGSERIAL PRIMARY KEY)
- `username` (VARCHAR UNIQUE)
- `password` (VARCHAR)
- `email` (VARCHAR)
- `first_name` (VARCHAR)
- `last_name` (VARCHAR)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)
- `is_active` (BOOLEAN)

#### Tabla: `exchange_history`

- `id` (BIGSERIAL PRIMARY KEY)
- `conversion_date` (TIMESTAMP)
- `from_currency` (VARCHAR(3))
- `to_currency` (VARCHAR(3))
- `original_amount` (NUMERIC(19,4))
- `converted_amount` (NUMERIC(19,4))
- `exchange_rate` (NUMERIC(10,6))
- `created_at` (TIMESTAMP)
- `user_id` (BIGINT FOREIGN KEY)

### Datos iniciales

El sistema crea automáticamente usuarios de prueba a través de `DataLoader`:

- Usuario ID 1: `admin` / `password`
- Usuario ID 2: `testuser` / `testpass`

## 🔧 Configuración

### Variables de entorno Docker Compose

```env
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://postgresql:5432/msla-challenge
SPRING_DATASOURCE_USERNAME=msla_user
SPRING_DATASOURCE_PASSWORD=msla_password
SPRING_JPA_HIBERNATE_DDL_AUTO=update

# External API
EXTERNAL_API_BASE_URL=https://api.apilayer.com/exchangerates_data
EXTERNAL_API_KEY=mock-api-key-for-development
```

### Configuración de desarrollo local

```env
# Database (application.properties)
spring.datasource.url=jdbc:postgresql://localhost:5432/msla-challenge
spring.datasource.username=postgres
spring.datasource.password=password

# External API
external.api.base-url=https://api.apilayer.com/exchangerates_data
external.api.key=JTFHpgzUeKBvL9TFwcSblmyIyPXe4WIg
```

### Tipos de cambio mockeados

Para desarrollo se utilizan tipos de cambio fijos implementados en `ExchangeRateClient`:

- USD → PEN: 3.739165
- PEN → USD: 0.267427
- USD → EUR: 0.85
- EUR → USD: 1.18

## 📖 Documentación OpenAPI

### Swagger UI

Acceder a la documentación interactiva en:
http://localhost:8080/swagger-ui.html

### Especificación OpenAPI

- YAML: `src/main/resources/api/exchange-api.yaml`
- JSON: http://localhost:8080/api-docs

## 🐳 Docker

### Servicios incluidos

- **PostgreSQL 15**: Base de datos principal con health check
- **Spring Boot App**: API del microservicio con health check

### Health Checks configurados

- **PostgreSQL**: Verifica conexión con `pg_isready`
- **API**: Verifica endpoint `/actuator/health`

### Comandos útiles

```bash
# Ver logs
docker-compose logs -f

# Ver logs solo de la aplicación
docker-compose logs -f msla-challenge-app

# Parar servicios
docker-compose down

# Limpiar volúmenes
docker-compose down -v

# Reconstruir solo la app
docker-compose up --build msla-challenge-app

# Verificar health checks
docker-compose ps
```

## 🔍 Monitoreo y Health Checks

### Health Check endpoint

```http
GET /actuator/health
```

### Logs de aplicación

```bash
# Ver logs en tiempo real
docker-compose logs -f msla-challenge-app

# Ver logs de PostgreSQL
docker-compose logs -f postgresql
```

## ⚠️ Consideraciones de Producción

Para un entorno de producción considerar:

1. **Seguridad**:

   - Usar JWT en lugar de autenticación básica
   - HTTPS obligatorio
   - Variables de entorno para credenciales

2. **API Externa**:

   - Configurar API key real de apilayer.com
   - Implementar circuit breaker
   - Cache para tipos de cambio

3. **Base de datos**:

   - Configuración de pool de conexiones
   - Backups automáticos
   - Réplicas de lectura

4. **Escalabilidad**:
   - Load balancer
   - Múltiples instancias
   - Redis para sesiones

## 📝 Notas adicionales

- Los tipos de cambio están mockeados en `ExchangeRateClient` para desarrollo
- El sistema usa autenticación en memoria para simplificar las pruebas
- Los timestamps se manejan en formato ISO 8601
- Las monedas usan códigos ISO 4217
- Los tests usan H2 en memoria con perfil "test"
- OpenAPI Generator genera código cliente y servidor automáticamente
