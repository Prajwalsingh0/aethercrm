-- V7: Audit trail for CRM changes (tenant-scoped)

DROP TABLE IF EXISTS audit_events CASCADE;

CREATE TABLE audit_events (
    id              UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    actor_id        UUID REFERENCES users(id),
    action          VARCHAR(50) NOT NULL,
    entity_type     VARCHAR(50) NOT NULL,
    entity_id       UUID,
    summary         VARCHAR(500) NOT NULL,
    details_json    TEXT,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_org_created ON audit_events(organization_id, created_at DESC);
CREATE INDEX idx_audit_entity ON audit_events(organization_id, entity_type, entity_id);


