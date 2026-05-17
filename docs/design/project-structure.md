# 项目结构

本文档提供 GCSC（BDAI-SC 学生中心）代码库的完整导航。你将了解项目如何划分为后端和前端、每个顶级目录包含什么内容，以及数据在各层之间如何流动。如果刚来到这里，建议先读 [概述](./overview) 获取高层摘要，或读 [快速上手](./quick-start) 立即上手运行项目。

## 高层架构

GCSC 是一个**全栈单体仓库**，后端采用 Java Spring Boot，前端采用 Vue 3 单页应用，双方仅通过 REST API 通信，由 JWT Token 保障安全，无服务端渲染或共享代码。数据库为 MySQL，文件上传存储在后端本地文件系统。`docs/` 目录下有一独立的 VitePress 文档站点，与应用本身无关。

## 顶级目录概览

仓库根目录包含四个主要目录，各自有明确的职责：

| 目录 | 用途 | 技术栈 |
|------|------|-------|
| `backend/` | REST API 服务器、业务逻辑、数据库访问 | Java 21、Spring Boot 3.3.5、Maven |
| `frontend/` | 单页应用、UI 组件、路由 | Vue 3.5、Vite 5.4 |
| `docs/` | VitePress 文档站点（即本文档所在） | VitePress 1.6 |
| `scripts/` | 测试和数据生成的工具脚本 | Python 3 |
| `assets/` | README 和文档中使用的截图和品牌图标 | 静态图片 |
| `skills/` | AI 助手上下文文件（CLAUDE.md 的配套） | Markdown |

