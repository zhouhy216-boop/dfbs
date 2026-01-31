-- V0029__machine_iccid_list_support.sql
-- Machine & ICCID list views: logistics_no on shipment, plan/platform/expiry_date on iccid

-- shipment: 回单号
ALTER TABLE shipment ADD COLUMN IF NOT EXISTS logistics_no VARCHAR(128);

-- md_iccid: plan, platform, expiry_date
ALTER TABLE md_iccid ADD COLUMN IF NOT EXISTS plan VARCHAR(128);
ALTER TABLE md_iccid ADD COLUMN IF NOT EXISTS platform VARCHAR(128);
ALTER TABLE md_iccid ADD COLUMN IF NOT EXISTS expiry_date DATE;
