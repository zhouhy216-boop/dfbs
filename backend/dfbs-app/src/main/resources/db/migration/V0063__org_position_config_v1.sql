-- V0063__org_position_config_v1.sql
-- INBOX-23 Position Config v1: global position catalog, per-org enabled, bindings, level templates.
-- Extend org_change_log allowed actions for position-related audit.

-- ========== Extend change log action constraint ==========
ALTER TABLE org_change_log DROP CONSTRAINT IF EXISTS chk_action;
ALTER TABLE org_change_log ADD CONSTRAINT chk_action CHECK (action IN (
    'CREATE','UPDATE','MOVE','ENABLE','DISABLE',
    'ORG_POSITION_ENABLE','ORG_POSITION_DISABLE','ORG_POSITION_BINDINGS_UPDATE','TEMPLATE_APPLIED_ON_ORG_CREATE'
));

-- ========== Global position catalog (company-wide) ==========
CREATE TABLE org_position_catalog (
    id BIGSERIAL PRIMARY KEY,
    base_name VARCHAR(64) NOT NULL,
    grade VARCHAR(16) NOT NULL,
    display_name VARCHAR(128) NOT NULL,
    short_name VARCHAR(64),
    order_index INT NOT NULL DEFAULT 0,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX ix_org_position_catalog_order ON org_position_catalog(order_index);
CREATE INDEX ix_org_position_catalog_enabled ON org_position_catalog(is_enabled);
COMMENT ON COLUMN org_position_catalog.grade IS 'PRIMARY=正, DEPUTY=副, ACTING=担当, NONE=无/职员';

-- ========== Per-org enabled positions ==========
CREATE TABLE org_position_enabled (
    id BIGSERIAL PRIMARY KEY,
    org_node_id BIGINT NOT NULL,
    position_id BIGINT NOT NULL,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_ope_org_node FOREIGN KEY (org_node_id) REFERENCES org_node(id) ON DELETE CASCADE,
    CONSTRAINT fk_ope_position FOREIGN KEY (position_id) REFERENCES org_position_catalog(id),
    CONSTRAINT uk_ope_org_position UNIQUE (org_node_id, position_id)
);
CREATE INDEX ix_org_position_enabled_org ON org_position_enabled(org_node_id);
CREATE INDEX ix_org_position_enabled_position ON org_position_enabled(position_id);

-- ========== Org position bindings (people assignment) ==========
CREATE TABLE org_position_binding (
    id BIGSERIAL PRIMARY KEY,
    org_node_id BIGINT NOT NULL,
    position_id BIGINT NOT NULL,
    person_id BIGINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_opb_org_node FOREIGN KEY (org_node_id) REFERENCES org_node(id) ON DELETE CASCADE,
    CONSTRAINT fk_opb_position FOREIGN KEY (position_id) REFERENCES org_position_catalog(id),
    CONSTRAINT fk_opb_person FOREIGN KEY (person_id) REFERENCES org_person(id) ON DELETE CASCADE,
    CONSTRAINT uk_opb_org_position_person UNIQUE (org_node_id, position_id, person_id)
);
CREATE INDEX ix_org_position_binding_org ON org_position_binding(org_node_id);
CREATE INDEX ix_org_position_binding_position ON org_position_binding(position_id);
CREATE INDEX ix_org_position_binding_person ON org_position_binding(person_id);

-- ========== Company recommended templates by org level ==========
CREATE TABLE org_level_position_template (
    id BIGSERIAL PRIMARY KEY,
    level_id BIGINT NOT NULL,
    position_id BIGINT NOT NULL,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_olpt_level FOREIGN KEY (level_id) REFERENCES org_level(id),
    CONSTRAINT fk_olpt_position FOREIGN KEY (position_id) REFERENCES org_position_catalog(id),
    CONSTRAINT uk_olpt_level_position UNIQUE (level_id, position_id)
);
CREATE INDEX ix_org_level_position_template_level ON org_level_position_template(level_id);
