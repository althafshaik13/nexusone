package dev.nexusone.workflow_service.exception;

public class NotTheApproverException extends RuntimeException {
    public NotTheApproverException() {
        super("You are not authorized to decide this step");
    }
}
