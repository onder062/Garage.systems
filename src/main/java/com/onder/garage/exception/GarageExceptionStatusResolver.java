package com.onder.garage.exception;

import org.springframework.http.HttpStatus;

/**
 * Maps domain exceptions to HTTP status codes without coupling exceptions to Spring.
 */
public final class GarageExceptionStatusResolver {

    private GarageExceptionStatusResolver() {
    }

    public static HttpStatus resolve(GarageException exception) {
        return switch (exception) {
            case VehicleNotFoundException ignored -> HttpStatus.NOT_FOUND;
            case GarageFullException ignored -> HttpStatus.CONFLICT;
            case VehicleAlreadyExistsException ignored -> HttpStatus.CONFLICT;
            case InvalidVehicleException ignored -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
