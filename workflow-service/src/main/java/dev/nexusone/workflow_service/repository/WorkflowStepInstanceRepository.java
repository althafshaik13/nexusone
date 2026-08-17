package dev.nexusone.workflow_service.repository;

import dev.nexusone.workflow_service.domain.StepStatus;
import dev.nexusone.workflow_service.domain.WorkflowStepInstance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkflowStepInstanceRepository extends JpaRepository<WorkflowStepInstance, UUID> {
    List<WorkflowStepInstance> findByWorkflowInstanceIdOrderByStepOrderAsc(UUID workflowInstanceId);
    Optional<WorkflowStepInstance> findByWorkflowInstanceIdAndStepOrder(UUID workflowInstanceId, int stepOrder);
    List<WorkflowStepInstance> findByResolvedApproverUserIdAndStatus(UUID resolvedApproverUserId, StepStatus status);
}
