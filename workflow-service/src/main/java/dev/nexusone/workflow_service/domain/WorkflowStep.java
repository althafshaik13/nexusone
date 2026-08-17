package dev.nexusone.workflow_service.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "workflow_steps")
public class WorkflowStep {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "workflow_definition_id", nullable = false)
    private UUID workflowDefinitionId;

    @Column(name = "step_order", nullable = false)
    private int stepOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "approver_type", nullable = false, length = 30)
    private ApproverType approverType;

    @Column(name = "approver_employee_id")
    private UUID approverEmployeeId;

    protected WorkflowStep() {
    }

    public WorkflowStep(UUID workflowDefinitionId, int stepOrder, ApproverType approverType, UUID approverEmployeeId) {
        this.workflowDefinitionId = workflowDefinitionId;
        this.stepOrder = stepOrder;
        this.approverType = approverType;
        this.approverEmployeeId = approverEmployeeId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getWorkflowDefinitionId() {
        return workflowDefinitionId;
    }

    public int getStepOrder() {
        return stepOrder;
    }

    public ApproverType getApproverType() {
        return approverType;
    }

    public UUID getApproverEmployeeId() {
        return approverEmployeeId;
    }
}
