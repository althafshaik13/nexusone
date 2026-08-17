package dev.nexusone.workflow_service.web;

import dev.nexusone.workflow_service.dto.CreateWorkflowDefinitionRequest;
import dev.nexusone.workflow_service.dto.WorkflowDefinitionResponse;
import dev.nexusone.workflow_service.service.WorkflowService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/workflow-definitions")
public class WorkflowDefinitionController {

    private final WorkflowService workflowService;

    public WorkflowDefinitionController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping
    public ResponseEntity<WorkflowDefinitionResponse> create(@Valid @RequestBody CreateWorkflowDefinitionRequest request) {
        var definition = workflowService.createDefinition(request);
        var steps = workflowService.getSteps(definition.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(WorkflowDefinitionResponse.from(definition, steps));
    }

    @GetMapping("/{requestType}")
    public WorkflowDefinitionResponse get(@PathVariable String requestType) {
        var definition = workflowService.getDefinitionByRequestType(requestType);
        var steps = workflowService.getSteps(definition.getId());
        return WorkflowDefinitionResponse.from(definition, steps);
    }

    @GetMapping
    public List<WorkflowDefinitionResponse> list() {
        return workflowService.listDefinitions().stream()
                .map(definition -> WorkflowDefinitionResponse.from(definition, workflowService.getSteps(definition.getId())))
                .toList();
    }
}
