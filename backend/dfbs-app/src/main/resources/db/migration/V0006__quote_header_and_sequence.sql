-- Quote header (MVP) and numbering sequence
CREATE TABLE IF NOT EXISTS quote_number_sequence (
    id BIGSERIAL PRIMARY KEY,
    user_initials VARCHAR(64) NOT NULL,
    year_month VARCHAR(8) NOT NULL,
    current_seq INT NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_quote_number_sequence_user_year_month
    ON quote_number_sequence(user_initials, year_month);

CREATE TABLE IF NOT EXISTS quote (
    id BIGSERIAL PRIMARY KEY,
    quote_no VARCHAR(64) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    source_type VARCHAR(32) NOT NULL,
    source_ref_id VARCHAR(256),
    customer_id BIGINT NOT NULL,
    recipient VARCHAR(500),
    phone VARCHAR(64),
    address VARCHAR(1000),
    currency VARCHAR(16),
    created_by VARCHAR(128),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS ix_quote_quote_no ON quote(quote_no);
CREATE INDEX IF NOT EXISTS ix_quote_customer_id ON quote(customer_id);
