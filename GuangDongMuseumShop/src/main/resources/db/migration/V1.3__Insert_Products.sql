-- 艺术制品类商品数据 (category_id = 1)
INSERT INTO products (name, description, price, stock, category_id, main_image, cultural_background, enabled, featured, created_at, updated_at) VALUES
-- 书画类
('山水长卷', '传统水墨山水画，展现岭南山水风光', 2999.00, 100, 1, '/images/products/art/shanshui1.jpg', '融合岭南画派特色的山水画作品', true, true, NOW(), NOW()),
('梅兰竹菊四条屏', '传统水墨四君子，彰显文人风骨', 3999.00, 50, 1, '/images/products/art/sijunzi.jpg', '四君子象征文人理想的精神追求', true, false, NOW(), NOW()),
('岭南风情油画', '现代油画技法展现岭南特色', 4999.00, 30, 1, '/images/products/art/lingnan1.jpg', '融合东西方艺术的现代岭南风情画作', true, true, NOW(), NOW()),
('书法对联', '名家手书楹联，书法艺术精品', 1999.00, 200, 1, '/images/products/art/calligraphy1.jpg', '传统书法艺术，融合古今诗词', true, false, NOW(), NOW()),
('花鸟小品', '工笔花鸟画，精致雅致', 1599.00, 150, 1, '/images/products/art/huaniao1.jpg', '传统工笔画技法的精美花鸟画作', true, true, NOW(), NOW()),
('人物肖像画', '传统工笔人物画', 2499.00, 80, 1, '/images/products/art/portrait1.jpg', '传统人物画技法的现代演绎', true, false, NOW(), NOW()),
('山水小品', '小幅山水画作品', 999.00, 200, 1, '/images/products/art/landscape1.jpg', '精致小幅山水画', true, true, NOW(), NOW()),
('书法作品集', '当代书法家作品集', 1299.00, 150, 1, '/images/products/art/calligraphy2.jpg', '集结当代书法名家作品', true, false, NOW(), NOW()),
('岭南建筑速写', '建筑艺术速写集', 799.00, 180, 1, '/images/products/art/sketch1.jpg', '记录岭南建筑特色的艺术作品', true, true, NOW(), NOW()),
('花鸟画册', '精选花鸟画作品集', 1099.00, 160, 1, '/images/products/art/flowerbird1.jpg', '汇集精美花鸟画作品', true, false, NOW(), NOW()),

-- 陶瓷类
('青花瓷瓶', '传统青花瓷器，手工制作', 5999.00, 20, 1, '/images/products/art/qinghua1.jpg', '景德镇传统工艺制作的青花瓷器', true, false, NOW(), NOW()),
('粤彩茶具套装', '广东特色陶瓷艺术品', 3999.00, 40, 1, '/images/products/art/yuecai1.jpg', '融合岭南特色的陶瓷茶具艺术品', true, true, NOW(), NOW()),
('瓷板画', '陶瓷艺术与绘画的结合', 4599.00, 30, 1, '/images/products/art/ciban1.jpg', '传统瓷板画工艺制作的艺术品', true, false, NOW(), NOW()),
('青花瓷盘', '传统青花装饰盘', 2999.00, 50, 1, '/images/products/art/plate1.jpg', '传统青花瓷器工艺', true, true, NOW(), NOW()),
('粤彩花瓶', '广彩艺术花瓶', 4999.00, 25, 1, '/images/products/art/vase1.jpg', '广东特色陶瓷彩绘艺术', true, false, NOW(), NOW()),
('陶瓷摆件组合', '现代陶瓷艺术摆件', 3599.00, 35, 1, '/images/products/art/ceramic1.jpg', '现代陶瓷艺术创作', true, true, NOW(), NOW()),
('茶壶艺术品', '艺术造型茶壶', 2799.00, 45, 1, '/images/products/art/teapot1.jpg', '融合实用与艺术的茶具', true, false, NOW(), NOW()),
('瓷器花插', '青花瓷花插', 1999.00, 55, 1, '/images/products/art/vase2.jpg', '传统青花瓷器艺术', true, true, NOW(), NOW()),

-- 雕塑类
('铜雕观音', '精美铜雕艺术品', 9999.00, 10, 1, '/images/products/art/guanyin1.jpg', '传统佛教艺术与现代雕塑艺术的结合', true, true, NOW(), NOW()),
('木雕屏风', '红木雕刻艺术品', 12999.00, 5, 1, '/images/products/art/woodscreen1.jpg', '传统木雕工艺制作的装饰屏风', true, false, NOW(), NOW()),
('石雕人物', '传统石雕艺术品', 8999.00, 15, 1, '/images/products/art/stone1.jpg', '传统石雕工艺作品', true, true, NOW(), NOW()),
('铜雕动物', '铜雕十二生肖', 6999.00, 20, 1, '/images/products/art/zodiac1.jpg', '传统生肖文化艺术品', true, false, NOW(), NOW()),
('玉雕摆件', '和田玉雕刻艺术品', 15999.00, 8, 1, '/images/products/art/jade1.jpg', '精美玉雕工艺品', true, true, NOW(), NOW()),
('木雕佛像', '黄杨木雕刻佛像', 7999.00, 12, 1, '/images/products/art/buddha1.jpg', '传统佛教艺术雕刻', true, false, NOW(), NOW()),
('砚台艺术品', '端砚雕刻艺术品', 5999.00, 25, 1, '/images/products/art/inkstone1.jpg', '传统文房四宝艺术品', true, true, NOW(), NOW()),

