-- V0043__work_order_service_flow.sql
-- Work Order Service Flow: extend work_order, add work_order_record, work_order_part.

-- 1) Extend work_order with new columns (order_no, type, customer/service fields, audit)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'work_order' AND column_name = 'order_no') THEN
        ALTER TABLE work_order ADD COLUMN order_no VARCHAR(64);
        UPDATE work_order SET order_no = 'WO-LEGACY-' || id WHERE order_no IS NULL;
        ALTER TABLE work_order ALTER COLUMN order_no SET NOT NULL;
        CREATE UNIQUE INDEX IF NOT EXISTS uk_work_order_order_no ON work_order(order_no);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'work_order' AND column_name = 'type') THEN
        ALTER TABLE work_order ADD COLUMN type VARCHAR(32) NOT NULL DEFAULT 'REPAIR';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'work_order' AND column_name = 'customer_name') THEN
        ALTER TABLE work_order ADD COLUMN customer_name VARCHAR(256) NOT NULL DEFAULT '';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'work_order' AND column_name = 'contact_person') THEN
        ALTER TABLE work_order ADD COLUMN contact_person VARCHAR(128) NOT NULL DEFAULT '';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'work_order' AND column_name = 'contact_phone') THEN
        ALTER TABLE work_order ADD COLUMN contact_phone VARCHAR(64) NOT NULL DEFAULT '';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'work_order' AND column_name = 'service_address') THEN
        ALTER TABLE work_order ADD COLUMN service_address VARCHAR(500) NOT NULL DEFAULT '';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'work_order' AND column_name = 'device_model_id') THEN
        ALTER TABLE work_order ADD COLUMN device_model_id BIGINT;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'work_order' AND column_name = 'machine_no') THEN
        ALTER TABLE work_order ADD COLUMN machine_no VARCHAR(128);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'work_order' AND column_name = 'issue_description') THEN
        ALTER TABLE work_order ADD COLUMN issue_description TEXT;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'work_order' AND column_name = 'appointment_time') THEN
        ALTER TABLE work_order ADD COLUMN appointment_time TIMESTAMP;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'work_order' AND column_name = 'dispatcher_id') THEN
        ALTER TABLE work_order ADD COLUMN dispatcher_id BIGINT;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'work_order' AND column_name = 'service_manager_id') THEN
        ALTER TABLE work_order ADD COLUMN service_manager_id BIGINT;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'work_order' AND column_name = 'customer_signature_url') THEN
        ALTER TABLE work_order ADD COLUMN customer_signature_url VARCHAR(512);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'work_order' AND column_name = 'created_by') THEN
        ALTER TABLE work_order ADD COLUMN created_by VARCHAR(64);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'work_order' AND column_name = 'updated_at') THEN
        ALTER TABLE work_order ADD COLUMN updated_at TIMESTAMP;
        UPDATE work_order SET updated_at = created_at WHERE updated_at IS NULL;
        ALTER TABLE work_order ALTER COLUMN updated_at SET NOT NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'work_order' AND column_name = 'updated_by') THEN
        ALTER TABLE work_order ADD COLUMN updated_by VARCHAR(64);
    END IF;
END $$;

-- Allow quote_id/initiator_id nullable for work orders not created from quote
ALTER TABLE work_order ALTER COLUMN quote_id DROP NOT NULL;
ALTER TABLE work_order ALTER COLUMN initiator_id DROP NOT NULL;

CREATE INDEX IF NOT EXISTS ix_work_order_service_manager_id ON work_order(service_manager_id);
CREATE INDEX IF NOT EXISTS ix_work_order_type ON work_order(type);

-- 2) work_order_record
CREATE TABLE IF NOT EXISTS work_order_record (
    id BIGSERIAL PRIMARY KEY,
    work_order_id BIGINT NOT NULL,
    description TEXT,
    attachment_url VARCHAR(512),
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(64),
    updated_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(64)
);
CREATE INDEX IF NOT EXISTS ix_work_order_record_work_order_id ON work_order_record(work_order_id);

-- 3) work_order_part
CREATE TABLE IF NOT EXISTS work_order_part (
    id BIGSERIAL PRIMARY KEY,
    work_order_id BIGINT NOT NULL,
    part_no VARCHAR(128) NOT NULL,
    part_name VARCHAR(256),
    quantity INTEGER NOT NULL,
    usage_status VARCHAR(32) NOT NULL,
    warehouse_id BIGINT,
    stock_record_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(64),
    updated_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(64)
);
CREATE INDEX IF NOT EXISTS ix_work_order_part_work_order_id ON work_order_part(work_order_id);
