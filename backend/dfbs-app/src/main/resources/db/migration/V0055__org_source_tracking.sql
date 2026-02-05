-- Source traceability for platform_org (from Application vs Manual)
ALTER TABLE platform_org ADD COLUMN IF NOT EXISTS source_application_id BIGINT;
ALTER TABLE platform_org ADD COLUMN IF NOT EXISTS source_type VARCHAR(32);

COMMENT ON COLUMN platform_org.source_application_id IS 'ID of platform_account_application if org was created from approval';
COMMENT ON COLUMN platform_org.source_type IS 'APP = from application approval, MANUAL = admin created';
