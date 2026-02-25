-- V0073__perm_role_enabled.sql
-- Add enabled flag to role templates; existing rows get default true.

ALTER TABLE perm_role ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT true;
