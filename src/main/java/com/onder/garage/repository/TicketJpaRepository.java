package com.onder.garage.repository;

import com.onder.garage.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketJpaRepository extends JpaRepository<Ticket, String> {
}
