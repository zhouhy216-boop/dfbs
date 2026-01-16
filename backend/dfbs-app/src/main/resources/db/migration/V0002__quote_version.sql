CREATE TABLE IF NOT EXISTS quote_version (
  id UUID PRIMARY KEY,
  quote_no TEXT NOT NULL,
  version_no INT NOT NULL,
  is_active BOOLEAN NOT NULL DEFAULT FALSE,
  active_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_quote_version_quote_no_version_no
  ON quote_version(quote_no, version_no);