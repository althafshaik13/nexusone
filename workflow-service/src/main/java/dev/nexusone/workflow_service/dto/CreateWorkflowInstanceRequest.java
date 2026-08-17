package dev.nexusone.workflow_service.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateWorkflowInstanceRequest(
        @NotBlank String requestType,
        @NotBlank String title,
        String description
) {
}
