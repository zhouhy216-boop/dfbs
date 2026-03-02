-- BIZPERM-260302-001-02.b: Business Module Catalog persistence (CN tree + operation points).

CREATE TABLE biz_perm_catalog_node (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT REFERENCES biz_perm_catalog_node(id),
    cn_name VARCHAR(128) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX ix_biz_perm_catalog_node_parent ON biz_perm_catalog_node(parent_id);

CREATE TABLE biz_perm_operation_point (
    id BIGSERIAL PRIMARY KEY,
    node_id BIGINT REFERENCES biz_perm_catalog_node(id) ON DELETE SET NULL,
    permission_key VARCHAR(128) NOT NULL,
    cn_name VARCHAR(128),
    sort_order INT NOT NULL DEFAULT 0,
    handled_only BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_biz_perm_operation_point_key UNIQUE (permission_key)
);
CREATE INDEX ix_biz_perm_operation_point_node ON biz_perm_operation_point(node_id);

COMMENT ON TABLE biz_perm_catalog_node IS 'Business module catalog tree (CN labels). node_id null in operation_point = unclassified.';
COMMENT ON TABLE biz_perm_operation_point IS 'Permission keys mapped to catalog nodes; node_id NULL = unclaimed/unclassified.';
