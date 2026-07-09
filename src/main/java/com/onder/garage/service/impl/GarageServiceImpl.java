package com.onder.garage.service.impl;

import static com.onder.garage.constant.GarageConstants.SPOT_PREFIX;
import static com.onder.garage.util.ParkingSpotUtils.countOccupied;
import static com.onder.garage.util.ParkingSpotUtils.findSpotsForVehicle;
import static com.onder.garage.util.ParkingSpotUtils.sortBySlotOrder;

import com.onder.garage.dto.GarageStatusResponse;
import com.onder.garage.dto.ParkVehicleRequest;
import com.onder.garage.dto.ParkVehicleResponse;
import com.onder.garage.dto.VehicleResponse;
import com.onder.garage.exception.VehicleAlreadyExistsException;
import com.onder.garage.exception.VehicleNotFoundException;
import com.onder.garage.model.ParkingSpot;
import com.onder.garage.model.Ticket;
import com.onder.garage.model.Vehicle;
import com.onder.garage.repository.GarageRepository;
import com.onder.garage.service.GarageService;
import com.onder.garage.util.PlateNormalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Lock-based garage service with consecutive-slot allocation.
 */
@Service
public class GarageServiceImpl implements GarageService {

    private final GarageRepository garageRepository;
    private final int capacity;
    private final ReentrantLock garageLock = new ReentrantLock(true);
    private volatile boolean initialized;

    public GarageServiceImpl(
            GarageRepository garageRepository,
            @Value("${garage.capacity:10}") int capacity
    ) {
        this.garageRepository = garageRepository;
        if (capacity <= 0) {
            throw new IllegalArgumentException("garage.capacity must be greater than zero");
        }
        this.capacity = capacity;
    }

    @Override
    public ParkVehicleResponse parkVehicle(ParkVehicleRequest request) {
        return executeLocked(() -> {
            String normalizedPlate = PlateNormalizer.normalize(request.plateNumber());
            ensureVehicleNotParked(normalizedPlate);

            var vehicleType = request.vehicleType();
            int requiredSlots = vehicleType.getRequiredSlots();
            List<ParkingSpot> orderedSpots = getOrderedSpots();
            List<ParkingSpot> allocatedSpots = SlotAllocator.findNearestAvailable(
                    orderedSpots,
                    requiredSlots,
                    vehicleType
            );

            Vehicle vehicle = Vehicle.builder()
                    .plateNumber(normalizedPlate)
                    .ownerName(request.ownerName())
                    .vehicleType(vehicleType)
                    .ticketId(garageRepository.generateTicketId())
                    .build();

            Ticket ticket = Ticket.builder()
                    .ticketId(vehicle.getTicketId())
                    .allocatedSlots(requiredSlots)
                    .vehicle(vehicle)
                    .build();

            occupySpots(allocatedSpots, vehicle);
            garageRepository.saveVehicle(vehicle);
            garageRepository.saveTicket(ticket);

            return new ParkVehicleResponse(
                    ticket.getTicketId(),
                    ticket.getAllocatedSlots(),
                    VehicleResponseMapper.fromAllocatedSpots(vehicle, allocatedSpots)
            );
        });
    }

    @Override
    public VehicleResponse removeVehicle(String plateNumber) {
        return executeLocked(() -> {
            String normalizedPlate = PlateNormalizer.normalize(plateNumber);
            Vehicle vehicle = garageRepository.findVehicle(normalizedPlate)
                    .orElseThrow(() -> new VehicleNotFoundException(normalizedPlate));

            List<ParkingSpot> orderedSpots = getOrderedSpots();
            List<ParkingSpot> allocatedSpots = findSpotsForVehicle(orderedSpots, normalizedPlate);

            releaseSpotsForPlate(orderedSpots, normalizedPlate);
            garageRepository.removeVehicle(normalizedPlate);
            garageRepository.removeTicket(vehicle.getTicketId());

            return VehicleResponseMapper.fromAllocatedSpots(vehicle, allocatedSpots);
        });
    }

    @Override
    public GarageStatusResponse getGarageStatus() {
        return executeLocked(() -> {
            List<ParkingSpot> orderedSpots = getOrderedSpots();
            List<VehicleResponse> parkedVehicles = garageRepository.findAllVehicles()
                    .stream()
                    .map(vehicle -> VehicleResponseMapper.fromOrderedSpots(vehicle, orderedSpots))
                    .toList();

            int occupied = countOccupied(orderedSpots);
            return new GarageStatusResponse(capacity, occupied, capacity - occupied, parkedVehicles);
        });
    }

    @Override
    public int getRemainingCapacity() {
        return executeLocked(() -> capacity - countOccupied(getOrderedSpots()));
    }

    @Override
    public int getOccupiedCapacity() {
        return executeLocked(() -> countOccupied(getOrderedSpots()));
    }

    @Override
    public List<VehicleResponse> getAllVehicles() {
        return executeLocked(() -> {
            List<ParkingSpot> orderedSpots = getOrderedSpots();
            return garageRepository.findAllVehicles()
                    .stream()
                    .map(vehicle -> VehicleResponseMapper.fromOrderedSpots(vehicle, orderedSpots))
                    .toList();
        });
    }

    @Override
    public VehicleResponse findVehicle(String plateNumber) {
        return executeLocked(() -> {
            String normalizedPlate = PlateNormalizer.normalize(plateNumber);
            Vehicle vehicle = garageRepository.findVehicle(normalizedPlate)
                    .orElseThrow(() -> new VehicleNotFoundException(normalizedPlate));
            return VehicleResponseMapper.fromOrderedSpots(vehicle, getOrderedSpots());
        });
    }

    private <T> T executeLocked(Supplier<T> action) {
        garageLock.lock();
        try {
            ensureInitialized();
            return action.get();
        } finally {
            garageLock.unlock();
        }
    }

    private void ensureInitialized() {
        if (initialized) {
            return;
        }

        List<ParkingSpot> existingSpots = garageRepository.findAllParkingSpots();
        if (existingSpots.isEmpty()) {
            List<ParkingSpot> generatedSpots = new ArrayList<>(capacity);
            for (int i = 1; i <= capacity; i++) {
                generatedSpots.add(ParkingSpot.builder()
                        .id(SPOT_PREFIX + i)
                        .occupied(false)
                        .vehicle(null)
                        .build());
            }
            garageRepository.saveParkingSpots(generatedSpots);
        }
        initialized = true;
    }

    private void ensureVehicleNotParked(String plateNumber) {
        if (garageRepository.findVehicle(plateNumber).isPresent()) {
            throw new VehicleAlreadyExistsException(plateNumber);
        }
    }

    private List<ParkingSpot> getOrderedSpots() {
        return sortBySlotOrder(garageRepository.findAllParkingSpots());
    }

    private void occupySpots(List<ParkingSpot> spots, Vehicle vehicle) {
        for (ParkingSpot spot : spots) {
            spot.setOccupied(true);
            spot.setVehicle(vehicle);
            garageRepository.saveParkingSpot(spot);
        }
    }

    private void releaseSpotsForPlate(List<ParkingSpot> orderedSpots, String plateNumber) {
        for (ParkingSpot spot : orderedSpots) {
            if (spot.getVehicle() != null && plateNumber.equals(spot.getVehicle().getPlateNumber())) {
                spot.setOccupied(false);
                spot.setVehicle(null);
                garageRepository.saveParkingSpot(spot);
            }
        }
    }
}
