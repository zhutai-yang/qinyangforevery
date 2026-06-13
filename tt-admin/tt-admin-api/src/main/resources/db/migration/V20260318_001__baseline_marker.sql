-- Flyway baseline marker
-- 基线表结构见 schema.sql（Docker/本地 init 已执行全量 DDL 时使用 baseline-on-migrate）
-- 后续增量变更在此目录新增 V{YYYYMMDD}_{seq}__{desc}.sql，并同步更新 schema.sql

SELECT 1;
