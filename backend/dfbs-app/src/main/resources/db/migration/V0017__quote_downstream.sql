-- V0017__quote_downstream.sql
-- Quote downstream entry: Shipment and WorkOrder (one quote -> one downstream doc)

CREATE TABLE IF NOT EXISTS shipment (
    id BIGSERIAL PRIMARY KEY,
    quote_id BIGINT NOT NULL,
    initiator_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED',
    entrust_matter VARCHAR(500),
    ship_date DATE,
    quantity INTEGER,
    model VARCHAR(256),
    need_packaging BOOLEAN,
    pickup_contact VARCHAR(128),
    pickup_phone VARCHAR(64),
    need_loading BOOLEAN,
    pickup_address VARCHAR(500),
    receiver_contact VARCHAR(128),
    receiver_phone VARCHAR(64),
    need_unloading BOOLEAN,
    delivery_address VARCHAR(500),
    remark TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_shipment_quote FOREIGN KEY (quote_id) REFERENCES quote(id)
);

CREATE INDEX IF NOT EXISTS ix_shipment_quote_id ON shipment(quote_id);
CREATE INDEX IF NOT EXISTS ix_shipment_status ON shipment(status);

CREATE TABLE IF NOT EXISTS work_order (
    id BIGSERIAL PRIMARY KEY,
    quote_id BIGINT NOT NULL,
    initiator_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED',
    summary VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_work_order_quote FOREIGN KEY (quote_id) REFERENCES quote(id)
);

CREATE INDEX IF NOT EXISTS ix_work_order_quote_id ON work_order(quote_id);
CREATE INDEX IF NOT EXISTS ix_work_order_status ON work_order(status);

-- Add downstream link columns to quote (idempotent)
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'quote' AND column_name = 'downstream_type') THEN
    ALTER TABLE quote ADD COLUMN downstream_type VARCHAR(32);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'quote' AND column_name = 'downstream_id') THEN
    ALTER TABLE quote ADD COLUMN downstream_id BIGINT;
  END IF;
END $$;

CREATE INDEX IF NOT EXISTS ix_quote_downstream ON quote(downstream_type, downstream_id) WHERE downstream_id IS NOT NULL;
