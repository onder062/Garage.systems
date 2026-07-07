package com.onder.garage.dto;

import com.onder.garage.model.VehicleType;
import java.util.List;

/**
 * Immutable vehicle representation for API responses.
 */
public record VehicleResponse(
        String plateNumber,
        String ownerName,
        VehicleType vehicleType,
        String ticketId,
        List<Integer> allocatedSlots
) {
    public VehicleResponse {
        allocatedSlots = List.copyOf(allocatedSlots);
    }
}
