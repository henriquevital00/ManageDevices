# ManageDevices API

A robust RESTful API for managing devices with comprehensive features including CRUD operations, optimistic locking, cursor-based pagination, full-text search, and complete audit history tracking.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Testing](#testing)
- [Docker Deployment](#docker-deployment)
- [API Endpoints](#api-endpoints)
- [Business Rules](#business-rules)
- [Development](#development)
- [Contributing](#contributing)

---

## 🎯 Overview

ManageDevices is a production-ready Spring Boot application that provides a complete device management system. It follows **Hexagonal Architecture** (Ports and Adapters) and adheres to **SOLID principles**, ensuring clean, maintainable, and testable code.

The application manages devices with three states (AVAILABLE, IN_USE, INACTIVE) and maintains a complete audit trail of all operations through a separate history table.

---

## ✨ Features

### Core Functionality
- ✅ **Full CRUD Operations** - Create, Read, Update, Delete devices
- ✅ **Device State Management** - AVAILABLE, IN_USE, INACTIVE states
- ✅ **Optimistic Locking** - Prevents concurrent modification conflicts using version field
- ✅ **Audit History** - Complete tracking of all device operations (CREATE, UPDATE, DELETE)
- ✅ **Full-Text Search** - PostgreSQL trigram-based search on device names and brands
- ✅ **Cursor-based Pagination** - Efficient pagination ordered by creation time
- ✅ **Input Validation** - Comprehensive request validation with detailed error messages
- ✅ **Global Exception Handling** - Consistent error responses across the API

### Technical Features
- ✅ **Hexagonal Architecture** - Clean separation of concerns (Domain, Application, Infrastructure)
- ✅ **Database Migrations** - Flyway for versioned database changes
- ✅ **API Documentation** - OpenAPI/Swagger UI with comprehensive endpoint documentation
- ✅ **Health Checks** - Spring Boot Actuator endpoints
- ✅ **Docker Support** - Multi-stage build with PostgreSQL setup
- ✅ **Comprehensive Testing** - Unit and integration tests with high coverage

---

## 🏗️ Architecture

The application follows **Hexagonal Architecture** (Ports and Adapters):

```
┌─────────────────────────────────────────────────────────────┐
│                    REST API Layer                            │
│              (ManageDevicesController)                       │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                 Application Layer                            │
│     (Use Cases: Create, Update, Delete, Get, List)          │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                   Domain Layer                               │
│      (Device, DeviceFilter, Enums, Exceptions)              │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│               Infrastructure Layer                           │
│  (Repository Adapters, JPA Entities, Database)              │
└─────────────────────────────────────────────────────────────┘
```

### Layers

1. **Domain Layer** (`org.example.domain`)
   - Core business entities (Device)
   - Value objects (DeviceFilter, CursorPage)
   - Enums (DeviceStateEnum, OperationTypeEnum)
   - Domain exceptions

2. **Application Layer** (`org.example.app`)
   - Use case implementations
   - Port interfaces (in/out)
   - Business logic orchestration

3. **Infrastructure Layer** (`org.example.infra`)
   - REST controllers
   - JPA repositories
   - Database entities
   - Adapters
   - Configuration

---

## 🛠️ Tech Stack

### Backend
- **Java 21** - Latest LTS version
- **Spring Boot 3.2.9** - Application framework
- **Spring Data JPA** - Database access
- **Hibernate** - ORM implementation
- **PostgreSQL 16** - Primary database
- **Flyway** - Database migration tool

### Documentation & API
- **SpringDoc OpenAPI 3** - API documentation
- **Swagger UI** - Interactive API explorer

### Testing
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework
- **AssertJ** - Fluent assertions
- **Spring Boot Test** - Integration testing

### Build & Deployment
- **Maven** - Build tool
- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration

---

## 🚀 Getting Started

### Prerequisites

- **Java 21** or higher
- **Maven 3.9+**
- **PostgreSQL 16** (or use Docker)
- **Docker** (optional, for containerized deployment)

### Database Setup

#### Option 1: Docker (Recommended)

```bash
docker compose up -d
```

This starts both PostgreSQL and the application.

#### Option 2: Local PostgreSQL

1. Create database:
```sql
CREATE DATABASE managedevices;
CREATE USER managedevices WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE managedevices TO managedevices;
```

2. Set environment variables:
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/managedevices
export SPRING_DATASOURCE_USERNAME=managedevices
export SPRING_DATASOURCE_PASSWORD=your_password
export SERVER_PORT=8080
```

### Running the Application

#### Using Maven
```bash
# Build the project
mvn clean package

# Run the application
mvn spring-boot:run
```

#### Using Docker
```bash
# Build and start
docker compose up --build -d

# View logs
docker compose logs -f app

# Stop
docker compose down
```

#### Using JAR
```bash
# Build
mvn clean package -DskipTests

# Run
java -jar target/ManageDevices-1.0-SNAPSHOT.jar
```

### Verify Installation

```bash
# Check health
curl http://localhost:8080/actuator/health

# Expected response:
{"status":"UP"}
```

---

## 📚 API Documentation

Once the application is running, access the interactive API documentation:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v1/api-docs

The Swagger UI provides:
- Complete API documentation
- Interactive endpoint testing
- Request/response examples
- Schema definitions

---

## 🗄️ Database Schema

### Device Table
```sql
CREATE TABLE manage_devices.device (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255) NOT NULL,
    brand           VARCHAR(255) NOT NULL,
    state           VARCHAR(50) NOT NULL,
    creation_time   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version         BIGINT NOT NULL DEFAULT 0
);
```

### Device History Table
```sql
CREATE TABLE manage_devices.device_history (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id       UUID NOT NULL,
    name            VARCHAR(255) NOT NULL,
    brand           VARCHAR(255) NOT NULL,
    state           VARCHAR(50) NOT NULL,
    operation_type  VARCHAR(50) NOT NULL,
    operation_time  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### Indexes
- **Primary keys**: Both tables
- **Trigram index**: `device.name` (for full-text search)
- **Creation time index**: `device.creation_time` (for pagination)

---

## 🧪 Testing

The project has comprehensive test coverage with **50+ unit and integration tests**.

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=DeviceRepositoryAdapterTest
mvn test -Dtest=ManageDevicesControllerTest
```

### Run with Coverage
```bash
mvn clean verify
```

### Test Categories

1. **Unit Tests** - Use case implementations, adapters, domain logic
2. **Integration Tests** - Controller endpoints with MockMvc
3. **Repository Tests** - Database operations and queries

### Test Coverage

- ✅ **CreateDeviceImpl** - 10 tests
- ✅ **UpdateDeviceImpl** - 15 tests
- ✅ **DeleteDeviceImpl** - 12 tests
- ✅ **GetDeviceByIdImpl** - 8 tests
- ✅ **ListDevicesImpl** - 8 tests
- ✅ **DeviceRepositoryAdapter** - 50 tests
- ✅ **DeviceHistoryRepositoryAdapter** - 28 tests
- ✅ **ManageDevicesController** - 25 tests
- ✅ **OpenApiConfig** - 38 tests

---

## 🐳 Docker Deployment

### Quick Start

```bash
# Start everything
docker compose up -d

# View logs
docker compose logs -f

# Stop everything
docker compose down

# Clean everything (including data)
docker compose down -v
```

### Environment Variables

Create a `.env` file (optional):
```bash
POSTGRES_USER=managedevices
POSTGRES_PASSWORD=secure_password_change_me
SERVER_PORT=8080
```

### Services

- **PostgreSQL**: Internal port 5432 (not exposed to host)
- **Application**: http://localhost:8080

### Docker Features

✅ Multi-stage build (optimized image size ~400MB)  
✅ Non-root user for security  
✅ Health checks for both services  
✅ Persistent volume for database data  
✅ Automatic dependency management  
✅ Flyway migrations run on startup  

---

## 🔌 API Endpoints

### Device Management

#### Create Device
```http
POST /v1/devices
Content-Type: application/json

{
  "name": "iPhone 15 Pro",
  "brand": "Apple",
  "state": "AVAILABLE"
}
```

**Response**: `201 Created`
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "name": "iPhone 15 Pro",
  "brand": "APPLE",
  "state": "AVAILABLE",
  "creationTime": "2026-01-19T10:30:00",
  "version": 0
}
```

#### List Devices (with search and filters)
```http
GET /v1/devices?search=iphone&state=AVAILABLE&size=20
```

**Query Parameters**:
- `search` - Full-text search on name and brand
- `brand` - Filter by brand (exact match)
- `state` - Filter by state (AVAILABLE, IN_USE, INACTIVE)
- `cursor` - UUID for pagination
- `size` - Page size (1-100, default 20)

**Response**: `200 OK`
```json
{
  "content": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "name": "iPhone 15 Pro",
      "brand": "APPLE",
      "state": "AVAILABLE",
      "creationTime": "2026-01-19T10:30:00",
      "version": 0
    }
  ],
  "nextCursor": "223e4567-e89b-12d3-a456-426614174001",
  "size": 20,
  "hasNext": true
}
```

#### Get Device by ID
```http
GET /v1/devices/{id}
```

**Response**: `200 OK` or `404 Not Found`

#### Update Device
```http
PUT /v1/devices/{id}
Content-Type: application/json

{
  "name": "iPhone 15 Pro Max",
  "brand": "Apple",
  "state": "IN_USE",
  "version": 0
}
```

**Response**: `200 OK` or `409 Conflict` (optimistic lock or device in use) or `404 Bot Found`

#### Delete Device
```http
DELETE /v1/devices/{id}
```

**Response**: `204 No Content`, `404 Not Found`, or `409 Conflict` (device in use)

---

## 📏 Business Rules

### Device States
- **AVAILABLE** - Device is available for use
- **IN_USE** - Device is currently in use
- **INACTIVE** - Device is inactive/retired

### State Transition Rules

1. **Update Restrictions for IN_USE Devices**
   - Cannot change `name` when device is IN_USE
   - Cannot change `brand` when device is IN_USE
   - Can change `state` to transition out of IN_USE

2. **Delete Restrictions**
   - Cannot delete devices in IN_USE state
   - Returns `409 Conflict` when attempted

### Data Normalization
- **Brand names** are automatically converted to UPPERCASE
- **Search terms** are case-insensitive

### Optimistic Locking
- Uses `version` field to prevent concurrent modifications
- Client must send current version in update request
- Returns `409 Conflict` if version mismatch detected

### Audit History
- All operations (CREATE, UPDATE, DELETE) are logged
- History includes device snapshot and operation type
- History records are immutable (never updated or deleted)

### Pagination
- Devices ordered by `creationTime DESC` (newest first)
- Uses `id` as tiebreaker for consistent ordering
- Cursor-based for efficient large dataset handling

## 📊 Monitoring & Health

### Health Check Endpoint
```http
GET /actuator/health
```

Response:
```json
{
  "status": "UP"
}
```

### Application Info
```http
GET /actuator/info
```
