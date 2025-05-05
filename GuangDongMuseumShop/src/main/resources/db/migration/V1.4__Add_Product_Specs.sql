-- 创建规格模板表
CREATE TABLE IF NOT EXISTS spec_templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    category_id BIGINT,
    specs JSON,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建商品规格表
CREATE TABLE IF NOT EXISTS product_specs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    spec_name VARCHAR(100) NOT NULL,
    spec_options JSON,
    sort_order INT DEFAULT 0,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- 插入规格模板数据
INSERT INTO spec_templates (name, code, category_id, specs) VALUES 
('艺术制品规格', 'ART_SPEC', 1, '{"尺寸": ["小幅(30x40cm)", "中幅(50x70cm)", "大幅(70x100cm)"], "装裱": ["简装", "精装", "豪华装裱"], "题字": ["无题字", "自定义题字"]}'),
('茶具规格', 'TEA_SET_SPEC', 4, '{"材质": ["陶瓷", "紫砂", "玻璃"], "容量": ["小杯(100ml)", "中杯(150ml)", "大杯(200ml)"], "款式": ["简约", "传统", "现代"]}'),
('首饰规格', 'JEWELRY_SPEC', 5, '{"材质": ["银饰", "玉石", "珍珠"], "尺寸": ["小号", "中号", "大号"], "包装": ["简装", "精装", "礼盒"]}'),
('定制文创规格', 'CUSTOM_SPEC', 2, '{"尺寸": ["小号", "中号", "大号"], "材质": ["纸质", "丝绸", "金属"], "定制内容": ["无", "自定义"]}'),
('广馆文创规格', 'MUSEUM_SPEC', 3, '{"类型": ["书签", "明信片", "摆件"], "材质": ["纸质", "金属", "树脂"], "包装": ["简装", "精装", "礼盒"]}');

-- 插入商品规格数据
-- 山水长卷规格
INSERT INTO product_specs (product_id, spec_name, spec_options, sort_order) VALUES 
(17, '尺寸', '[{"value": "small", "label": "小幅(30x40cm)", "enabled": true}, {"value": "medium", "label": "中幅(50x70cm)", "enabled": true}, {"value": "large", "label": "大幅(70x100cm)", "enabled": true}]', 1),
(17, '装裱', '[{"value": "simple", "label": "简装", "enabled": true}, {"value": "deluxe", "label": "精装", "enabled": true}, {"value": "premium", "label": "豪华装裱", "enabled": true}]', 2),
(17, '题字', '[{"value": "none", "label": "无题字", "enabled": true}, {"value": "custom", "label": "自定义题字", "enabled": true}]', 3);

-- 茶具套装规格
INSERT INTO product_specs (product_id, spec_name, spec_options, sort_order) VALUES 
(18, '材质', '[{"value": "ceramic", "label": "陶瓷", "enabled": true}, {"value": "purple_clay", "label": "紫砂", "enabled": true}, {"value": "glass", "label": "玻璃", "enabled": true}]', 1),
(18, '容量', '[{"value": "small", "label": "小杯(100ml)", "enabled": true}, {"value": "medium", "label": "中杯(150ml)", "enabled": true}, {"value": "large", "label": "大杯(200ml)", "enabled": true}]', 2),
(18, '款式', '[{"value": "simple", "label": "简约", "enabled": true}, {"value": "traditional", "label": "传统", "enabled": true}, {"value": "modern", "label": "现代", "enabled": true}]', 3);

-- 珍珠项链规格
INSERT INTO product_specs (product_id, spec_name, spec_options, sort_order) VALUES 
(19, '材质', '[{"value": "pearl", "label": "珍珠", "enabled": true}, {"value": "silver", "label": "银饰", "enabled": true}]', 1),
(19, '尺寸', '[{"value": "40cm", "label": "40cm", "enabled": true}, {"value": "45cm", "label": "45cm", "enabled": true}, {"value": "50cm", "label": "50cm", "enabled": true}]', 2),
(19, '包装', '[{"value": "simple", "label": "简装", "enabled": true}, {"value": "gift", "label": "礼盒", "enabled": true}]', 3);

-- 定制丝巾规格
INSERT INTO product_specs (product_id, spec_name, spec_options, sort_order) VALUES 
(20, '尺寸', '[{"value": "small", "label": "小号(50x50cm)", "enabled": true}, {"value": "medium", "label": "中号(70x70cm)", "enabled": true}, {"value": "large", "label": "大号(90x90cm)", "enabled": true}]', 1),
(20, '材质', '[{"value": "silk", "label": "真丝", "enabled": true}, {"value": "satin", "label": "缎面", "enabled": true}]', 2),
(20, '图案', '[{"value": "traditional", "label": "传统纹样", "enabled": true}, {"value": "custom", "label": "自定义图案", "enabled": true}]', 3);

