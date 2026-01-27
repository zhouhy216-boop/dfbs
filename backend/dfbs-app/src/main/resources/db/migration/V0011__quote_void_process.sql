-- V0011__quote_void_process.sql
-- Create quote void application table and update quote table

-- 1. Create quote_void_application table
CREATE TABLE IF NOT EXISTS quote_void_application (
    id BIGSERIAL PRIMARY KEY,
    quote_id BIGINT NOT NULL,
    applicant_id BIGINT NOT NULL,
    apply_reason VARCHAR(200) NOT NULL,
    apply_time TIMESTAMPTZ NOT NULL,
    attachment_urls VARCHAR(2000),
    auditor_id BIGINT,
    audit_time TIMESTAMPTZ,
    audit_result VARCHAR(32),
    audit_note VARCHAR(1000),
    CONSTRAINT fk_void_application_quote FOREIGN KEY (quote_id) REFERENCES quote(id)
);

-- 2. Alter quote table: add void_status
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'quote' AND column_name = 'void_status') THEN
        ALTER TABLE quote ADD COLUMN void_status VARCHAR(32) NOT NULL DEFAULT 'NONE';
    END IF;
END $$;

-- Create indexes
CREATE INDEX IF NOT EXISTS ix_void_application_quote_id ON quote_void_application(quote_id);
CREATE INDEX IF NOT EXISTS ix_void_application_applicant_id ON quote_void_application(applicant_id);
CREATE INDEX IF NOT EXISTS ix_void_application_audit_result ON quote_void_application(audit_result);
CREATE INDEX IF NOT EXISTS ix_quote_void_status ON quote(void_status);
