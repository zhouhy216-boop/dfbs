-- V0026__account_statement.sql
-- Customer Account Statement: app_user flag, account_statement, account_statement_item

-- 1. Alter app_user: can_manage_statements
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS can_manage_statements BOOLEAN NOT NULL DEFAULT false;

-- 2. Create account_statement
CREATE TABLE IF NOT EXISTS account_statement (
    id BIGSERIAL PRIMARY KEY,
    statement_no VARCHAR(64) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    customer_name VARCHAR(256),
    currency VARCHAR(16) NOT NULL,
    total_amount NUMERIC(19,4) NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    payment_id BIGINT,
    creator_id BIGINT,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_account_statement_customer_id ON account_statement(customer_id);
CREATE INDEX IF NOT EXISTS ix_account_statement_status ON account_statement(status);
CREATE INDEX IF NOT EXISTS ix_account_statement_created_at ON account_statement(created_at);

-- 3. Create account_statement_item
CREATE TABLE IF NOT EXISTS account_statement_item (
    id BIGSERIAL PRIMARY KEY,
    statement_id BIGINT NOT NULL,
    quote_id BIGINT NOT NULL,
    quote_no VARCHAR(64) NOT NULL,
    quote_total NUMERIC(19,4) NOT NULL DEFAULT 0,
    quote_paid NUMERIC(19,4) NOT NULL DEFAULT 0,
    quote_unpaid NUMERIC(19,4) NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS ix_account_statement_item_statement_id ON account_statement_item(statement_id);
CREATE INDEX IF NOT EXISTS ix_account_statement_item_quote_id ON account_statement_item(quote_id);
