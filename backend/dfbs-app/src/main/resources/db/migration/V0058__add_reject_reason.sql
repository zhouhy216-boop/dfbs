-- Allow longer reject reasons (column may already exist as VARCHAR(512) from V0050).
ALTER TABLE platform_account_applications ALTER COLUMN reject_reason TYPE TEXT;
