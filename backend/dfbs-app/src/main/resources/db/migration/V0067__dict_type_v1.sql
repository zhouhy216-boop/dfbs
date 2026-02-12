-- V0067__dict_type_v1.sql
-- Dictionary Types (admin-only): stable type_code, display name, enable/disable.

CREATE TABLE dict_type (
    id BIGSERIAL PRIMARY KEY,
    type_code VARCHAR(64) NOT NULL,
    type_name VARCHAR(128) NOT NULL,
    description VARCHAR(512),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_dict_type_code UNIQUE (type_code)
);
CREATE INDEX ix_dict_type_enabled ON dict_type(enabled);
CREATE INDEX ix_dict_type_type_name ON dict_type(type_name);
