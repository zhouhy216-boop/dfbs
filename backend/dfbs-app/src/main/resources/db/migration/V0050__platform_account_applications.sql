CREATE TABLE platform_account_applications (
    id BIGSERIAL PRIMARY KEY,
    application_no VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    platform VARCHAR(32) NOT NULL,
    customer_id BIGINT NOT NULL,
    org_code VARCHAR(128) NOT NULL,
    org_name VARCHAR(256) NOT NULL,
    contact_person VARCHAR(128),
    phone VARCHAR(64),
    email VARCHAR(256),
    region VARCHAR(128),
    sales_person VARCHAR(128),
    machine_no VARCHAR(128),
    sim_count INTEGER,
    package_type VARCHAR(128) NOT NULL,
    is_new_org BOOLEAN NOT NULL DEFAULT FALSE,
    applicant_id BIGINT,
    planner_id BIGINT,
    admin_id BIGINT,
    reject_reason VARCHAR(512),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_platform_account_app_no UNIQUE (application_no),
    CONSTRAINT fk_platform_account_app_customer FOREIGN KEY (customer_id) REFERENCES md_customer(id)
);

CREATE INDEX idx_platform_account_app_status ON platform_account_applications(status);
CREATE INDEX idx_platform_account_app_platform ON platform_account_applications(platform);
CREATE INDEX idx_platform_account_app_customer ON platform_account_applications(customer_id);
