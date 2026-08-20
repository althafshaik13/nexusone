CREATE TABLE organization_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    event_type VARCHAR(50) NOT NULL,
    actor_id UUID NOT NULL,
    payload_json JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_organization_events_organization_id ON organization_events(organization_id, created_at);
