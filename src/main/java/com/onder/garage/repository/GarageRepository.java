package com.onder.garage.repository;

import com.onder.garage.model.ParkingSpot;
import com.onder.garage.model.Ticket;
import com.onder.garage.model.Vehicle;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Persistence contract for in-memory garage state.
 */
public interface GarageRepository {
    String generateTicketId();

    Vehicle saveVehicle(Vehicle vehicle);

    Optional<Vehicle> removeVehicle(String plateNumber);

    Optional<Vehicle> findVehicle(String plateNumber);

    Ticket saveTicket(Ticket ticket);

    Optional<Ticket> findTicket(String ticketId);

    Optional<Ticket> removeTicket(String ticketId);

    List<Vehicle> findAllVehicles();

    ParkingSpot saveParkingSpot(ParkingSpot parkingSpot);

    void saveParkingSpots(Collection<ParkingSpot> parkingSpots);

    List<ParkingSpot> findAllParkingSpots();
}
