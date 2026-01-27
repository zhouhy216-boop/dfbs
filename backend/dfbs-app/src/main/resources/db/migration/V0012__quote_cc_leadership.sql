-- V0012__quote_cc_leadership.sql
-- Create business_line and notification tables, update quote table

-- 1. Create business_line table
CREATE TABLE IF NOT EXISTS business_line (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    leader_ids VARCHAR(1000),  -- JSON array or comma-separated user IDs
    is_active BOOLEAN NOT NULL DEFAULT true
);

-- 2. Create notification table
CREATE TABLE IF NOT EXISTS notification (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    target_url VARCHAR(500),
    is_read BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL
);

-- 3. Alter quote table: add business_line_id
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'quote' AND column_name = 'business_line_id') THEN
        ALTER TABLE quote ADD COLUMN business_line_id BIGINT;
    END IF;
END $$;

-- 4. Add foreign key constraint
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_quote_business_line'
    ) THEN
        ALTER TABLE quote 
        ADD CONSTRAINT fk_quote_business_line 
        FOREIGN KEY (business_line_id) REFERENCES business_line(id);
    END IF;
END $$;

-- 5. Create indexes
CREATE INDEX IF NOT EXISTS ix_notification_user_id ON notification(user_id);
CREATE INDEX IF NOT EXISTS ix_notification_is_read ON notification(is_read);
CREATE INDEX IF NOT EXISTS ix_notification_created_at ON notification(created_at);
CREATE INDEX IF NOT EXISTS ix_quote_business_line_id ON quote(business_line_id);

-- 6. Seed default Business Lines
INSERT INTO business_line (name, leader_ids, is_active) VALUES
    ('General Sales', '[1,2]', true),
    ('After-Sales', '[3,4]', true)
ON CONFLICT (name) DO NOTHING;
