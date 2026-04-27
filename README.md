<h1 align="center">BDAI-SC</h1>

<p align="center">

[![Java 21](https://img.shields.io/badge/Java-21-5382a1?style=flat-square&logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 3.3](https://img.shields.io/badge/Spring%20Boot-3.3.5-6db33f?style=flat-square&logo=spring)](https://spring.io/projects/spring-boot)
[![Vue 3](https://img.shields.io/badge/Vue-3.5-4fc08d?style=flat-square&logo=vuedotjs)](https://vuejs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479a1?style=flat-square&logo=mysql)](https://www.mysql.com/)
[![JWT](https://img.shields.io/badge/JWT-0.12.6-000?style=flat-square&logo=jsonwebtokens)](https://github.com/jwtk/jjwt)

</p>
  
---

## 技术栈

### 后端

| 技术            | 版本   | 用途       |
| --------------- | ------ | ---------- |
| Java            | 21     | 运行时     |
| Spring Boot     | 3.3.5  | Web 框架   |
| Spring Security | 6.x    | 认证与授权 |
| JPA / Hibernate | 6.x    | ORM        |
| MySQL           | 8.0    | 数据库     |
| JJWT            | 0.12.6 | JWT 令牌   |
| Maven           | 3.x    | 构建工具   |

### 前端

| 技术              | 版本 | 用途        |
| ----------------- | ---- | ----------- |
| Vue               | 3.5  | UI 框架     |
| Vite              | 5.4  | 构建工具    |
| Vue Router        | 4.4  | 路由        |
| Axios             | 1.7  | HTTP 客户端 |
| AG Grid           | 31.3 | 数据表格    |
| jsPDF + autoTable | 2.5  | PDF 导出    |
| xlsx              | 0.18 | Excel 导出  |
| docx-preview      | 0.3  | Word 预览   |

---

## 快速启动

### 前置条件

- JDK 21+
- Node.js 18+
- MySQL 8.0+
- Maven 3.9+

### 1. 配置环境

```bash
cp .env.example .env
```

按需修改 `.env` 中的数据库密码、JWT 密钥等配置。

### 2. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS bdai_sc DEFAULT CHARACTER SET utf8mb4;
CREATE USER IF NOT EXISTS 'bdai_sc'@'localhost' IDENTIFIED BY 'bdai_sc';
GRANT ALL PRIVILEGES ON bdai_sc.* TO 'bdai_sc'@'localhost';
FLUSH PRIVILEGES;
```

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
# 后端地址: http://localhost:8080
```

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
# 前端地址: http://localhost:5173
```

> 首次启动后 JPA 会自动建表（`ddl-auto: update`），无需手动执行 SQL 初始化脚本。

---

## 项目结构

```
GCSC/
├── backend/                                # Spring Boot 后端
│   ├── src/main/java/com/gcsc/studentcenter/
│   │   ├── StudentCenterApplication.java   # 启动类
│   │   ├── config/                         # 安全配置、CORS、JWT 过滤器
│   │   ├── controller/                     # REST 控制器
│   │   │   ├── AuthController.java         # 登录/注册
│   │   │   ├── AchievementController.java  # 成就 CRUD
│   │   │   ├── StudentProfileController.java # 学生档案
│   │   │   ├── AdminController.java        # 管理后台
│   │   │   ├── UploadController.java       # 文件上传
│   │   │   └── SystemSettingsController.java
│   │   ├── service/                        # 业务逻辑层
│   │   ├── repository/                     # 数据访问层
│   │   ├── entity/                         # JPA 实体
│   │   ├── dto/                            # 请求/响应 DTO
│   │   └── exception/                      # 全局异常处理
│   ├── src/main/resources/
│   │   └── application.yml                 # 主配置（从 .env 导入变量）
│   └── pom.xml
│
├── frontend/                          # Vue 3 前端
│   ├── src/
│   │   ├── api/                       # Axios 请求模块
│   │   │   ├── request.js             # axios 实例 + 拦截器
│   │   │   ├── auth.js                # 认证
│   │   │   ├── profile.js             # 学生档案
│   │   │   └── achievements.js        # 成就接口
│   │   ├── assets/styles/             # 全局 CSS
│   │   │   ├── _variables.css         # CSS 变量
│   │   │   ├── layout.css             # 布局
│   │   │   ├── dialogs.css            # 对话框/Toast
│   │   │   └── achievements.css       # 成就页样式
│   │   ├── components/                # Vue 组件
│   │   ├── composables/               # 组合式函数
│   │   ├── constants/                 # 菜单/成就类型常量
│   │   ├── layouts/                   # 页面布局（DashboardLayout）
│   │   ├── router/                    # Vue Router 配置
│   │   ├── utils/                     # 导出/渲染/工具函数
│   │   └── views/                     # 页面级组件
│   │       ├── LoginView.vue          # 登录
│   │       ├── RegisterView.vue       # 注册
│   │       ├── AchievementsView.vue   # 成就墙
│   │       ├── MyInfosView.vue        # 我的信息
│   │       ├── StudentInfoView.vue    # 学生信息（教师/管理员）
│   │       ├── NotificationsView.vue  # 通知中心
│   │       └── AdminView.vue          # 管理后台
│   ├── index.html
│   ├── vite.config.js
│   └── package.json
│
├── assets/                            # 产品截图
├── scripts/                           # 工具脚本
└── .env.example                       # 环境配置示例
```

---

## 数据库

### 表概览

| 表名                          | 用途                                                       |
| ----------------------------- | ---------------------------------------------------------- |
| `users`                       | 用户（登录账号、角色、密码哈希）                           |
| `student_profiles`            | 学生档案（个人信息、政治面貌、家庭信息）                   |
| `education_experiences`       | 教育经历（嵌入表）                                         |
| `cadre_experiences`           | 班干部经历（嵌入表）                                       |
| `achievement_*`               | 9 类成就表（竞赛/科研/论文/专利/证书/作品/期刊/双百/培训） |
| `achievement_review_requests` | 成就审核请求                                               |
| `profile_review_requests`     | 档案变更审核请求                                           |
| `posts`                       | 动态/通知                                                  |
| `post_media`                  | 动态媒体附件                                               |
| `contacts`                    | 教师/部门联系方式                                          |

---

## API 概览

### 认证

| 方法 | 路径                 | 说明                                      |
| ---- | -------------------- | ----------------------------------------- |
| POST | `/api/auth/register` | 注册（username + displayName + password） |
| POST | `/api/auth/login`    | 登录（返回 JWT）                          |
| GET  | `/api/auth/me`       | 获取当前用户信息                          |

### 学生档案

| 方法 | 路径                           | 说明                            |
| ---- | ------------------------------ | ------------------------------- |
| GET  | `/api/profile`                 | 获取当前用户档案                |
| PUT  | `/api/profile`                 | 更新档案                        |
| GET  | `/api/profile/{id}`            | 获取指定用户档案（教师/管理员） |
| GET  | `/api/profile/students`        | 分页查询学生列表（教师/管理员） |
| POST | `/api/profile/review-requests` | 提交档案变更申请                |

### 成就

| 方法       | 路径                                           | 说明                    |
| ---------- | ---------------------------------------------- | ----------------------- |
| GET/POST   | `/api/achievements/{type}`                     | 获取/创建某类型成就     |
| GET        | `/api/achievements/{type}/{id}`                | 获取单条成就详情        |
| PUT/DELETE | `/api/achievements/{type}/{id}`                | 更新/删除成就           |
| POST       | `/api/achievements/{type}/{id}/review-request` | 提交审核                |
| POST       | `/api/achievements/{type}/{id}/approve`        | 审核通过（教师/管理员） |
| POST       | `/api/achievements/{type}/{id}/reject`         | 审核拒绝                |

> `type` 可选值: `contest` `research` `paper` `patent` `certificate` `works` `journal` `double-hundred` `training`

### 管理

| 方法     | 路径                         | 说明                 |
| -------- | ---------------------------- | -------------------- |
| GET      | `/api/admin/stats`           | 系统统计             |
| GET      | `/api/admin/students`        | 学生列表（支持筛选） |
| PUT      | `/api/admin/users/{id}/role` | 修改用户角色         |
| GET/POST | `/api/admin/review-settings` | 审核设置             |
| GET      | `/api/admin/upload-settings` | 上传设置             |

### 文件

| 方法 | 路径          | 说明                                 |
| ---- | ------------- | ------------------------------------ |
| POST | `/api/upload` | 上传文件（支持图片/视频/PDF/Office） |
| GET  | `/uploads/**` | 访问已上传文件                       |

---

## 成就类型

| 类型 | 实体                       | 关键字段                               |
| ---- | -------------------------- | -------------------------------------- |
| 竞赛 | `AchievementContest`       | 竞赛名称、奖项等级、获奖人数、获奖日期 |
| 科研 | `AchievementResearch`      | 项目名称、项目负责人、指导教师工号     |
| 论文 | `AchievementPaper`         | 论文题目、期刊名称、发表日期、收录情况 |
| 专利 | `AchievementPatent`        | 专利名称、专利类型、授权号、授权日期   |
| 证书 | `AchievementCertificate`   | 证书名称、证书类型、获得日期           |
| 作品 | `AchievementWorks`         | 作品名称、发布场合、影响范围           |
| 期刊 | `AchievementJournal`       | 刊物名称、作品题目、发表日期           |
| 双百 | `AchievementDoubleHundred` | 双百工程相关                           |
| 培训 | `AchievementIeerTraining`  | 培训记录                               |

所有成就类型均支持：标题、描述、图片附件、创建时间、关联作者。

---

## 角色权限

| 功能         | STUDENT        | CADRE | TEACHER | ADMIN |
| ------------ | -------------- | ----- | ------- | ----- |
| 登录/注册    | ✅             | ✅    | ✅      | ✅    |
| 管理个人档案 | ✅（提交审核） | ✅    | ✅      | ✅    |
| 提交成就     | ✅             | ✅    | ✅      | ✅    |
| 审核成就     | ❌             | 本班  | ✅      | ✅    |
| 审核档案变更 | ❌             | 本班  | ✅      | ✅    |
| 查看学生档案 | 仅本人         | 本班  | 所管    | 全部  |
| 导出学生信息 | ❌             | ❌    | ✅      | ✅    |
| 系统设置     | ❌             | ❌    | ❌      | ✅    |
| 管理用户角色 | ❌             | ❌    | ❌      | ✅    |

> STUDENT 是默认角色, CADRE是班干部, TEACHER是老师, ADMIN是管理员

---

## 配置说明

所有配置通过 `.env` 文件管理，Spring Boot 通过 `application.yml` 中的 `spring.config.import` 自动导入：

```bash
# 关键配置项
BDAI_SC_DB_URL=jdbc:mysql://127.0.0.1:3306/bdai_sc?useUnicode=true&...
BDAI_SC_DB_USER=bdai_sc
BDAI_SC_DB_PASSWORD=bdai_sc
BDAI_SC_JWT_SECRET=your-super-secret-key
BDAI_SC_JWT_EXPIRES_MINUTES=120
BDAI_SC_UPLOAD_DIR=./uploads                # 文件存储目录
VITE_API_BASE=http://localhost:8080         # 前端 API 地址
```

---

## 开发指南

### 后端

```bash
# 运行
mvn spring-boot:run

# 构建 JAR
mvn clean package

# 运行测试
mvn test

# 单个测试类
mvn test -Dtest=AuthServiceTest
```

### 前端

```bash
# 安装依赖
npm install

# 开发服务器
npm run dev

# 生产构建
npm run build

# 预览构建结果
npm run preview
```

### 添加新成就类型

1. 在 `backend/entity/` 创建新实体，继承通用结构
2. 在 `backend/repository/` 创建对应的 `JpaRepository`
3. 在 `backend/controller/` 添加 REST 控制器
4. 在 `frontend/src/constants/achievementConstants.js` 添加类型常量
5. 在 `frontend/src/api/` 添加 API 请求模块
6. 在前端视图或成就页中接入

### 前端组件规范

- 使用 `<script setup>` 组合式 API
- 全局按钮样式：`ghost-button`（描边）、`action-button`（填充）
- 对话框使用 `sheet-overlay` / `sheet-modal` 模式（见 `dialogs.css`）
- 动画时长：`0.42s cubic-bezier(0.22, 1, 0.36, 1)`（transform）、`0.38s ease`（opacity）

---

## 贡献规范

### Git 提交规范

```
feat:     新功能
fix:      修复问题
style:    仅样式改动
docs:     文档改动
refactor: 重构（不改变功能）
chore:    工程维护
```

前缀后使用中文描述，如：`feat: 添加学生档案导出功能`

### 分支命名

```
feat/feature-name     # 新功能
fix/bug-description   # Bug 修复
page/page-name        # 新页面
style/description     # 样式调整
```

---

## 许可证

本项目仅供学习与内部使用。
