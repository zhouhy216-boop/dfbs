-- V0028__quote_part_link.sql
-- Link Quote to Part: machineId on quote, standardPrice/isPriceDeviated on quote_item

-- quote: machine_id for filtering (e.g. BOM parts)
ALTER TABLE quote ADD COLUMN IF NOT EXISTS machine_id BIGINT;
CREATE INDEX IF NOT EXISTS ix_quote_machine_id ON quote(machine_id);

-- quote_item: snapshot of Part.salesPrice and deviation flag
ALTER TABLE quote_item ADD COLUMN IF NOT EXISTS standard_price NUMERIC(19,4);
ALTER TABLE quote_item ADD COLUMN IF NOT EXISTS is_price_deviated BOOLEAN NOT NULL DEFAULT FALSE;
