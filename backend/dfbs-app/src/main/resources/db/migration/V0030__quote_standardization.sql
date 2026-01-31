-- V0030__quote_standardization.sql
-- Lenient entry, strict exit: relax customer_id; add customer_name, original_customer_name, original_part_name

-- quote: relax customer_id, add customer name fields
ALTER TABLE quote ALTER COLUMN customer_id DROP NOT NULL;
ALTER TABLE quote ADD COLUMN IF NOT EXISTS customer_name VARCHAR(256);
ALTER TABLE quote ADD COLUMN IF NOT EXISTS original_customer_name VARCHAR(256);

-- quote_item: add original_part_name (temp name before standardization)
ALTER TABLE quote_item ADD COLUMN IF NOT EXISTS original_part_name VARCHAR(500);
