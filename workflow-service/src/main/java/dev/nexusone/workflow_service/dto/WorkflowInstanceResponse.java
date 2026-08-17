package dev.nexusone.workflow_service.dto;

import dev.nexusone.workflow_service.domain.ApproverType;
import dev.nexusone.workflow_service.domain.InstanceStatus;
import dev.nexusone.workflow_service.domain.StepStatus;
import dev.nexusone.workflow_service.domain.WorkflowInstance;
import dev.nexusone.workflow_service.domain.WorkflowStepInstance;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WorkflowInstanceResponse(
        UUID id,
        String requestType,
        String title,
        String description,
        InstanceStatus status,
        int currentStepOrder,
        List<StepInstanceResponse> steps,
        Instant createdAt,
        Instant updatedAt
) {
    public record StepInstanceResponse(
            int stepOrder,
            ApproverType approverType,
            UUID resolvedApproverEmployeeId,
            UUID resolvedApproverUserId,
            StepStatus status,
            UUID decidedByUserId,
            Instant decidedAt,
            String comment
    ) {
        public static StepInstanceResponse from(WorkflowStepInstance s) {
            return new StepInstanceResponse(s.getStepOrder(), s.getApproverType(), s.getResolvedApproverEmployeeId(),
                    s.getResolvedApproverUserId(), s.getStatus(), s.getDecidedByUserId(), s.getDecidedAt(), s.getComment());
        }
    }

    public static WorkflowInstanceResponse from(WorkflowInstance instance, List<WorkflowStepInstance> steps) {
        return new WorkflowInstanceResponse(
                instance.getId(), instance.getRequestType(), instance.getTitle(), instance.getDescription(),
                instance.getStatus(), instance.getCurrentStepOrder(),
                steps.stream().map(StepInstanceResponse::from).toList(),
                instance.getCreatedAt(), instance.getUpdatedAt());
    }
}
