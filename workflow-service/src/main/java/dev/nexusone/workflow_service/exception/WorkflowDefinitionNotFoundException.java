package dev.nexusone.workflow_service.exception;

public class WorkflowDefinitionNotFoundException extends RuntimeException {
    public WorkflowDefinitionNotFoundException(String requestType) {
        super("No workflow definition found for request type: " + requestType);
    }
}
