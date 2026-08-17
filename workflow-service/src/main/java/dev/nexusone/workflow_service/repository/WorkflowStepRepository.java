package dev.nexusone.workflow_service.repository;

import dev.nexusone.workflow_service.domain.WorkflowStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkflowStepRepository extends JpaRepository<WorkflowStep, UUID> {
    List<WorkflowStep> findByWorkflowDefinitionIdOrderByStepOrderAsc(UUID workflowDefinitionId);
}
