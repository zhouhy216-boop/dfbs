-- V0014__quote_payment_workflow.sql
-- Quote Payment Confirmation Workflow: status expansion, workflow history, payment fields

-- 1. Quote: add customer_confirmer_id
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'quote' AND column_name = 'customer_confirmer_id') THEN
        ALTER TABLE quote ADD COLUMN customer_confirmer_id BIGINT;
    END IF;
END $$;

-- 2. quote_payment: add payment_time (mandatory), payment_batch_no (indexed), currency, is_finance_confirmed, note
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'quote_payment' AND column_name = 'payment_time') THEN
        ALTER TABLE quote_payment ADD COLUMN payment_time TIMESTAMPTZ;
        UPDATE quote_payment SET payment_time = paid_at WHERE payment_time IS NULL;
        ALTER TABLE quote_payment ALTER COLUMN payment_time SET NOT NULL;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'quote_payment' AND column_name = 'payment_batch_no') THEN
        ALTER TABLE quote_payment ADD COLUMN payment_batch_no VARCHAR(64);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'quote_payment' AND column_name = 'currency') THEN
        ALTER TABLE quote_payment ADD COLUMN currency VARCHAR(16);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'quote_payment' AND column_name = 'is_finance_confirmed') THEN
        ALTER TABLE quote_payment ADD COLUMN is_finance_confirmed BOOLEAN NOT NULL DEFAULT false;
        UPDATE quote_payment SET is_finance_confirmed = true WHERE status = 'CONFIRMED';
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'quote_payment' AND column_name = 'note') THEN
        ALTER TABLE quote_payment ADD COLUMN note VARCHAR(1000);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS ix_quote_payment_payment_batch_no ON quote_payment(payment_batch_no);

-- 3. quote_workflow_history table
CREATE TABLE IF NOT EXISTS quote_workflow_history (
    id BIGSERIAL PRIMARY KEY,
    quote_id BIGINT NOT NULL,
    operator_id BIGINT NOT NULL,
    action VARCHAR(32) NOT NULL,
    previous_status VARCHAR(32),
    current_status VARCHAR(32),
    reason TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_workflow_history_quote FOREIGN KEY (quote_id) REFERENCES quote(id)
);

CREATE INDEX IF NOT EXISTS ix_quote_workflow_history_quote_id ON quote_workflow_history(quote_id);
CREATE INDEX IF NOT EXISTS ix_quote_workflow_history_created_at ON quote_workflow_history(created_at);
