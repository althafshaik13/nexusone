package dev.nexusone.workflow_service.dto;

import dev.nexusone.workflow_service.domain.ApproverType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateWorkflowDefinitionRequest(
        @NotBlank String requestType,
        @NotBlank String name,
        @NotEmpty @Valid List<StepRequest> steps
) {
    public record StepRequest(
            @NotNull ApproverType approverType,
            UUID approverEmployeeId
    ) {
    }
}
