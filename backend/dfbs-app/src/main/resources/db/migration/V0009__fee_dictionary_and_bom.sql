-- V0009__fee_dictionary_and_bom.sql
-- Create Fee Dictionary and BOM tables, update quote_item

-- 1. Create fee_category table
CREATE TABLE IF NOT EXISTS fee_category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    is_active BOOLEAN NOT NULL DEFAULT true
);

-- 2. Create fee_type table
CREATE TABLE IF NOT EXISTS fee_type (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    category_id BIGINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    default_unit VARCHAR(32),
    allowed_units VARCHAR(500),
    replacement_fee_type_id BIGINT,
    fixed_spec_options VARCHAR(500),
    CONSTRAINT fk_fee_type_category FOREIGN KEY (category_id) REFERENCES fee_category(id)
);

-- 3. Create part table
CREATE TABLE IF NOT EXISTS part (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    spec VARCHAR(500),
    unit VARCHAR(32) DEFAULT '个',
    is_active BOOLEAN NOT NULL DEFAULT true,
    replacement_part_id BIGINT,
    CONSTRAINT fk_part_replacement FOREIGN KEY (replacement_part_id) REFERENCES part(id)
);

-- 4. Create product_bom table
CREATE TABLE IF NOT EXISTS product_bom (
    id BIGSERIAL PRIMARY KEY,
    product_id UUID NOT NULL,
    part_id BIGINT NOT NULL,
    qty INTEGER NOT NULL,
    CONSTRAINT fk_product_bom_product FOREIGN KEY (product_id) REFERENCES md_product(id),
    CONSTRAINT fk_product_bom_part FOREIGN KEY (part_id) REFERENCES part(id)
);

-- 5. Alter quote_item table: add fee_type_id and part_id
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'quote_item' AND column_name = 'fee_type_id') THEN
        ALTER TABLE quote_item ADD COLUMN fee_type_id BIGINT;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'quote_item' AND column_name = 'part_id') THEN
        ALTER TABLE quote_item ADD COLUMN part_id BIGINT;
    END IF;
END $$;

-- Add foreign key constraints for quote_item
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                   WHERE table_name = 'quote_item' 
                   AND constraint_name = 'fk_quote_item_fee_type') THEN
        ALTER TABLE quote_item
            ADD CONSTRAINT fk_quote_item_fee_type
            FOREIGN KEY (fee_type_id) REFERENCES fee_type(id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                   WHERE table_name = 'quote_item' 
                   AND constraint_name = 'fk_quote_item_part') THEN
        ALTER TABLE quote_item
            ADD CONSTRAINT fk_quote_item_part
            FOREIGN KEY (part_id) REFERENCES part(id);
    END IF;
END $$;

-- Create indexes
CREATE INDEX IF NOT EXISTS ix_fee_type_category_id ON fee_type(category_id);
CREATE INDEX IF NOT EXISTS ix_fee_type_active ON fee_type(is_active);
CREATE INDEX IF NOT EXISTS ix_part_active ON part(is_active);
CREATE INDEX IF NOT EXISTS ix_product_bom_product_id ON product_bom(product_id);
CREATE INDEX IF NOT EXISTS ix_product_bom_part_id ON product_bom(part_id);
CREATE INDEX IF NOT EXISTS ix_quote_item_fee_type_id ON quote_item(fee_type_id);
CREATE INDEX IF NOT EXISTS ix_quote_item_part_id ON quote_item(part_id);

-- ============================================
-- DATA SEEDING: Default Built-in Data
-- ============================================

-- Insert Fee Categories
INSERT INTO fee_category (name, is_active) VALUES
    ('配件费', true),
    ('维修费', true),
    ('服务费', true),
    ('平台费', true),
    ('流量费', true),
    ('包装费', true)
ON CONFLICT (name) DO NOTHING;

-- Insert Fee Types (with category mapping)
-- Note: Using subquery to get category_id by name
INSERT INTO fee_type (name, category_id, is_active, default_unit, allowed_units, fixed_spec_options) VALUES
    ('技术服务费', (SELECT id FROM fee_category WHERE name = '维修费'), true, '次', '次', NULL),
    ('登门费', (SELECT id FROM fee_category WHERE name = '服务费'), true, '次', '次', NULL),
    ('配件费', (SELECT id FROM fee_category WHERE name = '配件费'), true, '个', '个', NULL),
    ('平台服务费', (SELECT id FROM fee_category WHERE name = '平台费'), true, '点', '点', NULL),
    ('物联卡流量费', (SELECT id FROM fee_category WHERE name = '流量费'), true, '月', '月,年', '月,年'),
    ('仓储费', (SELECT id FROM fee_category WHERE name = '服务费'), true, '次', '次,日,月', '日,月'),
    ('运输费', (SELECT id FROM fee_category WHERE name = '服务费'), true, '次', '次', NULL),
    ('包装费', (SELECT id FROM fee_category WHERE name = '包装费'), true, '次', '次', NULL),
    ('施工费', (SELECT id FROM fee_category WHERE name = '服务费'), true, '次', '次', NULL),
    ('翻新费', (SELECT id FROM fee_category WHERE name = '服务费'), true, '次', '次', NULL),
    ('特殊费', (SELECT id FROM fee_category WHERE name = '服务费'), true, '次', '次', NULL),
    ('人工上楼费', (SELECT id FROM fee_category WHERE name = '服务费'), true, '次', '次', NULL)
ON CONFLICT (name) DO NOTHING;
