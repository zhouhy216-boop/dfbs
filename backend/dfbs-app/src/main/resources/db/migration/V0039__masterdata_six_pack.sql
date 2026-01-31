-- V0039__masterdata_six_pack.sql
-- Master Data Six-Pack: contracts, machine_models, spare_parts, model_part_lists, machines, sim_cards, logs.
-- Soft delete via status (ENABLE/DISABLE). Auditing: created_at, created_by, updated_at, updated_by.

-- 1) contracts
CREATE TABLE IF NOT EXISTS contracts (
    id BIGSERIAL PRIMARY KEY,
    contract_no VARCHAR(64) NOT NULL,
    customer_id BIGINT NOT NULL,
    start_date DATE,
    end_date DATE,
    attachment TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64)
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_contracts_contract_no ON contracts(contract_no);
CREATE INDEX IF NOT EXISTS ix_contracts_customer_id ON contracts(customer_id);
CREATE INDEX IF NOT EXISTS ix_contracts_status ON contracts(status);

-- 2) machine_models
CREATE TABLE IF NOT EXISTS machine_models (
    id BIGSERIAL PRIMARY KEY,
    model_name VARCHAR(200),
    model_no VARCHAR(64) NOT NULL,
    freight_info TEXT,
    warranty_info TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64)
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_machine_models_model_no ON machine_models(model_no);
CREATE INDEX IF NOT EXISTS ix_machine_models_status ON machine_models(status);

-- 3) spare_parts
CREATE TABLE IF NOT EXISTS spare_parts (
    id BIGSERIAL PRIMARY KEY,
    part_no VARCHAR(64) NOT NULL,
    name VARCHAR(200) NOT NULL,
    spec VARCHAR(500),
    unit VARCHAR(32) DEFAULT '个',
    reference_price NUMERIC(19,4),
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64)
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_spare_parts_part_no ON spare_parts(part_no);
CREATE INDEX IF NOT EXISTS ix_spare_parts_status ON spare_parts(status);

-- 4) model_part_lists (BOM)
CREATE TABLE IF NOT EXISTS model_part_lists (
    id BIGSERIAL PRIMARY KEY,
    model_id BIGINT NOT NULL,
    version VARCHAR(32) NOT NULL,
    effective_date DATE,
    items TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64)
);
CREATE INDEX IF NOT EXISTS ix_model_part_lists_model_id ON model_part_lists(model_id);
CREATE INDEX IF NOT EXISTS ix_model_part_lists_status ON model_part_lists(status);

-- 5) machines
CREATE TABLE IF NOT EXISTS machines (
    id BIGSERIAL PRIMARY KEY,
    machine_no VARCHAR(64) NOT NULL,
    serial_no VARCHAR(64) NOT NULL,
    customer_id BIGINT,
    contract_id BIGINT,
    model_id BIGINT,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64)
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_machines_machine_no ON machines(machine_no);
CREATE UNIQUE INDEX IF NOT EXISTS uk_machines_serial_no ON machines(serial_no);
CREATE INDEX IF NOT EXISTS ix_machines_customer_id ON machines(customer_id);
CREATE INDEX IF NOT EXISTS ix_machines_status ON machines(status);

-- 6) sim_cards
CREATE TABLE IF NOT EXISTS sim_cards (
    id BIGSERIAL PRIMARY KEY,
    card_no VARCHAR(64) NOT NULL,
    operator VARCHAR(64),
    plan_info TEXT,
    machine_id BIGINT,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64)
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sim_cards_card_no ON sim_cards(card_no);
CREATE INDEX IF NOT EXISTS ix_sim_cards_status ON sim_cards(status);
CREATE INDEX IF NOT EXISTS ix_sim_cards_machine_id ON sim_cards(machine_id);

-- 7) machine_ownership_log
CREATE TABLE IF NOT EXISTS machine_ownership_log (
    id BIGSERIAL PRIMARY KEY,
    machine_id BIGINT NOT NULL,
    old_customer_id BIGINT,
    new_customer_id BIGINT,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    changed_by VARCHAR(64),
    remark TEXT
);
CREATE INDEX IF NOT EXISTS ix_machine_ownership_log_machine_id ON machine_ownership_log(machine_id);

-- 8) sim_binding_log
CREATE TABLE IF NOT EXISTS sim_binding_log (
    id BIGSERIAL PRIMARY KEY,
    sim_id BIGINT NOT NULL,
    action VARCHAR(32) NOT NULL,
    machine_id BIGINT,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    changed_by VARCHAR(64),
    remark TEXT
);
CREATE INDEX IF NOT EXISTS ix_sim_binding_log_sim_id ON sim_binding_log(sim_id);
