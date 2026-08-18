package dev.nexusone.ticket_service.dto;

import dev.nexusone.ticket_service.domain.TicketEvent;

import java.time.Instant;
import java.util.UUID;

public record TicketEventResponse(
        UUID id,
        UUID ticketId,
        String eventType,
        UUID actorId,
        String payloadJson,
        Instant createdAt
) {
    public static TicketEventResponse from(TicketEvent event) {
        return new TicketEventResponse(event.getId(), event.getTicketId(), event.getEventType(),
                event.getActorId(), event.getPayloadJson(), event.getCreatedAt());
    }
}
