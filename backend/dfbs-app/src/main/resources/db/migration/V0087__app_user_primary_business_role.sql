-- MOCKACC-260309-001-01: Primary Business Role on account (identity basis for downstream flows).
-- One role per account; nullable for existing users.

ALTER TABLE app_user ADD COLUMN IF NOT EXISTS primary_business_role VARCHAR(64);
COMMENT ON COLUMN app_user.primary_business_role IS 'Primary business role (Chinese label); one per account; used as identity basis.';
