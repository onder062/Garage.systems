package com.onder.garage.repository;

import static com.onder.garage.constant.GarageConstants.TICKET_PREFIX;

import com.onder.garage.model.ParkingSpot;
import com.onder.garage.model.Ticket;
import com.onder.garage.model.TicketSequence;
import com.onder.garage.model.Vehicle;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * SQLite-backed repository implementation using Spring Data JPA.
 */
@Repository
@Transactional
public class JpaGarageRepository implements GarageRepository {

    private static final int SEQUENCE_ID = 1;

    private final VehicleJpaRepository vehicleJpaRepository;
    private final TicketJpaRepository ticketJpaRepository;
    private final ParkingSpotJpaRepository parkingSpotJpaRepository;
    private final TicketSequenceJpaRepository ticketSequenceJpaRepository;

    public JpaGarageRepository(
            VehicleJpaRepository vehicleJpaRepository,
            TicketJpaRepository ticketJpaRepository,
            ParkingSpotJpaRepository parkingSpotJpaRepository,
            TicketSequenceJpaRepository ticketSequenceJpaRepository
    ) {
        this.vehicleJpaRepository = vehicleJpaRepository;
        this.ticketJpaRepository = ticketJpaRepository;
        this.parkingSpotJpaRepository = parkingSpotJpaRepository;
        this.ticketSequenceJpaRepository = ticketSequenceJpaRepository;
    }

    @Override
    public String generateTicketId() {
        TicketSequence sequence = ticketSequenceJpaRepository.findById(SEQUENCE_ID)
                .orElseGet(() -> ticketSequenceJpaRepository.save(new TicketSequence(SEQUENCE_ID, 0)));
        int nextValue = sequence.getLastValue() + 1;
        sequence.setLastValue(nextValue);
        ticketSequenceJpaRepository.save(sequence);
        return TICKET_PREFIX + nextValue;
    }

    @Override
    public Vehicle saveVehicle(Vehicle vehicle) {
        Objects.requireNonNull(vehicle, "vehicle must not be null");
        Objects.requireNonNull(vehicle.getPlateNumber(), "plateNumber must not be null");
        return vehicleJpaRepository.save(vehicle);
    }

    @Override
    public Optional<Vehicle> removeVehicle(String plateNumber) {
        if (plateNumber == null) {
            return Optional.empty();
        }
        Optional<Vehicle> vehicle = vehicleJpaRepository.findById(plateNumber);
        vehicle.ifPresent(v -> vehicleJpaRepository.deleteById(plateNumber));
        return vehicle;
    }

    @Override
    public Optional<Vehicle> findVehicle(String plateNumber) {
        if (plateNumber == null) {
            return Optional.empty();
        }
        return vehicleJpaRepository.findById(plateNumber);
    }

    @Override
    public Ticket saveTicket(Ticket ticket) {
        Objects.requireNonNull(ticket, "ticket must not be null");
        Objects.requireNonNull(ticket.getTicketId(), "ticketId must not be null");
        return ticketJpaRepository.save(ticket);
    }

    @Override
    public Optional<Ticket> findTicket(String ticketId) {
        if (ticketId == null) {
            return Optional.empty();
        }
        return ticketJpaRepository.findById(ticketId);
    }

    @Override
    public Optional<Ticket> removeTicket(String ticketId) {
        if (ticketId == null) {
            return Optional.empty();
        }
        Optional<Ticket> ticket = ticketJpaRepository.findById(ticketId);
        ticket.ifPresent(t -> ticketJpaRepository.deleteById(ticketId));
        return ticket;
    }

    @Override
    public List<Vehicle> findAllVehicles() {
        return vehicleJpaRepository.findAll();
    }

    @Override
    public ParkingSpot saveParkingSpot(ParkingSpot parkingSpot) {
        Objects.requireNonNull(parkingSpot, "parkingSpot must not be null");
        Objects.requireNonNull(parkingSpot.getId(), "parkingSpot.id must not be null");
        Vehicle vehicle = parkingSpot.getVehicle();
        if (vehicle != null && vehicle.getPlateNumber() != null) {
            parkingSpot.setVehicle(vehicleJpaRepository.save(vehicle));
        }
        return parkingSpotJpaRepository.save(parkingSpot);
    }

    @Override
    public void saveParkingSpots(Collection<ParkingSpot> parkingSpots) {
        Objects.requireNonNull(parkingSpots, "parkingSpots must not be null");
        parkingSpotJpaRepository.saveAll(parkingSpots);
    }

    @Override
    public List<ParkingSpot> findAllParkingSpots() {
        return parkingSpotJpaRepository.findAll();
    }
}
