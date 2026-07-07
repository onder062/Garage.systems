package com.onder.garage.exception;

/**
 * Raised when vehicle input is missing or malformed.
 */
public class InvalidVehicleException extends GarageException {
    public InvalidVehicleException(String message) {
        super(message);
    }
}
