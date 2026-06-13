-- Docker MySQL 初始化数据（由 compose 挂载；库名由 MYSQL_DATABASE 创建）
-- 管理员账号仍由应用 DataInitConfig 写入

INSERT INTO fnd_role (code, name)
SELECT 'admin', '管理员' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM fnd_role WHERE code = 'admin');

INSERT INTO fnd_dict_type (code, name)
SELECT 'event_level', '赛事级别' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM fnd_dict_type WHERE code = 'event_level');

INSERT INTO fnd_dict_item (type_id, code, label, sort_order)
SELECT t.id, v.code, v.label, v.sort_order
FROM fnd_dict_type t
JOIN (
  SELECT 'international' AS code, '国际' AS label, 1 AS sort_order UNION ALL
  SELECT 'national', '全国', 2 UNION ALL
  SELECT 'provincial', '省/市', 3 UNION ALL
  SELECT 'club', '俱乐部/业余', 4
) v
WHERE t.code = 'event_level'
  AND NOT EXISTS (
    SELECT 1 FROM fnd_dict_item i WHERE i.type_id = t.id AND i.code = v.code
  );

INSERT INTO cfg_home_block (block_key, enabled, sort_order, config_json)
SELECT v.block_key, v.enabled, v.sort_order, NULL
FROM (
  SELECT 'banner' AS block_key, 1 AS enabled, 0 AS sort_order UNION ALL
  SELECT 'preview_matches', 1, 1 UNION ALL
  SELECT 'featured_players', 1, 2 UNION ALL
  SELECT 'featured_articles', 1, 3 UNION ALL
  SELECT 'ext_feed', 1, 4 UNION ALL
  SELECT 'highlight_athletes', 1, 5
) v
WHERE NOT EXISTS (SELECT 1 FROM cfg_home_block b WHERE b.block_key = v.block_key);
