-- V0045__work_order_refinement.sql
-- Add customer_id; optionally map existing customer_name to md_customer.

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'work_order' AND column_name = 'customer_id') THEN
        ALTER TABLE work_order ADD COLUMN customer_id BIGINT;
        -- Optional: map existing customer_name to customer id (first match by name, active customers only)
        UPDATE work_order wo
        SET customer_id = (
            SELECT c.id FROM md_customer c
            WHERE c.name = wo.customer_name AND c.merged_to_id IS NULL
            LIMIT 1
        )
        WHERE wo.customer_id IS NULL AND wo.customer_name IS NOT NULL AND wo.customer_name != '';
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS ix_work_order_customer_id ON work_order(customer_id);
