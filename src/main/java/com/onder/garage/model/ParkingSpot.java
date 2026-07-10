package com.onder.garage.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A single parking spot in the garage inventory.
 */
@Entity
@Table(name = "parking_spots")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ParkingSpot {
    @Id
    @Column(nullable = false)
    private String id;
    private boolean occupied;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehicle_plate_number")
    private Vehicle vehicle;
}
