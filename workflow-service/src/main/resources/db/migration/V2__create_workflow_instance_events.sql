CREATE TABLE workflow_instance_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workflow_instance_id UUID NOT NULL REFERENCES workflow_instances(id),
    event_type VARCHAR(50) NOT NULL,
    actor_id UUID NOT NULL,
    payload_json JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_workflow_instance_events_instance_id ON workflow_instance_events(workflow_instance_id, created_at);
