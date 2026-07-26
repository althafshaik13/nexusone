package dev.nexusone.ticket_service.repository;

import dev.nexusone.ticket_service.domain.Ticket;
import dev.nexusone.ticket_service.domain.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    List<Ticket> findByStatus(TicketStatus status);
    List<Ticket> findByCustomerId(UUID customerId);
    List<Ticket> findByStatusAndCustomerId(TicketStatus status, UUID customerId);
}
