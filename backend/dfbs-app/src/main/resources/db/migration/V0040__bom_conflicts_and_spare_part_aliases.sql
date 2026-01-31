-- V0040__bom_conflicts_and_spare_part_aliases.sql
-- BOM draft/conflict: spare_parts.aliases, bom_conflicts table. BOM status: DRAFT/PUBLISHED/DEPRECATED.

UPDATE model_part_lists SET status = 'PUBLISHED' WHERE status = 'ENABLE';
ALTER TABLE spare_parts ADD COLUMN IF NOT EXISTS aliases TEXT;

CREATE TABLE IF NOT EXISTS bom_conflicts (
    id BIGSERIAL PRIMARY KEY,
    bom_id BIGINT NOT NULL,
    row_part_no VARCHAR(64),
    row_name VARCHAR(200),
    row_index INT,
    type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING'
);
CREATE INDEX IF NOT EXISTS ix_bom_conflicts_bom_id ON bom_conflicts(bom_id);
CREATE INDEX IF NOT EXISTS ix_bom_conflicts_status ON bom_conflicts(status);
