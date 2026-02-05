-- V0059__platform_org_status.sql
-- Add status for lifecycle (ACTIVE, ARREARS, DELETED). DELETED triggers org_code_short rename to free unique index.

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'platform_org' AND column_name = 'status') THEN
        ALTER TABLE platform_org ADD COLUMN status VARCHAR(32);
        UPDATE platform_org SET status = 'ACTIVE';
    END IF;
END $$;
