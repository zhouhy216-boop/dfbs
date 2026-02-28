-- ACCTPERM Step-002-03.b: Admin-managed default password (persistent). key=auth.defaultPassword fallback to config.

CREATE TABLE IF NOT EXISTS app_setting (
    key VARCHAR(128) PRIMARY KEY,
    value TEXT,
    updated_by_user_id BIGINT,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE app_setting IS 'Key-value settings (e.g. auth.defaultPassword). Fallback to config when no row.';
