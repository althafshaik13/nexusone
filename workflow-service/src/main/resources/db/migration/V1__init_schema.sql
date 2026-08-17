CREATE TABLE workflow_definitions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    request_type VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE workflow_steps (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workflow_definition_id UUID NOT NULL REFERENCES workflow_definitions(id),
    step_order INT NOT NULL,
    approver_type VARCHAR(30) NOT NULL,
    approver_employee_id UUID,
    UNIQUE (workflow_definition_id, step_order)
);

CREATE TABLE workflow_instances (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workflow_definition_id UUID NOT NULL REFERENCES workflow_definitions(id),
    request_type VARCHAR(100) NOT NULL,
    requester_user_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    current_step_order INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_workflow_instances_requester ON workflow_instances(requester_user_id);

CREATE TABLE workflow_step_instances (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workflow_instance_id UUID NOT NULL REFERENCES workflow_instances(id),
    step_order INT NOT NULL,
    approver_type VARCHAR(30) NOT NULL,
    resolved_approver_employee_id UUID,
    resolved_approver_user_id UUID,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    decided_by_user_id UUID,
    decided_at TIMESTAMP,
    comment TEXT,
    UNIQUE (workflow_instance_id, step_order)
);

CREATE INDEX idx_workflow_step_instances_instance ON workflow_step_instances(workflow_instance_id);
CREATE INDEX idx_workflow_step_instances_approver ON workflow_step_instances(resolved_approver_user_id);
