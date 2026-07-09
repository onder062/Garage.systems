package com.onder.garage.controller;

import com.onder.garage.dto.AvailableSlotsResponse;
import com.onder.garage.dto.GarageStatusResponse;
import com.onder.garage.dto.OccupiedSlotsResponse;
import com.onder.garage.dto.ParkVehicleRequest;
import com.onder.garage.dto.ParkVehicleResponse;
import com.onder.garage.dto.VehicleResponse;
import com.onder.garage.service.GarageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Garage", description = "Vehicle parking, removal, and garage capacity operations")
public class GarageController {
    private final GarageService garageService;

    public GarageController(GarageService garageService) {
        this.garageService = garageService;
    }

    @Operation(summary = "Park a vehicle", description = "Allocates the nearest consecutive available slots with a one-slot buffer between vehicles")
    @PostMapping("/park")
    public ResponseEntity<ParkVehicleResponse> parkVehicle(@Valid @RequestBody ParkVehicleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(garageService.parkVehicle(request));
    }

    @Operation(summary = "Remove a vehicle", description = "Removes a parked vehicle by plate number and frees its allocated slots")
    @DeleteMapping("/remove/{plate}")
    public ResponseEntity<VehicleResponse> removeVehicle(@PathVariable @NotBlank String plate) {
        return ResponseEntity.ok(garageService.removeVehicle(plate));
    }

    @Operation(summary = "Garage status", description = "Returns capacity, occupied/available slots, and all parked vehicles with slot numbers")
    @GetMapping("/status")
    public ResponseEntity<GarageStatusResponse> getGarageStatus() {
        return ResponseEntity.ok(garageService.getGarageStatus());
    }

    @Operation(summary = "List parked vehicles", description = "Returns all currently parked vehicles with their allocated slot numbers")
    @GetMapping("/vehicles")
    public ResponseEntity<List<VehicleResponse>> getVehicles() {
        return ResponseEntity.ok(garageService.getAllVehicles());
    }

    @Operation(summary = "Find a vehicle", description = "Looks up a parked vehicle by plate number")
    @GetMapping("/find/{plate}")
    public ResponseEntity<VehicleResponse> findVehicle(@PathVariable @NotBlank String plate) {
        return ResponseEntity.ok(garageService.findVehicle(plate));
    }

    @Operation(summary = "Available capacity", description = "Returns the number of currently unoccupied parking slots")
    @GetMapping("/available")
    public ResponseEntity<AvailableSlotsResponse> getAvailableCapacity() {
        return ResponseEntity.ok(new AvailableSlotsResponse(garageService.getRemainingCapacity()));
    }

    @Operation(summary = "Occupied capacity", description = "Returns the number of currently occupied parking slots")
    @GetMapping("/occupied")
    public ResponseEntity<OccupiedSlotsResponse> getOccupiedCapacity() {
        return ResponseEntity.ok(new OccupiedSlotsResponse(garageService.getOccupiedCapacity()));
    }
}
