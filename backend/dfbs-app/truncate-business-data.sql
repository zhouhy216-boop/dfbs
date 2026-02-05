-- Truncate business data for fresh start (local dev). Data loss is intended.
-- Tables: work_order (and children via CASCADE), platform_account_applications, platform_org, md_customer, contracts.

-- 1. Transactional / child data (work order and its records/parts)
TRUNCATE TABLE work_order RESTART IDENTITY CASCADE;

-- 2. Platform applications (CASCADE will truncate platform_org and platform_org_customers if they reference it)
TRUNCATE TABLE platform_account_applications RESTART IDENTITY CASCADE;

-- 3. Platform orgs and link table (explicit; CASCADE clears platform_org_customers)
TRUNCATE TABLE platform_org RESTART IDENTITY CASCADE;

-- 4. Master data & temp pool (customer = md_customer, contract = contracts)
TRUNCATE TABLE md_customer RESTART IDENTITY CASCADE;
TRUNCATE TABLE contracts RESTART IDENTITY CASCADE;
