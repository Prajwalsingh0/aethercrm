-- V6: Outbound email messages log

CREATE TABLE email_messages (
    id              UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    direction       VARCHAR(20) NOT NULL DEFAULT 'OUTBOUND',
    status          VARCHAR(30) NOT NULL DEFAULT 'QUEUED',
    from_address    VARCHAR(255) NOT NULL,
    to_addresses    TEXT NOT NULL,
    cc_addresses    TEXT,
    bcc_addresses   TEXT,
    subject         VARCHAR(500) NOT NULL,
    body_text       TEXT,
    body_html       TEXT,
    related_type    VARCHAR(50),
    related_id      UUID,
    provider        VARCHAR(50),
    error_message   TEXT,
    sent_at         TIMESTAMP WITH TIME ZONE,
    created_by      UUID REFERENCES users(id),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_email_org ON email_messages(organization_id);
CREATE INDEX idx_email_related ON email_messages(related_type, related_id);
CREATE INDEX idx_email_status ON email_messages(organization_id, status);
CREATE INDEX idx_email_created ON email_messages(organization_id, created_at);
