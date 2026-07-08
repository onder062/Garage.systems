package com.onder.garage.repository;

import com.onder.garage.model.TicketSequence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketSequenceJpaRepository extends JpaRepository<TicketSequence, Integer> {
}
