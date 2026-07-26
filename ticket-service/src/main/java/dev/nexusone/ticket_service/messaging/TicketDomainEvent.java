package dev.nexusone.ticket_service.messaging;

import java.time.Instant;
import java.util.UUID;

public record TicketDomainEvent(
        String eventType,
        UUID ticketId,
        UUID customerId,
        UUID assignedAgentId,
        String status,
        String previousStatus,
        String priority,
        Instant occurredAt
) {
}
