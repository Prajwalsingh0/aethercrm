-- V4: Support tickets, Notifications, AI conversations, Workflows

CREATE TABLE tickets (
    id              UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    ticket_number   VARCHAR(50) NOT NULL,
    subject         VARCHAR(500) NOT NULL,
    description     TEXT,
    status          VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    priority        VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    category        VARCHAR(100),
    contact_id      UUID REFERENCES contacts(id),
    account_id      UUID REFERENCES accounts(id),
    assignee_id     UUID REFERENCES users(id),
    sla_due_at      TIMESTAMP WITH TIME ZONE,
    resolved_at     TIMESTAMP WITH TIME ZONE,
    satisfaction_score INTEGER,
    tags_json       TEXT,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID REFERENCES users(id),
    updated_by      UUID REFERENCES users(id),
    deleted_at      TIMESTAMP WITH TIME ZONE,
    UNIQUE (organization_id, ticket_number)
);

CREATE INDEX idx_tickets_org ON tickets(organization_id);
CREATE INDEX idx_tickets_status ON tickets(organization_id, status);
CREATE INDEX idx_tickets_assignee ON tickets(assignee_id);

CREATE TABLE ticket_comments (
    id              UUID PRIMARY KEY,
    ticket_id       UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    author_id       UUID REFERENCES users(id),
    body            TEXT NOT NULL,
    is_internal     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ticket_comments_ticket ON ticket_comments(ticket_id);

CREATE TABLE notifications (
    id              UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    user_id         UUID NOT NULL REFERENCES users(id),
    type            VARCHAR(50) NOT NULL,
    title           VARCHAR(255) NOT NULL,
    body            TEXT,
    link            VARCHAR(500),
    is_read         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_user ON notifications(user_id, is_read);

CREATE TABLE ai_conversations (
    id              UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    user_id         UUID NOT NULL REFERENCES users(id),
    title           VARCHAR(255),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ai_messages (
    id              UUID PRIMARY KEY,
    conversation_id UUID NOT NULL REFERENCES ai_conversations(id) ON DELETE CASCADE,
    role            VARCHAR(20) NOT NULL,
    content         TEXT,
    tool_calls_json TEXT,
    tool_call_id    VARCHAR(100),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ai_messages_conv ON ai_messages(conversation_id);

CREATE TABLE ai_tool_executions (
    id              UUID PRIMARY KEY,
    conversation_id UUID REFERENCES ai_conversations(id),
    user_id         UUID NOT NULL REFERENCES users(id),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    tool_name       VARCHAR(100) NOT NULL,
    arguments_json  TEXT,
    result_json     TEXT,
    status          VARCHAR(50) NOT NULL,
    error_message   TEXT,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE workflow_definitions (
    id              UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    trigger_type    VARCHAR(50) NOT NULL,
    trigger_config_json TEXT NOT NULL,
    conditions_json TEXT,
    actions_json    TEXT NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID REFERENCES users(id)
);

CREATE INDEX idx_workflows_org ON workflow_definitions(organization_id);

CREATE TABLE workflow_executions (
    id              UUID PRIMARY KEY,
    workflow_id     UUID NOT NULL REFERENCES workflow_definitions(id),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    status          VARCHAR(50) NOT NULL,
    trigger_payload_json TEXT,
    result_json     TEXT,
    error_message   TEXT,
    started_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finished_at     TIMESTAMP WITH TIME ZONE,
    idempotency_key VARCHAR(255)
);

CREATE INDEX idx_workflow_exec_wf ON workflow_executions(workflow_id);

CREATE TABLE knowledge_articles (
    id              UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    title           VARCHAR(500) NOT NULL,
    body            TEXT NOT NULL,
    status          VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    category        VARCHAR(100),
    tags_json       TEXT,
    created_by      UUID REFERENCES users(id),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at    TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_kb_org ON knowledge_articles(organization_id);
