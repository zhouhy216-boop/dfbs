-- V0061__org_structure_v1.sql
-- Org Structure v1: configurable levels, org tree, people directory, audit log (Super Admin only).
-- No FK to platform_org; this is a separate in-system org tree.

-- ========== Org Level (层级字典, max 8) ==========
CREATE TABLE org_level (
    id BIGSERIAL PRIMARY KEY,
    order_index INT NOT NULL DEFAULT 0,
    display_name VARCHAR(64) NOT NULL,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_by VARCHAR(64)
);
CREATE INDEX ix_org_level_order ON org_level(order_index);
CREATE INDEX ix_org_level_enabled ON org_level(is_enabled);

-- ========== Org Node (组织树节点) ==========
CREATE TABLE org_node (
    id BIGSERIAL PRIMARY KEY,
    level_id BIGINT NOT NULL,
    parent_id BIGINT,
    name VARCHAR(256) NOT NULL,
    remark TEXT,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_by VARCHAR(64),
    CONSTRAINT fk_org_node_level FOREIGN KEY (level_id) REFERENCES org_level(id),
    CONSTRAINT fk_org_node_parent FOREIGN KEY (parent_id) REFERENCES org_node(id) ON DELETE SET NULL
);
CREATE INDEX ix_org_node_level ON org_node(level_id);
CREATE INDEX ix_org_node_parent ON org_node(parent_id);
CREATE INDEX ix_org_node_enabled ON org_node(is_enabled);

-- ========== Job Level (职级字典) ==========
CREATE TABLE job_level (
    id BIGSERIAL PRIMARY KEY,
    display_name VARCHAR(64) NOT NULL,
    order_index INT NOT NULL DEFAULT 0
);
CREATE INDEX ix_job_level_order ON job_level(order_index);

-- Seed job levels (职员、正/副班长、正/副系长、正/副课长、正/副部长、正/副本部长、总经理助理、正/副总经理)
INSERT INTO job_level (display_name, order_index) VALUES
('职员', 10),
('副班长', 20),
('正班长', 21),
('副系长', 30),
('正系长', 31),
('副课长', 40),
('正课长', 41),
('副部长', 50),
('正部长', 51),
('副本部长', 60),
('正本部长', 61),
('总经理助理', 70),
('副总经理', 80),
('正总经理', 81);

-- ========== Person (人员) ==========
CREATE TABLE org_person (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    phone VARCHAR(64) NOT NULL,
    email VARCHAR(256),
    remark TEXT,
    job_level_id BIGINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_by VARCHAR(64),
    CONSTRAINT fk_org_person_job_level FOREIGN KEY (job_level_id) REFERENCES job_level(id)
);
CREATE INDEX ix_org_person_job_level ON org_person(job_level_id);
CREATE INDEX ix_org_person_active ON org_person(is_active);

-- ========== Person affiliation (primary + secondary org) ==========
-- One row per (person, org_node); is_primary=true for primary org (exactly one per person).
CREATE TABLE person_affiliation (
    id BIGSERIAL PRIMARY KEY,
    person_id BIGINT NOT NULL,
    org_node_id BIGINT NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_pa_person FOREIGN KEY (person_id) REFERENCES org_person(id) ON DELETE CASCADE,
    CONSTRAINT fk_pa_org_node FOREIGN KEY (org_node_id) REFERENCES org_node(id),
    CONSTRAINT uk_pa_person_org UNIQUE (person_id, org_node_id)
);
CREATE INDEX ix_person_affiliation_person ON person_affiliation(person_id);
CREATE INDEX ix_person_affiliation_org ON person_affiliation(org_node_id);
-- Ensure at most one primary per person (partial unique not in all PG versions; enforce in app or trigger)
CREATE UNIQUE INDEX uk_person_primary ON person_affiliation(person_id) WHERE is_primary = TRUE;

-- ========== Change Log (变更留痕) ==========
CREATE TABLE org_change_log (
    id BIGSERIAL PRIMARY KEY,
    object_type VARCHAR(32) NOT NULL,
    object_id BIGINT NOT NULL,
    action VARCHAR(32) NOT NULL,
    operator_id BIGINT,
    operator_name VARCHAR(128),
    timestamp TIMESTAMP NOT NULL DEFAULT NOW(),
    summary_text TEXT,
    diff_json TEXT,
    CONSTRAINT chk_object_type CHECK (object_type IN ('LEVEL','ORG_NODE','PERSON')),
    CONSTRAINT chk_action CHECK (action IN ('CREATE','UPDATE','MOVE','ENABLE','DISABLE'))
);
CREATE INDEX ix_org_change_log_object ON org_change_log(object_type, object_id);
CREATE INDEX ix_org_change_log_operator ON org_change_log(operator_id);
CREATE INDEX ix_org_change_log_timestamp ON org_change_log(timestamp);

-- ========== Super Admin: grant ROLE_SUPER_ADMIN to admin user ==========
UPDATE app_user
SET authorities = '["ROLE_ADMIN","ROLE_SUPER_ADMIN"]'
WHERE username = 'admin' AND (authorities IS NULL OR authorities NOT LIKE '%ROLE_SUPER_ADMIN%');
