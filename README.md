# BTP Ktor Postgres

This project is a sample Ktor application that connects to a PostgreSQL database. It is designed for deployment on the SAP Business Technology Platform (BTP), Cloud Foundry environment, but it can also be run locally.

- [Ktor Documentation](https://ktor.io/docs/home.html)
- [Ktor GitHub page](https://github.com/ktorio/ktor)
- The [Ktor Slack chat](https://app.slack.com/client/T09229ZC6/C0A974TJ9). You'll need to [request an invite](https://surveys.jetbrains.com/s3/kotlin-slack-sign-up) to join.

## Features

| Name                                                                   | Description                                                                        |
| ------------------------------------------------------------------------|------------------------------------------------------------------------------------ |
| [Koin](https://start.ktor.io/p/koin)                                   | Provides dependency injection                                                      |
| [Authentication](https://start.ktor.io/p/auth)                         | Provides extension point for handling the Authorization header                     |
| [Routing](https://start.ktor.io/p/routing)                             | Provides a structured routing DSL                                                  |
| [Authentication JWT](https://start.ktor.io/p/auth-jwt)                 | Handles JSON Web Token (JWT) bearer authentication scheme                          |
| [Call Logging](https://start.ktor.io/p/call-logging)                   | Logs client requests                                                               |
| [kotlinx.serialization](https://start.ktor.io/p/kotlinx-serialization) | Handles JSON serialization using kotlinx.serialization library                     |
| [Content Negotiation](https://start.ktor.io/p/content-negotiation)     | Provides automatic content conversion according to Content-Type and Accept headers |
| [Exposed](https://start.ktor.io/p/exposed)                             | Adds Exposed database to your application                                          |
| [Postgres](https://start.ktor.io/p/postgres)                           | Adds Postgres database to your application                                         |

## Configuration

The application is configured via environment variables.

### Database

The database connection is configured to work seamlessly on Cloud Foundry by parsing `VCAP_SERVICES`. For local development, it falls back to environment variables.

| Variable | Description | Default | 
|---|---|---|
| `DB_SERVICE_NAME` | The name of the PostgreSQL service instance to look for in `VCAP_SERVICES`. | `postgreSQL-dev` |
| `PG_SSLMODE` | SSL mode for the JDBC connection. Use `verify-full` for strict validation or `require` for basic encryption. | `verify-full` |
| `LOCAL_DB_JSON_PATH` | Path to a local JSON file containing database credentials. See `local-db-example.json`. | `local-db.json` |

### Connection Pool (HikariCP)

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
