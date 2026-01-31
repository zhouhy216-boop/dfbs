-- V0019__shipment_panorama.sql
-- Shipment panorama: type, approval, Normal/Entrust fields, shipment_machine

DO $$
BEGIN
  -- quote_id nullable (for Normal and Internal Entrust)
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'shipment' AND column_name = 'quote_id') THEN
    ALTER TABLE shipment ALTER COLUMN quote_id DROP NOT NULL;
  END IF;

  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'shipment' AND column_name = 'type') THEN
    ALTER TABLE shipment ADD COLUMN type VARCHAR(32) NOT NULL DEFAULT 'ENTRUST_CUSTOMER';
  END IF;

  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'shipment' AND column_name = 'approval_status') THEN
    ALTER TABLE shipment ADD COLUMN approval_status VARCHAR(32) NOT NULL DEFAULT 'APPROVED';
  END IF;

  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'shipment' AND column_name = 'applicant_id') THEN
    ALTER TABLE shipment ADD COLUMN applicant_id BIGINT;
  END IF;

  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'shipment' AND column_name = 'contract_no') THEN
    ALTER TABLE shipment ADD COLUMN contract_no VARCHAR(128);
  END IF;

  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'shipment' AND column_name = 'salesperson_name') THEN
    ALTER TABLE shipment ADD COLUMN salesperson_name VARCHAR(128);
  END IF;

  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'shipment' AND column_name = 'packaging_type') THEN
    ALTER TABLE shipment ADD COLUMN packaging_type VARCHAR(32);
  END IF;

  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'shipment' AND column_name = 'receiver_name') THEN
    ALTER TABLE shipment ADD COLUMN receiver_name VARCHAR(128);
  END IF;

  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'shipment' AND column_name = 'unload_service') THEN
    ALTER TABLE shipment ADD COLUMN unload_service BOOLEAN;
  END IF;

  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'shipment' AND column_name = 'receipt_url') THEN
    ALTER TABLE shipment ADD COLUMN receipt_url VARCHAR(512);
  END IF;

  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'shipment' AND column_name = 'ticket_url') THEN
    ALTER TABLE shipment ADD COLUMN ticket_url VARCHAR(512);
  END IF;
END $$;

CREATE TABLE IF NOT EXISTS shipment_machine (
    id BIGSERIAL PRIMARY KEY,
    shipment_id BIGINT NOT NULL,
    model VARCHAR(256),
    machine_no VARCHAR(128) NOT NULL,
    CONSTRAINT fk_shipment_machine_shipment FOREIGN KEY (shipment_id) REFERENCES shipment(id)
);

CREATE INDEX IF NOT EXISTS ix_shipment_machine_shipment_id ON shipment_machine(shipment_id);
