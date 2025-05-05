-- 创建数据库
CREATE DATABASE IF NOT EXISTS guangdong_museum_shop DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE guangdong_museum_shop;

-- ===============================
-- 表结构创建
-- ===============================

-- 用户相关表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    avatar_url VARCHAR(255),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 角色表
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- 商品分类表
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    parent_id BIGINT,
    level INTEGER,
    sort INT NOT NULL DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES categories(id)
);

-- 商品表
CREATE TABLE IF NOT EXISTS products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    main_image VARCHAR(255),
    cultural_background TEXT,
    sales INT NOT NULL DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    category_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- 商品图片表
CREATE TABLE IF NOT EXISTS product_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    sort INT NOT NULL DEFAULT 0,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- 订单表
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    shipping_name VARCHAR(50) NOT NULL,
    shipping_phone VARCHAR(20) NOT NULL,
    shipping_address TEXT NOT NULL,
    payment_method VARCHAR(20),
    payment_time TIMESTAMP,
    shipping_time TIMESTAMP,
    completion_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 订单项表
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    product_image VARCHAR(255),
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- 评价表
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    rating INT NOT NULL COMMENT '评分(1-5星)',
    content TEXT COMMENT '评价内容',
    anonymous BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- 消息表
CREATE TABLE IF NOT EXISTS messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(20) NOT NULL,
    related_id VARCHAR(50),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 支付记录表
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_no VARCHAR(50) NOT NULL UNIQUE,
    order_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    paid_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- 评价图片表
CREATE TABLE IF NOT EXISTS review_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    review_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    FOREIGN KEY (review_id) REFERENCES reviews(id)
);

-- 删除现有的购物车相关表（如果存在）
DROP TABLE IF EXISTS cart_items;
DROP TABLE IF EXISTS carts;

-- 购物车表
CREATE TABLE IF NOT EXISTS carts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 购物车项表
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    cart_id BIGINT,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    price DECIMAL(10,2) NOT NULL,
    selected BOOLEAN NOT NULL DEFAULT TRUE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (cart_id) REFERENCES carts(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- 地址表
CREATE TABLE IF NOT EXISTS addresses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    receiver VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    province VARCHAR(50) NOT NULL,
    city VARCHAR(50) NOT NULL,
    district VARCHAR(50) NOT NULL,
    detail_address VARCHAR(200) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 创建必要的索引
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_enabled ON products(enabled);
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_reviews_product ON reviews(product_id);
CREATE INDEX idx_reviews_user ON reviews(user_id);
CREATE INDEX idx_messages_user ON messages(user_id);
CREATE INDEX idx_messages_type ON messages(type);
CREATE INDEX idx_messages_created_at ON messages(created_at);

-- ===============================
-- 基础数据
-- ===============================

-- 插入角色（如果不存在）
INSERT INTO roles (name, description)
SELECT 'ADMIN', '管理员'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ADMIN');

INSERT INTO roles (name, description)
SELECT 'USER', '普通用户'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'USER');

-- 插入管理员账号 (密码: admin123)
INSERT INTO users (username, password, email, enabled)
SELECT 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHsM', 'admin@example.com', true
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

-- 关联管理员用户和管理员角色（如果关联不存在）
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin' 
AND r.name = 'ADMIN'
AND NOT EXISTS (
    SELECT 1 
    FROM user_roles ur 
    WHERE ur.user_id = u.id 
    AND ur.role_id = r.id
);

-- 插入基础商品分类
INSERT INTO categories (name, description, parent_id, sort) VALUES
('文创用品', '博物馆文创产品', NULL, 1),
('珠宝首饰', '传统珠宝首饰', NULL, 2),
('陶瓷皿', '传统陶瓷器皿', NULL, 3),
('书籍音像', '相关书籍音像', NULL, 4);

-- ===============================
-- 测试数据（仅在开发环境使用）
-- ===============================

-- 可以通过环境变量或配置控制是否执行以下测试数据插入
-- SET @LOAD_TEST_DATA = TRUE;
-- SELECT IF(@LOAD_TEST_DATA = TRUE, 'Loading test data...', 'Skipping test data...') AS message;

-- IF @LOAD_TEST_DATA = TRUE THEN

-- 插入测试用户 (密码都是123456)
INSERT INTO users (username, password, email, phone, enabled) VALUES
('user1', '$2a$10$X/hX5qrs.NPpVqsX1ZhlKOvL7rH/sX0h0RGfPD7h9qwKHZPYculEa', 'user1@example.com', '13800138001', true),
('user2', '$2a$10$X/hX5qrs.NPpVqsX1ZhlKOvL7rH/sX0h0RGfPD7h9qwKHZPYculEa', 'user2@example.com', '13800138002', true),
('user3', '$2a$10$X/hX5qrs.NPpVqsX1ZhlKOvL7rH/sX0h0RGfPD7h9qwKHZPYculEa', 'user3@example.com', '13800138003', true);