-- 博物馆文创套装规格
INSERT INTO product_specs (product_id, spec_name, spec_options, sort_order) VALUES 
(21, '套装类型', '[{"value": "basic", "label": "基础套装", "enabled": true}, {"value": "deluxe", "label": "豪华套装", "enabled": true}, {"value": "collector", "label": "收藏版", "enabled": true}]', 1),
(21, '内容', '[{"value": "stationery", "label": "文具套装", "enabled": true}, {"value": "accessories", "label": "配饰套装", "enabled": true}, {"value": "complete", "label": "完整套装", "enabled": true}]', 2),
(21, '包装', '[{"value": "simple", "label": "简装", "enabled": true}, {"value": "gift", "label": "礼盒", "enabled": true}, {"value": "premium", "label": "豪华礼盒", "enabled": true}]', 3);

-- 插入艺术品规格模板
INSERT INTO spec_templates (name, code, category_id, specs, enabled) VALUES
('艺术品规格模板', 'art_spec_template', 1, 
 '[
    {"name": "尺寸", "options": [
        {"value": "small", "label": "小幅(30x40cm)", "enabled": true},
        {"value": "medium", "label": "中幅(50x70cm)", "enabled": true},
        {"value": "large", "label": "大幅(70x100cm)", "enabled": true}
    ]},
    {"name": "装裱", "options": [
        {"value": "simple", "label": "简装", "enabled": true},
        {"value": "deluxe", "label": "精装", "enabled": true},
        {"value": "premium", "label": "豪华装裱", "enabled": true}
    ]},
    {"name": "题字", "options": [
        {"value": "none", "label": "无题字", "enabled": true},
        {"value": "custom", "label": "自定义题字", "enabled": true}
    ]}
 ]', 
 true
);

-- 插入规格模板数据
INSERT INTO spec_templates (name, code, category_id, specs, enabled) VALUES
-- 书画类模板
('书画类规格', 'PAINTING', 1, 
 '{
   "specs": [
     {
       "name": "尺寸",
       "options": [
         {"value": "small", "label": "小号(30x40cm)", "enabled": true},
         {"value": "medium", "label": "中号(50x70cm)", "enabled": true},
         {"value": "large", "label": "大号(70x100cm)", "enabled": true}
       ]
     },
     {
       "name": "材质",
       "options": [
         {"value": "xuan_paper", "label": "宣纸", "enabled": true},
         {"value": "silk", "label": "绢本", "enabled": true},
         {"value": "special_paper", "label": "特种纸", "enabled": true}
       ]
     },
     {
       "name": "装裱",
       "options": [
         {"value": "scroll", "label": "卷轴", "enabled": true},
         {"value": "frame", "label": "框装", "enabled": true},
         {"value": "board", "label": "裱板", "enabled": true}
       ]
     }
   ]
 }',
 true),

-- 陶瓷类模板
('陶瓷类规格', 'CERAMIC', 1,
 '{
   "specs": [
     {
       "name": "规格",
       "options": [
         {"value": "small", "label": "小件(高度<20cm)", "enabled": true},
         {"value": "medium", "label": "中件(20-40cm)", "enabled": true},
         {"value": "large", "label": "大件(>40cm)", "enabled": true}
       ]
     },
     {
       "name": "材质工艺",
       "options": [
         {"value": "blue_white", "label": "青花瓷", "enabled": true},
         {"value": "yue_cai", "label": "粤彩", "enabled": true},
         {"value": "glaze", "label": "釉下彩", "enabled": true}
       ]
     },
     {
       "name": "制作工艺",
       "options": [
         {"value": "handmade", "label": "纯手工", "enabled": true},
         {"value": "semi_handmade", "label": "半手工", "enabled": true},
         {"value": "machine", "label": "机器压制", "enabled": true}
       ]
     }
   ]
 }',
 true),

-- 茶具类模板
('茶具类规格', 'TEA_SET', 4,
 '{
   "specs": [
     {
       "name": "材质",
       "options": [
         {"value": "purple_clay", "label": "紫砂", "enabled": true},
         {"value": "sterling_silver", "label": "纯银", "enabled": true},
         {"value": "copper", "label": "紫铜", "enabled": true}
       ]
     },
     {
       "name": "容量",
       "options": [
         {"value": "150ml", "label": "150ml", "enabled": true},
         {"value": "200ml", "label": "200ml", "enabled": true},
         {"value": "250ml", "label": "250ml", "enabled": true}
       ]
     },
     {
       "name": "工艺",
       "options": [
         {"value": "handmade", "label": "全手工", "enabled": true},
         {"value": "semi_handmade", "label": "半手工", "enabled": true},
         {"value": "machine", "label": "机器压制", "enabled": true}
       ]
     }
   ]
 }',
 true),

-- 文创衍生品模板
('文创衍生品规格', 'CULTURAL_PRODUCT', 3,
 '{
   "specs": [
     {
       "name": "尺寸",
       "options": [
         {"value": "s", "label": "S", "enabled": true},
         {"value": "m", "label": "M", "enabled": true},
         {"value": "l", "label": "L", "enabled": true}
       ]
     },
     {
       "name": "颜色",
       "options": [
         {"value": "classic", "label": "经典款", "enabled": true},
         {"value": "modern", "label": "现代款", "enabled": true},
         {"value": "limited", "label": "限定款", "enabled": true}
       ]
     }
   ]
 }',
 true); 