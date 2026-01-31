-- V0022__inventory_mvp.sql
-- Two-tier inventory: warehouse, inventory, inventory_log, transfer_order, special_outbound_request

CREATE TABLE IF NOT EXISTS warehouse (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    type VARCHAR(32) NOT NULL,
    manager_id BIGINT
);

CREATE TABLE IF NOT EXISTS inventory (
    id BIGSERIAL PRIMARY KEY,
    warehouse_id BIGINT NOT NULL,
    sku VARCHAR(128) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    CONSTRAINT uq_inventory_warehouse_sku UNIQUE (warehouse_id, sku),
    CONSTRAINT fk_inventory_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouse(id)
);

CREATE INDEX IF NOT EXISTS ix_inventory_warehouse_id ON inventory(warehouse_id);
CREATE INDEX IF NOT EXISTS ix_inventory_warehouse_sku ON inventory(warehouse_id, sku);

CREATE TABLE IF NOT EXISTS inventory_log (
    id BIGSERIAL PRIMARY KEY,
    warehouse_id BIGINT NOT NULL,
    sku VARCHAR(128) NOT NULL,
    change_amount INTEGER NOT NULL,
    after_quantity INTEGER NOT NULL,
    type VARCHAR(32) NOT NULL,
    related_id BIGINT,
    operator_id BIGINT,
    remark VARCHAR(500),
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_inventory_log_warehouse_sku ON inventory_log(warehouse_id, sku);

CREATE TABLE IF NOT EXISTS transfer_order (
    id BIGSERIAL PRIMARY KEY,
    source_warehouse_id BIGINT NOT NULL,
    target_warehouse_id BIGINT NOT NULL,
    sku VARCHAR(128) NOT NULL,
    quantity INTEGER NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    logistics_url VARCHAR(512),
    apply_reason VARCHAR(500),
    audit_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    operator_id BIGINT,
    CONSTRAINT fk_transfer_source FOREIGN KEY (source_warehouse_id) REFERENCES warehouse(id),
    CONSTRAINT fk_transfer_target FOREIGN KEY (target_warehouse_id) REFERENCES warehouse(id)
);

CREATE INDEX IF NOT EXISTS ix_transfer_order_source ON transfer_order(source_warehouse_id);
CREATE INDEX IF NOT EXISTS ix_transfer_order_target ON transfer_order(target_warehouse_id);

CREATE TABLE IF NOT EXISTS special_outbound_request (
    id BIGSERIAL PRIMARY KEY,
    warehouse_id BIGINT NOT NULL,
    sku VARCHAR(128) NOT NULL,
    quantity INTEGER NOT NULL,
    type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING_APPROVAL',
    apply_reason VARCHAR(500),
    audit_reason VARCHAR(500),
    auditor_id BIGINT,
    audit_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    operator_id BIGINT,
    CONSTRAINT fk_special_outbound_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouse(id)
);

CREATE INDEX IF NOT EXISTS ix_special_outbound_warehouse ON special_outbound_request(warehouse_id);

-- Seed: 1 HQ, 2 Branch warehouses (idempotent: only when empty)
INSERT INTO warehouse (name, type, manager_id)
SELECT '总部大库', 'HQ', NULL WHERE NOT EXISTS (SELECT 1 FROM warehouse LIMIT 1);
INSERT INTO warehouse (name, type, manager_id)
SELECT '办事处A', 'BRANCH', NULL WHERE NOT EXISTS (SELECT 1 FROM warehouse WHERE name = '办事处A' LIMIT 1);
INSERT INTO warehouse (name, type, manager_id)
SELECT '办事处B', 'BRANCH', NULL WHERE NOT EXISTS (SELECT 1 FROM warehouse WHERE name = '办事处B' LIMIT 1);
