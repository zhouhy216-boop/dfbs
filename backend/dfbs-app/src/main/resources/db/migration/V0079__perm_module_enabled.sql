-- ACCTPERM Step-002-04.c: Module visibility enable/disable.

ALTER TABLE perm_module ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT true;

COMMENT ON COLUMN perm_module.enabled IS 'When false, module can be hidden in tree/UI (visibility).';
