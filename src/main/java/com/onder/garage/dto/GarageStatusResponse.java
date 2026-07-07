package com.onder.garage.dto;

import java.util.List;

/**
 * Snapshot response for current garage occupancy state.
 */
public record GarageStatusResponse(
        Integer capacity,
        Integer occupiedSlots,
        Integer availableSlots,
        List<VehicleResponse> parkedVehicles
) {
    public GarageStatusResponse {
        parkedVehicles = List.copyOf(parkedVehicles);
    }
}
