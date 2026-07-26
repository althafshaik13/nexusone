package dev.nexusone.ticket_service.repository;

import dev.nexusone.ticket_service.domain.TicketEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TicketEventRepository extends JpaRepository<TicketEvent, UUID> {
    List<TicketEvent> findByTicketId(UUID ticketId);
}
