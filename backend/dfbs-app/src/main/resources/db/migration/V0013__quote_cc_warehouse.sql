-- V0013__quote_cc_warehouse.sql
-- Create warehouse_config table and update quote table

-- 1. Create warehouse_config table (singleton-like config)
CREATE TABLE IF NOT EXISTS warehouse_config (
    id BIGSERIAL PRIMARY KEY,
    user_ids VARCHAR(1000) NOT NULL DEFAULT '[]'  -- JSON array or comma-separated user IDs
);

-- 2. Alter quote table: add warehouse notification flags
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'quote' AND column_name = 'is_warehouse_cc_sent') THEN
        ALTER TABLE quote ADD COLUMN is_warehouse_cc_sent BOOLEAN NOT NULL DEFAULT false;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'quote' AND column_name = 'is_warehouse_ship_sent') THEN
        ALTER TABLE quote ADD COLUMN is_warehouse_ship_sent BOOLEAN NOT NULL DEFAULT false;
    END IF;
END $$;

-- 3. Create indexes
CREATE INDEX IF NOT EXISTS ix_quote_warehouse_cc_sent ON quote(is_warehouse_cc_sent);
CREATE INDEX IF NOT EXISTS ix_quote_warehouse_ship_sent ON quote(is_warehouse_ship_sent);

-- 4. Seed: Insert 1 row into warehouse_config (empty or mock user IDs)
INSERT INTO warehouse_config (id, user_ids) VALUES (1, '[]')
ON CONFLICT (id) DO NOTHING;
