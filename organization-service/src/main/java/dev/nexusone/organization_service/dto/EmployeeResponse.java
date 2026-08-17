package dev.nexusone.organization_service.dto;

import dev.nexusone.organization_service.domain.Employee;

import java.time.Instant;
import java.util.UUID;

public record EmployeeResponse(
        UUID id,
        UUID organizationId,
        UUID userId,
        String email,
        String jobTitle,
        UUID departmentId,
        UUID teamId,
        UUID managerId,
        Instant createdAt
) {
    public static EmployeeResponse from(Employee e) {
        return new EmployeeResponse(e.getId(), e.getOrganizationId(), e.getUserId(), e.getEmail(),
                e.getJobTitle(), e.getDepartmentId(), e.getTeamId(), e.getManagerId(), e.getCreatedAt());
    }
}
