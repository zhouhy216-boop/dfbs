-- V0065__org_level_order_index_unique.sql
-- Ensure configurable levels (exclude 公司) have unique order_index. Company stays at 1.

-- Repair: assign contiguous order_index 2,3,... to configurable levels by current order then id
WITH configurable AS (
    SELECT id, ROW_NUMBER() OVER (ORDER BY order_index, id) AS rn
    FROM org_level
    WHERE display_name <> '公司'
)
UPDATE org_level
SET order_index = configurable.rn + 1
FROM configurable
WHERE org_level.id = configurable.id;

-- Partial unique index: one order_index per configurable level (公司 excluded)
CREATE UNIQUE INDEX uk_org_level_order_index_configurable
ON org_level (order_index)
WHERE display_name <> '公司';
