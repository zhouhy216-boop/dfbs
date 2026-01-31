-- V0023__repair_ledger.sql
-- Repair record (after-sales repair ledger)

CREATE TABLE IF NOT EXISTS repair_record (
    id BIGSERIAL PRIMARY KEY,
    customer_name VARCHAR(256) NOT NULL,
    machine_no VARCHAR(128) NOT NULL,
    machine_model VARCHAR(256) NOT NULL,
    repair_date TIMESTAMP NOT NULL,
    issue_description TEXT NOT NULL,
    resolution TEXT NOT NULL,
    person_in_charge VARCHAR(128) NOT NULL,
    warranty_status VARCHAR(32) NOT NULL,
    old_work_order_no VARCHAR(128) NOT NULL,
    source_type VARCHAR(32) NOT NULL,
    work_order_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    operator_id BIGINT,
    CONSTRAINT uq_repair_record_old_work_order_no UNIQUE (old_work_order_no)
);

CREATE UNIQUE INDEX IF NOT EXISTS ix_repair_record_old_work_order_no ON repair_record(old_work_order_no);
CREATE INDEX IF NOT EXISTS ix_repair_record_customer_name ON repair_record(customer_name);
CREATE INDEX IF NOT EXISTS ix_repair_record_machine_no ON repair_record(machine_no);
CREATE INDEX IF NOT EXISTS ix_repair_record_repair_date ON repair_record(repair_date);
