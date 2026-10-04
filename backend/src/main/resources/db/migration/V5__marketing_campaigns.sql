-- V5: Marketing campaigns

CREATE TABLE campaigns (
    id              UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    type            VARCHAR(50) NOT NULL DEFAULT 'EMAIL',
    status          VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    budget          DECIMAL(19, 4),
    currency        VARCHAR(3) DEFAULT 'INR',
    start_date      DATE,
    end_date        DATE,
    target_audience TEXT,
    metrics_json    TEXT,
    owner_id        UUID REFERENCES users(id),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID REFERENCES users(id),
    updated_by      UUID REFERENCES users(id),
    deleted_at      TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_campaigns_org ON campaigns(organization_id);
CREATE INDEX idx_campaigns_status ON campaigns(organization_id, status);
