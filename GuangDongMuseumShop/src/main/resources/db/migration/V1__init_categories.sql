-- 清空现有分类
TRUNCATE TABLE categories;

-- 插入新的分类
INSERT INTO categories (id, name, description, parent_id, sort_order, created_at, updated_at) VALUES
(1, '精选文创', '博物馆精选文创产品', NULL, 1, NOW(), NOW()),
(2, '非遗文创', '非物质文化遗产文创产品', NULL, 2, NOW(), NOW()),
(3, '文创礼品', '博物馆特色文创礼品', NULL, 3, NOW(), NOW()),
(4, '茶器茶具', '传统茶道文化用品', NULL, 4, NOW(), NOW()),
(5, '丝巾织品', '丝绸艺术织品', NULL, 5, NOW(), NOW()),
(6, '文房四宝', '传统文房用品', NULL, 6, NOW(), NOW());

-- 设置自增起始值
ALTER TABLE categories AUTO_INCREMENT = 7; 