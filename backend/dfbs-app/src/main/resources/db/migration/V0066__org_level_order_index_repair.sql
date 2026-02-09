-- V0066__org_level_order_index_repair.sql
-- Repair configurable levels' order_index: contiguous 2..N+1, no 1000+, no duplicates.
-- Idempotent: only changes rows where current order_index is wrong.

-- Re-normalize configurable levels (exclude 公司) to order_index 2, 3, ... by (order_index asc, id asc)
WITH configurable AS (
    SELECT id, ROW_NUMBER() OVER (ORDER BY order_index ASC, id ASC) AS rn
    FROM org_level
    WHERE display_name <> '公司'
)
UPDATE org_level
SET order_index = configurable.rn + 1
FROM configurable
WHERE org_level.id = configurable.id
  AND org_level.order_index IS DISTINCT FROM (configurable.rn + 1);
