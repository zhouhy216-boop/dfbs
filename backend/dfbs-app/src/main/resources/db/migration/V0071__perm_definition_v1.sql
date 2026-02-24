-- V0071__perm_definition_v1.sql
-- Permission model v1: actions (default set) + module tree nodes. keyFormat: <moduleKey>:<actionKey>.

CREATE TABLE perm_action (
    id BIGSERIAL PRIMARY KEY,
    action_key VARCHAR(64) NOT NULL,
    label VARCHAR(128) NOT NULL,
    CONSTRAINT uk_perm_action_key UNIQUE (action_key)
);
CREATE INDEX ix_perm_action_key ON perm_action(action_key);

CREATE TABLE perm_module (
    id BIGSERIAL PRIMARY KEY,
    module_key VARCHAR(64) NOT NULL,
    label VARCHAR(128) NOT NULL,
    parent_id BIGINT REFERENCES perm_module(id),
    CONSTRAINT uk_perm_module_key UNIQUE (module_key)
);
CREATE INDEX ix_perm_module_parent ON perm_module(parent_id);

CREATE TABLE perm_module_action (
    id BIGSERIAL PRIMARY KEY,
    module_id BIGINT NOT NULL REFERENCES perm_module(id) ON DELETE CASCADE,
    action_key VARCHAR(64) NOT NULL,
    CONSTRAINT uk_perm_module_action UNIQUE (module_id, action_key)
);
CREATE INDEX ix_perm_module_action_module ON perm_module_action(module_id);

-- Seed v1 default actions (idempotent: safe on restart).
INSERT INTO perm_action (action_key, label)
VALUES
    ('VIEW', '查看'),
    ('CREATE', '创建'),
    ('EDIT', '编辑'),
    ('SUBMIT', '提交'),
    ('APPROVE', '审批'),
    ('REJECT', '拒绝'),
    ('ASSIGN', '分配'),
    ('CLOSE', '关闭'),
    ('DELETE', '删除'),
    ('EXPORT', '导出')
ON CONFLICT (action_key) DO NOTHING;
