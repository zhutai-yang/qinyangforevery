-- 本地重建库（手动执行，不自动运行）
-- 用法：mysql -uroot -p < tools/rebuild-database.sql

SOURCE ../schema.sql;
SOURCE ../data.sql;
