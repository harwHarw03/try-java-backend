# 🌬️ AirScope

IoT Environmental Data Backend — built with Java Spring Boot.

Collects and analyzes air quality data (temperature, humidity, CO₂, PM2.5) from IoT devices.

---

## 📋 Prerequisites

| Tool | Version | Install |
|------|---------|---------|
| Java | 17+ | `brew install openjdk@17` |
| Maven | 3.5+ | bundled via `./mvnw` |
| PostgreSQL | 14+ | `brew install postgresql` |
| AWS CLI | any | `brew install awscli` (for DynamoDB Local) |

---

## 🚀 Quick Start

```bash
# 1. Setup the databases (first time only)
chmod +x setup.sh run.sh db.sh
./setup.sh

# 2. Create .env file from example
cp .env.example .env

# 3. Start the application
./run.sh

# 4. Open Swagger UI in your browser
open http://localhost:8080/swagger-ui.html
```

---

## 📁 Project Structure

```
src/main/java/com/airscope/
├── controller/          # HTTP layer — handles requests and responses
│   ├── AuthController.java
│   ├── DeviceController.java
│   ├── AlertController.java
│   └── SensorDataController.java
│
├── service/             # Business logic layer
│   ├── AuthService.java
│   ├── DeviceService.java
│   ├── AlertService.java
│   └── SensorDataService.java
│
├── repository/          # PostgreSQL database access (JPA)
│   ├── UserRepository.java
│   ├── DeviceRepository.java
│   └── AlertRepository.java
│
├── dynamodb/            # DynamoDB database access
│   ├── SensorData.java
│   └── SensorDataRepository.java
│
├── model/               # PostgreSQL entities
│   ├── User.java
│   ├── Device.java
│   └── Alert.java
│
├── dto/                 # API request/response shapes
│   ├── AuthDto.java
│   ├── DeviceDto.java
│   ├── AlertDto.java
│   ├── SensorDataDto.java
│   └── ErrorResponse.java
│
├── security/            # JWT authentication
│   ├── JwtUtil.java
│   ├── JwtAuthFilter.java
│   └── UserDetailsServiceImpl.java
│
├── config/              # Spring configuration
│   ├── SecurityConfig.java
│   ├── DynamoDbConfig.java
│   └── SwaggerConfig.java
│
└── util/                # Utilities and algorithms
    ├── AirQualityCalculator.java
    ├── AppExceptions.java
    └── GlobalExceptionHandler.java
```

---

## 🔌 API Reference

> **API Version**: All endpoints are prefixed with `/api/v1`

### Authentication

#### Register
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "mypassword123"
}
```
Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "user@example.com",
  "role": "ROLE_USER",
  "message": "Registration successful"
}
```

#### Login
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "mypassword123"
}
```

#### Refresh Token
```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

---

### Devices
> All device endpoints require: `Authorization: Bearer <your-token>`

#### Register a device
```http
POST /api/v1/devices
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Living Room Sensor"
}
```
Response:
```json
{
  "id": 1,
  "name": "Living Room Sensor",
  "userId": 3
}
```

#### List my devices
```http
GET /api/v1/devices
Authorization: Bearer <token>
```

#### Update a device
```http
PUT /api/v1/devices/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Updated Device Name"
}
```

#### Delete a device
```http
DELETE /api/v1/devices/{id}
Authorization: Bearer <token>
```

---

### Sensor Data

#### Submit a reading
```http
POST /api/v1/data
Authorization: Bearer <token>
Content-Type: application/json

{
  "deviceId": "1",
  "temperature": 22.5,
  "humidity": 55.0,
  "co2": 850.0,
  "pm25": 12.0
}
```

#### Get recent readings
```http
GET /api/v1/devices/1/data?limit=50
Authorization: Bearer <token>
```

#### Get air quality score
```http
GET /api/v1/devices/1/score
Authorization: Bearer <token>
```
Response:
```json
{
  "deviceId": "1",
  "score": 74.5,
  "category": "Moderate",
  "explanation": "Score: 74.5/100. CO2 is elevated (950.0 ppm)."
}
```

#### Get CO2 trend
```http
GET /api/v1/devices/1/trends
Authorization: Bearer <token>
```
Response:
```json
{
  "deviceId": "1",
  "metric": "CO2",
  "trend": "INCREASING",
  "averageValue": 920.5,
  "latestValue": 1050.0,
  "message": "CO2 levels are rising. Consider ventilating the room."
}
```

---

### Alerts

#### Create an alert
```http
POST /api/v1/alerts
Authorization: Bearer <token>
Content-Type: application/json

{
  "type": "CO2",
  "threshold": 1000.0,
  "deviceId": 1
}
```

#### List alerts for a device
```http
GET /api/v1/alerts/device/1
Authorization: Bearer <token>
```

#### Delete an alert
```http
DELETE /api/v1/alerts/{id}
Authorization: Bearer <token>
```

---

### Health Check

```http
GET /actuator/health
```

## 🧮 Algorithms

### Air Quality Score (0–100)
```
score = 100
      - (pm25 × 1.5)
      - (co2 / 50)
      - humidity penalty (if outside 40–60%)

Categories:
  80–100 → Good
  60–79  → Moderate
  40–59  → Poor
  0–39   → Hazardous
```

### Trend Detection
Splits recent readings into two halves (older vs newer) and compares averages:
- Change > +5% → INCREASING
- Change < -5% → DECREASING
- Otherwise   → STABLE

---

## 🗄️ Database Schema

### PostgreSQL
```sql
users   (id, email, password, role)
devices (id, name, user_id)
alerts  (id, type, threshold, device_id)
```

### DynamoDB — `sensor_data`
```
Partition Key: deviceId  (String)
Sort Key:      timestamp (String, ISO 8601)
Attributes:    temperature, humidity, co2, pm25
```

---

## 🛠️ DB Utility Commands

```bash
./db.sh users    # list all users
./db.sh devices  # list all devices
./db.sh alerts   # list all alerts
./db.sh dynamo   # scan DynamoDB sensor_data
./db.sh reset    # wipe and recreate the DB
./db.sh psql     # open interactive psql shell
```

---

## 🧪 Running Tests

```bash
./mvnw test
```

---

## ⚙️ Configuration

Edit `src/main/resources/application.properties`:

| Property | Description |
|----------|-------------|
| `spring.datasource.url` | PostgreSQL connection string |
| `jwt.secret` | Secret key for signing JWTs (change in production!) |
| `jwt.expiration` | Token lifetime in ms (default: 86400000 = 24h) |
| `aws.region` | AWS region |
| `aws.dynamodb.local` | `true` for DynamoDB Local, `false` for real AWS |

---

## 🔐 Security Notes

- Passwords are hashed with **BCrypt** — never stored in plain text
- JWT tokens expire after 24 hours by default
- All endpoints except `/auth/**` require a valid JWT
- Users can only access their **own** devices and data
