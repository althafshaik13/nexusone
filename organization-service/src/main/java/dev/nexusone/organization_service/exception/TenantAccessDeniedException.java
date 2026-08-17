package dev.nexusone.organization_service.exception;

public class TenantAccessDeniedException extends RuntimeException {
    public TenantAccessDeniedException() {
        super("Resource not found");
    }
}
