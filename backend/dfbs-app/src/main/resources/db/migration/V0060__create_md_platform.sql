-- V0060__create_md_platform.sql
-- Platform configuration (rules, code format) for dropdowns and validation.

CREATE TABLE md_platform (
    id BIGSERIAL PRIMARY KEY,
    platform_name VARCHAR(64) NOT NULL,
    platform_code VARCHAR(64) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    rule_unique_email BOOLEAN NOT NULL DEFAULT FALSE,
    rule_unique_phone BOOLEAN NOT NULL DEFAULT FALSE,
    rule_unique_org_name BOOLEAN NOT NULL DEFAULT FALSE,
    code_validator_type VARCHAR(32) NOT NULL DEFAULT 'NONE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_by VARCHAR(64),
    CONSTRAINT uk_md_platform_code UNIQUE (platform_code)
);

COMMENT ON TABLE md_platform IS 'Platform master: display name, code, uniqueness rules, org code validator type.';
COMMENT ON COLUMN md_platform.code_validator_type IS 'NONE, UPPERCASE, CHINESE, MIXED';

-- Initial data from current logic
INSERT INTO md_platform (platform_name, platform_code, is_active, rule_unique_email, rule_unique_phone, rule_unique_org_name, code_validator_type)
VALUES
    ('映翰通', 'INHAND', TRUE, TRUE, FALSE, FALSE, 'UPPERCASE'),
    ('恒动', 'HENDONG', TRUE, FALSE, TRUE, FALSE, 'CHINESE'),
    ('京品', 'JINGPIN', TRUE, FALSE, FALSE, FALSE, 'UPPERCASE');
