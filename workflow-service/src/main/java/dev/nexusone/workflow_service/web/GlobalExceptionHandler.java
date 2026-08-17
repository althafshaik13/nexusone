package dev.nexusone.workflow_service.web;

import dev.nexusone.workflow_service.exception.InvalidWorkflowStateException;
import dev.nexusone.workflow_service.exception.NotTheApproverException;
import dev.nexusone.workflow_service.exception.UnresolvableApproverException;
import dev.nexusone.workflow_service.exception.WorkflowDefinitionNotFoundException;
import dev.nexusone.workflow_service.exception.WorkflowInstanceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WorkflowDefinitionNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleDefNotFound(WorkflowDefinitionNotFoundException ex) {
        return body(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(WorkflowInstanceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleInstanceNotFound(WorkflowInstanceNotFoundException ex) {
        return body(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UnresolvableApproverException.class)
    public ResponseEntity<Map<String, Object>> handleUnresolvableApprover(UnresolvableApproverException ex) {
        return body(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(InvalidWorkflowStateException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidState(InvalidWorkflowStateException ex) {
        return body(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(NotTheApproverException.class)
    public ResponseEntity<Map<String, Object>> handleNotApprover(NotTheApproverException ex) {
        return body(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");
        return body(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseEntity<Map<String, Object>> body(HttpStatus status, String message) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("timestamp", Instant.now().toString());
        payload.put("status", status.value());
        payload.put("error", status.getReasonPhrase());
        payload.put("message", message);
        return ResponseEntity.status(status).body(payload);
    }
}
