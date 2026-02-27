-- V0075__perm_audit_log.sql
-- Coarse-grained audit for RBAC management (template, override, module, test kit, vision).

CREATE TABLE perm_audit_log (
    id BIGSERIAL PRIMARY KEY,
    actor_user_id BIGINT,
    actor_username VARCHAR(128),
    action_type VARCHAR(64) NOT NULL,
    target_type VARCHAR(32) NOT NULL,
    target_id BIGINT,
    target_key VARCHAR(128),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    note VARCHAR(512)
);
CREATE INDEX ix_perm_audit_log_created_at ON perm_audit_log(created_at DESC);
CREATE INDEX ix_perm_audit_log_action_type ON perm_audit_log(action_type);
CREATE INDEX ix_perm_audit_log_target ON perm_audit_log(target_type, target_id);
