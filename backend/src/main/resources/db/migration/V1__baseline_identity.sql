-- AetherCRM Baseline Schema — Identity & Multi-tenancy

CREATE TABLE organizations (
    id              UUID PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    slug            VARCHAR(100) NOT NULL UNIQUE,
    status          VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    plan            VARCHAR(50)  NOT NULL DEFAULT 'FREE',
    settings_json   TEXT,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE roles (
    id              UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    name            VARCHAR(100) NOT NULL,
    description     VARCHAR(500),
    is_system       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (organization_id, name)
);

CREATE TABLE permissions (
    id              UUID PRIMARY KEY,
    code            VARCHAR(100) NOT NULL UNIQUE,
    description     VARCHAR(500),
    module          VARCHAR(50)  NOT NULL
);

CREATE TABLE role_permissions (
    role_id         UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id   UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE users (
    id              UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    email           VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    display_name    VARCHAR(200),
    phone           VARCHAR(50),
    avatar_url      VARCHAR(500),
    status          VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    role_id         UUID REFERENCES roles(id),
    last_login_at   TIMESTAMP WITH TIME ZONE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at      TIMESTAMP WITH TIME ZONE,
    UNIQUE (organization_id, email)
);

CREATE INDEX idx_users_org ON users(organization_id);
CREATE INDEX idx_users_email ON users(email);

CREATE TABLE refresh_tokens (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash      VARCHAR(255) NOT NULL UNIQUE,
    expires_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_agent      VARCHAR(500),
    ip_address      VARCHAR(50)
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);

CREATE TABLE audit_events (
    id              UUID PRIMARY KEY,
    organization_id UUID REFERENCES organizations(id),
    user_id         UUID REFERENCES users(id),
    action          VARCHAR(100) NOT NULL,
    entity_type     VARCHAR(100),
    entity_id       UUID,
    details_json    TEXT,
    ip_address      VARCHAR(50),
    user_agent      VARCHAR(500),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_org ON audit_events(organization_id);
CREATE INDEX idx_audit_created ON audit_events(created_at);

INSERT INTO permissions (id, code, description, module) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'org:manage', 'Manage organization settings', 'IDENTITY'),
    ('a0000000-0000-0000-0000-000000000002', 'user:manage', 'Manage users', 'IDENTITY'),
    ('a0000000-0000-0000-0000-000000000003', 'lead:read', 'View leads', 'CRM'),
    ('a0000000-0000-0000-0000-000000000004', 'lead:write', 'Create and edit leads', 'CRM'),
    ('a0000000-0000-0000-0000-000000000005', 'lead:delete', 'Delete leads', 'CRM'),
    ('a0000000-0000-0000-0000-000000000006', 'contact:read', 'View contacts', 'CRM'),
    ('a0000000-0000-0000-0000-000000000007', 'contact:write', 'Create and edit contacts', 'CRM'),
    ('a0000000-0000-0000-0000-000000000008', 'account:read', 'View accounts', 'CRM'),
    ('a0000000-0000-0000-0000-000000000009', 'account:write', 'Create and edit accounts', 'CRM'),
    ('a0000000-0000-0000-0000-000000000010', 'opportunity:read', 'View opportunities', 'SALES'),
    ('a0000000-0000-0000-0000-000000000011', 'opportunity:write', 'Create and edit opportunities', 'SALES'),
    ('a0000000-0000-0000-0000-000000000012', 'task:read', 'View tasks', 'ACTIVITY'),
    ('a0000000-0000-0000-0000-000000000013', 'task:write', 'Create and edit tasks', 'ACTIVITY'),
    ('a0000000-0000-0000-0000-000000000014', 'ticket:read', 'View support tickets', 'SUPPORT'),
    ('a0000000-0000-0000-0000-000000000015', 'ticket:write', 'Create and edit tickets', 'SUPPORT'),
    ('a0000000-0000-0000-0000-000000000016', 'campaign:read', 'View campaigns', 'MARKETING'),
    ('a0000000-0000-0000-0000-000000000017', 'campaign:write', 'Create and edit campaigns', 'MARKETING'),
    ('a0000000-0000-0000-0000-000000000018', 'report:read', 'View reports', 'REPORTING'),
    ('a0000000-0000-0000-0000-000000000019', 'ai:use', 'Use AI copilot', 'AI'),
    ('a0000000-0000-0000-0000-000000000020', 'workflow:manage', 'Manage workflows', 'WORKFLOW');
