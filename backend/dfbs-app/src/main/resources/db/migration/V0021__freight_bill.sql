-- V0021__freight_bill.sql
-- Freight bill: freight_bill, freight_bill_item, shipment.freight_bill_id

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'shipment' AND column_name = 'freight_bill_id') THEN
    ALTER TABLE shipment ADD COLUMN freight_bill_id BIGINT;
  END IF;
END $$;

CREATE INDEX IF NOT EXISTS ix_shipment_freight_bill_id ON shipment(freight_bill_id);

CREATE TABLE IF NOT EXISTS freight_bill (
    id BIGSERIAL PRIMARY KEY,
    bill_no VARCHAR(64) NOT NULL UNIQUE,
    carrier VARCHAR(256) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    total_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
    attachment_url VARCHAR(512),
    audit_time TIMESTAMP,
    auditor_id BIGINT,
    created_time TIMESTAMP NOT NULL,
    creator_id BIGINT
);

CREATE TABLE IF NOT EXISTS freight_bill_item (
    id BIGSERIAL PRIMARY KEY,
    bill_id BIGINT NOT NULL,
    shipment_id BIGINT NOT NULL,
    shipment_no VARCHAR(64),
    financial_category VARCHAR(32) NOT NULL,
    machine_model VARCHAR(256),
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(19,4),
    line_total DECIMAL(19,4),
    additional_charges TEXT,
    remark VARCHAR(500),
    CONSTRAINT fk_freight_bill_item_bill FOREIGN KEY (bill_id) REFERENCES freight_bill(id),
    CONSTRAINT fk_freight_bill_item_shipment FOREIGN KEY (shipment_id) REFERENCES shipment(id)
);

CREATE INDEX IF NOT EXISTS ix_freight_bill_item_bill_id ON freight_bill_item(bill_id);
CREATE INDEX IF NOT EXISTS ix_freight_bill_item_shipment_id ON freight_bill_item(shipment_id);
