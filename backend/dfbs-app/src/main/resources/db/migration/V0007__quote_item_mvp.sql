-- V0007__quote_item_mvp.sql
-- 报价明细（QuoteItem）MVP 表结构
-- 关联新的 quote 表（quote_id BIGINT），而非 quote_version
-- 处理 quantity 字段类型转换（从 NUMERIC 到 INTEGER）

-- 添加新字段（如果不存在）
DO $$
BEGIN
    -- quote_id (关联新的 quote 表)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'quote_item' AND column_name = 'quote_id') THEN
        ALTER TABLE quote_item ADD COLUMN quote_id BIGINT;
    END IF;

    -- line_order (序号)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'quote_item' AND column_name = 'line_order') THEN
        ALTER TABLE quote_item ADD COLUMN line_order INTEGER;
    END IF;

    -- expense_type (费用类型枚举)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'quote_item' AND column_name = 'expense_type') THEN
        ALTER TABLE quote_item ADD COLUMN expense_type VARCHAR(32);
    END IF;

    -- description (描述)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'quote_item' AND column_name = 'description') THEN
        ALTER TABLE quote_item ADD COLUMN description VARCHAR(1000);
    END IF;

    -- spec (规格型号)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'quote_item' AND column_name = 'spec') THEN
        ALTER TABLE quote_item ADD COLUMN spec VARCHAR(500);
    END IF;

    -- unit (单位)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'quote_item' AND column_name = 'unit') THEN
        ALTER TABLE quote_item ADD COLUMN unit VARCHAR(32);
    END IF;

    -- warehouse (仓库枚举)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'quote_item' AND column_name = 'warehouse') THEN
        ALTER TABLE quote_item ADD COLUMN warehouse VARCHAR(32);
    END IF;

    -- 预留字段 attr1-attr10
    FOR i IN 1..10 LOOP
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                       WHERE table_name = 'quote_item' AND column_name = 'attr' || i) THEN
            EXECUTE format('ALTER TABLE quote_item ADD COLUMN attr%s VARCHAR(500)', i);
        END IF;
    END LOOP;
END $$;

-- 修复 quantity 字段类型：从 NUMERIC(19,2) 转换为 INTEGER
DO $$
BEGIN
    -- 检查 quantity 字段是否存在且类型为 numeric
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'quote_item' 
               AND column_name = 'quantity' 
               AND data_type = 'numeric') THEN
        -- 添加新字段
        ALTER TABLE quote_item ADD COLUMN quantity_new INTEGER;
        -- 迁移数据（如果有）
        UPDATE quote_item SET quantity_new = quantity::INTEGER WHERE quantity IS NOT NULL;
        -- 删除旧字段
        ALTER TABLE quote_item DROP COLUMN quantity;
        -- 重命名新字段
        ALTER TABLE quote_item RENAME COLUMN quantity_new TO quantity;
        -- 设置 NOT NULL
        ALTER TABLE quote_item ALTER COLUMN quantity SET NOT NULL;
    END IF;
END $$;

-- 修改 quote_version_id 约束：允许为 NULL（因为新业务逻辑使用 quote_id）
DO $$
BEGIN
    -- 检查 quote_version_id 字段是否存在且为 NOT NULL
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'quote_item' 
               AND column_name = 'quote_version_id' 
               AND is_nullable = 'NO') THEN
        -- 移除 NOT NULL 约束
        ALTER TABLE quote_item ALTER COLUMN quote_version_id DROP NOT NULL;
    END IF;
END $$;

-- 修改 item_type 约束：允许为 NULL（因为新业务逻辑使用 expense_type）
DO $$
BEGIN
    -- 检查 item_type 字段是否存在且为 NOT NULL
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'quote_item' 
               AND column_name = 'item_type' 
               AND is_nullable = 'NO') THEN
        -- 移除 NOT NULL 约束
        ALTER TABLE quote_item ALTER COLUMN item_type DROP NOT NULL;
    END IF;
END $$;

-- 修改 source_type 约束：允许为 NULL（因为新业务逻辑不填充该字段）
DO $$
BEGIN
    -- 检查 source_type 字段是否存在且为 NOT NULL
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'quote_item' 
               AND column_name = 'source_type' 
               AND is_nullable = 'NO') THEN
        -- 移除 NOT NULL 约束
        ALTER TABLE quote_item ALTER COLUMN source_type DROP NOT NULL;
    END IF;
END $$;

-- 修改 item_name 约束：允许为 NULL（因为新业务逻辑使用 description）
DO $$
BEGIN
    -- 检查 item_name 字段是否存在且为 NOT NULL
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'quote_item' 
               AND column_name = 'item_name' 
               AND is_nullable = 'NO') THEN
        -- 移除 NOT NULL 约束
        ALTER TABLE quote_item ALTER COLUMN item_name DROP NOT NULL;
    END IF;
END $$;

-- 创建索引
CREATE INDEX IF NOT EXISTS ix_quote_item_quote_id ON quote_item(quote_id);
CREATE INDEX IF NOT EXISTS ix_quote_item_line_order ON quote_item(quote_id, line_order);

-- 添加外键约束（如果 quote 表存在）
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'quote') THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                       WHERE table_name = 'quote_item' 
                       AND constraint_name = 'fk_quote_item_quote_id') THEN
            ALTER TABLE quote_item
                ADD CONSTRAINT fk_quote_item_quote_id
                FOREIGN KEY (quote_id) REFERENCES quote(id);
        END IF;
    END IF;
END $$;
