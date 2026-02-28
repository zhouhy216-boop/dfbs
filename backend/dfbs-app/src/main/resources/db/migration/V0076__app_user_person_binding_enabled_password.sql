-- ACCTPERM Step-02.b: Person↔Account binding (1:1), enabled, password storage.
-- org_person_id: 1:1 link to org_person; UNIQUE so one person at most one account.
-- enabled: account can be disabled; disabled accounts cannot login.
-- password_hash: BCrypt hash; NULL for existing users (legacy login via defaultPassword in config until reset).

ALTER TABLE app_user ADD COLUMN IF NOT EXISTS org_person_id BIGINT REFERENCES org_person(id) ON DELETE SET NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_app_user_org_person_id ON app_user(org_person_id) WHERE org_person_id IS NOT NULL;

ALTER TABLE app_user ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT true;

ALTER TABLE app_user ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);
COMMENT ON COLUMN app_user.password_hash IS 'BCrypt hash; NULL = legacy user, login via dfbs.auth.defaultPassword until reset.';
