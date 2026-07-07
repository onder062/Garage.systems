# Architecture Explanation

## High-Level Design

The project follows a layered architecture:

- **Controller** (`com.onder.garage.controller`)
  - Accepts HTTP requests.
  - Validates input.
  - Delegates to service layer.
- **Service** (`com.onder.garage.service`, `com.onder.garage.service.impl`)
  - Implements business rules and orchestration.
  - Owns thread-safe critical sections.
- **Repository** (`com.onder.garage.repository`)
  - Stores data in memory only.
  - Uses concurrent collections.
- **Exception handling** (`com.onder.garage.exception`)
  - Maps domain/validation failures to consistent JSON responses.
- **DTOs / Models**
  - DTOs are immutable records for API boundaries.
  - Models represent core domain state.

## Request Flow

1. Request hits `GarageController`.
2. Validation runs (`@Valid`, `@NotBlank`, etc.).
3. Controller invokes `GarageService`.
4. Service acquires `ReentrantLock` for operation-level consistency.
5. Service interacts with `GarageRepository`.
6. Response DTO is returned to caller.
7. Any exception is normalized by `GlobalExceptionHandler`.

## Concurrency Strategy

- `InMemoryGarageRepository` uses `ConcurrentHashMap` for thread-safe structures.
- `GarageServiceImpl` uses a fair `ReentrantLock` to make multi-step operations atomic.
- Occupied slot count is derived from parking spot state at read time.
- Ticket generation is delegated to repository with `AtomicInteger`.

This combination protects both:

- single-map thread safety, and
- cross-entity consistency (vehicle + ticket + spots updates together).

## Allocation Algorithm

For each parking request:

1. Determine required slots from `VehicleType`.
2. Read spots ordered by numeric spot id.
3. Scan left-to-right for first consecutive free block of required size.
4. Mark selected spots occupied with vehicle reference.
5. Persist vehicle and ticket.

Time complexity of allocation scan: **O(n)** for `n` parking spots.

## Error Handling Model

`GlobalExceptionHandler` is the single API error boundary:

- `GarageFullException` -> `409`
- `VehicleAlreadyExistsException` -> `409`
- `VehicleNotFoundException` -> `404`
- `InvalidVehicleException` -> `400`
- Validation exceptions -> `400`
- Unexpected failures -> `500`

## Clean Architecture Notes

- Business logic is not in controllers/repositories.
- Repository has no business rules, only storage responsibilities.
- DTOs decouple API contracts from internal model mutability.
- Service depends on `GarageRepository` abstraction.

