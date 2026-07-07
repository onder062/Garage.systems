package com.onder.garage.exception;

/**
 * Raised when a vehicle cannot be found by plate number.
 */
public class VehicleNotFoundException extends GarageException {
    public VehicleNotFoundException(String plateNumber) {
        super("Vehicle not found: " + plateNumber);
    }
}
