-- V0020__damage_record.sql
-- Damage record: config tables + damage_record, seed data

CREATE TABLE IF NOT EXISTS damage_type (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    is_enabled BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE IF NOT EXISTS damage_treatment (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    behavior VARCHAR(32) NOT NULL,
    is_enabled BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE IF NOT EXISTS damage_record (
    id BIGSERIAL PRIMARY KEY,
    shipment_id BIGINT NOT NULL,
    shipment_machine_id BIGINT NOT NULL,
    occurrence_time TIMESTAMP NOT NULL,
    damage_type_id BIGINT NOT NULL,
    treatment_id BIGINT NOT NULL,
    description TEXT,
    attachment_urls TEXT NOT NULL,
    repair_stage VARCHAR(32),
    compensation_status VARCHAR(32),
    settlement_details TEXT,
    compensation_amount DECIMAL(19,4),
    repair_fee DECIMAL(19,4),
    penalty_amount DECIMAL(19,4),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    operator_id BIGINT,
    CONSTRAINT fk_damage_record_shipment FOREIGN KEY (shipment_id) REFERENCES shipment(id),
    CONSTRAINT fk_damage_record_shipment_machine FOREIGN KEY (shipment_machine_id) REFERENCES shipment_machine(id),
    CONSTRAINT fk_damage_record_damage_type FOREIGN KEY (damage_type_id) REFERENCES damage_type(id),
    CONSTRAINT fk_damage_record_treatment FOREIGN KEY (treatment_id) REFERENCES damage_treatment(id)
);

CREATE INDEX IF NOT EXISTS ix_damage_record_shipment_id ON damage_record(shipment_id);
CREATE INDEX IF NOT EXISTS ix_damage_record_shipment_machine_id ON damage_record(shipment_machine_id);

-- Seed damage types (idempotent: insert only if empty)
INSERT INTO damage_type (name, is_enabled)
SELECT * FROM (VALUES
    ('破损', true),
    ('丢件', true),
    ('浸水', true),
    ('污染', true),
    ('其他', true)
) AS v(name, is_enabled)
WHERE NOT EXISTS (SELECT 1 FROM damage_type LIMIT 1);

-- Seed damage treatments (idempotent: insert only if empty)
INSERT INTO damage_treatment (name, behavior, is_enabled)
SELECT * FROM (VALUES
    ('赔偿客户', 'COMPENSATION', true),
    ('返厂维修', 'REPAIR', true),
    ('自行处理', 'GENERAL', true)
) AS v(name, behavior, is_enabled)
WHERE NOT EXISTS (SELECT 1 FROM damage_treatment LIMIT 1);
