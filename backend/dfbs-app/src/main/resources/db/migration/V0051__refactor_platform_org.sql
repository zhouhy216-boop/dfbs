-- ========== platform_org: rename columns ==========
ALTER TABLE platform_org RENAME COLUMN org_code TO org_code_short;
ALTER TABLE platform_org RENAME COLUMN org_name TO org_full_name;

-- ========== platform_org: many-to-many with customers ==========
CREATE TABLE platform_org_customers (
    org_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    PRIMARY KEY (org_id, customer_id),
    CONSTRAINT fk_poc_org FOREIGN KEY (org_id) REFERENCES platform_org(id) ON DELETE CASCADE,
    CONSTRAINT fk_poc_customer FOREIGN KEY (customer_id) REFERENCES md_customer(id)
);

-- Migrate existing customer_id into join table
INSERT INTO platform_org_customers (org_id, customer_id)
SELECT id, customer_id FROM platform_org WHERE customer_id IS NOT NULL;

-- Drop old FK and column
ALTER TABLE platform_org DROP CONSTRAINT IF EXISTS fk_platform_org_customer;
ALTER TABLE platform_org DROP COLUMN IF EXISTS customer_id;

-- Recreate unique index on new column name (index may keep same name)
DROP INDEX IF EXISTS uk_platform_org_code;
CREATE UNIQUE INDEX uk_platform_org_code ON platform_org(platform, org_code_short);

-- ========== platform_account_applications: rename columns ==========
ALTER TABLE platform_account_applications RENAME COLUMN org_code TO org_code_short;
ALTER TABLE platform_account_applications RENAME COLUMN org_name TO org_full_name;

-- ========== platform_account_applications: drop columns ==========
ALTER TABLE platform_account_applications DROP COLUMN IF EXISTS machine_no;
ALTER TABLE platform_account_applications DROP COLUMN IF EXISTS sim_count;
ALTER TABLE platform_account_applications DROP COLUMN IF EXISTS package_type;
ALTER TABLE platform_account_applications DROP COLUMN IF EXISTS is_new_org;

-- ========== platform_account_applications: add new columns ==========
ALTER TABLE platform_account_applications ADD COLUMN IF NOT EXISTS contract_no VARCHAR(128);
ALTER TABLE platform_account_applications ADD COLUMN IF NOT EXISTS price DECIMAL(19,4);
ALTER TABLE platform_account_applications ADD COLUMN IF NOT EXISTS quantity INTEGER;
ALTER TABLE platform_account_applications ADD COLUMN IF NOT EXISTS reason TEXT;
ALTER TABLE platform_account_applications ADD COLUMN IF NOT EXISTS is_cc_planner BOOLEAN NOT NULL DEFAULT FALSE;
