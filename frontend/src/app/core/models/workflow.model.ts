export type InstanceStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export type StepStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'SKIPPED';

export type ApproverType = 'MANAGER' | 'SPECIFIC_EMPLOYEE';

export type Decision = 'APPROVE' | 'REJECT';

export interface WorkflowDefinitionStep {
  stepOrder: number;
  approverType: ApproverType;
  approverEmployeeId: string | null;
}

export interface WorkflowDefinitionResponse {
  id: string;
  requestType: string;
  name: string;
  steps: WorkflowDefinitionStep[];
  createdAt: string;
}

export interface WorkflowStepInstanceResponse {
  stepOrder: number;
  approverType: ApproverType;
  resolvedApproverEmployeeId: string | null;
  resolvedApproverUserId: string | null;
  status: StepStatus;
  decidedByUserId: string | null;
  decidedAt: string | null;
  comment: string | null;
}

export interface WorkflowInstanceResponse {
  id: string;
  requestType: string;
  title: string;
  description: string | null;
  status: InstanceStatus;
  currentStepOrder: number;
  steps: WorkflowStepInstanceResponse[];
  createdAt: string;
  updatedAt: string;
}

export interface WorkflowStatsResponse {
  countByStatus: Record<string, number>;
  countByRequestType: Record<string, number>;
  averageDecisionHours: number | null;
}
