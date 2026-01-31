-- V0016__quote_void_process.sql
-- Quote Void Process: multi-stage void request (INITIATOR / FINANCE / LEADER)

CREATE TABLE IF NOT EXISTS quote_void_request (
    id BIGSERIAL PRIMARY KEY,
    quote_id BIGINT NOT NULL,
    requester_id BIGINT NOT NULL,
    reason TEXT NOT NULL,
    current_stage VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    previous_status VARCHAR(32),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_void_request_quote FOREIGN KEY (quote_id) REFERENCES quote(id)
);

CREATE INDEX IF NOT EXISTS ix_quote_void_request_quote_id ON quote_void_request(quote_id);
CREATE INDEX IF NOT EXISTS ix_quote_void_request_status ON quote_void_request(status);
CREATE INDEX IF NOT EXISTS ix_quote_void_request_created_at ON quote_void_request(created_at);
