# BTP Ktor with PostgreSQL and XSUAA

This project is a production-ready Ktor application that demonstrates secure REST API development with PostgreSQL database integration and SAP BTP XSUAA authentication. It's designed for deployment on the SAP Business Technology Platform (BTP) Cloud Foundry environment while maintaining local development capabilities.

## Table of Contents
- [Architecture Overview](#architecture-overview)
- [Features](#features)
- [API Endpoints](#api-endpoints)
- [Authentication & Authorization](#authentication--authorization)
- [Database Integration](#database-integration)
- [Configuration](#configuration)
- [Local Development](#local-development)
- [Deployment](#deployment)
- [Troubleshooting](#troubleshooting)

## Architecture Overview

The application follows a clean architecture with clear separation of concerns:

```
src/main/kotlin/
├── Application.kt          # Application entry point and configuration
├── di/                    # Dependency injection setup
├── routing/               # API route definitions
├── security/              # Authentication and authorization
├── db/                    # Database configuration and migrations
├── repository/            # Data access layer
├── service/               # Business logic
└── dto/                   # Data transfer objects
```

## Features

### Core Features
- **Ktor Framework**: Lightweight, asynchronous web framework for Kotlin
- **PostgreSQL Integration**: Robust relational database with connection pooling
- **XSUAA Authentication**: Secure authentication using SAP BTP's XSUAA service
- **Dependency Injection**: Managed by Koin for clean architecture
- **RESTful API**: Following best practices for resource management
- **JWT Validation**: Secure token validation with proper scopes and authorities

### Technical Stack

| Component | Technology | Purpose |
|-----------|------------|---------|
| **Web Framework** | Ktor 2.x | Asynchronous web framework |
| **Language** | Kotlin 1.8+ | Modern, concise, and safe programming language |
| **Database** | PostgreSQL 13+ | Relational database |
| **ORM** | Exposed | Type-safe SQL DSL and lightweight ORM |
| **Dependency Injection** | Koin | Lightweight dependency injection framework |
| **Authentication** | XSUAA + JWT | SAP BTP's authentication service |
| **Connection Pooling** | HikariCP | High-performance JDBC connection pool |
| **Build Tool** | Gradle (Kotlin DSL) | Build automation and dependency management |

## API Endpoints

All endpoints are prefixed with `/api/v1` and require valid JWT authentication.

### User Management

| Method | Endpoint | Description | Required Scope |
|--------|----------|-------------|----------------|
| `GET`  | `/users` | List all users | `$XSAPPNAME.user` |
| `POST` | `/users` | Create new user | `$XSAPPNAME.admin` |
| `GET`  | `/users/{id}` | Get user by ID | `$XSAPPNAME.user` |
| `PUT`  | `/users/{id}` | Update user | `$XSAPPNAME.admin` |
| `DELETE` | `/users/{id}` | Delete user | `$XSAPPNAME.admin` |

## Authentication & Authorization

The application uses SAP BTP's XSUAA service for authentication and authorization:

### XSUAA Integration
- JWT tokens issued by XSUAA are validated on each request
- Token validation includes signature verification, expiration, and audience checks
- Scopes are extracted from the JWT for fine-grained authorization

### JWT Validation Flow
1. Client obtains JWT from XSUAA
2. Token is sent in the `Authorization: Bearer <token>` header
3. Server validates token using XSUAA's public keys
4. Request is authorized based on token scopes

## Database Integration

### PostgreSQL Connection
- Uses HikariCP for efficient connection pooling
- Supports both Cloud Foundry service binding and local development
- SSL encryption for secure database communication

### Database Schema Management
- Uses Exposed's SchemaUtils for automatic schema creation
- Supports database migrations through Flyway
- Connection pooling configuration for optimal performance

## Configuration

The application is highly configurable through environment variables:

### Required Environment Variables

| Variable | Description | Required | Default |
|----------|-------------|----------|---------|
| `PORT` | Application port | No | 8080 |
| `DB_SERVICE_NAME` | PostgreSQL service name in VCAP_SERVICES | No | `postgreSQL-dev` |
| `PG_SSLMODE` | PostgreSQL SSL mode | No | `verify-full` |
| `XSUAA_SERVICE_NAME` | XSUAA service name in VCAP_SERVICES | No | `xsuaa` |

### Database Configuration

```yaml
# Example local-db.json for development
{
  "hostname": "localhost",
  "port": 5432,
  "dbname": "mydb",
  "username": "user",
  "password": "password",
  "sslmode": "require"
}
```

## Local Development

### Prerequisites
- JDK 17+
- PostgreSQL 13+
- Docker (optional, for running PostgreSQL locally)

### Running with Docker Compose

```bash
docker-compose up -d
```

### Running Tests

```bash
./gradlew test
```

## Deployment

### Cloud Foundry Deployment

1. Build the application:
   ```bash
   ./gradlew build
   ```

2. Deploy to Cloud Foundry:
   ```bash
   cf push
   ```

### Required Services

```bash
# Create PostgreSQL service
cf create-service postgresql-db development postgresql-service

# Create XSUAA service
cf create-service xsuaa application xsuaa-service -c xs-security.json

# Bind services to application
cf bind-service your-app-name postgresql-service
cf bind-service your-app-name xsuaa-service
```

## Troubleshooting

### Common Issues

1. **Database Connection Issues**
   - Verify the database service is running
   - Check connection string and credentials
   - Ensure proper SSL configuration

2. **Authentication Failures**
   - Verify XSUAA service binding
   - Check JWT token validity and scopes
   - Ensure correct XSUAA configuration in `xs-security.json`

3. **Performance Issues**
   - Review HikariCP connection pool settings
   - Check database query performance
   - Monitor application metrics

### Logging

Application logs can be viewed using:
```bash
cf logs your-app-name --recent
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

| Variable | Description | Default |
|---|---|---|
| `DB_POOL_MAX` | Maximum number of connections in the pool. | `10` |
| `DB_POOL_MIN` | Minimum number of idle connections. | `2` |

## Building and Running

### Cloud Foundry Deployment

1.  **Build the application:**

    ```bash
    ./gradlew buildFatJar
    ```

2.  **Deploy to Cloud Foundry:**

    Make sure you have a PostgreSQL service instance created and named `postgreSQL-dev` (or update `manifest.yml` and the `DB_SERVICE_NAME` environment variable).

    ```bash
    cf push
    ```

### Local Development

1.  **Set up local database credentials:**

    Create a `local-db.json` file in the root of the project with your PostgreSQL connection details. You can use `local-db-example.json` as a template.

2.  **Run the application:**

    ```bash
    ./gradlew run
    ```

The server will start on `http://0.0.0.0:8080`.
