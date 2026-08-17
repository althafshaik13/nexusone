package dev.nexusone.workflow_service;

import dev.nexusone.workflow_service.client.OrganizationClient;
import dev.nexusone.workflow_service.domain.ApproverType;
import dev.nexusone.workflow_service.domain.Decision;
import dev.nexusone.workflow_service.domain.InstanceStatus;
import dev.nexusone.workflow_service.domain.WorkflowInstance;
import dev.nexusone.workflow_service.domain.WorkflowStepInstance;
import dev.nexusone.workflow_service.dto.CreateWorkflowDefinitionRequest;
import dev.nexusone.workflow_service.dto.CreateWorkflowInstanceRequest;
import dev.nexusone.workflow_service.dto.DecideStepRequest;
import dev.nexusone.workflow_service.exception.NotTheApproverException;
import dev.nexusone.workflow_service.service.WorkflowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class WorkflowLeaveRequestIntegrationTest {

    @Autowired
    private WorkflowService workflowService;

    @MockitoBean
    private OrganizationClient organizationClient;

    @Test
    @Transactional
    void twoStepLeaveRequestApprovalFlowsThroughManagerThenSpecificEmployee() {
        UUID requesterUserId = UUID.randomUUID();
        UUID requesterEmployeeId = UUID.randomUUID();
        UUID managerUserId = UUID.randomUUID();
        UUID managerEmployeeId = UUID.randomUUID();
        UUID hrUserId = UUID.randomUUID();
        UUID hrEmployeeId = UUID.randomUUID();

        when(organizationClient.getMyEmployee(any())).thenReturn(
                new OrganizationClient.EmployeeDto(requesterEmployeeId, UUID.randomUUID(), requesterUserId,
                        "requester@acme.test", "Engineer", null, null, managerEmployeeId, null));
        when(organizationClient.getEmployee(eq(managerEmployeeId), any())).thenReturn(
                new OrganizationClient.EmployeeDto(managerEmployeeId, UUID.randomUUID(), managerUserId,
                        "manager@acme.test", "Engineering Manager", null, null, null, null));
        when(organizationClient.getEmployee(eq(hrEmployeeId), any())).thenReturn(
                new OrganizationClient.EmployeeDto(hrEmployeeId, UUID.randomUUID(), hrUserId,
                        "hr@acme.test", "HR Specialist", null, null, null, null));

        workflowService.createDefinition(new CreateWorkflowDefinitionRequest(
                "LEAVE_REQUEST", "Leave Request Approval",
                List.of(
                        new CreateWorkflowDefinitionRequest.StepRequest(ApproverType.MANAGER, null),
                        new CreateWorkflowDefinitionRequest.StepRequest(ApproverType.SPECIFIC_EMPLOYEE, hrEmployeeId)
                )));

        WorkflowInstance instance = workflowService.submitInstance(
                new CreateWorkflowInstanceRequest("LEAVE_REQUEST", "Annual leave", "Aug 20-25"),
                requesterUserId, "Bearer test-token");

        assertEquals(InstanceStatus.PENDING, instance.getStatus());
        assertEquals(1, instance.getCurrentStepOrder());

        List<WorkflowStepInstance> steps = workflowService.getStepInstances(instance.getId());
        assertEquals(2, steps.size());
        assertEquals(managerUserId, steps.get(0).getResolvedApproverUserId());
        assertEquals(hrUserId, steps.get(1).getResolvedApproverUserId());

        assertThrows(NotTheApproverException.class, () ->
                workflowService.decide(instance.getId(), 1, UUID.randomUUID(), new DecideStepRequest(Decision.APPROVE, null)));

        workflowService.decide(instance.getId(), 1, managerUserId, new DecideStepRequest(Decision.APPROVE, "Looks fine"));
        WorkflowInstance afterStep1 = workflowService.getInstance(instance.getId());
        assertEquals(InstanceStatus.PENDING, afterStep1.getStatus());
        assertEquals(2, afterStep1.getCurrentStepOrder());

        workflowService.decide(instance.getId(), 2, hrUserId, new DecideStepRequest(Decision.APPROVE, "Approved"));
        WorkflowInstance afterStep2 = workflowService.getInstance(instance.getId());
        assertEquals(InstanceStatus.APPROVED, afterStep2.getStatus());
    }

    @Test
    @Transactional
    void rejectionAtAnyStepSkipsRemainingStepsAndRejectsInstance() {
        UUID requesterUserId = UUID.randomUUID();
        UUID managerUserId = UUID.randomUUID();
        UUID managerEmployeeId = UUID.randomUUID();

        when(organizationClient.getMyEmployee(any())).thenReturn(
                new OrganizationClient.EmployeeDto(UUID.randomUUID(), UUID.randomUUID(), requesterUserId,
                        "requester2@acme.test", "Engineer", null, null, managerEmployeeId, null));
        when(organizationClient.getEmployee(eq(managerEmployeeId), any())).thenReturn(
                new OrganizationClient.EmployeeDto(managerEmployeeId, UUID.randomUUID(), managerUserId,
                        "manager2@acme.test", "Manager", null, null, null, null));

        workflowService.createDefinition(new CreateWorkflowDefinitionRequest(
                "EXPENSE_REQUEST", "Expense Approval",
                List.of(new CreateWorkflowDefinitionRequest.StepRequest(ApproverType.MANAGER, null))));

        WorkflowInstance instance = workflowService.submitInstance(
                new CreateWorkflowInstanceRequest("EXPENSE_REQUEST", "Laptop purchase", null),
                requesterUserId, "Bearer test-token");

        workflowService.decide(instance.getId(), 1, managerUserId, new DecideStepRequest(Decision.REJECT, "Not approved"));

        WorkflowInstance rejected = workflowService.getInstance(instance.getId());
        assertEquals(InstanceStatus.REJECTED, rejected.getStatus());
    }
}
