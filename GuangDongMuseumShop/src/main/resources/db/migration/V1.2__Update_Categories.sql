-- 按照依赖关系顺序删除数据
DELETE FROM order_items;
DELETE FROM orders;
DELETE FROM cart_items;
DELETE FROM reviews;
DELETE FROM product_images;
DELETE FROM products;
DELETE FROM categories;

-- 插入新的分类
INSERT INTO categories (id, name, description, parent_id, sort_order, created_at, updated_at) VALUES
(1, '艺术制品', '博物馆艺术制品', NULL, 1, NOW(), NOW()),
(2, '私人定制', '私人定制文创产品', NULL, 2, NOW(), NOW()),
(3, '广馆文创', '广东博物馆特色文创', NULL, 3, NOW(), NOW()),
(4, '茶道精品', '传统茶道文化精品', NULL, 4, NOW(), NOW()),
(5, '国风首饰', '中国传统风格首饰', NULL, 5, NOW(), NOW());

-- 设置自增起始值
ALTER TABLE categories AUTO_INCREMENT = 6; 