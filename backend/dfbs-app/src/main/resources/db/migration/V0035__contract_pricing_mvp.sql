-- V0035__contract_pricing_mvp.sql
-- PriceBook / Contract Pricing MVP: contract_price_header, contract_price_item, quote/quote_item extensions.

-- 1. contract_price_header
CREATE TABLE IF NOT EXISTS contract_price_header (
    id BIGSERIAL PRIMARY KEY,
    contract_name VARCHAR(256) NOT NULL,
    customer_id BIGINT NOT NULL,
    effective_date DATE,
    expiration_date DATE,
    priority INT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_contract_price_header_customer FOREIGN KEY (customer_id) REFERENCES md_customer(id)
);

CREATE INDEX IF NOT EXISTS ix_contract_price_header_customer_id ON contract_price_header(customer_id);
CREATE INDEX IF NOT EXISTS ix_contract_price_header_status ON contract_price_header(status);
CREATE INDEX IF NOT EXISTS ix_contract_price_header_effective ON contract_price_header(effective_date);
CREATE INDEX IF NOT EXISTS ix_contract_price_header_expiration ON contract_price_header(expiration_date);

-- 2. contract_price_item
CREATE TABLE IF NOT EXISTS contract_price_item (
    id BIGSERIAL PRIMARY KEY,
    header_id BIGINT NOT NULL,
    item_type VARCHAR(32) NOT NULL,
    unit_price DECIMAL(19,2) NOT NULL,
    currency VARCHAR(16) NOT NULL DEFAULT 'CNY',
    CONSTRAINT fk_contract_price_item_header FOREIGN KEY (header_id) REFERENCES contract_price_header(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_contract_price_item_header_id ON contract_price_item(header_id);
CREATE INDEX IF NOT EXISTS ix_contract_price_item_item_type ON contract_price_item(item_type);

-- 3. quote: add first_submission_time
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'quote' AND column_name = 'first_submission_time') THEN
        ALTER TABLE quote ADD COLUMN first_submission_time TIMESTAMP NULL;
    END IF;
END $$;

-- 4. quote_item: add price_source_info, manual_price_reason
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'quote_item' AND column_name = 'price_source_info') THEN
        ALTER TABLE quote_item ADD COLUMN price_source_info TEXT NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'quote_item' AND column_name = 'manual_price_reason') THEN
        ALTER TABLE quote_item ADD COLUMN manual_price_reason TEXT NULL;
    END IF;
END $$;
