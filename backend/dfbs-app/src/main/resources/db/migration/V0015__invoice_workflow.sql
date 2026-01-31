-- V0015__invoice_workflow.sql
-- Invoice Application workflow: Application -> Record -> ItemRef, Quote invoiced fields

-- 1. Quote: add invoiced_amount, invoice_status
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'quote' AND column_name = 'invoiced_amount') THEN
        ALTER TABLE quote ADD COLUMN invoiced_amount NUMERIC(19,2) NOT NULL DEFAULT 0;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'quote' AND column_name = 'invoice_status') THEN
        ALTER TABLE quote ADD COLUMN invoice_status VARCHAR(32) NOT NULL DEFAULT 'UNINVOICED';
    END IF;
END $$;

-- 2. invoice_application
CREATE TABLE IF NOT EXISTS invoice_application (
    id BIGSERIAL PRIMARY KEY,
    application_no VARCHAR(64) NOT NULL UNIQUE,
    collector_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    total_amount NUMERIC(19,2) NOT NULL,
    currency VARCHAR(16) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    invoice_title VARCHAR(200),
    tax_id VARCHAR(100),
    address VARCHAR(500),
    phone VARCHAR(64),
    bank_name VARCHAR(200),
    bank_account VARCHAR(100),
    email VARCHAR(128),
    audit_time TIMESTAMPTZ,
    auditor_id BIGINT,
    reject_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_invoice_application_collector_id ON invoice_application(collector_id);
CREATE INDEX IF NOT EXISTS ix_invoice_application_status ON invoice_application(status);
CREATE INDEX IF NOT EXISTS ix_invoice_application_created_at ON invoice_application(created_at);

-- 3. invoice_record
CREATE TABLE IF NOT EXISTS invoice_record (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    invoice_type VARCHAR(32) NOT NULL DEFAULT 'NORMAL',
    tax_rate NUMERIC(5,4),
    content VARCHAR(500),
    CONSTRAINT fk_invoice_record_application FOREIGN KEY (application_id) REFERENCES invoice_application(id)
);

CREATE INDEX IF NOT EXISTS ix_invoice_record_application_id ON invoice_record(application_id);

-- 4. invoice_item_ref
CREATE TABLE IF NOT EXISTS invoice_item_ref (
    id BIGSERIAL PRIMARY KEY,
    invoice_record_id BIGINT NOT NULL,
    quote_id BIGINT NOT NULL,
    quote_item_id BIGINT NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    CONSTRAINT fk_invoice_item_ref_record FOREIGN KEY (invoice_record_id) REFERENCES invoice_record(id),
    CONSTRAINT fk_invoice_item_ref_quote FOREIGN KEY (quote_id) REFERENCES quote(id)
);

CREATE INDEX IF NOT EXISTS ix_invoice_item_ref_record_id ON invoice_item_ref(invoice_record_id);
CREATE INDEX IF NOT EXISTS ix_invoice_item_ref_quote_id ON invoice_item_ref(quote_id);
