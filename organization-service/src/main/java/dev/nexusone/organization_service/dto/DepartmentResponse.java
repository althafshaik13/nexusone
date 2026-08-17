package dev.nexusone.organization_service.dto;

import dev.nexusone.organization_service.domain.Department;

import java.time.Instant;
import java.util.UUID;

public record DepartmentResponse(UUID id, UUID organizationId, String name, Instant createdAt) {
    public static DepartmentResponse from(Department dept) {
        return new DepartmentResponse(dept.getId(), dept.getOrganizationId(), dept.getName(), dept.getCreatedAt());
    }
}
