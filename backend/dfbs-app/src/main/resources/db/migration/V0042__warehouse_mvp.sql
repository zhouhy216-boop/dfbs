-- V0042__warehouse_mvp.sql
-- Warehouse MVP: wh_warehouse, wh_inventory, wh_stock_record, wh_replenish_request (audit columns).

-- 1) wh_warehouse (Central/Satellite; Central is singleton logically)
CREATE TABLE IF NOT EXISTS wh_warehouse (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    type VARCHAR(32) NOT NULL,
    manager_id BIGINT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(64),
    updated_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(64)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_wh_warehouse_name ON wh_warehouse(name);
CREATE INDEX IF NOT EXISTS ix_wh_warehouse_type ON wh_warehouse(type);
CREATE INDEX IF NOT EXISTS ix_wh_warehouse_is_active ON wh_warehouse(is_active);

-- 2) wh_inventory (snapshot: warehouse + part_no, quantity, safety_threshold)
CREATE TABLE IF NOT EXISTS wh_inventory (
    id BIGSERIAL PRIMARY KEY,
    warehouse_id BIGINT NOT NULL,
    part_no VARCHAR(128) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0,
    safety_threshold INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(64),
    updated_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(64),
    CONSTRAINT uq_wh_inventory_warehouse_part UNIQUE (warehouse_id, part_no)
);

CREATE INDEX IF NOT EXISTS ix_wh_inventory_warehouse_id ON wh_inventory(warehouse_id);
CREATE INDEX IF NOT EXISTS ix_wh_inventory_warehouse_part ON wh_inventory(warehouse_id, part_no);

-- 3) wh_stock_record (inbound/outbound/replenish log)
CREATE TABLE IF NOT EXISTS wh_stock_record (
    id BIGSERIAL PRIMARY KEY,
    warehouse_id BIGINT NOT NULL,
    part_no VARCHAR(128) NOT NULL,
    type VARCHAR(32) NOT NULL,
    quantity INTEGER NOT NULL,
    balance_after INTEGER NOT NULL,
    ref_type VARCHAR(32),
    ref_no VARCHAR(64),
    remark VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(64),
    updated_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(64)
);

CREATE INDEX IF NOT EXISTS ix_wh_stock_record_warehouse_part ON wh_stock_record(warehouse_id, part_no);
CREATE INDEX IF NOT EXISTS ix_wh_stock_record_created_at ON wh_stock_record(created_at);

-- 4) wh_replenish_request (L1/L2 approval workflow)
CREATE TABLE IF NOT EXISTS wh_replenish_request (
    id BIGSERIAL PRIMARY KEY,
    request_no VARCHAR(64) NOT NULL,
    target_warehouse_id BIGINT NOT NULL,
    applicant_id BIGINT NOT NULL,
    part_no VARCHAR(128) NOT NULL,
    quantity INTEGER NOT NULL,
    reason VARCHAR(500),
    status VARCHAR(32) NOT NULL,
    l1_approver_id BIGINT,
    l1_comment VARCHAR(500),
    l1_time TIMESTAMP,
    l2_approver_id BIGINT,
    l2_comment VARCHAR(500),
    l2_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(64),
    updated_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(64)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_wh_replenish_request_no ON wh_replenish_request(request_no);
CREATE INDEX IF NOT EXISTS ix_wh_replenish_request_status ON wh_replenish_request(status);
CREATE INDEX IF NOT EXISTS ix_wh_replenish_request_target ON wh_replenish_request(target_warehouse_id);
