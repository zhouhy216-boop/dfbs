-- BIZPERM-260302-001-04.b: Per-account op scope (ALL vs HANDLED_ONLY).

CREATE TABLE biz_perm_user_op_scope (
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    permission_key VARCHAR(128) NOT NULL,
    scope VARCHAR(32) NOT NULL CHECK (scope IN ('ALL', 'HANDLED_ONLY')),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_by_user_id BIGINT,
    PRIMARY KEY (user_id, permission_key)
);
CREATE INDEX ix_biz_perm_user_op_scope_user ON biz_perm_user_op_scope(user_id);

COMMENT ON TABLE biz_perm_user_op_scope IS 'Per-account scope per permission key: ALL or HANDLED_ONLY (latter only when catalog op point has handled_only=true).';
