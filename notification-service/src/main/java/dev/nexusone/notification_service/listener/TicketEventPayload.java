package dev.nexusone.notification_service.listener;

import java.time.Instant;

// Deliberately re-declared here rather than shared from ticket-service:
// each service owns its view of the event contract (schema-by-convention for
// now; a schema registry is the eventual answer if this drifts).
public record TicketEventPayload(
        String eventType,
        String ticketId,
        String customerId,
        String assignedAgentId,
        String status,
        String previousStatus,
        String priority,
        Instant occurredAt
) {
}
