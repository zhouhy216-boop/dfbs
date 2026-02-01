-- V0041__quote_payment_created_at.sql
-- Add created_at to quote_payment for sorting and auditing (fix "No property 'createdAt'" error)

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'quote_payment' AND column_name = 'created_at') THEN
        ALTER TABLE quote_payment ADD COLUMN created_at TIMESTAMPTZ;
        UPDATE quote_payment SET created_at = submitted_at WHERE created_at IS NULL;
        ALTER TABLE quote_payment ALTER COLUMN created_at SET NOT NULL;
    END IF;
END $$;
