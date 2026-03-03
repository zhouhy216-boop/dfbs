-- DICT-260303-001-04.b: Type B status transition rules (from -> to edges per dict type).

CREATE TABLE dict_transition (
    id BIGSERIAL PRIMARY KEY,
    type_id BIGINT NOT NULL REFERENCES dict_type(id) ON DELETE CASCADE,
    from_item_id BIGINT NOT NULL REFERENCES dict_item(id) ON DELETE CASCADE,
    to_item_id BIGINT NOT NULL REFERENCES dict_item(id) ON DELETE CASCADE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_dict_transition_type_from_to UNIQUE (type_id, from_item_id, to_item_id)
);
CREATE INDEX ix_dict_transition_type ON dict_transition(type_id);

COMMENT ON TABLE dict_transition IS 'Type B: allowed status transitions (from_item -> to_item) per dictionary type.';
