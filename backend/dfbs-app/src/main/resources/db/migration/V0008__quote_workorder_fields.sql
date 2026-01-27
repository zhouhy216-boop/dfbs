-- V0008__quote_workorder_fields.sql
-- Add fields for Work Order to Quote feature

DO $$
BEGIN
    -- source_id (to store workOrderNo)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'quote' AND column_name = 'source_id') THEN
        ALTER TABLE quote ADD COLUMN source_id VARCHAR(256);
    END IF;

    -- machine_info (snapshot of machine model/serial)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'quote' AND column_name = 'machine_info') THEN
        ALTER TABLE quote ADD COLUMN machine_info VARCHAR(1000);
    END IF;

    -- assignee_id (serviceManagerUserId)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'quote' AND column_name = 'assignee_id') THEN
        ALTER TABLE quote ADD COLUMN assignee_id BIGINT;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS ix_quote_source_id ON quote(source_id);
CREATE INDEX IF NOT EXISTS ix_quote_assignee_id ON quote(assignee_id);
