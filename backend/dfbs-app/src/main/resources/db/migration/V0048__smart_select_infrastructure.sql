-- V0048__smart_select_infrastructure.sql
-- Smart Select V2: is_temp (temp pool) and last_used_at (MRU) on master tables.
-- Unique key indexes already exist: uk_* on contract_no, model_no, part_no, machine_no, serial_no, card_no, etc.

-- md_customer
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'md_customer' AND column_name = 'is_temp') THEN
        ALTER TABLE md_customer ADD COLUMN is_temp BOOLEAN NOT NULL DEFAULT false;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'md_customer' AND column_name = 'last_used_at') THEN
        ALTER TABLE md_customer ADD COLUMN last_used_at TIMESTAMP;
    END IF;
END $$;
CREATE INDEX IF NOT EXISTS ix_md_customer_is_temp ON md_customer(is_temp);
CREATE INDEX IF NOT EXISTS ix_md_customer_last_used_at ON md_customer(last_used_at) WHERE last_used_at IS NOT NULL;

-- contracts
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'contracts' AND column_name = 'is_temp') THEN
        ALTER TABLE contracts ADD COLUMN is_temp BOOLEAN NOT NULL DEFAULT false;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'contracts' AND column_name = 'last_used_at') THEN
        ALTER TABLE contracts ADD COLUMN last_used_at TIMESTAMP;
    END IF;
END $$;
CREATE INDEX IF NOT EXISTS ix_contracts_is_temp ON contracts(is_temp);
CREATE INDEX IF NOT EXISTS ix_contracts_last_used_at ON contracts(last_used_at) WHERE last_used_at IS NOT NULL;

-- machine_models
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'machine_models' AND column_name = 'is_temp') THEN
        ALTER TABLE machine_models ADD COLUMN is_temp BOOLEAN NOT NULL DEFAULT false;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'machine_models' AND column_name = 'last_used_at') THEN
        ALTER TABLE machine_models ADD COLUMN last_used_at TIMESTAMP;
    END IF;
END $$;
CREATE INDEX IF NOT EXISTS ix_machine_models_is_temp ON machine_models(is_temp);
CREATE INDEX IF NOT EXISTS ix_machine_models_last_used_at ON machine_models(last_used_at) WHERE last_used_at IS NOT NULL;

-- spare_parts
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'spare_parts' AND column_name = 'is_temp') THEN
        ALTER TABLE spare_parts ADD COLUMN is_temp BOOLEAN NOT NULL DEFAULT false;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'spare_parts' AND column_name = 'last_used_at') THEN
        ALTER TABLE spare_parts ADD COLUMN last_used_at TIMESTAMP;
    END IF;
END $$;
CREATE INDEX IF NOT EXISTS ix_spare_parts_is_temp ON spare_parts(is_temp);
CREATE INDEX IF NOT EXISTS ix_spare_parts_last_used_at ON spare_parts(last_used_at) WHERE last_used_at IS NOT NULL;

-- machines
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'machines' AND column_name = 'is_temp') THEN
        ALTER TABLE machines ADD COLUMN is_temp BOOLEAN NOT NULL DEFAULT false;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'machines' AND column_name = 'last_used_at') THEN
        ALTER TABLE machines ADD COLUMN last_used_at TIMESTAMP;
    END IF;
END $$;
CREATE INDEX IF NOT EXISTS ix_machines_is_temp ON machines(is_temp);
CREATE INDEX IF NOT EXISTS ix_machines_last_used_at ON machines(last_used_at) WHERE last_used_at IS NOT NULL;

-- sim_cards
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'sim_cards' AND column_name = 'is_temp') THEN
        ALTER TABLE sim_cards ADD COLUMN is_temp BOOLEAN NOT NULL DEFAULT false;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'sim_cards' AND column_name = 'last_used_at') THEN
        ALTER TABLE sim_cards ADD COLUMN last_used_at TIMESTAMP;
    END IF;
END $$;
CREATE INDEX IF NOT EXISTS ix_sim_cards_is_temp ON sim_cards(is_temp);
CREATE INDEX IF NOT EXISTS ix_sim_cards_last_used_at ON sim_cards(last_used_at) WHERE last_used_at IS NOT NULL;

-- model_part_lists (extends BaseMasterEntity)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'model_part_lists' AND column_name = 'is_temp') THEN
        ALTER TABLE model_part_lists ADD COLUMN is_temp BOOLEAN NOT NULL DEFAULT false;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'model_part_lists' AND column_name = 'last_used_at') THEN
        ALTER TABLE model_part_lists ADD COLUMN last_used_at TIMESTAMP;
    END IF;
END $$;
CREATE INDEX IF NOT EXISTS ix_model_part_lists_is_temp ON model_part_lists(is_temp);
CREATE INDEX IF NOT EXISTS ix_model_part_lists_last_used_at ON model_part_lists(last_used_at) WHERE last_used_at IS NOT NULL;
