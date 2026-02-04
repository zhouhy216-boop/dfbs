CREATE TABLE platform_org (
    id BIGSERIAL PRIMARY KEY,
    platform VARCHAR(32) NOT NULL,
    org_code VARCHAR(128) NOT NULL,
    org_name VARCHAR(256) NOT NULL,
    customer_id BIGINT NOT NULL,
    contact_person VARCHAR(128),
    contact_phone VARCHAR(64),
    contact_email VARCHAR(256),
    sales_person VARCHAR(128),
    region VARCHAR(128),
    remark TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_by VARCHAR(64),
    CONSTRAINT fk_platform_org_customer FOREIGN KEY (customer_id) REFERENCES md_customer(id)
);

CREATE UNIQUE INDEX uk_platform_org_code ON platform_org(platform, org_code);
