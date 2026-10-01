CREATE TABLE pipeline_stages (
    id              UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    name            VARCHAR(100) NOT NULL,
    position        INTEGER NOT NULL DEFAULT 0,
    probability     INTEGER NOT NULL DEFAULT 0,
    is_won          BOOLEAN NOT NULL DEFAULT FALSE,
    is_lost         BOOLEAN NOT NULL DEFAULT FALSE,
    color           VARCHAR(20),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (organization_id, name)
);

CREATE INDEX idx_pipeline_stages_org ON pipeline_stages(organization_id);

CREATE TABLE products (
    id              UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    name            VARCHAR(255) NOT NULL,
    sku             VARCHAR(100),
    description     TEXT,
    unit_price      DECIMAL(19, 4) NOT NULL DEFAULT 0,
    currency        VARCHAR(3) DEFAULT 'INR',
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    category        VARCHAR(100),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at      TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_products_org ON products(organization_id);

CREATE TABLE opportunities (
    id              UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    name            VARCHAR(255) NOT NULL,
    account_id      UUID REFERENCES accounts(id),
    contact_id      UUID REFERENCES contacts(id),
    stage_id        UUID NOT NULL REFERENCES pipeline_stages(id),
    amount          DECIMAL(19, 4) DEFAULT 0,
    currency        VARCHAR(3) DEFAULT 'INR',
    probability     INTEGER DEFAULT 0,
    expected_close_date DATE,
    actual_close_date   DATE,
    owner_id        UUID REFERENCES users(id),
    source          VARCHAR(100),
    description     TEXT,
    win_reason      VARCHAR(255),
    loss_reason     VARCHAR(255),
    status          VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    tags_json       TEXT,
    custom_fields_json TEXT,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID REFERENCES users(id),
    updated_by      UUID REFERENCES users(id),
    deleted_at      TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_opportunities_org ON opportunities(organization_id);
CREATE INDEX idx_opportunities_stage ON opportunities(stage_id);
CREATE INDEX idx_opportunities_owner ON opportunities(owner_id);
CREATE INDEX idx_opportunities_account ON opportunities(account_id);
CREATE INDEX idx_opportunities_close ON opportunities(organization_id, expected_close_date);

ALTER TABLE leads
    ADD CONSTRAINT fk_leads_converted_opportunity
    FOREIGN KEY (converted_opportunity_id) REFERENCES opportunities(id);

CREATE TABLE opportunity_products (
    id              UUID PRIMARY KEY,
    opportunity_id  UUID NOT NULL REFERENCES opportunities(id) ON DELETE CASCADE,
    product_id      UUID REFERENCES products(id),
    product_name    VARCHAR(255) NOT NULL,
    quantity        DECIMAL(12, 2) NOT NULL DEFAULT 1,
    unit_price      DECIMAL(19, 4) NOT NULL,
    discount_percent DECIMAL(5, 2) DEFAULT 0,
    total           DECIMAL(19, 4) NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_opp_products_opp ON opportunity_products(opportunity_id);

CREATE TABLE quotes (
    id              UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    opportunity_id  UUID REFERENCES opportunities(id),
    quote_number    VARCHAR(50) NOT NULL,
    status          VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    valid_until     DATE,
    subtotal        DECIMAL(19, 4) DEFAULT 0,
    tax_amount      DECIMAL(19, 4) DEFAULT 0,
    discount_amount DECIMAL(19, 4) DEFAULT 0,
    total           DECIMAL(19, 4) DEFAULT 0,
    currency        VARCHAR(3) DEFAULT 'INR',
    notes           TEXT,
    owner_id        UUID REFERENCES users(id),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID REFERENCES users(id),
    deleted_at      TIMESTAMP WITH TIME ZONE,
    UNIQUE (organization_id, quote_number)
);

CREATE INDEX idx_quotes_org ON quotes(organization_id);
CREATE INDEX idx_quotes_opp ON quotes(opportunity_id);

CREATE TABLE quote_line_items (
    id              UUID PRIMARY KEY,
    quote_id        UUID NOT NULL REFERENCES quotes(id) ON DELETE CASCADE,
    product_id      UUID REFERENCES products(id),
    description     VARCHAR(500) NOT NULL,
    quantity        DECIMAL(12, 2) NOT NULL DEFAULT 1,
    unit_price      DECIMAL(19, 4) NOT NULL,
    discount_percent DECIMAL(5, 2) DEFAULT 0,
    tax_percent     DECIMAL(5, 2) DEFAULT 0,
    total           DECIMAL(19, 4) NOT NULL,
    position        INTEGER DEFAULT 0
);

CREATE INDEX idx_quote_items_quote ON quote_line_items(quote_id);

CREATE TABLE stage_history (
    id              UUID PRIMARY KEY,
    opportunity_id  UUID NOT NULL REFERENCES opportunities(id) ON DELETE CASCADE,
    from_stage_id   UUID REFERENCES pipeline_stages(id),
    to_stage_id     UUID NOT NULL REFERENCES pipeline_stages(id),
    changed_by      UUID REFERENCES users(id),
    changed_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    note            TEXT
);

CREATE INDEX idx_stage_history_opp ON stage_history(opportunity_id);
