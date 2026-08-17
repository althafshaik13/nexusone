package dev.nexusone.organization_service.exception;

import java.util.UUID;

public class EmployeeNotFoundException extends RuntimeException {
    public EmployeeNotFoundException(UUID id) {
        super("Employee not found: " + id);
    }

    public EmployeeNotFoundException(String message) {
        super(message);
    }
}
