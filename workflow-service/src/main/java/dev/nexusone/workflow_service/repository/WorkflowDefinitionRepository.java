package dev.nexusone.workflow_service.repository;

import dev.nexusone.workflow_service.domain.WorkflowDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, UUID> {
    Optional<WorkflowDefinition> findByRequestType(String requestType);
    List<WorkflowDefinition> findAllByOrderByNameAsc();
}
