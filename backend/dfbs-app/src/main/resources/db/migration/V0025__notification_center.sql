-- V0025__notification_center.sql
-- Notification Center MVP: app_user preference, notification type/priority/action

-- 1. Alter app_user: allow_normal_notification
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS allow_normal_notification BOOLEAN NOT NULL DEFAULT true;

-- 2. Alter notification: type, related_id, priority, is_action_required
ALTER TABLE notification ADD COLUMN IF NOT EXISTS type VARCHAR(32);
ALTER TABLE notification ADD COLUMN IF NOT EXISTS related_id BIGINT;
ALTER TABLE notification ADD COLUMN IF NOT EXISTS priority VARCHAR(16) NOT NULL DEFAULT 'NORMAL';
ALTER TABLE notification ADD COLUMN IF NOT EXISTS is_action_required BOOLEAN NOT NULL DEFAULT false;

-- 3. Indexes (created_at already in V0012; ensure user_id indexed)
CREATE INDEX IF NOT EXISTS ix_notification_user_id ON notification(user_id);
CREATE INDEX IF NOT EXISTS ix_notification_created_at ON notification(created_at);
