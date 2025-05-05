# jianluo的毕业设计

## 项目概述

一个专注于博物馆文化创意产品在线销售系统。本项目采用前后端分离架构，基于Spring Boot 3.4.1和Vue3开发，旨在为博物馆文创产品提供一个专业、高效、用户友好的线上销售渠道。

### 项目特点

- 🎨 专业的文创产品展示系统
- 🛡️ 完善的安全认证机制
- 🚀 高性能的前后端分离架构
- 📱 响应式设计，支持多端访问
- 🛒 直观的购物流程
- 📊 强大的后台管理功能
- 📈 全面的数据统计分析

## 业务流程

### 1. 用户管理流程
- **用户注册**
  - 表单信息验证
  - 用户名和邮箱唯一性检查
  - 密码安全加密
  - 账号创建与权限分配
- **用户登录**
  - 身份认证
  - JWT令牌生成
  - 会话管理
- **个人信息管理**
  - 基本信息维护
  - 收货地址管理

### 2. 文创产品展示流程
- **分类展示**
  - 产品类型分类
  - 文化主题分类
  - 新品展示
  - 热销排行
- **详情展示**
  - 基本信息展示
  - 图片展示
  - 规格与价格
  - 库存与销量
  - 相关推荐

### 3. 购物流程
- **购物车管理**
  - 商品添加
  - 数量修改
  - 商品选择
  - 一键清空
- **订单创建**
  - 订单信息确认
  - 收货地址选择
  - 支付处理
  - 订单状态更新
- **订单管理**
  - 订单查询
  - 订单详情
  - 订单取消
  - 收货确认

### 4. 数据统计与分析
- **销售统计**
  - 实时销售数据
  - 销售趋势分析
  - 商品销量排名
  - 销售额统计
- **用户行为分析**
  - 浏览记录分析
  - 购买偏好分析
  - 评价数据分析

### 5. 搜索与推荐
- **商品搜索**
  - 关键词智能搜索
  - 分类筛选
  - 价格区间筛选
- **个性化推荐**
  - 相似商品推荐
  - 热销商品推荐
  - 搜索历史记录

### 6. 消息通知
- **订单通知**
  - 创建成功通知
  - 支付状态通知
  - 发货通知
  - 收货提醒
- **营销通知**
  - 新品上架推送
  - 促销活动提醒
  - 优惠券通知
  - 会员活动推送

## 系统架构

### 后端技术栈
- **核心框架**: Spring Boot 3.4.1
- **安全框架**: Spring Security + JWT
- **数据持久化**: Spring Data JPA
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **对象映射**: MapStruct
- **API文档**: Swagger/OpenAPI
- **构建工具**: Maven
- **其他工具**: Lombok, Commons-lang3

### 前端技术栈
- **核心框架**: Vue 3 + TypeScript
- **构建工具**: Vite
- **UI组件**: Element Plus
- **状态管理**: Pinia
- **路由管理**: Vue Router
- **富文本编辑**: WangEditor
- **HTTP客户端**: Axios
- **样式处理**: Sass/SCSS

## 核心功能

### 用户端功能
- **账户管理**
  - 个人信息管理
  - 账户安全设置

- **商品功能**
  - 分类浏览（多级分类）
  - 高级搜索过滤
  - 商品详情展示
  - 收藏夹管理

- **购物功能**
  - 购物车管理
  - 订单创建与支付
  - 订单状态跟踪

### 管理端功能
- **商品管理**
  - 商品信息维护
  - 库存管理
  - 分类体系管理
  - 商品上下架
  - 图片资源管理

- **订单管理**
  - 订单状态管理
  - 支付记录查询
  - 订单明细查看
  - 发货管理

- **系统管理**
  - 管理员账户管理
  - 权限配置
  - 系统参数设置
  - 数据统计分析


## 环境要求

- JDK 17 或更高版本
- Node.js 16.x 或更高版本
- MySQL 8.0+
- Redis 6.0+
- Maven 3.6+
- 现代浏览器（Chrome, Firefox, Safari, Edge等）

## 快速开始
```bash
# 克隆项目
git clone git@github.com:jianluo999/GuangDongMuseumShop.git
```

