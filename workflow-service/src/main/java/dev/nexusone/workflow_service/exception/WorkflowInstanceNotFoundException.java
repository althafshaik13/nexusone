package dev.nexusone.workflow_service.exception;

import java.util.UUID;

public class WorkflowInstanceNotFoundException extends RuntimeException {
    public WorkflowInstanceNotFoundException(UUID id) {
        super("Workflow instance not found: " + id);
    }
}
