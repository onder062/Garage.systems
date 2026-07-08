package com.onder.garage.repository;

import static com.onder.garage.constant.GarageConstants.TICKET_PREFIX;

import com.onder.garage.model.ParkingSpot;
import com.onder.garage.model.Ticket;
import com.onder.garage.model.Vehicle;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe in-memory repository implementation for unit tests.
 */
public class InMemoryGarageRepository implements GarageRepository {

    private final AtomicInteger ticketSequence = new AtomicInteger(0);
    private final ConcurrentMap<String, Vehicle> vehiclesByPlate = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Ticket> ticketsById = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ParkingSpot> parkingSpotsById = new ConcurrentHashMap<>();

    @Override
    public String generateTicketId() {
        int nextValue = ticketSequence.incrementAndGet();
        return TICKET_PREFIX + nextValue;
    }

    @Override
    public Vehicle saveVehicle(Vehicle vehicle) {
        Objects.requireNonNull(vehicle, "vehicle must not be null");
        Objects.requireNonNull(vehicle.getPlateNumber(), "plateNumber must not be null");
        vehiclesByPlate.put(vehicle.getPlateNumber(), vehicle);
        return vehicle;
    }

    @Override
    public Optional<Vehicle> removeVehicle(String plateNumber) {
        if (plateNumber == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(vehiclesByPlate.remove(plateNumber));
    }

    @Override
    public Optional<Vehicle> findVehicle(String plateNumber) {
        if (plateNumber == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(vehiclesByPlate.get(plateNumber));
    }

    @Override
    public Ticket saveTicket(Ticket ticket) {
        Objects.requireNonNull(ticket, "ticket must not be null");
        Objects.requireNonNull(ticket.getTicketId(), "ticketId must not be null");
        ticketsById.put(ticket.getTicketId(), ticket);
        return ticket;
    }

    @Override
    public Optional<Ticket> findTicket(String ticketId) {
        if (ticketId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(ticketsById.get(ticketId));
    }

    @Override
    public Optional<Ticket> removeTicket(String ticketId) {
        if (ticketId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(ticketsById.remove(ticketId));
    }

    @Override
    public List<Vehicle> findAllVehicles() {
        return List.copyOf(vehiclesByPlate.values());
    }

    @Override
    public ParkingSpot saveParkingSpot(ParkingSpot parkingSpot) {
        Objects.requireNonNull(parkingSpot, "parkingSpot must not be null");
        Objects.requireNonNull(parkingSpot.getId(), "parkingSpot.id must not be null");
        parkingSpotsById.put(parkingSpot.getId(), parkingSpot);
        return parkingSpot;
    }

    @Override
    public void saveParkingSpots(Collection<ParkingSpot> parkingSpots) {
        Objects.requireNonNull(parkingSpots, "parkingSpots must not be null");
        parkingSpots.forEach(this::saveParkingSpot);
    }

    @Override
    public List<ParkingSpot> findAllParkingSpots() {
        return List.copyOf(parkingSpotsById.values());
    }
}
