-- V0038__after_sales_mvp.sql
-- After-Sales (Exchange/Repair) from Shipment.

CREATE TABLE IF NOT EXISTS after_sales (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    source_shipment_id BIGINT NOT NULL,
    machine_no VARCHAR(128) NOT NULL,
    reason TEXT,
    attachments TEXT,
    related_new_shipment_id BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS ix_after_sales_source_shipment ON after_sales(source_shipment_id);
CREATE INDEX IF NOT EXISTS ix_after_sales_status ON after_sales(status);
CREATE INDEX IF NOT EXISTS ix_after_sales_machine_no ON after_sales(machine_no);
