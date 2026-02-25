-- V0072__perm_role_rbac.sql
-- RBAC: roles and role-permission bindings. permission_key format: <moduleKey>:<actionKey>.

CREATE TABLE perm_role (
    id BIGSERIAL PRIMARY KEY,
    role_key VARCHAR(64) NOT NULL,
    label VARCHAR(128) NOT NULL,
    CONSTRAINT uk_perm_role_key UNIQUE (role_key)
);
CREATE INDEX ix_perm_role_key ON perm_role(role_key);

CREATE TABLE perm_role_permission (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL REFERENCES perm_role(id) ON DELETE CASCADE,
    permission_key VARCHAR(128) NOT NULL,
    CONSTRAINT uk_perm_role_permission UNIQUE (role_id, permission_key)
);
CREATE INDEX ix_perm_role_permission_role ON perm_role_permission(role_id);
