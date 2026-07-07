package com.onder.garage.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Vehicle categories and their slot requirements inside the garage.
 */
@Getter
@RequiredArgsConstructor
public enum VehicleType {
    CAR(1),
    JEEP(2),
    TRUCK(4);

    private final int requiredSlots;
}
