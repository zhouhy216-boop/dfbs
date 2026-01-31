-- V0027__bom_versioning.sql
-- Part master data: system_no, drawing_no, sales_price. BOM versioning: bom_version, bom_item.

-- 1. Alter part table: system_no, drawing_no, sales_price
ALTER TABLE part ADD COLUMN IF NOT EXISTS system_no VARCHAR(64) UNIQUE;
ALTER TABLE part ADD COLUMN IF NOT EXISTS drawing_no VARCHAR(128);
ALTER TABLE part ADD COLUMN IF NOT EXISTS sales_price NUMERIC(19,4) NOT NULL DEFAULT 0;

CREATE UNIQUE INDEX IF NOT EXISTS ix_part_system_no ON part(system_no) WHERE system_no IS NOT NULL;

-- 2. Create bom_version (machine_id is Long; no FK to md_machine to allow flexibility)
CREATE TABLE IF NOT EXISTS bom_version (
    id BIGSERIAL PRIMARY KEY,
    machine_id BIGINT NOT NULL,
    version INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT false,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    created_by BIGINT
);

CREATE INDEX IF NOT EXISTS ix_bom_version_machine_id ON bom_version(machine_id);
CREATE INDEX IF NOT EXISTS ix_bom_version_is_active ON bom_version(is_active);

-- 3. Create bom_item
CREATE TABLE IF NOT EXISTS bom_item (
    id BIGSERIAL PRIMARY KEY,
    version_id BIGINT NOT NULL,
    part_id BIGINT NOT NULL,
    index_no VARCHAR(32) NOT NULL,
    quantity NUMERIC(19,4) NOT NULL DEFAULT 1,
    is_optional BOOLEAN NOT NULL DEFAULT false,
    remark VARCHAR(500)
);

CREATE INDEX IF NOT EXISTS ix_bom_item_version_id ON bom_item(version_id);
CREATE INDEX IF NOT EXISTS ix_bom_item_part_id ON bom_item(part_id);
