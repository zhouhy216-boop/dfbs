-- V0068__dict_item_v1.sql
-- Dictionary Items per type: stable value, label, sort, enabled, optional 1-level parent.

CREATE TABLE dict_item (
    id BIGSERIAL PRIMARY KEY,
    type_id BIGINT NOT NULL,
    item_value VARCHAR(64) NOT NULL,
    item_label VARCHAR(128) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    note VARCHAR(512),
    parent_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_dict_item_type FOREIGN KEY (type_id) REFERENCES dict_type(id) ON DELETE CASCADE,
    CONSTRAINT fk_dict_item_parent FOREIGN KEY (parent_id) REFERENCES dict_item(id) ON DELETE SET NULL,
    CONSTRAINT uk_dict_item_type_value UNIQUE (type_id, item_value)
);
CREATE INDEX ix_dict_item_type_enabled ON dict_item(type_id, enabled);
CREATE INDEX ix_dict_item_type_parent ON dict_item(type_id, parent_id);
CREATE INDEX ix_dict_item_label ON dict_item(item_label);
