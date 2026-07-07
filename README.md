# Garagesystems

Thread-safe in-memory garage parking API built with Java 21, Spring Boot, and Maven.

## Tech Stack

- Java 21 (LTS)
- Spring Boot 3.5.x
- Maven Wrapper
- JUnit 5 + Mockito
- JaCoCo coverage reporting

## Architecture Summary

- **Controller layer**: HTTP API contract (`GarageController`)
- **Service layer**: business rules + slot allocation (`GarageServiceImpl`)
- **Repository layer**: concurrent in-memory persistence (`InMemoryGarageRepository`)
- **Exception layer**: centralized API error mapping (`GlobalExceptionHandler`)

Detailed architecture: [`ARCHITECTURE.md`](ARCHITECTURE.md)

## Run

```bash
./mvnw spring-boot:run
```

Default port: `8080`

## Test

```bash
./mvnw test
```

Coverage report:

- HTML: `target/site/jacoco/index.html`
- CSV: `target/site/jacoco/jacoco.csv`

## Configuration

`application.properties`:

- `garage.capacity` (fixed Vodafone case value: `10`)

Example:

```properties
garage.capacity=10
```

## API Overview

Base path: `/garage`

- `POST /park`
- `DELETE /remove/{plate}`
- `GET /status`
- `GET /vehicles`
- `GET /find/{plate}`
- `GET /available`
- `GET /occupied`

Detailed endpoint documentation: [`API_DOCUMENTATION.md`](API_DOCUMENTATION.md)

## Postman Collection

Import collection file:

- [`Garagesystems.postman_collection.json`](Garagesystems.postman_collection.json)

Use collection variable:

- `baseUrl` (default `http://localhost:8080`)

