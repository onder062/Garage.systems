package com.onder.garage.dto;

/**
 * Response payload after a successful parking allocation.
 */
public record ParkVehicleResponse(
        String ticketId,
        Integer allocatedSlots,
        VehicleResponse vehicle
) {
}
