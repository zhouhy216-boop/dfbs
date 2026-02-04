-- source_type: FACTORY | SERVICE
ALTER TABLE platform_account_applications
    ADD COLUMN IF NOT EXISTS source_type VARCHAR(32) NOT NULL DEFAULT 'FACTORY';

-- customer_name: snapshot when customer_id is null (new customer free text)
ALTER TABLE platform_account_applications
    ADD COLUMN IF NOT EXISTS customer_name VARCHAR(256);

-- Allow null customer_id when application is created with free-text customer name
ALTER TABLE platform_account_applications
    DROP CONSTRAINT IF EXISTS fk_platform_account_app_customer;
ALTER TABLE platform_account_applications
    ALTER COLUMN customer_id DROP NOT NULL;

-- Backfill customer_name from md_customer for existing rows (optional, for display)
UPDATE platform_account_applications a
SET customer_name = c.name
FROM md_customer c
WHERE a.customer_id = c.id AND a.customer_name IS NULL;
