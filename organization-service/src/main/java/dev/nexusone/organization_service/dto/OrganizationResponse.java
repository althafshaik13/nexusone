package dev.nexusone.organization_service.dto;

import dev.nexusone.organization_service.domain.Organization;

import java.time.Instant;
import java.util.UUID;

public record OrganizationResponse(UUID id, String name, Instant createdAt) {
    public static OrganizationResponse from(Organization org) {
        return new OrganizationResponse(org.getId(), org.getName(), org.getCreatedAt());
    }
}
