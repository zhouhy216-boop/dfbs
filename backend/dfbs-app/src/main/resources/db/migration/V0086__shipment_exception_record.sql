-- SHIPFLOW-260304-001-05: minimal exception records (shipment/device-level)
CREATE TABLE IF NOT EXISTS shipment_exception_record (
    id BIGSERIAL PRIMARY KEY,
    shipment_id BIGINT NOT NULL REFERENCES shipment(id),
    machine_id BIGINT NULL REFERENCES shipment_machine(id),
    exception_type VARCHAR(64) NULL,
    description TEXT NOT NULL,
    responsibility VARCHAR(128) NULL,
    evidence_url TEXT NULL,
    operator_id BIGINT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS ix_shipment_exception_record_shipment_created
    ON shipment_exception_record(shipment_id, created_at DESC);
COMMENT ON TABLE shipment_exception_record IS 'Manual exception records for shipment (and optional device); multiple per shipment.';