> 来源：[README.md](README.md#L1-L50)、[CLAUDE.md](CLAUDE.md#L1-L30)

## 后端结构（`backend/`）

后端遵循**经典 Spring Boot 分层架构**，包边界清晰。Maven 管理依赖，`application.yml` 从仓库根目录的 `.env` 文件读取环境变量。主入口是 [StudentCenterApplication.java](backend/src/main/java/com/gcsc/studentcenter/StudentCenterApplication.java#L1-L15)，标注了 `@SpringBootApplication` 和 `@EnableScheduling`。

### 包布局

```
backend/src/main/java/com/gcsc/studentcenter/
├── StudentCenterApplication.java      ← 应用入口
├── config/                            ← 基础设施配置
│   ├── SecurityConfig.java            ← 安全过滤器链与路由规则
│   ├── JwtAuthenticationFilter.java   ← JWT Token 验证过滤器
│   ├── GlobalCorsFilter.java          ← CORS 来源白名单
│   └── WebConfig.java                 ← Web MVC 定制
├── controller/                        ← REST API 端点（10 个 Controller）
├── service/                           ← 业务逻辑层（14 个 Service）
├── repository/                        ← JPA 数据访问（16 个 Repository）
├── entity/                            ← JPA 实体类（19 个 Entity）
├── dto/                               ← 请求/响应数据传输对象
└── exception/
    └── GlobalExceptionHandler.java    ← 统一错误响应
```

### 关键设计决策

后端按**关注点分层**而非按功能模块组织代码。每个垂直切片（如"成就"）分散在 `controller`、`service`、`repository` 和 `entity` 包中。`config/` 包处理横切关注点——安全、CORS、JWT 过滤——全部通过 Spring 的依赖注入接入。

一个值得注意的模式是**多态成就系统**：9 个独立的 JPA 实体类（`AchievementContest`、`AchievementPaper`、`AchievementPatent` 等）各有自己的 Repository，共享 title、description、media attachments 和 user association 的通用结构。这些由单一的 `AchievementService` 根据类型字符串分发来编排。

`application.yml` 通过环境变量配置所有外部连接（数据库 URL、JWT Secret、CORS 来源、上传目录、服务器端口），包含合理默认值。`spring.config.import` 指令从仓库根目录的 `.env` 文件导入变量。

> 来源：[pom.xml](backend/pom.xml#L1-L77)、[application.yml](backend/src/main/resources/application.yml#L1-L39)、[StudentCenterApplication.java](backend/src/main/java/com/gcsc/studentcenter/StudentCenterApplication.java#L1-L15)

## 前端结构（`frontend/`）

前端是一个 **Vue 3 单页应用**，基于 Vite 构建。全部采用组合式 API（`<script setup>`），代码按 Web 开发关注点组织：API 模块、可复用组件、页面级视图、Composable Hook、路由、样式和工具函数。

### 目录布局

```
frontend/src/
├── main.js                            ← 应用引导与路由注册
├── App.vue                            ← 根组件（RouterView + ToastContainer）
├── api/                               ← Axios 请求模块（12 个文件）
│   ├── request.js                     ← Axios 实例 + JWT 拦截器
│   ├── auth.js                        ← 登录/注册调用
│   ├── profile.js                     ← 学生档案 CRUD
│   ├── achievements.js                ← 成就查询
│   └── ...                            ← 其他领域专用模块
├── views/                             ← 页面级路由组件（10 个视图）
├── components/                        ← 共享可复用组件（16 个文件）
│   └── achievement/                   ← 成就专用子组件
├── composables/                       ← Vue Composable Hook（16 个 Hook）
├── constants/                         ← 菜单定义、成就类型配置
├── layouts/
│   └── DashboardLayout.vue            ← 已认证页面外壳（侧边栏 + 顶部栏）
├── router/
│   └── index.js                       ← 路由定义 + 导航守卫
├── utils/                             ← 纯工具函数（PDF、Excel、媒体）
└── assets/
    ├── fonts/                         ← 鸿蒙黑体字体文件
    └── styles/                        ← 19 个模块化 CSS 文件
        ├── _variables.css              ← CSS 自定义属性（主题颜色、阴影、动画）
        ├── styles.css                  ← 主样式表（导入所有模块）
        ├── layout.css                  ← 工作台外壳网格与响应式断点
        ├── dialogs.css                 ← 模态框/抽屉叠加层样式
        └── ...                         ← 各功能专用样式表
```

### 前端如何组合在一起

[main.js](frontend/src/main.js#L1-L14) 入口创建 Vue 实例、注册 Vue Router、导入全局样式表，并在 `window.$toast` 上暴露全局 toast 工具。[App.vue](frontend/src/App.vue#L1-L9) 很简洁——只有 `<RouterView />` 和 `<ToastContainer />`，因为所有布局结构都位于 [DashboardLayout.vue](frontend/src/layouts/DashboardLayout.vue) 中。

[router/index.js](frontend/src/router/index.js#L1-L83) 中的路由定义了两组路由：**游客路由**（`/login`、`/register`）和**已认证路由**（`/` 下所有子路由，嵌套在 `DashboardLayout` 内）。`beforeEach` 守卫检查 `localStorage` 中是否有 JWT Token，对教师专属和管理员专属页面执行基于角色的访问控制，并在系统设置关闭时拦截注册页面的访问。

[api/request.js](frontend/src/api/request.js#L1-L36) 中的 Axios 实例自动为每个出站请求附加 JWT `Authorization` Header，并在收到 401 响应时重定向到 `/login`。领域专用 API 模块（`auth.js`、`profile.js` 等）导入这个实例并导出干净的异步函数。

**CSS 架构**：前端使用 19 个模块化 CSS 文件，通过单一主 `styles.css` 导入。主题 token 定义在 `_variables.css` 中作为 CSS 自定义属性（`--primary`、`--card`、`--shadow` 等），所有组件共享一致的主题风格。未使用 CSS 预处理器——仅有原生 CSS 加变量。

> 来源：[main.js](frontend/src/main.js#L1-L14)、[App.vue](frontend/src/App.vue#L1-L9)、[router/index.js](frontend/src/router/index.js#L1-L83)、[api/request.js](frontend/src/api/request.js#L1-L36)、[_variables.css](frontend/src/assets/styles/_variables.css#L1-L65)

## 支持目录

### `docs/` — 文档站点

一个独立的 VitePress 站点，生成你现在正在阅读的文档。它**与应用完全独立**——不导入任何源代码，有自己的 `package.json`，VitePress 是唯一依赖。文档页面分为四个部分：`guide/`（教程）、`design/`（技术深度解析）、`manual/`（用户手册）、`api/`（API 参考）。静态资源（图标、图片、截图）放在 `docs/public/`。

> 来源：[docs/package.json](docs/package.json#L1-L14)、[docs/index.md](docs/index.md#L1-L54)

### `scripts/` — 工具脚本

包含 `generate_test_data.py`，一个生成真实感测试 SQL 插入语句的 Python 脚本。使用 `faker` 库生成中文姓名、电话号码、地址和学生记录，各表之间关系（`users`、`student_profiles`、教育/干部经历、九种成就类型）完整。对于用真实数据填充开发环境极有价值，无需手动输入。

> 来源：[generate_test_data.py](scripts/generate_test_data.py#L1-L30)

### `skills/` — AI 助手上下文

作为 `CLAUDE.md` 的配套，提供结构化的参考文件。[SKILL.md](skills/SKILL.md#L1-L45) 索引将主题映射到详细参考文档，覆盖后端架构、前端约定、功能工作流、文件位置和构建说明。这些不是应用代码，而是帮助 AI 工具理解项目模式和约定的元数据。

> 来源：[SKILL.md](skills/SKILL.md#L1-L45)

### `assets/` — 项目品牌

包含产品截图（按日期命名，如 `2026-03-09-1.png`）和品牌图标（Logo、设置图标），主要用于 README 和文档站点。这些文件**不被应用代码引用**——前端有自己的 `public/` 和 `src/assets/` 目录。

> 来源：[README.md](README.md#L200-L250)

## 技术栈汇总

| 层级 | 技术 | 版本 | 配置文件 |
|------|------|------|---------|
| 运行时 | Java (JDK) | 21 | 系统级 |
| 后端框架 | Spring Boot | 3.3.5 | [pom.xml](backend/pom.xml#L1-L77) |
| 安全 | Spring Security + JJWT | 6.x / 0.12.6 | [SecurityConfig.java](backend/src/main/java/com/gcsc/studentcenter/config/SecurityConfig.java#L1-L49) |
| ORM | Hibernate（通过 Spring Data JPA） | 6.x | [application.yml](backend/src/main/resources/application.yml#L1-L39) |
| 数据库 | MySQL | 8.0 | `.env` 文件 |
| 后端构建 | Maven | 3.x | [pom.xml](backend/pom.xml#L1-L77) |
| 前端框架 | Vue | 3.5 | [package.json](frontend/package.json#L1-L28) |
| 前端构建 | Vite | 5.4 | [vite.config.js](frontend/vite.config.js#L1-L30) |
| 路由 | Vue Router | 4.4 | [router/index.js](frontend/src/router/index.js#L1-L83) |
| HTTP 客户端 | Axios | 1.7 | [api/request.js](frontend/src/api/request.js#L1-L36) |
| 数据表格 | AG Grid Community | 31.3 | [package.json](frontend/package.json#L1-L28) |
| 文档 | VitePress | 1.6 | [docs/package.json](docs/package.json#L1-L14) |

> 来源：[README.md](README.md#L20-L55)、[pom.xml](backend/pom.xml#L1-L77)、[package.json](frontend/package.json#L1-L28)

## 数据流概览

理解单个请求如何在代码库中穿行，是有效导航代码库的关键。以下是经过认证的 API 调用的典型流程：

请求经过三个检查点：**Vue Router 守卫**（检查 `localStorage` 中是否有 Token）、**Axios 拦截器**（将 Token 作为 `Authorization` Header 附加）、**Spring Security 过滤器链**（验证 JWT 并建立安全上下文）。只有三个检查全部通过，请求才到达 Controller。

> 来源：[router/index.js](frontend/src/router/index.js#L52-L83)、[api/request.js](frontend/src/api/request.js#L1-L36)、[SecurityConfig.java](backend/src/main/java/com/gcsc/studentcenter/config/SecurityConfig.java#L1-L49)

## 下一步

本文档提供了全局视图。根据你想做的事，推荐以下阅读顺序：

- 想深入了解**后端和前端各层如何交互**，继续读 [架构总览](./architecture)。
- 想了解**用户如何认证以及 JWT Token 如何在系统中流动**，跳读 [安全与 JWT 认证](./jwt-auth)。
- 想探索**前端如何组织路由并按角色保护页面**，读 [Vue Router 和路由守卫](./router)。
- 想学习**前端 API 调用如何组织**，见 [Axios 请求层](./axios-layer)。