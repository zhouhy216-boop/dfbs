-- 最小占位：证明 Flyway 可用
CREATE TABLE IF NOT EXISTS platform_bootstrap (
  id UUID PRIMARY KEY,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);