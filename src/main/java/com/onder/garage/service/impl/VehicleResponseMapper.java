package com.onder.garage.service.impl;

import static com.onder.garage.util.ParkingSpotUtils.findSpotsForVehicle;
import static com.onder.garage.util.ParkingSpotUtils.slotOrder;

import com.onder.garage.dto.VehicleResponse;
import com.onder.garage.model.ParkingSpot;
import com.onder.garage.model.Vehicle;
import java.util.List;

/**
 * Maps internal vehicle state to API response records.
 */
final class VehicleResponseMapper {

    private VehicleResponseMapper() {
    }

    static VehicleResponse fromOrderedSpots(Vehicle vehicle, List<ParkingSpot> orderedSpots) {
        return map(vehicle, findSpotsForVehicle(orderedSpots, vehicle.getPlateNumber()));
    }

    static VehicleResponse fromAllocatedSpots(Vehicle vehicle, List<ParkingSpot> allocatedSpots) {
        return map(vehicle, allocatedSpots);
    }

    private static VehicleResponse map(Vehicle vehicle, List<ParkingSpot> allocatedSpots) {
        List<Integer> slotNumbers = allocatedSpots.stream()
                .map(spot -> slotOrder(spot))
                .sorted()
                .toList();
        return new VehicleResponse(
                vehicle.getPlateNumber(),
                vehicle.getOwnerName(),
                vehicle.getVehicleType(),
                vehicle.getTicketId(),
                slotNumbers
        );
    }
}
