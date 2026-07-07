package com.onder.garage.exception;

/**
 * Raised when a vehicle with the same plate already exists.
 */
public class VehicleAlreadyExistsException extends GarageException {
    public VehicleAlreadyExistsException(String plateNumber) {
        super("Vehicle already exists with plate: " + plateNumber);
    }
}