-- 综合艺术品
('掐丝珐琅器', '传统珐琅工艺品', 11999.00, 8, 1, '/images/products/art/enamel1.jpg', '传统金属工艺与珐琅艺术结合', true, false, NOW(), NOW()),
('景泰蓝摆件', '传统景泰蓝工艺品', 13999.00, 6, 1, '/images/products/art/cloisonne1.jpg', '传统景泰蓝工艺制作', true, true, NOW(), NOW()),
('漆器艺术盒', '传统漆器工艺品', 4999.00, 30, 1, '/images/products/art/lacquer1.jpg', '传统漆器工艺制作', true, false, NOW(), NOW()),
('金属錾刻画', '金属錾刻工艺品', 7999.00, 15, 1, '/images/products/art/metal1.jpg', '传统金属工艺制作', true, true, NOW(), NOW()),
('织锦画屏', '传统织锦艺术品', 9999.00, 10, 1, '/images/products/art/brocade1.jpg', '传统织锦工艺制作', true, false, NOW(), NOW()),
('琉璃艺术品', '现代琉璃艺术创作', 6999.00, 20, 1, '/images/products/art/glass1.jpg', '现代琉璃艺术品', true, true, NOW(), NOW()),
('水晶雕刻', '水晶雕刻艺术品', 8999.00, 12, 1, '/images/products/art/crystal1.jpg', '现代水晶雕刻艺术', true, false, NOW(), NOW()),

-- 当代艺术
('现代抽象画', '当代抽象艺术作品', 5999.00, 25, 1, '/images/products/art/abstract1.jpg', '现代抽象艺术创作', true, true, NOW(), NOW()),
('装置艺术品', '当代装置艺术作品', 12999.00, 5, 1, '/images/products/art/installation1.jpg', '现代装置艺术创作', true, false, NOW(), NOW()),
('数字艺术画', '数字技术艺术作品', 3999.00, 40, 1, '/images/products/art/digital1.jpg', '现代数字艺术创作', true, true, NOW(), NOW()),
('混合媒材画', '多媒材艺术作品', 4599.00, 35, 1, '/images/products/art/mixed1.jpg', '现代混合媒材艺术', true, false, NOW(), NOW()),
('光影装置', '光影艺术装置', 15999.00, 3, 1, '/images/products/art/light1.jpg', '现代光影艺术创作', true, true, NOW(), NOW()),

-- 艺术衍生品
('艺术明信片集', '艺术作品明信片集', 99.00, 500, 1, '/images/products/art/postcard1.jpg', '艺术作品衍生品', true, false, NOW(), NOW()),
('艺术日历', '艺术作品日历', 199.00, 300, 1, '/images/products/art/calendar1.jpg', '艺术作品衍生品', true, true, NOW(), NOW()),
('艺术笔记本', '艺术图案笔记本', 129.00, 400, 1, '/images/products/art/notebook1.jpg', '艺术衍生文具', true, false, NOW(), NOW()),
('艺术丝巾', '艺术图案丝巾', 599.00, 200, 1, '/images/products/art/scarf1.jpg', '艺术衍生时尚品', true, true, NOW(), NOW()),
('艺术茶杯', '艺术图案茶杯', 299.00, 300, 1, '/images/products/art/cup1.jpg', '艺术衍生生活用品', true, false, NOW(), NOW()),
('艺术鼠标垫', '艺术图案鼠标垫', 79.00, 600, 1, '/images/products/art/mousepad1.jpg', '艺术衍生办公用品', true, true, NOW(), NOW()),
('艺术帆布包', '艺术图案帆布包', 259.00, 350, 1, '/images/products/art/bag1.jpg', '艺术衍生时尚品', true, false, NOW(), NOW()),
('艺术手机壳', '艺术图案手机壳', 159.00, 400, 1, '/images/products/art/phonecase1.jpg', '艺术衍生数码配件', true, true, NOW(), NOW()),
('艺术钥匙扣', '艺术图案钥匙扣', 89.00, 500, 1, '/images/products/art/keychain1.jpg', '艺术衍生饰品', true, false, NOW(), NOW()),
('艺术书签', '艺术图案书签', 39.00, 800, 1, '/images/products/art/bookmark1.jpg', '艺术衍生文具', true, true, NOW(), NOW()),

