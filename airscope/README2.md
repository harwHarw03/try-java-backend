# AirScope - Running the Modified Project

This guide explains how to run the AirScope project after all improvements have been applied.

---

## Prerequisites

Ensure you have the following installed:

| Tool | Version | Command |
|------|---------|---------|
| Java | 17+ | `java -version` |
| Maven | 3.5+ | `./mvnw -v` |
| PostgreSQL | 14+ | `pg_isready` |
| AWS CLI | any | `aws --version` |

---

## Quick Start

### 1. Clone/Setup the Project

```bash
cd airscope
```

### 2. Create Environment File

Copy the example environment file and fill in your values:

```bash
cp .env.example .env
```

Edit `.env` with your actual credentials:

```bash
# Required: Generate a strong JWT secret (min 256 bits)
# You can generate one with: openssl rand -base64 32
JWT_SECRET=your_secure_jwt_secret_key_min_32_chars_here

# PostgreSQL credentials
POSTGRES_USER=airscope_user
POSTGRES_PASSWORD=your_secure_password
```

### 3. Start Databases

```bash
# Make scripts executable
chmod +x setup.sh run.sh db.sh

# Start PostgreSQL and DynamoDB Local
./setup.sh
```

This will:
- Start PostgreSQL on port 5432
- Create the `airscope` database
- Start DynamoDB Local on port 8000
- Create the `sensor_data` table automatically

### 4. Run the Application

```bash
./run.sh
```

The application will start on `http://localhost:8080`

---

## Environment Variables

All sensitive configuration has been moved to environment variables. Create a `.env` file based on `.env.example`:

| Variable | Description | Default |
|----------|-------------|---------|
| `POSTGRES_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/airscope` |
| `POSTGRES_USER` | PostgreSQL username | `airscope_user` |
| `POSTGRES_PASSWORD` | PostgreSQL password | `airscope_pass` |
| `JWT_SECRET` | JWT signing key (REQUIRED in production) | dev key |
| `JWT_EXPIRATION` | Access token expiration (ms) | `86400000` (24h) |
| `JWT_REFRESH_EXPIRATION` | Refresh token expiration (ms) | `604800000` (7 days) |
| `AWS_REGION` | AWS region | `ap-northeast-1` |
| `AWS_ACCESS_KEY_ID` | AWS credentials | (empty for local) |
| `AWS_SECRET_ACCESS_KEY` | AWS credentials | (empty for local) |
| `DYNAMODB_LOCAL` | Use DynamoDB Local | `true` |
| `DYNAMODB_LOCAL_ENDPOINT` | DynamoDB Local URL | `http://localhost:8000` |

---

## API Endpoints (v1)

All endpoints are prefixed with `/api/v1`.

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Register new user |
| POST | `/api/v1/auth/login` | Login and get tokens |
| POST | `/api/v1/auth/refresh` | Refresh access token |

### Devices

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/devices` | Register a device |
| GET | `/api/v1/devices` | List my devices |
| PUT | `/api/v1/devices/{id}` | Update device |
| DELETE | `/api/v1/devices/{id}` | Delete device |

### Sensor Data

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/data` | Submit sensor reading |
| GET | `/api/v1/devices/{id}/data` | Get readings |
| GET | `/api/v1/devices/{id}/score` | Get air quality score |
| GET | `/api/v1/devices/{id}/trends` | Get CO2 trend |

### Alerts

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/alerts` | Create alert |
| GET | `/api/v1/alerts/device/{id}` | List device alerts |
| DELETE | `/api/v1/alerts/{id}` | Delete alert |

### Health Check

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/actuator/health` | Application health status |

---

## Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=AirQualityCalculatorTest

# Run with coverage
./mvnw test jacoco:report
```

### Test Classes Added

- `AirQualityCalculatorTest` - Algorithm tests
- `DeviceServiceTest` - Device service unit tests
- `AuthServiceTest` - Auth service unit tests
- `JwtUtilTest` - JWT utility tests
- `DeviceControllerTest` - Controller integration tests

---

## Database Commands

```bash
# List users
./db.sh users

# List devices
./db.sh devices

# List alerts
./db.sh alerts

# Scan DynamoDB sensor data
./db.sh dynamo

# Reset databases
./db.sh reset

# Open PostgreSQL shell
./db.sh psql
```

---

## Rate Limiting

Rate limiting is enabled by default:

| Endpoint Pattern | Limit |
|-----------------|-------|
| `/api/v1/auth/**` | 10 requests/minute |
| All other endpoints | 60 requests/minute |

When exceeded, returns `429 Too Many Requests`.

---

## New Features in This Version

1. **Refresh Tokens** - Access tokens expire in 24h, refresh tokens in 7 days
2. **Input Validation** - All sensor values have min/max constraints
3. **Alert Evaluation** - Background job checks thresholds every 5 minutes
4. **Device Management** - Full CRUD (Create, Read, Update, Delete)
5. **Health Checks** - `/actuator/health` endpoint
6. **API Versioning** - All endpoints prefixed with `/api/v1`

---

## Troubleshooting

### Application won't start

1. Check PostgreSQL is running:
   ```bash
   pg_isready -h localhost -p 5432
   ```

2. Check DynamoDB Local is running:
   ```bash
   curl http://localhost:8000
   ```

3. Verify database exists:
   ```bash
   ./db.sh psql -c "\l"
   ```

### Tests failing

1. Ensure databases are running
2. Check `.env` file exists
3. Try cleaning and rebuilding:
   ```bash
   ./mvnw clean compile
   ```

### 401 Unauthorized errors

1. Token expired - use `/api/v1/auth/refresh` to get new tokens
2. Check `Authorization: Bearer <token>` header format

---

## Development

### Build the project

```bash
./mvnw clean package
```

### Run without tests

```bash
./mvnw package -DskipTests
```

### View Swagger UI

Open in browser: `http://localhost:8080/swagger-ui.html`

---

## Production Checklist

Before deploying to production:

- [ ] Set strong `JWT_SECRET` (min 256 bits)
- [ ] Configure real PostgreSQL credentials
- [ ] Configure real AWS credentials for DynamoDB
- [ ] Set `DYNAMODB_LOCAL=false`
- [ ] Enable HTTPS
- [ ] Configure rate limits for production traffic
- [ ] Set up monitoring (Prometheus, Grafana)
