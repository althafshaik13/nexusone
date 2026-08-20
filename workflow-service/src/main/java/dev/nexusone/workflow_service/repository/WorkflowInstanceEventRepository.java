package dev.nexusone.workflow_service.repository;

import dev.nexusone.workflow_service.domain.WorkflowInstanceEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkflowInstanceEventRepository extends JpaRepository<WorkflowInstanceEvent, UUID> {
    List<WorkflowInstanceEvent> findByWorkflowInstanceIdOrderByCreatedAtAsc(UUID workflowInstanceId);
}
