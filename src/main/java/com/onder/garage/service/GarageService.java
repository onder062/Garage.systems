package com.onder.garage.service;

import com.onder.garage.dto.GarageStatusResponse;
import com.onder.garage.dto.ParkVehicleRequest;
import com.onder.garage.dto.ParkVehicleResponse;
import com.onder.garage.dto.VehicleResponse;
import java.util.List;

/**
 * Service contract for garage operations.
 */
public interface GarageService {
    ParkVehicleResponse parkVehicle(ParkVehicleRequest request);

    VehicleResponse removeVehicle(String plateNumber);

    GarageStatusResponse getGarageStatus();

    int getRemainingCapacity();

    int getOccupiedCapacity();

    List<VehicleResponse> getAllVehicles();

    VehicleResponse findVehicle(String plateNumber);
}
