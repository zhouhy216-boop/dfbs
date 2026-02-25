-- V0074__perm_user_role_template_and_override.sql
-- Per-account: assigned role template + add/remove overrides. effective = (template ∪ add) \ remove.

CREATE TABLE perm_user_role_template (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES app_user(id) ON DELETE CASCADE,
    role_id BIGINT REFERENCES perm_role(id) ON DELETE SET NULL
);
CREATE INDEX ix_perm_user_role_template_user ON perm_user_role_template(user_id);
CREATE INDEX ix_perm_user_role_template_role ON perm_user_role_template(role_id);

CREATE TABLE perm_user_permission_override (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    permission_key VARCHAR(128) NOT NULL,
    op VARCHAR(16) NOT NULL CHECK (op IN ('ADD', 'REMOVE')),
    CONSTRAINT uk_perm_user_permission_override UNIQUE (user_id, permission_key, op)
);
CREATE INDEX ix_perm_user_permission_override_user ON perm_user_permission_override(user_id);
