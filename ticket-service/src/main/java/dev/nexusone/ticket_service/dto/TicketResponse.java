package dev.nexusone.ticket_service.dto;

import dev.nexusone.ticket_service.domain.Ticket;
import dev.nexusone.ticket_service.domain.TicketPriority;
import dev.nexusone.ticket_service.domain.TicketStatus;

import java.time.Instant;
import java.util.UUID;

public record TicketResponse(
        UUID id,
        String subject,
        String description,
        TicketStatus status,
        TicketPriority priority,
        UUID customerId,
        UUID assignedAgentId,
        Instant slaDueAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getSubject(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCustomerId(),
                ticket.getAssignedAgentId(),
                ticket.getSlaDueAt(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }
}
