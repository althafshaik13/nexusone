package dev.nexusone.workflow_service.repository;

import dev.nexusone.workflow_service.domain.WorkflowInstance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, UUID> {
    List<WorkflowInstance> findByRequesterUserId(UUID requesterUserId);
}
