-- V0018__shipment_workflow.sql
-- Extend shipment workflow fields and timestamps

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = current_schema()
      AND table_name = 'shipment'
      AND column_name = 'carrier'
  ) THEN
    ALTER TABLE shipment ADD COLUMN carrier VARCHAR(256);
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = current_schema()
      AND table_name = 'shipment'
      AND column_name = 'exception_reason'
  ) THEN
    ALTER TABLE shipment ADD COLUMN exception_reason TEXT;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = current_schema()
      AND table_name = 'shipment'
      AND column_name = 'cancel_reason'
  ) THEN
    ALTER TABLE shipment ADD COLUMN cancel_reason TEXT;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = current_schema()
      AND table_name = 'shipment'
      AND column_name = 'accepted_at'
  ) THEN
    ALTER TABLE shipment ADD COLUMN accepted_at TIMESTAMPTZ;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = current_schema()
      AND table_name = 'shipment'
      AND column_name = 'shipped_at'
  ) THEN
    ALTER TABLE shipment ADD COLUMN shipped_at TIMESTAMPTZ;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = current_schema()
      AND table_name = 'shipment'
      AND column_name = 'completed_at'
  ) THEN
    ALTER TABLE shipment ADD COLUMN completed_at TIMESTAMPTZ;
  END IF;
END $$;

