-- V0069__dict_label_snapshot_demo.sql
-- Admin-only demo table for historical label stability (no business impact).

CREATE TABLE dict_label_snapshot_demo (
    id BIGSERIAL PRIMARY KEY,
    type_code VARCHAR(64) NOT NULL,
    item_value VARCHAR(64) NOT NULL,
    item_label_snapshot VARCHAR(128) NOT NULL,
    note VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX ix_dict_label_snapshot_demo_type_code ON dict_label_snapshot_demo(type_code);
CREATE INDEX ix_dict_label_snapshot_demo_created_at ON dict_label_snapshot_demo(created_at);
