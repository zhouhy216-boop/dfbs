-- V0064__org_position_catalog_and_templates_seed.sql
-- Seed position catalog and level templates (v1 defaults). Run after V0063.

-- Catalog: base_name, grade, display_name, short_name, order_index
-- Grade: PRIMARY=正, DEPUTY=副, ACTING=担当, NONE=无
INSERT INTO org_position_catalog (base_name, grade, display_name, short_name, order_index, is_enabled) VALUES
('总经理', 'NONE', '总经理', '总经理', 10, true),
('本部长', 'PRIMARY', '正本部长', '本部长', 21, true),
('本部长', 'DEPUTY', '副本部长', '本部长', 22, true),
('本部长', 'ACTING', '担当本部长', '本部长', 23, true),
('部长', 'PRIMARY', '正部长', '部长', 31, true),
('部长', 'DEPUTY', '副部长', '部长', 32, true),
('部长', 'ACTING', '担当部长', '部长', 33, true),
('课长', 'PRIMARY', '正课长', '课长', 41, true),
('课长', 'DEPUTY', '副课长', '课长', 42, true),
('课长', 'ACTING', '担当课长', '课长', 43, true),
('系长', 'PRIMARY', '正系长', '系长', 51, true),
('系长', 'DEPUTY', '副系长', '系长', 52, true),
('系长', 'ACTING', '担当系长', '系长', 53, true),
('班长', 'PRIMARY', '正班长', '班长', 61, true),
('班长', 'DEPUTY', '副班长', '班长', 62, true),
('班长', 'ACTING', '担当班长', '班长', 63, true),
('职员', 'NONE', '职员', '职员', 100, true);

-- Templates: level (by display_name) -> position_id. One row per (level, position).
-- 公司: 总经理 + 职员
INSERT INTO org_level_position_template (level_id, position_id, is_enabled)
SELECT l.id, p.id, true FROM org_level l CROSS JOIN org_position_catalog p
WHERE l.display_name = '公司' AND p.base_name IN ('总经理','职员');
-- 本部: 正/副/担当本部长 + 职员
INSERT INTO org_level_position_template (level_id, position_id, is_enabled)
SELECT l.id, p.id, true FROM org_level l CROSS JOIN org_position_catalog p
WHERE l.display_name = '本部' AND ((p.base_name = '本部长' AND p.grade IN ('PRIMARY','DEPUTY','ACTING')) OR p.base_name = '职员');
-- 部: 正/副/担当部长 + 职员
INSERT INTO org_level_position_template (level_id, position_id, is_enabled)
SELECT l.id, p.id, true FROM org_level l CROSS JOIN org_position_catalog p
WHERE l.display_name = '部' AND ((p.base_name = '部长' AND p.grade IN ('PRIMARY','DEPUTY','ACTING')) OR p.base_name = '职员');
-- 课: 正/副/担当课长 + 职员
INSERT INTO org_level_position_template (level_id, position_id, is_enabled)
SELECT l.id, p.id, true FROM org_level l CROSS JOIN org_position_catalog p
WHERE l.display_name = '课' AND ((p.base_name = '课长' AND p.grade IN ('PRIMARY','DEPUTY','ACTING')) OR p.base_name = '职员');
-- 系: 正/副/担当系长 + 职员
INSERT INTO org_level_position_template (level_id, position_id, is_enabled)
SELECT l.id, p.id, true FROM org_level l CROSS JOIN org_position_catalog p
WHERE l.display_name = '系' AND ((p.base_name = '系长' AND p.grade IN ('PRIMARY','DEPUTY','ACTING')) OR p.base_name = '职员');
-- 班: 正/副/担当班长 + 职员
INSERT INTO org_level_position_template (level_id, position_id, is_enabled)
SELECT l.id, p.id, true FROM org_level l CROSS JOIN org_position_catalog p
WHERE l.display_name = '班' AND ((p.base_name = '班长' AND p.grade IN ('PRIMARY','DEPUTY','ACTING')) OR p.base_name = '职员');
