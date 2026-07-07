# API Documentation

Base URL: `http://localhost:8080`  
Base Path: `/garage`

## Endpoints

### Park Vehicle

- **Method**: `POST`
- **Path**: `/garage/park`
- **Request Body**:

```json
{
  "plateNumber": "34ABC34",
  "ownerName": "Alice",
  "vehicleType": "CAR"
}
```

- **Success**: `201 Created`

```json
{
  "ticketId": "TKT-1",
  "allocatedSlots": 1,
  "vehicle": {
    "plateNumber": "34ABC34",
    "ownerName": "Alice",
    "vehicleType": "CAR",
    "ticketId": "TKT-1"
  }
}
```

- **Errors**:
  - `400 Bad Request` (validation/input)
  - `409 Conflict` (already parked / no capacity)

### Remove Vehicle

- **Method**: `DELETE`
- **Path**: `/garage/remove/{plate}`
- **Success**: `200 OK`
- **Errors**:
  - `400 Bad Request`
  - `404 Not Found`

### Garage Status

- **Method**: `GET`
- **Path**: `/garage/status`
- **Success**: `200 OK`

```json
{
  "capacity": 10,
  "occupiedSlots": 5,
  "availableSlots": 5,
  "parkedVehicles": [
    {
      "plateNumber": "34CAR34",
      "ownerName": "Alice",
      "vehicleType": "CAR",
      "ticketId": "TKT-1",
      "allocatedSlots": [1]
    },
    {
      "plateNumber": "34TRK34",
      "ownerName": "Bob",
      "vehicleType": "TRUCK",
      "ticketId": "TKT-2",
      "allocatedSlots": [3, 4, 5, 6]
    }
  ]
}
```

### List Vehicles

- **Method**: `GET`
- **Path**: `/garage/vehicles`
- **Success**: `200 OK`

### Find Vehicle

- **Method**: `GET`
- **Path**: `/garage/find/{plate}`
- **Success**: `200 OK`
- **Errors**:
  - `400 Bad Request`
  - `404 Not Found`

### Available Capacity

- **Method**: `GET`
- **Path**: `/garage/available`
- **Success**: `200 OK`

```json
{
  "availableSlots": 5
}
```

### Occupied Capacity

- **Method**: `GET`
- **Path**: `/garage/occupied`
- **Success**: `200 OK`

```json
{
  "occupiedSlots": 5
}
```

## Error Response Contract

All handled errors are returned as JSON:

```json
{
  "timestamp": "2026-07-07T09:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/garage/park",
  "validationErrors": {
    "plateNumber": "must not be blank"
  }
}
```

## Business Rules

- Vehicle plate is unique among parked vehicles.
- `CAR` needs 1 slot.
- `JEEP` needs 2 consecutive slots.
- `TRUCK` needs 4 consecutive slots.
- Allocation always chooses nearest available consecutive block.
- A 1-slot buffer must exist between two parked vehicles.
- Buffer slots are not marked occupied; they are only treated as non-allocatable boundaries between vehicles.
- No extra buffer is required after the last parked vehicle.

## Placement Rule (Vodafone Case)

When a vehicle occupies its own width, the algorithm enforces a 1-slot gap to the next vehicle.

Example layout:

- `CAR(width=1)` -> `Slot [1]`
- `Slot [2]` -> empty buffer
- `TRUCK(width=4)` -> `Slots [3,4,5,6]`

