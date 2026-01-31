-- V0024__permission_request.sql
-- App user (for permission request flow) and permission_request table

CREATE TABLE IF NOT EXISTS app_user (
    id BIGSERIAL PRIMARY KEY,
    can_request_permission BOOLEAN NOT NULL DEFAULT false,
    authorities TEXT
);

CREATE TABLE IF NOT EXISTS permission_request (
    id BIGSERIAL PRIMARY KEY,
    applicant_id BIGINT NOT NULL,
    target_user_id BIGINT NOT NULL,
    description TEXT NOT NULL,
    reason TEXT NOT NULL,
    expected_time VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    handler_id BIGINT,
    handle_time TIMESTAMP,
    admin_comment TEXT,
    snapshot_before TEXT,
    snapshot_after TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS ix_permission_request_applicant ON permission_request(applicant_id);
CREATE INDEX IF NOT EXISTS ix_permission_request_status ON permission_request(status);
CREATE INDEX IF NOT EXISTS ix_permission_request_target_user ON permission_request(target_user_id);
