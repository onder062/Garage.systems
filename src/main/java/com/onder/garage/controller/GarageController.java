package com.onder.garage.controller;

import com.onder.garage.dto.AvailableSlotsResponse;
import com.onder.garage.dto.GarageStatusResponse;
import com.onder.garage.dto.OccupiedSlotsResponse;
import com.onder.garage.dto.ParkVehicleRequest;
import com.onder.garage.dto.ParkVehicleResponse;
import com.onder.garage.dto.VehicleResponse;
import com.onder.garage.service.GarageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for parking, lookup, and garage capacity visibility.
 */
@RestController
@RequestMapping(value = "/garage", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class GarageController {
    private final GarageService garageService;

    public GarageController(GarageService garageService) {
        this.garageService = garageService;
    }

    /**
     * Parks a vehicle using nearest consecutive available slots.
     */
    @PostMapping("/park")
    public ResponseEntity<ParkVehicleResponse> parkVehicle(@Valid @RequestBody ParkVehicleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(garageService.parkVehicle(request));
    }

    /**
     * Removes a parked vehicle by plate number and frees allocated slots.
     */
    @DeleteMapping("/remove/{plate}")
    public ResponseEntity<VehicleResponse> removeVehicle(@PathVariable @NotBlank String plate) {
        return ResponseEntity.ok(garageService.removeVehicle(plate));
    }

    /**
     * Returns capacity, occupied slots, available slots, and parked vehicles.
     */
    @GetMapping("/status")
    public ResponseEntity<GarageStatusResponse> getGarageStatus() {
        return ResponseEntity.ok(garageService.getGarageStatus());
    }

    /**
     * Returns all currently parked vehicles.
     */
    @GetMapping("/vehicles")
    public ResponseEntity<List<VehicleResponse>> getVehicles() {
        return ResponseEntity.ok(garageService.getAllVehicles());
    }

    /**
     * Finds a parked vehicle by plate number.
     */
    @GetMapping("/find/{plate}")
    public ResponseEntity<VehicleResponse> findVehicle(@PathVariable @NotBlank String plate) {
        return ResponseEntity.ok(garageService.findVehicle(plate));
    }

    /**
     * Returns currently available slot count.
     */
    @GetMapping("/available")
    public ResponseEntity<AvailableSlotsResponse> getAvailableCapacity() {
        return ResponseEntity.ok(new AvailableSlotsResponse(garageService.getRemainingCapacity()));
    }

    /**
     * Returns currently occupied slot count.
     */
    @GetMapping("/occupied")
    public ResponseEntity<OccupiedSlotsResponse> getOccupiedCapacity() {
        return ResponseEntity.ok(new OccupiedSlotsResponse(garageService.getOccupiedCapacity()));
    }
}
