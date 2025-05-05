-- 检查是否已存在管理员角色和账户
INSERT INTO roles (name) 
SELECT 'ROLE_ADMIN' WHERE NOT EXISTS (SELECT * FROM roles WHERE name = 'ROLE_ADMIN');

-- 创建管理员账户 (密码为 'admin123')
INSERT INTO users (username, password, email)
SELECT 'admin', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'admin@example.com'
WHERE NOT EXISTS (SELECT * FROM users WHERE username = 'admin');

-- 关联管理员角色
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN'
AND NOT EXISTS (
    SELECT 1 FROM user_roles ur 
    WHERE ur.user_id = u.id AND ur.role_id = r.id
); 