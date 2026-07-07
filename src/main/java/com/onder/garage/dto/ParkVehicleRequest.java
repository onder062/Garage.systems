package com.onder.garage.dto;

import com.onder.garage.model.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for parking a vehicle.
 */
public record ParkVehicleRequest(
        @NotBlank String plateNumber,
        @NotBlank String ownerName,
        @NotNull VehicleType vehicleType
) {
}
