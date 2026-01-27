-- V0010__payment_record.sql
-- Create payment tracking tables and update quote table

-- 1. Create payment_method table
CREATE TABLE IF NOT EXISTS payment_method (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    is_active BOOLEAN NOT NULL DEFAULT true
);

-- 2. Create quote_payment table
CREATE TABLE IF NOT EXISTS quote_payment (
    id BIGSERIAL PRIMARY KEY,
    quote_id BIGINT NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    method_id BIGINT NOT NULL,
    paid_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'SUBMITTED',
    submitter_id BIGINT NOT NULL,
    submitted_at TIMESTAMPTZ NOT NULL,
    confirmer_id BIGINT,
    confirmed_at TIMESTAMPTZ,
    confirm_note VARCHAR(1000),
    remark VARCHAR(1000),
    attachment_urls VARCHAR(2000),
    CONSTRAINT fk_quote_payment_quote FOREIGN KEY (quote_id) REFERENCES quote(id),
    CONSTRAINT fk_quote_payment_method FOREIGN KEY (method_id) REFERENCES payment_method(id)
);

-- 3. Create quote_collector_history table
CREATE TABLE IF NOT EXISTS quote_collector_history (
    id BIGSERIAL PRIMARY KEY,
    quote_id BIGINT NOT NULL,
    from_user_id BIGINT,
    to_user_id BIGINT NOT NULL,
    changed_by BIGINT NOT NULL,
    changed_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_collector_history_quote FOREIGN KEY (quote_id) REFERENCES quote(id)
);

-- 4. Alter quote table: add payment-related columns
DO $$
BEGIN
    -- payment_status
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'quote' AND column_name = 'payment_status') THEN
        ALTER TABLE quote ADD COLUMN payment_status VARCHAR(32) NOT NULL DEFAULT 'UNPAID';
    END IF;

    -- collector_id
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'quote' AND column_name = 'collector_id') THEN
        ALTER TABLE quote ADD COLUMN collector_id BIGINT;
    END IF;

    -- parent_quote_id
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'quote' AND column_name = 'parent_quote_id') THEN
        ALTER TABLE quote ADD COLUMN parent_quote_id BIGINT;
    END IF;
END $$;

-- Add foreign key for parent_quote_id
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                   WHERE table_name = 'quote' 
                   AND constraint_name = 'fk_quote_parent_quote') THEN
        ALTER TABLE quote
            ADD CONSTRAINT fk_quote_parent_quote
            FOREIGN KEY (parent_quote_id) REFERENCES quote(id);
    END IF;
END $$;

-- Create indexes
CREATE INDEX IF NOT EXISTS ix_payment_method_active ON payment_method(is_active);
CREATE INDEX IF NOT EXISTS ix_quote_payment_quote_id ON quote_payment(quote_id);
CREATE INDEX IF NOT EXISTS ix_quote_payment_status ON quote_payment(status);
CREATE INDEX IF NOT EXISTS ix_quote_payment_submitted_at ON quote_payment(submitted_at);
CREATE INDEX IF NOT EXISTS ix_collector_history_quote_id ON quote_collector_history(quote_id);
CREATE INDEX IF NOT EXISTS ix_collector_history_changed_at ON quote_collector_history(changed_at);
CREATE INDEX IF NOT EXISTS ix_quote_payment_status ON quote(payment_status);
CREATE INDEX IF NOT EXISTS ix_quote_collector_id ON quote(collector_id);
CREATE INDEX IF NOT EXISTS ix_quote_parent_quote_id ON quote(parent_quote_id);

-- ============================================
-- DATA SEEDING: Payment Methods
-- ============================================

INSERT INTO payment_method (name, is_active) VALUES
    ('对公转账', true),
    ('微信', true),
    ('支付宝', true)
ON CONFLICT (name) DO NOTHING;
