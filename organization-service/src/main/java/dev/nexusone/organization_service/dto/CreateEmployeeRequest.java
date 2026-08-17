package dev.nexusone.organization_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateEmployeeRequest(
        @NotNull UUID userId,
        @Email @NotNull String email,
        String jobTitle,
        UUID departmentId,
        UUID teamId,
        UUID managerId
) {
}
