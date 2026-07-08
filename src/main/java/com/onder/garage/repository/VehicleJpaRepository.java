package com.onder.garage.repository;

import com.onder.garage.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleJpaRepository extends JpaRepository<Vehicle, String> {
}
