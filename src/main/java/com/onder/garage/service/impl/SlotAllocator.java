package com.onder.garage.service.impl;

import com.onder.garage.exception.GarageFullException;
import com.onder.garage.model.ParkingSpot;
import com.onder.garage.model.VehicleType;
import java.util.ArrayList;
import java.util.List;

/**
 * Finds the nearest consecutive free block while preserving a one-slot buffer between vehicles.
 */
final class SlotAllocator {

    private SlotAllocator() {
    }

    static List<ParkingSpot> findNearestAvailable(
            List<ParkingSpot> spots,
            int requiredSlots,
            VehicleType vehicleType
    ) {
        for (int start = 0; start <= spots.size() - requiredSlots; start++) {
            int end = start + requiredSlots - 1;
            if (!isBlockCompletelyFree(spots, start, end)) {
                continue;
            }
            if (start > 0 && spots.get(start - 1).isOccupied()) {
                continue;
            }
            if (end < spots.size() - 1 && spots.get(end + 1).isOccupied()) {
                continue;
            }
            return new ArrayList<>(spots.subList(start, end + 1));
        }
        throw new GarageFullException(vehicleType, requiredSlots);
    }

    private static boolean isBlockCompletelyFree(List<ParkingSpot> spots, int start, int end) {
        for (int i = start; i <= end; i++) {
            if (spots.get(i).isOccupied()) {
                return false;
            }
        }
        return true;
    }
}