### 数据库初始化

1. 确保MySQL服务已启动：

2. 执行数据库初始化脚本：
```bash
# 进入项目目录
cd GuangDongMuseumShop

# 执行mysql数据库表结构初始化脚本
src/main/resources/db/init.sql

# 执行mysql管理员账户初始化脚本
src/main/resources/db/init-admin.sql

```

初始化后的默认管理员账户信息：
- 用户名：admin
- 密码：admin123
- 邮箱：admin@example.com

### 后端服务启动

```bash

# 进入项目目录
cd GuangDongMuseumShop

# 修改src/main/resources/application.properties和application.yml下面的数据库配置

# 安装依赖
mvn clean install

# 启动项目
mvn spring-boot:run
```

### 前端项目启动

```bash
# 进入前端项目目录
cd GuangDongMuseumShop-vue

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产环境代码
npm run build
```

## 项目结构

```
GuangDongMuseumShop
├── backend/                     # 后端项目根目录
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/          # Java源代码
│   │   │   │   └── com/gdcp/
│   │   │   │       ├── config/       # 配置类
│   │   │   │       ├── controller/   # 控制器
│   │   │   │       ├── service/      # 服务层
│   │   │   │       ├── repository/   # 数据访问层
│   │   │   │       ├── model/        # 数据模型
│   │   │   │       ├── dto/          # 数据传输对象
│   │   │   │       ├── security/     # 安全相关
│   │   │   │       └── utils/        # 工具类
│   │   │   └── resources/    # 配置文件
│   │   └── test/             # 测试代码
│   │
│   └── pom.xml               # Maven配置文件
│
├── frontend/                 # 前端项目根目录
│   ├── src/
│   │   ├── api/             # API接口
│   │   ├── assets/          # 静态资源
│   │   ├── components/      # 公共组件
│   │   ├── composables/     # 组合式函数
│   │   ├── layouts/         # 布局组件
│   │   ├── router/          # 路由配置
│   │   ├── store/           # 状态管理
│   │   ├── styles/          # 全局样式
│   │   ├── utils/           # 工具函数
│   │   └── views/           # 页面组件
│   ├── package.json         # 项目依赖
│   └── vite.config.ts       # Vite配置
│
└── docs/                    # 项目文档
```

## 配置说明

### 后端配置
配置文件位于 `src/main/resources/` 目录：
- `application.yml`: 主配置文件
- `application-dev.yml`: 开发环境配置
- `application-prod.yml`: 生产环境配置

主要配置项包括：
- 数据库连接配置
- Redis连接配置
- JWT密钥配置
- 文件上传配置
- 跨域配置
- 日志配置

### 前端配置
- `.env.development`: 开发环境配置
- `.env.production`: 生产环境配置
- `vite.config.ts`: Vite构建配置
- `tsconfig.json`: TypeScript配置

## 部署指南

### 后端部署
1. 准备服务器环境（JDK, MySQL, Redis）
2. 配置数据库和Redis
3. 修改生产环境配置文件
4. 打包项目：`mvn clean package`
5. 运行jar包：`java -jar target/guangdong-museum-shop.jar`

### 前端部署
1. 安装Node.js环境
2. 修改生产环境API配置
3. 构建项目：`npm run build`
4. 将dist目录部署到Web服务器
   
### 关键页面
![image](https://github.com/user-attachments/assets/5bd603b3-714c-4900-a7a2-289235dc74b9)

![image](https://github.com/user-attachments/assets/6ab61069-d0c3-4c15-8115-c758b08d3d36)

![image](https://github.com/user-attachments/assets/cf73dee3-f205-4f1f-90e9-f85d68c6b56c)

![image](https://github.com/user-attachments/assets/71f0cf66-d917-45f1-a922-b27cb348f636)

### 补丁
新品上架没有显示出来是在 
ProductServiceImpl 中的 getNewProducts 方法中，设置了以下条件：
1.商品必须是上架状态 (enabled = true)
2.商品必须是最近30天内创建的
3.没有满足这些条件的商品就不显示

管理员页面登录不上是管理员密码初始值设置问题，重新创建数据库脚本即可，初始化密码忘了，自行解决。










