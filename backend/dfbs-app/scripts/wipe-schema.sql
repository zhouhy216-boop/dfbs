-- Wipe public schema to let Flyway rebuild from scratch.
-- Run against database: dfbs (or dfbs_app if that's your DB name).
-- Connection: localhost:5432, user: dfbs, password: dfbs

DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
