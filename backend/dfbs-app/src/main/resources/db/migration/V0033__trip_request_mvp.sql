-- V0033__trip_request_mvp.sql
-- Trip Request MVP: trip_request table.

CREATE TABLE IF NOT EXISTS trip_request (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    city VARCHAR(256) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    purpose TEXT NOT NULL,
    est_transport_cost DECIMAL(19,2) NOT NULL DEFAULT 0,
    est_accommodation_cost DECIMAL(19,2) NOT NULL DEFAULT 0,
    currency VARCHAR(8) NOT NULL DEFAULT 'CNY',
    work_order_id BIGINT,
    independent_reason TEXT,
    cancellation_reason TEXT,
    approver_leader_id BIGINT,
    approve_leader_time TIMESTAMPTZ,
    approver_finance_id BIGINT,
    approve_finance_time TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS ix_trip_request_created_by ON trip_request(created_by);
CREATE INDEX IF NOT EXISTS ix_trip_request_status ON trip_request(status);
CREATE INDEX IF NOT EXISTS ix_trip_request_start_date ON trip_request(start_date);
