-- 第一步：如果同一个 quote_no 现在有多个 active，只保留 1 个，其余全部关掉
WITH ranked AS (
  SELECT
    id,
    quote_no,
    ROW_NUMBER() OVER (
      PARTITION BY quote_no
      ORDER BY id DESC
    ) AS rn
  FROM quote_version
  WHERE is_active = TRUE
)
UPDATE quote_version qv
SET is_active = FALSE
FROM ranked r
WHERE qv.id = r.id
  AND r.rn > 1;

-- 第二步：数据库级强制规则
-- 同一个 quote_no，只允许 1 条 is_active = true
CREATE UNIQUE INDEX uq_quote_version_one_active_per_quote_no
ON quote_version (quote_no)
WHERE is_active = TRUE;
