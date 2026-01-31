-- V0032__expense_claim_mvp.sql
-- Expense & Claim MVP: expense table, claim table, linkage.

-- 1) expense
CREATE TABLE IF NOT EXISTS expense (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by BIGINT NOT NULL,
    expense_date DATE NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(8) NOT NULL DEFAULT 'CNY',
    expense_type VARCHAR(32),
    description TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    quote_id BIGINT,
    work_order_id BIGINT,
    inventory_outbound_id BIGINT,
    trip_request_id BIGINT,
    claim_id BIGINT
);

CREATE INDEX IF NOT EXISTS ix_expense_created_by ON expense(created_by);
CREATE INDEX IF NOT EXISTS ix_expense_status ON expense(status);
CREATE INDEX IF NOT EXISTS ix_expense_claim_id ON expense(claim_id);
CREATE INDEX IF NOT EXISTS ix_expense_expense_date ON expense(expense_date);

-- 2) claim
CREATE TABLE IF NOT EXISTS claim (
    id BIGSERIAL PRIMARY KEY,
    claim_no VARCHAR(64) NOT NULL,
    title VARCHAR(256),
    total_amount DECIMAL(19,2),
    currency VARCHAR(8),
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    submitter_id BIGINT,
    submit_time TIMESTAMPTZ,
    approver_id BIGINT,
    approve_time TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by BIGINT
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_claim_claim_no ON claim(claim_no);
CREATE INDEX IF NOT EXISTS ix_claim_status ON claim(status);
CREATE INDEX IF NOT EXISTS ix_claim_created_by ON claim(created_by);

-- FK: expense.claim_id -> claim.id
ALTER TABLE expense ADD CONSTRAINT fk_expense_claim
    FOREIGN KEY (claim_id) REFERENCES claim(id);
