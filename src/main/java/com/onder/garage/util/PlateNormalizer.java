package com.onder.garage.util;

import com.onder.garage.exception.InvalidVehicleException;
import java.util.Locale;

/**
 * Normalizes vehicle plate numbers for consistent lookup and storage.
 */
public final class PlateNormalizer {

    private PlateNormalizer() {
    }

    public static String normalize(String plateNumber) {
        if (plateNumber == null || plateNumber.isBlank()) {
            throw new InvalidVehicleException("plateNumber must not be blank");
        }
        return plateNumber.trim().toUpperCase(Locale.ROOT);
    }
}
