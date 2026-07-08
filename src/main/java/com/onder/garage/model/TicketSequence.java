package com.onder.garage.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Monotonic ticket id sequence stored in SQLite.
 */
@Entity
@Table(name = "ticket_sequence")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketSequence {

    @Id
    private Integer id;

    @Column(nullable = false)
    private int lastValue;
}
