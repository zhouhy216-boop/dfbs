-- V0062__org_level_default_seed.sql
-- Preseed default org levels (公司/本部/部/课/系/班) only when table is empty.

INSERT INTO org_level (order_index, display_name, is_enabled, created_at, updated_at)
SELECT t.order_index, t.display_name, t.is_enabled, NOW(), NOW()
FROM (
    SELECT 1 AS order_index, '公司' AS display_name, true AS is_enabled
    UNION ALL SELECT 2, '本部', true
    UNION ALL SELECT 3, '部', true
    UNION ALL SELECT 4, '课', true
    UNION ALL SELECT 5, '系', true
    UNION ALL SELECT 6, '班', true
) t
WHERE NOT EXISTS (SELECT 1 FROM org_level LIMIT 1);
