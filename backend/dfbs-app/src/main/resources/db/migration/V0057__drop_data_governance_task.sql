-- Remove data_governance_task table (replaced by temp-pool / is_temp flow).
-- Safe to run even if V0056 was never applied (IF EXISTS).
DROP TABLE IF EXISTS data_governance_task;
