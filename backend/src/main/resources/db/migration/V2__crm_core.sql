CREATE TABLE accounts (
    id              UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    name            VARCHAR(255) NOT NULL,
    website         VARCHAR(500),
    industry        VARCHAR(100),
    size            VARCHAR(50),
    annual_revenue  DECIMAL(19, 4),
    currency        VARCHAR(3) DEFAULT 'INR',
    phone           VARCHAR(50),
    email           VARCHAR(255),
    billing_address TEXT,
    shipping_address TEXT,
    description     TEXT,
    owner_id        UUID REFERENCES users(id),
    status          VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    tags_json       TEXT,
    custom_fields_json TEXT,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID REFERENCES users(id),
    updated_by      UUID REFERENCES users(id),
    deleted_at      TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_accounts_org ON accounts(organization_id);
CREATE INDEX idx_accounts_owner ON accounts(owner_id);
CREATE INDEX idx_accounts_name ON accounts(organization_id, name);

CREATE TABLE contacts (
    id              UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    account_id      UUID REFERENCES accounts(id),
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(255),
    phone           VARCHAR(50),
    mobile          VARCHAR(50),
    title           VARCHAR(150),
    department      VARCHAR(100),
    owner_id        UUID REFERENCES users(id),
    status          VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    source          VARCHAR(100),
    tags_json       TEXT,
    custom_fields_json TEXT,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID REFERENCES users(id),
    updated_by      UUID REFERENCES users(id),
    deleted_at      TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_contacts_org ON contacts(organization_id);
CREATE INDEX idx_contacts_account ON contacts(account_id);
CREATE INDEX idx_contacts_email ON contacts(organization_id, email);
CREATE INDEX idx_contacts_owner ON contacts(owner_id);

CREATE TABLE leads (
    id              UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    first_name      VARCHAR(100),
    last_name       VARCHAR(100),
    email           VARCHAR(255),
    phone           VARCHAR(50),
    company         VARCHAR(255),
    title           VARCHAR(150),
    source          VARCHAR(100),
    status          VARCHAR(50)  NOT NULL DEFAULT 'NEW',
    score           INTEGER DEFAULT 0,
    score_factors_json TEXT,
    owner_id        UUID REFERENCES users(id),
    description     TEXT,
    address         TEXT,
    website         VARCHAR(500),
    annual_revenue  DECIMAL(19, 4),
    currency        VARCHAR(3) DEFAULT 'INR',
    tags_json       TEXT,
    custom_fields_json TEXT,
    converted_at    TIMESTAMP WITH TIME ZONE,
    converted_contact_id UUID REFERENCES contacts(id),
    converted_account_id UUID REFERENCES accounts(id),
    converted_opportunity_id UUID,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID REFERENCES users(id),
    updated_by      UUID REFERENCES users(id),
    deleted_at      TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_leads_org ON leads(organization_id);
CREATE INDEX idx_leads_status ON leads(organization_id, status);
CREATE INDEX idx_leads_owner ON leads(owner_id);
CREATE INDEX idx_leads_email ON leads(organization_id, email);
CREATE INDEX idx_leads_score ON leads(organization_id, score);

CREATE TABLE activities (
    id              UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    type            VARCHAR(50)  NOT NULL,
    subject         VARCHAR(500) NOT NULL,
    description     TEXT,
    status          VARCHAR(50)  NOT NULL DEFAULT 'OPEN',
    priority        VARCHAR(20)  DEFAULT 'MEDIUM',
    due_at          TIMESTAMP WITH TIME ZONE,
    completed_at    TIMESTAMP WITH TIME ZONE,
    owner_id        UUID REFERENCES users(id),
    related_type    VARCHAR(50),
    related_id      UUID,
    metadata_json   TEXT,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID REFERENCES users(id),
    updated_by      UUID REFERENCES users(id),
    deleted_at      TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_activities_org ON activities(organization_id);
CREATE INDEX idx_activities_owner ON activities(owner_id);
CREATE INDEX idx_activities_related ON activities(related_type, related_id);
CREATE INDEX idx_activities_due ON activities(organization_id, due_at);
CREATE INDEX idx_activities_status ON activities(organization_id, status);
