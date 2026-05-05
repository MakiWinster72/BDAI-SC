# 架构总览

GCSC（BDAI-SC 学生中心）是一款用于管理学生成就、档案和审核工作的全栈 Web 应用。本文呈现系统的高层架构蓝图——各层职责、数据流向和关键设计决策。无论你是新贡献者入职还是追踪某功能的端到端实现，这都是你的结构参考点。

## 高层系统架构

应用遵循经典的**客户端-服务器**分离模式，边界清晰：Java Spring Boot 后端通过 Axios 层为 Vue 3 SPA 提供 REST API，后端数据由 MySQL 持久化。身份认证完全无状态，通过 JWT 实现，消除了服务端会话管理的需要。

Vite 开发服务器将 `/api` 和 `/uploads` 请求代理到后端，开发阶段前端和后端表现为同一来源。生产环境中，前端构建为静态资源，由后端提供服务或部署在反向代理之后。

> 来源：[vite.config.js](frontend/vite.config.js#L1-L29)、[application.yml](backend/src/main/resources/application.yml#L1-L39)、[request.js](frontend/src/api/request.js#L1-L36)

## 技术栈

| 层级             | 技术                                | 版本                    | 用途                                   |
| ---------------- | ----------------------------------- | ----------------------- | -------------------------------------- |
| **运行时**       | Java                                | 21                      | 后端语言运行时                         |
| **框架**         | Spring Boot                         | 3.3.5                   | 自动配置的应用框架                     |
| **ORM**          | Spring Data JPA / Hibernate         | （由 Spring Boot 管理） | 对象-关系映射，`ddl-auto: update`      |
| **数据库**       | MySQL                               | —                       | 所有领域数据的持久化存储               |
| **安全**         | Spring Security + JWT (jjwt 0.12.6) | —                       | 无状态身份认证与授权                   |
| **前端**         | Vue 3                               | —                       | UI 框架，组合式 API + `<script setup>` |
| **前端构建工具** | Vite                                | —                       | 快速开发服务器、HMR、生产打包          |
| **HTTP 客户端**  | Axios                               | —                       | 带拦截器的 API 通信                    |
| **调度**         | `@EnableScheduling`                 | —                       | 在应用入口启用定时任务                 |

> 来源：[pom.xml](backend/pom.xml#L1-L77)、[StudentCenterApplication.java](backend/src/main/java/com/gcsc/studentcenter/StudentCenterApplication.java#L1-L15)

## 后端包结构

后端位于 `com.gcsc.studentcenter`，遵循**分层架构**模式，包按关注点严格分离。每层各司其职：Controller 处理 HTTP、Service 包含业务逻辑、Repository 管理数据访问、Entity 定义领域模型。

| 包            | 包含内容                                                             | 数量 | 职责                                    |
| ------------- | -------------------------------------------------------------------- | ---- | --------------------------------------- |
| `config/`     | SecurityConfig, JwtAuthenticationFilter, WebConfig, GlobalCorsFilter | 4    | 框架配置、安全过滤器链                  |
| `controller/` | REST 端点类                                                          | 10   | HTTP 请求处理，通过 `@Valid` 做输入校验 |
| `service/`    | 业务逻辑类                                                           | 13   | 核心领域操作、业务编排                  |
| `repository/` | `JpaRepository` 接口                                                 | 15   | 数据访问，通过方法名派生查询            |
| `entity/`     | JPA 实体类 + 枚举                                                    | 18   | 领域模型、数据库表映射                  |
| `dto/`        | 请求和响应数据传输对象                                               | 32   | API 契约定义、校验注解                  |
| `exception/`  | GlobalExceptionHandler                                               | 1    | 集中式错误处理                          |

> 来源：[CLAUDE.md](CLAUDE.md#L35-L47)、[GlobalExceptionHandler.java](backend/src/main/java/com/gcsc/studentcenter/exception/GlobalExceptionHandler.java)

## 身份认证与安全模型

系统采用**无状态基于 JWT** 的认证架构。不存在服务端会话——每个请求都在 `Authorization` Header 中携带自己的凭证。Spring Security 过滤器链强制执行访问规则，`JwtAuthenticationFilter` 将 Token 转换为 Spring Security 的 `SecurityContext` 主体。

`UserRole` 枚举定义的四个角色——`STUDENT`、`TEACHER`、`ADMIN`、`CADRE`——直接映射为 Spring Security 的权限，格式为 `ROLE_STUDENT`、`ROLE_TEACHER` 等。路由级权限在后端通过 `@PreAuthorize` 或 SecurityConfig 规则、在前端通过检查 `meta.allowedRoles` 的 Vue Router `beforeEach` 守卫来双向强制。

JWT 密钥、过期时间和 CORS 来源全部可通过环境变量配置（`BDAI_SC_JWT_SECRET`、`BDAI_SC_JWT_EXPIRES_MINUTES`、`BDAI_SC_CORS_ALLOWED_ORIGINS`），从项目根目录的 `.env` 文件加载。这使得安全配置在不同部署环境间可移植，无需修改代码。

> 来源：[SecurityConfig.java](backend/src/main/java/com/gcsc/studentcenter/config/SecurityConfig.java#L1-L49)、[JwtAuthenticationFilter.java](backend/src/main/java/com/gcsc/studentcenter/config/JwtAuthenticationFilter.java#L1-L67)、[UserRole.java](backend/src/main/java/com/gcsc/studentcenter/entity/UserRole.java#L1-L9)、[router/index.js](frontend/src/router/index.js#L52-L82)

## 前端应用结构

Vue 3 前端在 `src/` 下组织为 6 个顶级目录，每个有清晰的责任边界。应用入口点（[main.js](frontend/src/main.js#L1-L14)）创建 Vue 实例、挂载路由、导入全局样式表，并暴露全局 `$toast` 工具。

| 目录           | 用途                      | 关键文件                                                                                                    |
| -------------- | ------------------------- | ----------------------------------------------------------------------------------------------------------- |
| `views/`       | 页面级路由组件            | 9 个视图：Login、Register、Achievements、MyInfos、Notifications、StudentInfo、Settings、Admin、ClassReviews |
| `components/`  | 共享可复用 UI 组件        | 16 个组件，包含 DashboardSidebar、BrandHeader、ToastContainer、AchievementCardBody                          |
| `composables/` | Vue 组合式 API 逻辑模块   | 14 个 Composable：useAchievementEditor、useDashboardShell、useNotifications、useStudentPdfExport 等         |
| `api/`         | 按领域分的 Axios 请求模块 | 12 个模块：auth、profile、achievement、admin、upload、request（基础）等                                     |
| `router/`      | 路由定义和守卫            | 单一 index.js，含嵌套在 DashboardLayout 下的路由                                                            |
| `utils/`       | 纯工具函数                | pdfRenderer、sheetRenderer、docxRenderer、viewTransition、media helpers                                     |
| `constants/`   | 全局常量与 Schema         | 菜单定义、成就类型常量、明细字段 Schema                                                                     |
| `layouts/`     | 布局包装组件              | DashboardLayout.vue——所有已认证视图的外壳                                                                   |

> 来源：[main.js](frontend/src/main.js#L1-L14)、[router/index.js](frontend/src/router/index.js#L1-L83)、[DashboardLayout.vue](frontend/src/layouts/DashboardLayout.vue#L1-L190)

## 路由与布局架构

所有已认证视图共享通用的 `DashboardLayout` 包装器，提供侧边栏导航、品牌顶部栏和底部栏。该布局支持**嵌入模式**（通过 `?embed=1` 触发），可在 iframe 嵌入场景下去除侧边栏、顶部栏和底部栏。

`beforeEach` 守卫按顺序强制三条规则：（1）未认证用户重定向到 `/login`；（2）已认证用户访问游客专属页面时重定向到 `/myinfos`；（3）角色受限路由拒绝角色不匹配的用户。注册页面额外由系统设置（`gcsc_allowRegistration` 存在于 localStorage）控制。

> 来源：[router/index.js](frontend/src/router/index.js#L1-L83)、[DashboardLayout.vue](frontend/src/layouts/DashboardLayout.vue#L105-L115)

## 成就系统架构

成就子系统是应用中最具架构特色的部分。系统未采用单表多态设计，而是使用**9 个独立 JPA 实体**——每种成就类型一个，各有自己对应的 `JpaRepository`。`AchievementService` 通过类型字符串的 `switch` 分发操作。

| 实体                       | 成就类型（中文） | 表名                          |
| -------------------------- | ---------------- | ----------------------------- |
| `AchievementContest`       | 竞赛             | `achievement_contests`        |
| `AchievementResearch`      | 科研             | `achievement_researches`      |
| `AchievementPaper`         | 论文             | `achievement_papers`          |
| `AchievementPatent`        | 专利             | `achievement_patents`         |
| `AchievementCertificate`   | 证书             | `achievement_certificates`    |
| `AchievementWorks`         | 作品             | `achievement_works`           |
| `AchievementJournal`       | 期刊             | `achievement_journals`        |
| `AchievementDoubleHundred` | 双百             | `achievement_double_hundreds` |
| `AchievementIeerTraining`  | 培训             | `achievement_ieer_trainings`  |

所有 9 个实体共享一个通用结构模式：到 `AppUser`（作者）的 `@ManyToOne` 引用、特定于成就类别的类型化字段，以及通用媒体字段（`imageUrl`、`imageUrls`、`attachments`）。前端对应映射到专用的明细渲染组件和 `constants/achievementDetailSchemas.js` 中的 Schema 驱动表单系统。

`AchievementService` 中的多态分发使用 `switch` 语句处理归一化的类型字符串，而非策略模式或继承层次结构。这种务实的方式使分发逻辑一目了然、可调试，放弃了在成就类型不常变化的系统中不那么重要的扩展性。

> 来源：[AchievementService.java](backend/src/main/java/com/gcsc/studentcenter/service/AchievementService.java#L83-L127)、[AchievementContest.java](backend/src/main/java/com/gcsc/studentcenter/entity/AchievementContest.java#L1-L44)、[CLAUDE.md](CLAUDE.md#L55-L67)

## 配置与环境

整个技术栈通过**环境变量**配合合理默认值配置，遵循十二要素应用理念。后端和前端均从项目根目录的共享 `.env` 文件加载配置。

| 变量                           | 默认值                                 | 作用域             |
| ------------------------------ | -------------------------------------- | ------------------ |
| `BDAI_SC_BACKEND_PORT`         | `8080`                                 | 后端服务器端口     |
| `BDAI_SC_FRONTEND_PORT`        | `5173`                                 | 前端开发服务器端口 |
| `BDAI_SC_DB_URL`               | `jdbc:mysql://127.0.0.1:3306/bdai_sc…` | MySQL 连接         |
| `BDAI_SC_JWT_SECRET`           | `bdai_sc-student-center-…`             | JWT 签名密钥       |
| `BDAI_SC_JWT_EXPIRES_MINUTES`  | `120`                                  | Token 过期时间     |
| `BDAI_SC_CORS_ALLOWED_ORIGINS` | `*`                                    | CORS 来源白名单    |
| `BDAI_SC_UPLOAD_DIR`           | `./uploads`                            | 文件上传目录       |
| `BDAI_SC_SITE_URL`             | `http://localhost:5173`                | 前端公开 URL       |

后端通过 Spring 的 `optional:file:.env[.properties]` 配置导入环境变量，Vite 通过将 `envDir` 指向项目根目录的 `loadEnv()` 读取。这个共享的 `.env` 文件是部署配置的单一事实来源。

> 来源：[application.yml](backend/src/main/resources/application.yml#L1-L39)、[vite.config.js](frontend/vite.config.js#L5-L8)

## 项目目录可视化地图

以下图表展示带架构标注的完整目录布局，突出了上文讨论的关键文件和模块。

```
gcsc/
├── backend/                          │   └── src/main/
│       ├── java/…/studentcenter/
│       │   ├── StudentCenterApplication.java   # 入口点，@EnableScheduling
│       │   ├── config/               # 安全、CORS、JWT 过滤器链
│       │   ├── controller/           # 10 个 REST Controller
│       │   ├── service/              # 13 个业务逻辑 Service
│       │   ├── repository/           # 15 个 JPA Repository 接口
│       │   ├── entity/               # 18 个 JPA 实体 + UserRole 枚举
│       │   ├── dto/                  # 32 个请求/响应 DTO
│       │   └── exception/            # GlobalExceptionHandler
│       └── resources/
│           └── application.yml       # 所有配置，含环境变量默认值
│
├── frontend/                         # Vue 3 SPA
│   └── src/
│       ├── main.js                   # 应用引导
│       ├── App.vue                   # 根组件（RouterView + Toast）
│       ├── router/index.js           # 路由 + beforeEach 守卫
│       ├── layouts/DashboardLayout.vue  # 已认证外壳
│       ├── views/                    # 9 个页面级组件
│       ├── components/               # 16 个共享组件
│       ├── composables/              # 14 个组合式函数
│       ├── api/                      # 12 个 Axios API 模块
│       ├── utils/                    # 导出渲染器、过渡动画
│       ├── constants/                # 菜单、成就 Schema
│       └── assets/styles/            # 19 个 CSS 模块（全局）
│
├── .env                              # 共享环境变量
└── scripts/generate_test_data.py     # 测试数据生成器
```

## 下一步

此总览建立了结构基础。从这里开始，阅读顺序取决于你想深入理解系统的哪一部分：

- **安全深度解析**：[安全与 JWT 认证](./jwt-auth) 详细介绍了 Token 生成、验证和 JWT 过滤器内部原理。
- **API 契约**：[REST API 设计](./rest-api) 记录了每个端点、请求/响应结构和错误码。
- **数据层**：[数据模型与 JPA 实体](./student-profile-model) 绘制了完整的实体关系图和字段级文档。
- **成就内部原理**：[九种成就类型](./achievement-system) 深入解释了每个实体的字段和多态分发模式。
- **前端模式**：[Vue Router 和路由守卫](./router) → [Axios 请求层](./axios-layer) → [Composable 模式](./achievement-editor) 逐层讲解前端架构。

