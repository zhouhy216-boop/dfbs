-- ACCTPERM Step-03: optional description for role template (CN name + description UX).
ALTER TABLE perm_role ADD COLUMN IF NOT EXISTS description VARCHAR(512);
