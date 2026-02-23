-- V0070__quote_item_expense_type_label_snapshot.sql
-- 1) Seed dict_type quote_expense_type (idempotent)
INSERT INTO dict_type (type_code, type_name, description, enabled, created_at, updated_at)
VALUES ('quote_expense_type', '报价明细类型', '报价单明细费用类型', true, NOW(), NOW())
ON CONFLICT (type_code) DO NOTHING;

-- 2) Seed dict_item rows (type_id from subquery; idempotent)
INSERT INTO dict_item (type_id, item_value, item_label, sort_order, enabled, note, parent_id, created_at, updated_at)
SELECT dt.id, v.item_value, v.item_label, v.sort_order, true, NULL, NULL, NOW(), NOW()
FROM dict_type dt,
     (VALUES
         ('REPAIR', '维修费', 1),
         ('ON_SITE', '上门费', 2),
         ('PARTS', '配件费', 3),
         ('PLATFORM', '平台费', 4),
         ('DATA_PLAN', '流量费', 5),
         ('STORAGE', '仓储费', 6),
         ('SHIPPING', '运输费', 7),
         ('PACKING', '包装费', 8),
         ('CONSTRUCTION', '施工费', 9),
         ('OTHER', '其他', 10)
     ) AS v(item_value, item_label, sort_order)
WHERE dt.type_code = 'quote_expense_type'
ON CONFLICT (type_id, item_value) DO NOTHING;

-- 3) Add snapshot column to quote_item
ALTER TABLE quote_item ADD COLUMN IF NOT EXISTS expense_type_label_snapshot VARCHAR(128);

-- 4) Backfill: set snapshot from dict_item where match, else expense_type
UPDATE quote_item qi
SET expense_type_label_snapshot = COALESCE(
    (SELECT di.item_label
     FROM dict_item di
     JOIN dict_type dt ON dt.id = di.type_id
     WHERE dt.type_code = 'quote_expense_type'
       AND di.item_value = qi.expense_type::TEXT
     LIMIT 1),
    qi.expense_type::TEXT
)
WHERE qi.expense_type_label_snapshot IS NULL;
