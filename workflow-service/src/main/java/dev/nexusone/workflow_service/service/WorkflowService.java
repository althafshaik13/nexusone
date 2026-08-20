package dev.nexusone.workflow_service.service;

import dev.nexusone.workflow_service.client.OrganizationClient;
import dev.nexusone.workflow_service.client.OrganizationClient.EmployeeDto;
import dev.nexusone.workflow_service.domain.ApproverType;
import dev.nexusone.workflow_service.domain.Decision;
import dev.nexusone.workflow_service.domain.InstanceStatus;
import dev.nexusone.workflow_service.domain.StepStatus;
import dev.nexusone.workflow_service.domain.WorkflowDefinition;
import dev.nexusone.workflow_service.domain.WorkflowInstance;
import dev.nexusone.workflow_service.domain.WorkflowInstanceEvent;
import dev.nexusone.workflow_service.domain.WorkflowStep;
import dev.nexusone.workflow_service.domain.WorkflowStepInstance;
import dev.nexusone.workflow_service.dto.CreateWorkflowDefinitionRequest;
import dev.nexusone.workflow_service.dto.CreateWorkflowInstanceRequest;
import dev.nexusone.workflow_service.dto.DecideStepRequest;
import dev.nexusone.workflow_service.dto.WorkflowStatsResponse;
import dev.nexusone.workflow_service.exception.InvalidWorkflowStateException;
import dev.nexusone.workflow_service.exception.NotTheApproverException;
import dev.nexusone.workflow_service.exception.UnresolvableApproverException;
import dev.nexusone.workflow_service.exception.WorkflowDefinitionNotFoundException;
import dev.nexusone.workflow_service.exception.WorkflowInstanceNotFoundException;
import dev.nexusone.workflow_service.repository.WorkflowDefinitionRepository;
import dev.nexusone.workflow_service.repository.WorkflowInstanceEventRepository;
import dev.nexusone.workflow_service.repository.WorkflowInstanceRepository;
import dev.nexusone.workflow_service.repository.WorkflowStepInstanceRepository;
import dev.nexusone.workflow_service.repository.WorkflowStepRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class WorkflowService {

    private final WorkflowDefinitionRepository definitionRepository;
    private final WorkflowStepRepository stepRepository;
    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowStepInstanceRepository stepInstanceRepository;
    private final OrganizationClient organizationClient;
    private final WorkflowInstanceEventRepository instanceEventRepository;
    private final ObjectMapper objectMapper;

    public WorkflowService(WorkflowDefinitionRepository definitionRepository,
                            WorkflowStepRepository stepRepository,
                            WorkflowInstanceRepository instanceRepository,
                            WorkflowStepInstanceRepository stepInstanceRepository,
                            OrganizationClient organizationClient,
                            WorkflowInstanceEventRepository instanceEventRepository,
                            ObjectMapper objectMapper) {
        this.definitionRepository = definitionRepository;
        this.stepRepository = stepRepository;
        this.instanceRepository = instanceRepository;
        this.stepInstanceRepository = stepInstanceRepository;
        this.organizationClient = organizationClient;
        this.instanceEventRepository = instanceEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public WorkflowDefinition createDefinition(CreateWorkflowDefinitionRequest request) {
        WorkflowDefinition definition = definitionRepository.save(
                new WorkflowDefinition(request.requestType(), request.name()));
        int order = 1;
        for (var stepRequest : request.steps()) {
            stepRepository.save(new WorkflowStep(definition.getId(), order++, stepRequest.approverType(),
                    stepRequest.approverEmployeeId()));
        }
        return definition;
    }

    @Transactional(readOnly = true)
    public WorkflowDefinition getDefinitionByRequestType(String requestType) {
        return definitionRepository.findByRequestType(requestType)
                .orElseThrow(() -> new WorkflowDefinitionNotFoundException(requestType));
    }

    @Transactional(readOnly = true)
    public List<WorkflowDefinition> listDefinitions() {
        return definitionRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public List<WorkflowStep> getSteps(UUID definitionId) {
        return stepRepository.findByWorkflowDefinitionIdOrderByStepOrderAsc(definitionId);
    }

    @Transactional
    public WorkflowInstance submitInstance(CreateWorkflowInstanceRequest request, UUID requesterUserId, String bearerToken) {
        WorkflowDefinition definition = getDefinitionByRequestType(request.requestType());
        List<WorkflowStep> steps = getSteps(definition.getId());

        WorkflowInstance instance = instanceRepository.save(new WorkflowInstance(
                definition.getId(), definition.getRequestType(), requesterUserId, request.title(), request.description()));

        for (WorkflowStep step : steps) {
            ResolvedApprover resolved = resolveApprover(step, bearerToken);
            stepInstanceRepository.save(new WorkflowStepInstance(
                    instance.getId(), step.getStepOrder(), step.getApproverType(),
                    resolved.employeeId(), resolved.userId()));
        }
        recordEvent(instance.getId(), "WORKFLOW_SUBMITTED", requesterUserId,
                Map.of("requestType", request.requestType(), "title", request.title()));
        return instance;
    }

    private ResolvedApprover resolveApprover(WorkflowStep step, String bearerToken) {
        try {
            if (step.getApproverType() == ApproverType.SPECIFIC_EMPLOYEE) {
                EmployeeDto employee = organizationClient.getEmployee(step.getApproverEmployeeId(), bearerToken);
                return new ResolvedApprover(employee.id(), employee.userId());
            }
            EmployeeDto requester = organizationClient.getMyEmployee(bearerToken);
            if (requester.managerId() == null) {
                throw new UnresolvableApproverException(
                        "Cannot resolve MANAGER approver: requester has no manager on file");
            }
            EmployeeDto manager = organizationClient.getEmployee(requester.managerId(), bearerToken);
            return new ResolvedApprover(manager.id(), manager.userId());
        } catch (RestClientException e) {
            throw new UnresolvableApproverException(
                    "Failed to resolve approver for step " + step.getStepOrder() + " via organization-service: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public WorkflowInstance getInstance(UUID id) {
        return instanceRepository.findById(id).orElseThrow(() -> new WorkflowInstanceNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<WorkflowStepInstance> getStepInstances(UUID instanceId) {
        return stepInstanceRepository.findByWorkflowInstanceIdOrderByStepOrderAsc(instanceId);
    }

    @Transactional(readOnly = true)
    public List<WorkflowInstance> listMyInstances(UUID requesterUserId) {
        return instanceRepository.findByRequesterUserId(requesterUserId);
    }

    @Transactional(readOnly = true)
    public List<WorkflowStepInstance> listPendingApprovalsFor(UUID approverUserId) {
        return stepInstanceRepository.findByResolvedApproverUserIdAndStatus(approverUserId, StepStatus.PENDING).stream()
                .filter(step -> {
                    WorkflowInstance instance = getInstance(step.getWorkflowInstanceId());
                    return instance.getStatus() == InstanceStatus.PENDING && instance.getCurrentStepOrder() == step.getStepOrder();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean canView(UUID instanceId, UUID userId, boolean isAdmin) {
        if (isAdmin) {
            return true;
        }
        WorkflowInstance instance = getInstance(instanceId);
        if (instance.getRequesterUserId().equals(userId)) {
            return true;
        }
        return getStepInstances(instanceId).stream()
                .anyMatch(s -> userId.equals(s.getResolvedApproverUserId()));
    }

    @Transactional
    public WorkflowStepInstance decide(UUID instanceId, int stepOrder, UUID actingUserId, DecideStepRequest request) {
        WorkflowInstance instance = getInstance(instanceId);
        if (instance.getStatus() != InstanceStatus.PENDING) {
            throw new InvalidWorkflowStateException("Workflow instance is already " + instance.getStatus());
        }
        if (stepOrder != instance.getCurrentStepOrder()) {
            throw new InvalidWorkflowStateException("Step " + stepOrder + " is not the current active step (current is "
                    + instance.getCurrentStepOrder() + ")");
        }
        WorkflowStepInstance step = stepInstanceRepository.findByWorkflowInstanceIdAndStepOrder(instanceId, stepOrder)
                .orElseThrow(() -> new WorkflowInstanceNotFoundException(instanceId));
        if (step.getResolvedApproverUserId() == null || !step.getResolvedApproverUserId().equals(actingUserId)) {
            throw new NotTheApproverException();
        }

        step.setDecidedByUserId(actingUserId);
        step.setDecidedAt(Instant.now());
        step.setComment(request.comment());

        Map<String, Object> decisionPayload = new LinkedHashMap<>();
        decisionPayload.put("stepOrder", stepOrder);
        decisionPayload.put("comment", request.comment());

        if (request.decision() == Decision.REJECT) {
            step.setStatus(StepStatus.REJECTED);
            instance.setStatus(InstanceStatus.REJECTED);
            skipRemainingSteps(instanceId, stepOrder);
            recordEvent(instanceId, "WORKFLOW_STEP_REJECTED", actingUserId, decisionPayload);
            recordEvent(instanceId, "WORKFLOW_REJECTED", actingUserId, Map.of());
        } else {
            step.setStatus(StepStatus.APPROVED);
            boolean hasNext = stepInstanceRepository.findByWorkflowInstanceIdAndStepOrder(instanceId, stepOrder + 1).isPresent();
            recordEvent(instanceId, "WORKFLOW_STEP_APPROVED", actingUserId, decisionPayload);
            if (hasNext) {
                instance.setCurrentStepOrder(stepOrder + 1);
            } else {
                instance.setStatus(InstanceStatus.APPROVED);
                recordEvent(instanceId, "WORKFLOW_APPROVED", actingUserId, Map.of());
            }
        }
        return step;
    }

    @Transactional(readOnly = true)
    public WorkflowStatsResponse getStats() {
        Map<String, Long> countByStatus = new LinkedHashMap<>();
        for (Object[] row : instanceRepository.countByStatus()) {
            countByStatus.put(((InstanceStatus) row[0]).name(), (Long) row[1]);
        }
        Map<String, Long> countByRequestType = new LinkedHashMap<>();
        for (Object[] row : instanceRepository.countByRequestType()) {
            countByRequestType.put((String) row[0], (Long) row[1]);
        }
        Double averageDecisionHours = instanceRepository.averageDecisionHours();
        return new WorkflowStatsResponse(countByStatus, countByRequestType, averageDecisionHours);
    }

    private void skipRemainingSteps(UUID instanceId, int fromStepOrderExclusive) {
        for (WorkflowStepInstance s : getStepInstances(instanceId)) {
            if (s.getStepOrder() > fromStepOrderExclusive && s.getStatus() == StepStatus.PENDING) {
                s.setStatus(StepStatus.SKIPPED);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<WorkflowInstanceEvent> listEvents(UUID instanceId) {
        return instanceEventRepository.findByWorkflowInstanceIdOrderByCreatedAtAsc(instanceId);
    }

    private void recordEvent(UUID instanceId, String eventType, UUID actorId, Map<String, Object> payload) {
        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to serialize workflow instance event payload", e);
        }
        instanceEventRepository.save(new WorkflowInstanceEvent(instanceId, eventType, actorId, json));
    }

    private record ResolvedApprover(UUID employeeId, UUID userId) {
    }
}
