package com.onder.garage.util;

import static com.onder.garage.constant.GarageConstants.SPOT_PREFIX;

import com.onder.garage.model.ParkingSpot;
import java.util.Comparator;
import java.util.List;

/**
 * Parking spot ordering and lookup helpers.
 */
public final class ParkingSpotUtils {

    private ParkingSpotUtils() {
    }

    public static List<ParkingSpot> sortBySlotOrder(List<ParkingSpot> spots) {
        return spots.stream()
                .sorted(Comparator.comparingInt(ParkingSpotUtils::slotOrder))
                .toList();
    }

    public static int slotOrder(ParkingSpot spot) {
        String id = spot.getId();
        if (id == null || !id.startsWith(SPOT_PREFIX)) {
            return Integer.MAX_VALUE;
        }
        return Integer.parseInt(id.substring(SPOT_PREFIX.length()));
    }

    public static List<ParkingSpot> findSpotsForVehicle(List<ParkingSpot> allSpots, String plateNumber) {
        return allSpots.stream()
                .filter(spot -> spot.getVehicle() != null && plateNumber.equals(spot.getVehicle().getPlateNumber()))
                .toList();
    }

    public static int countOccupied(List<ParkingSpot> spots) {
        return (int) spots.stream().filter(ParkingSpot::isOccupied).count();
    }
}
