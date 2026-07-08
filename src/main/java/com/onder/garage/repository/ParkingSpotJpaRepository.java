package com.onder.garage.repository;

import com.onder.garage.model.ParkingSpot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParkingSpotJpaRepository extends JpaRepository<ParkingSpot, String> {
}
