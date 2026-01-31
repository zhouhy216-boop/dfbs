-- V0031__customer_merge_support.sql
-- Customer Merge MVP: id as BIGINT (match Quote), merged_to_id, unique name (active only), alias & merge_log tables.
-- Safe UUID -> BIGINT: drop old id, add new id with sequence default (PostgreSQL assigns 1,2,3.. to existing rows).

-- 1) Replace md_customer.id (UUID) with BIGINT. No FK points to md_customer.id (only customer_code is referenced).
CREATE SEQUENCE IF NOT EXISTS md_customer_id_seq;

ALTER TABLE md_customer DROP CONSTRAINT IF EXISTS md_customer_pkey;
ALTER TABLE md_customer DROP COLUMN IF EXISTS id;

ALTER TABLE md_customer ADD COLUMN id BIGINT NOT NULL DEFAULT nextval('md_customer_id_seq');
ALTER TABLE md_customer ADD PRIMARY KEY (id);
ALTER TABLE md_customer ALTER COLUMN id SET DEFAULT nextval('md_customer_id_seq');

SELECT setval('md_customer_id_seq', (SELECT COALESCE(MAX(id), 1) FROM md_customer));

-- 2) Add merged_to_id and partial unique index on name (active only)
ALTER TABLE md_customer ADD COLUMN IF NOT EXISTS merged_to_id BIGINT NULL;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_md_customer_merged_to') THEN
        ALTER TABLE md_customer ADD CONSTRAINT fk_md_customer_merged_to
            FOREIGN KEY (merged_to_id) REFERENCES md_customer(id);
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS uk_md_customer_name_active
    ON md_customer(name)
    WHERE status = 'ACTIVE' AND deleted_at IS NULL;

-- 3) md_customer_alias (drop first so re-run after partial failure is safe)
DROP TABLE IF EXISTS md_customer_merge_log;
DROP TABLE IF EXISTS md_customer_alias;

CREATE TABLE IF NOT EXISTS md_customer_alias (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    alias_name VARCHAR(200) NOT NULL,
    CONSTRAINT fk_customer_alias_customer FOREIGN KEY (customer_id) REFERENCES md_customer(id)
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_md_customer_alias_name ON md_customer_alias(alias_name);

-- 4) md_customer_merge_log (dropped above)
CREATE TABLE IF NOT EXISTS md_customer_merge_log (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by VARCHAR(128),
    source_customer_id BIGINT NOT NULL,
    target_customer_id BIGINT NOT NULL,
    source_snapshot TEXT,
    target_snapshot TEXT,
    merge_reason VARCHAR(512)
);