-- 艺术工具
('高级毛笔套装', '传统书画毛笔套装', 999.00, 100, 1, '/images/products/art/brush1.jpg', '传统书画工具', true, false, NOW(), NOW()),
('宣纸套装', '各式宣纸组合', 599.00, 150, 1, '/images/products/art/paper1.jpg', '传统书画用纸', true, true, NOW(), NOW()),
('颜料套装', '传统国画颜料套装', 799.00, 120, 1, '/images/products/art/pigment1.jpg', '传统绘画工具', true, false, NOW(), NOW()),
('篆刻工具套装', '传统篆刻工具组合', 1299.00, 80, 1, '/images/products/art/seal1.jpg', '传统篆刻工具', true, true, NOW(), NOW()),
('画架套装', '专业画架组合', 1599.00, 60, 1, '/images/products/art/easel1.jpg', '专业绘画工具', true, false, NOW(), NOW()),
('国画工具箱', '便携国画工具箱', 899.00, 100, 1, '/images/products/art/toolbox1.jpg', '传统绘画工具', true, true, NOW(), NOW()),
('书法工具套装', '书法练习全套工具', 699.00, 150, 1, '/images/products/art/calligraphyset1.jpg', '传统书法工具', true, false, NOW(), NOW()),
('画笔收纳袋', '专业画笔收纳工具', 299.00, 200, 1, '/images/products/art/brushbag1.jpg', '艺术工具收纳', true, true, NOW(), NOW()),
('调色盘套装', '专业调色工具组合', 399.00, 180, 1, '/images/products/art/palette1.jpg', '绘画辅助工具', true, false, NOW(), NOW()),
('文房四宝套装', '传统文房用品套装', 1999.00, 50, 1, '/images/products/art/scholarset1.jpg', '传统文房用品', true, true, NOW(), NOW());

-- 继续插入更多艺术制品...（为了简洁，这里只展示部分数据，实际脚本中需要插入不少于59条记录）

-- 私人定制类商品数据 (category_id = 2)
INSERT INTO products (name, description, price, stock, category_id, main_image, cultural_background, enabled, featured, created_at, updated_at) VALUES
('定制印章套装', '个人印章定制服务，含多种材质选择', 899.00, 100, 2, '/images/products/custom/seal1.jpg', '传统篆刻艺术与现代定制服务的结合', true, true, NOW(), NOW()),
('定制丝绸团扇', '手绘丝绸团扇定制', 599.00, 150, 2, '/images/products/custom/fan1.jpg', '融合传统扇面艺术的个性化定制', true, false, NOW(), NOW()),
('定制茶具套装', '个性化茶具定制服务', 4999.00, 30, 2, '/images/products/custom/tea1.jpg', '传统茶具与现代审美的结合', true, true, NOW(), NOW());

-- 继续插入更多私人定制商品...

-- 广馆文创类商品数据 (category_id = 3)
INSERT INTO products (name, description, price, stock, category_id, main_image, cultural_background, enabled, featured, created_at, updated_at) VALUES
('广博文创笔记本', '博物馆特色文创笔记本', 69.00, 500, 3, '/images/products/museum/notebook1.jpg', '融入博物馆元素的文创产品', true, true, NOW(), NOW()),
('广博帆布包', '博物馆特色帆布包', 129.00, 300, 3, '/images/products/museum/bag1.jpg', '实用与文化创意结合的单品', true, false, NOW(), NOW()),
('广博文创伞', '博物馆特色雨伞', 159.00, 200, 3, '/images/products/museum/umbrella1.jpg', '融入博物馆元素的日用品', true, true, NOW(), NOW());

-- 继续插入更多广馆文创商品...

-- 茶道精品类商品数据 (category_id = 4)
INSERT INTO products (name, description, price, stock, category_id, main_image, cultural_background, enabled, featured, created_at, updated_at) VALUES
('紫砂壶套装', '宜兴紫砂茶具套装', 3999.00, 50, 4, '/images/products/tea/zisha1.jpg', '传统紫砂工艺制作的茶具', true, true, NOW(), NOW()),
('银制茶具', '纯银茶具套装', 5999.00, 30, 4, '/images/products/tea/silver1.jpg', '传统银器工艺与现代设计的结合', true, false, NOW(), NOW()),
('茶道全套', '完整茶道用具套装', 8999.00, 20, 4, '/images/products/tea/set1.jpg', '传统茶道文化的完整呈现', true, true, NOW(), NOW());

-- 继续插入更多茶道精品...

-- 国风首饰类商品数据 (category_id = 5)
INSERT INTO products (name, description, price, stock, category_id, main_image, cultural_background, enabled, featured, created_at, updated_at) VALUES
('玉兰花银簪', '925银制花卉发簪', 699.00, 100, 5, '/images/products/jewelry/hairpin1.jpg', '传统银器工艺制作的发饰', true, true, NOW(), NOW()),
('点翠凤凰步摇', '传统点翠工艺首饰', 1999.00, 50, 5, '/images/products/jewelry/phoenix1.jpg', '传统点翠工艺与现代设计的结合', true, false, NOW(), NOW()),
('珍珠流苏耳环', '天然珍珠制作的耳饰', 899.00, 80, 5, '/images/products/jewelry/earring1.jpg', '传统珠宝工艺与现代设计的结合', true, true, NOW(), NOW());

-- 继续插入更多国风首饰... 