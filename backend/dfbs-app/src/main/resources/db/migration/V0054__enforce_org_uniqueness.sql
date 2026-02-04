-- V0054__enforce_org_uniqueness.sql
-- Ensure (platform, org_code_short) is unique; drop any old index and create one with canonical name.

DROP INDEX IF EXISTS uk_platform_org_code;
DROP INDEX IF EXISTS idx_platform_org_unique;
CREATE UNIQUE INDEX idx_platform_org_unique ON platform_org (platform, org_code_short);
