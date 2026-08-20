package dev.nexusone.organization_service.dto;

import dev.nexusone.organization_service.domain.OrganizationEvent;

import java.time.Instant;
import java.util.UUID;

public record OrganizationEventResponse(
        UUID id,
        UUID organizationId,
        String eventType,
        UUID actorId,
        String payloadJson,
        Instant createdAt
) {
    public static OrganizationEventResponse from(OrganizationEvent event) {
        return new OrganizationEventResponse(event.getId(), event.getOrganizationId(), event.getEventType(),
                event.getActorId(), event.getPayloadJson(), event.getCreatedAt());
    }
}
