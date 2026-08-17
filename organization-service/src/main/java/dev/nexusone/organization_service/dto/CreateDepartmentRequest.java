package dev.nexusone.organization_service.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateDepartmentRequest(@NotBlank String name) {
}
