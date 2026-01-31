-- V0034__payment_allocation_mvp.sql
-- Payment -> Allocation -> Quote model.

-- 1. payment table
CREATE TABLE IF NOT EXISTS payment (
    id BIGSERIAL PRIMARY KEY,
    payment_no VARCHAR(64) NOT NULL,
    customer_id BIGINT NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(16) NOT NULL,
    received_at DATE NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    statement_id BIGINT,
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_payment_payment_no UNIQUE (payment_no),
    CONSTRAINT fk_payment_statement FOREIGN KEY (statement_id) REFERENCES account_statement(id)
);

CREATE INDEX IF NOT EXISTS ix_payment_customer_id ON payment(customer_id);
CREATE INDEX IF NOT EXISTS ix_payment_status ON payment(status);
CREATE INDEX IF NOT EXISTS ix_payment_statement_id ON payment(statement_id);
CREATE INDEX IF NOT EXISTS ix_payment_received_at ON payment(received_at);

-- 2. payment_allocation table
CREATE TABLE IF NOT EXISTS payment_allocation (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    quote_id BIGINT NOT NULL,
    allocated_amount DECIMAL(19,2) NOT NULL,
    period VARCHAR(64),
    CONSTRAINT fk_payment_allocation_payment FOREIGN KEY (payment_id) REFERENCES payment(id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_allocation_quote FOREIGN KEY (quote_id) REFERENCES quote(id)
);

CREATE INDEX IF NOT EXISTS ix_payment_allocation_payment_id ON payment_allocation(payment_id);
CREATE INDEX IF NOT EXISTS ix_payment_allocation_quote_id ON payment_allocation(quote_id);

-- 3. quote: add paid_amount
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'quote' AND column_name = 'paid_amount') THEN
        ALTER TABLE quote ADD COLUMN paid_amount DECIMAL(19,2) NOT NULL DEFAULT 0;
    END IF;
END $$;
