package dev.nexusone.workflow_service.dto;

import dev.nexusone.workflow_service.domain.ApproverType;
import dev.nexusone.workflow_service.domain.WorkflowDefinition;
import dev.nexusone.workflow_service.domain.WorkflowStep;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WorkflowDefinitionResponse(
        UUID id,
        String requestType,
        String name,
        List<StepResponse> steps,
        Instant createdAt
) {
    public record StepResponse(int stepOrder, ApproverType approverType, UUID approverEmployeeId) {
        public static StepResponse from(WorkflowStep step) {
            return new StepResponse(step.getStepOrder(), step.getApproverType(), step.getApproverEmployeeId());
        }
    }

    public static WorkflowDefinitionResponse from(WorkflowDefinition definition, List<WorkflowStep> steps) {
        return new WorkflowDefinitionResponse(
                definition.getId(), definition.getRequestType(), definition.getName(),
                steps.stream().map(StepResponse::from).toList(), definition.getCreatedAt());
    }
}
