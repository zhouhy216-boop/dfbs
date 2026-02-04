-- V0047__app_user_seed_admin.sql
-- Ensure an 'admin' user exists for login (username: admin, password not checked in MVP).

INSERT INTO app_user (username, nickname, can_request_permission, authorities, allow_normal_notification, can_manage_statements)
SELECT 'admin', 'Admin', true, '["ROLE_ADMIN"]', true, true
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE username = 'admin');
