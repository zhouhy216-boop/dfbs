-- V0053__relax_application_constraints.sql
-- Allow org_code_short and region to be null (filled by Admin at approval).

ALTER TABLE platform_account_applications ALTER COLUMN org_code_short DROP NOT NULL;
ALTER TABLE platform_account_applications ALTER COLUMN region DROP NOT NULL;
