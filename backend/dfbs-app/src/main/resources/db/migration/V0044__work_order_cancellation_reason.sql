-- V0044__work_order_cancellation_reason.sql
-- Add cancellation_reason for reject workflow.

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'work_order' AND column_name = 'cancellation_reason') THEN
        ALTER TABLE work_order ADD COLUMN cancellation_reason VARCHAR(512);
    END IF;
END $$;
