-- V0036__carrier_freight_bill_mvp.sql
-- Carrier dictionary, rules, freight_bill carrier_id/period, shipment carrier_id and is_billable_to_customer.

-- 1. md_carrier
CREATE TABLE IF NOT EXISTS md_carrier (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(256) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    CONSTRAINT uk_md_carrier_name UNIQUE (name)
);

CREATE INDEX IF NOT EXISTS ix_md_carrier_is_active ON md_carrier(is_active);

-- 2. md_carrier_rule
CREATE TABLE IF NOT EXISTS md_carrier_rule (
    id BIGSERIAL PRIMARY KEY,
    carrier_id BIGINT NOT NULL,
    match_keyword VARCHAR(256) NOT NULL,
    priority INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_md_carrier_rule_carrier FOREIGN KEY (carrier_id) REFERENCES md_carrier(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_md_carrier_rule_carrier_id ON md_carrier_rule(carrier_id);

-- 3. freight_bill: add carrier_id, period
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'freight_bill' AND column_name = 'carrier_id') THEN
        ALTER TABLE freight_bill ADD COLUMN carrier_id BIGINT NULL;
        ALTER TABLE freight_bill ADD CONSTRAINT fk_freight_bill_carrier FOREIGN KEY (carrier_id) REFERENCES md_carrier(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'freight_bill' AND column_name = 'period') THEN
        ALTER TABLE freight_bill ADD COLUMN period VARCHAR(32) NULL;
    END IF;
END $$;

-- 4. shipment: add carrier_id, is_billable_to_customer
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'shipment' AND column_name = 'carrier_id') THEN
        ALTER TABLE shipment ADD COLUMN carrier_id BIGINT NULL;
        ALTER TABLE shipment ADD CONSTRAINT fk_shipment_carrier FOREIGN KEY (carrier_id) REFERENCES md_carrier(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'shipment' AND column_name = 'is_billable_to_customer') THEN
        ALTER TABLE shipment ADD COLUMN is_billable_to_customer BOOLEAN NOT NULL DEFAULT false;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS ix_shipment_carrier_id ON shipment(carrier_id);

-- 5. Migrate shipment.type to new enum values (for Java enum refactor)
UPDATE shipment SET type = 'SALES_DELEGATE'    WHERE type = 'ENTRUST_INTERNAL';
UPDATE shipment SET type = 'CUSTOMER_DELEGATE' WHERE type = 'ENTRUST_CUSTOMER';
