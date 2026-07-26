package dev.nexusone.ticket_service.repository;

import dev.nexusone.ticket_service.domain.TicketComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TicketCommentRepository extends JpaRepository<TicketComment, UUID> {
    List<TicketComment> findByTicketId(UUID ticketId);
}