-- 用户角色关联
INSERT INTO user_roles (user_id, role_id) VALUES
(2, 2), -- user1是普通用户
(3, 2), -- user2是普通用户
(4, 2); -- user3是普通用户

-- 插入子分类
INSERT INTO categories (name, description, parent_id) VALUES
('复制品', '文物复制品', 1),
('文具', '文创文具', 1),
('服饰', '文创服饰', 1),
('明信片', '特色明信片', 2),
('工艺品', '传统工艺品', 2),
('字画', '书法绘画', 3),
('雕塑', '艺术雕塑', 3);

-- 插入商品
INSERT INTO products (name, description, category_id, price, stock, enabled, cultural_background) VALUES
('越王勾践剑模型', '越王勾践剑的精美复制品', 4, 299.99, 100, true, '越王勾践剑是春秋末期越王勾践的佩剑，为青铜剑，制作精。'),
('镇馆之宝明信片套装', '博物馆镇馆之宝系列明信片', 7, 29.99, 500, true, '收录博物馆十大镇馆之宝图片。'),
('青铜器摆件', '仿古青铜器艺术摆件', 5, 999.99, 50, true, '参考战国时期青铜器纹样设计。'),
('博物馆主题T恤', '博物馆文创主题T恤', 6, 99.99, 200, true, '融入博物馆元素的现代服饰设计。'),
('考古笔记', '考古主题创意笔记本', 5, 39.99, 300, true, '以考古元素为主题的文创产品。');

-- 插入商品图片
INSERT INTO product_images (product_id, image_url, sort) VALUES
(1, '/images/products/sword1.jpg', 0),
(1, '/images/products/sword2.jpg', 1),
(2, '/images/products/postcard1.jpg', 0),
(3, '/images/products/bronze1.jpg', 0),
(3, '/images/products/bronze2.jpg', 1);

-- 插入订单
INSERT INTO orders (order_no, user_id, total_amount, status, shipping_name, shipping_phone, shipping_address, payment_time) VALUES
('ORD202401010001', 2, 299.99, 'PAID', '张三', '13800138001', '广东省广州市天河区天河路1号', '2024-01-01 10:05:00'),
('ORD202401010002', 3, 29.99, 'PENDING', '李四', '13800138002', '广东省广州市越秀区中山路1号', NULL),
('ORD202401010003', 4, 1099.98, 'COMPLETED', '王五', '13800138003', '广东省广州市海珠区江南大道1号', '2024-01-01 11:05:00');

-- 插入订单项
INSERT INTO order_items (order_id, product_id, product_name, product_image, quantity, price) VALUES
(1, 1, '越王勾践剑模型', '/images/products/sword1.jpg', 1, 299.99),
(2, 2, '镇馆之宝明信片套装', '/images/products/postcard1.jpg', 1, 29.99),
(3, 3, '青铜器摆件', '/images/products/bronze1.jpg', 1, 999.99),
(3, 4, '博物馆主题T恤', '/images/products/tshirt1.jpg', 1, 99.99);

-- 插入支付记录
INSERT INTO payments (payment_no, order_id, amount, status, paid_time) VALUES
('PAY202401010001', 1, 299.99, 'PAID', '2024-01-01 10:05:00'),
('PAY202401010002', 2, 29.99, 'PENDING', NULL),
('PAY202401010003', 3, 1099.98, 'PAID', '2024-01-01 11:05:00');

-- 插入评价
INSERT INTO reviews (user_id, product_id, order_id, rating, content, anonymous) VALUES
(2, 1, 1, 5, '商品质量非常好，很精美', FALSE),
(4, 3, 3, 4, '做工精致，很有收藏价值', FALSE),
(4, 4, 3, 5, '衣服很舒适，设计很有特色', TRUE);

-- 插入评价图片
INSERT INTO review_images (review_id, image_url) VALUES
(1, '/images/reviews/review1_2.jpg'),
(2, '/images/reviews/review2_1.jpg');

-- 插入购物车项
INSERT INTO cart_items (user_id, product_id, quantity, selected, price) VALUES
(3, 3, 1, TRUE, 999.99),  -- 用户3的购物车：青铜器摆件
(3, 4, 2, TRUE, 99.99),   -- 用户3的购物车：主题T恤
(4, 2, 1, FALSE, 29.99);  -- 用户4的购物车：明信片套装

-- 插入消息
INSERT INTO messages (user_id, title, content, type, related_id, is_read) VALUES
(2, '订单支付成功', '您的订单ORD202401010001已支付成功', 'ORDER_STATUS', 'ORD202401010001', TRUE),
(3, '订单创建成功', '您的订单ORD202401010002已创建成功', 'ORDER_STATUS', 'ORD202401010002', FALSE),
(4, '订单已完成', '您的订单ORD202401010003已完成，请评价', 'ORDER_STATUS', 'ORD202401010003', TRUE),
(2, '优惠活动', '新年特惠活动开始啦！', 'PROMOTION', NULL, FALSE),
(3, '系统通知', '系统升级维护通知', 'SYSTEM', NULL, FALSE);

-- END IF; 
