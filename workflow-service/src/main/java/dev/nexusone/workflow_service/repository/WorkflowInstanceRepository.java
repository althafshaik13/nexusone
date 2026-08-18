package dev.nexusone.workflow_service.repository;

import dev.nexusone.workflow_service.domain.WorkflowInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, UUID> {
    List<WorkflowInstance> findByRequesterUserId(UUID requesterUserId);

    @Query("SELECT i.status, COUNT(i) FROM WorkflowInstance i GROUP BY i.status")
    List<Object[]> countByStatus();

    @Query("SELECT i.requestType, COUNT(i) FROM WorkflowInstance i GROUP BY i.requestType")
    List<Object[]> countByRequestType();

    @Query(value = """
            SELECT AVG(EXTRACT(EPOCH FROM (updated_at - created_at)) / 3600.0)
            FROM workflow_instances
            WHERE status IN ('APPROVED', 'REJECTED')
            """, nativeQuery = true)
    Double averageDecisionHours();
}
