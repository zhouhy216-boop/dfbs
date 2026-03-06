-- SHIPFLOW-260304-001-04: close action (no new status; closedAt marks closure)
ALTER TABLE shipment ADD COLUMN IF NOT EXISTS closed_at TIMESTAMPTZ;
COMMENT ON COLUMN shipment.closed_at IS 'Set when shipment is closed after sign-off (COMPLETED).';
