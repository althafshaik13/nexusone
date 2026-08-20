package dev.nexusone.workflow_service.dto;

import dev.nexusone.workflow_service.domain.WorkflowInstanceEvent;

import java.time.Instant;
import java.util.UUID;

public record WorkflowInstanceEventResponse(
        UUID id,
        UUID workflowInstanceId,
        String eventType,
        UUID actorId,
        String payloadJson,
        Instant createdAt
) {
    public static WorkflowInstanceEventResponse from(WorkflowInstanceEvent event) {
        return new WorkflowInstanceEventResponse(event.getId(), event.getWorkflowInstanceId(), event.getEventType(),
                event.getActorId(), event.getPayloadJson(), event.getCreatedAt());
    }
}
