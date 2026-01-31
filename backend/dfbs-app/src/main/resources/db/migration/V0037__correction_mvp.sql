-- V0037__correction_mvp.sql
-- Data Correction Slip: correction_record table; freight_bill status VOID.

-- 1. correction_record
CREATE TABLE IF NOT EXISTS correction_record (
    id BIGSERIAL PRIMARY KEY,
    correction_no VARCHAR(64) NOT NULL,
    target_type VARCHAR(32) NOT NULL,
    target_id BIGINT NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    changes_json TEXT,
    new_record_id BIGINT,
    occurred_date DATE NOT NULL,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    approved_by BIGINT,
    approved_at TIMESTAMP,
    CONSTRAINT uk_correction_record_no UNIQUE (correction_no)
);

CREATE INDEX IF NOT EXISTS ix_correction_record_target ON correction_record(target_type, target_id);
CREATE INDEX IF NOT EXISTS ix_correction_record_status ON correction_record(status);

-- 2. freight_bill: allow status VOID (application uses enum; DB stores string)
-- No migration needed if enum is extended in Java; existing rows keep DRAFT/CONFIRMED/SETTLED.
