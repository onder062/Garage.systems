package com.onder.garage.exception;

import com.onder.garage.model.VehicleType;

/**
 * Raised when there are not enough consecutive free slots for a vehicle.
 */
public class GarageFullException extends GarageException {
    public GarageFullException(VehicleType vehicleType, int requiredSlots) {
        super("Garage is full for " + vehicleType + ". Required consecutive slots: " + requiredSlots);
    }
}
