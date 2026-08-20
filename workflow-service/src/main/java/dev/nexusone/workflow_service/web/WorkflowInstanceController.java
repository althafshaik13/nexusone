package dev.nexusone.workflow_service.web;

import dev.nexusone.workflow_service.dto.CreateWorkflowInstanceRequest;
import dev.nexusone.workflow_service.dto.DecideStepRequest;
import dev.nexusone.workflow_service.dto.WorkflowInstanceEventResponse;
import dev.nexusone.workflow_service.dto.WorkflowInstanceResponse;
import dev.nexusone.workflow_service.dto.WorkflowStatsResponse;
import dev.nexusone.workflow_service.exception.WorkflowInstanceNotFoundException;
import dev.nexusone.workflow_service.service.WorkflowService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/workflow-instances")
public class WorkflowInstanceController {

    private final WorkflowService workflowService;

    public WorkflowInstanceController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping("/stats")
    public WorkflowStatsResponse getStats() {
        return workflowService.getStats();
    }

    @PostMapping
    public ResponseEntity<WorkflowInstanceResponse> submit(@AuthenticationPrincipal Jwt jwt,
                                                             HttpServletRequest httpRequest,
                                                             @Valid @RequestBody CreateWorkflowInstanceRequest request) {
        UUID requesterUserId = UUID.fromString(jwt.getSubject());
        String bearerToken = httpRequest.getHeader("Authorization");
        var instance = workflowService.submitInstance(request, requesterUserId, bearerToken);
        var steps = workflowService.getStepInstances(instance.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(WorkflowInstanceResponse.from(instance, steps));
    }

    @GetMapping("/{id}")
    public WorkflowInstanceResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        UUID userId = UUID.fromString(jwt.getSubject());
        boolean isAdmin = hasAdminRole(jwt);
        if (!workflowService.canView(id, userId, isAdmin)) {
            throw new WorkflowInstanceNotFoundException(id);
        }
        var instance = workflowService.getInstance(id);
        var steps = workflowService.getStepInstances(id);
        return WorkflowInstanceResponse.from(instance, steps);
    }

    @GetMapping("/{id}/events")
    public List<WorkflowInstanceEventResponse> listEvents(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        UUID userId = UUID.fromString(jwt.getSubject());
        boolean isAdmin = hasAdminRole(jwt);
        if (!workflowService.canView(id, userId, isAdmin)) {
            throw new WorkflowInstanceNotFoundException(id);
        }
        return workflowService.listEvents(id).stream().map(WorkflowInstanceEventResponse::from).collect(Collectors.toList());
    }

    @GetMapping("/mine")
    public List<WorkflowInstanceResponse> mine(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return workflowService.listMyInstances(userId).stream()
                .map(instance -> WorkflowInstanceResponse.from(instance, workflowService.getStepInstances(instance.getId())))
                .collect(Collectors.toList());
    }

    @GetMapping("/pending-my-approval")
    public List<WorkflowInstanceResponse> pendingMyApproval(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return workflowService.listPendingApprovalsFor(userId).stream()
                .map(stepInstance -> {
                    var instance = workflowService.getInstance(stepInstance.getWorkflowInstanceId());
                    var steps = workflowService.getStepInstances(instance.getId());
                    return WorkflowInstanceResponse.from(instance, steps);
                })
                .distinct()
                .collect(Collectors.toList());
    }

    @PostMapping("/{id}/steps/{stepOrder}/decide")
    public WorkflowInstanceResponse decide(@AuthenticationPrincipal Jwt jwt,
                                            @PathVariable UUID id,
                                            @PathVariable int stepOrder,
                                            @Valid @RequestBody DecideStepRequest request) {
        UUID actingUserId = UUID.fromString(jwt.getSubject());
        workflowService.decide(id, stepOrder, actingUserId, request);
        var instance = workflowService.getInstance(id);
        var steps = workflowService.getStepInstances(id);
        return WorkflowInstanceResponse.from(instance, steps);
    }

    @SuppressWarnings("unchecked")
    private boolean hasAdminRole(Jwt jwt) {
        Object realmAccessObj = jwt.getClaim("realm_access");
        if (!(realmAccessObj instanceof Map<?, ?> map)) {
            return false;
        }
        Object roles = map.get("roles");
        return roles instanceof List<?> list && list.contains("ADMIN");
    }
}
