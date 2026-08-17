package dev.nexusone.workflow_service.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workflow_step_instances")
public class WorkflowStepInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "workflow_instance_id", nullable = false)
    private UUID workflowInstanceId;

    @Column(name = "step_order", nullable = false)
    private int stepOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "approver_type", nullable = false, length = 30)
    private ApproverType approverType;

    @Column(name = "resolved_approver_employee_id")
    private UUID resolvedApproverEmployeeId;

    @Column(name = "resolved_approver_user_id")
    private UUID resolvedApproverUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StepStatus status = StepStatus.PENDING;

    @Column(name = "decided_by_user_id")
    private UUID decidedByUserId;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(columnDefinition = "TEXT")
    private String comment;

    protected WorkflowStepInstance() {
    }

    public WorkflowStepInstance(UUID workflowInstanceId, int stepOrder, ApproverType approverType,
                                 UUID resolvedApproverEmployeeId, UUID resolvedApproverUserId) {
        this.workflowInstanceId = workflowInstanceId;
        this.stepOrder = stepOrder;
        this.approverType = approverType;
        this.resolvedApproverEmployeeId = resolvedApproverEmployeeId;
        this.resolvedApproverUserId = resolvedApproverUserId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getWorkflowInstanceId() {
        return workflowInstanceId;
    }

    public int getStepOrder() {
        return stepOrder;
    }

    public ApproverType getApproverType() {
        return approverType;
    }

    public UUID getResolvedApproverEmployeeId() {
        return resolvedApproverEmployeeId;
    }

    public UUID getResolvedApproverUserId() {
        return resolvedApproverUserId;
    }

    public StepStatus getStatus() {
        return status;
    }

    public void setStatus(StepStatus status) {
        this.status = status;
    }

    public UUID getDecidedByUserId() {
        return decidedByUserId;
    }

    public void setDecidedByUserId(UUID decidedByUserId) {
        this.decidedByUserId = decidedByUserId;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(Instant decidedAt) {
        this.decidedAt = decidedAt;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
