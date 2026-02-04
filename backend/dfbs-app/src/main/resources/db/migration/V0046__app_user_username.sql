-- V0046__app_user_username.sql
-- Add username and nickname for login lookup and display.

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'app_user' AND column_name = 'username') THEN
        ALTER TABLE app_user ADD COLUMN username VARCHAR(64);
        UPDATE app_user SET username = 'user' || id WHERE username IS NULL;
        ALTER TABLE app_user ALTER COLUMN username SET NOT NULL;
        CREATE UNIQUE INDEX IF NOT EXISTS uk_app_user_username ON app_user(username);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = 'app_user' AND column_name = 'nickname') THEN
        ALTER TABLE app_user ADD COLUMN nickname VARCHAR(128);
        UPDATE app_user SET nickname = username WHERE nickname IS NULL;
    END IF;
END $$;
