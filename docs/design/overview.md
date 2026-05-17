# 概述

BDAI-SC 学生中心（BDAI-SC Student Center）是一款全栈 Web 应用，面向大数据与人工智能学院设计，用于管理学生档案、追踪学生成就、协调多角色审核工作流。系统提供统一的工作台，学生提交成果、教师审核批准、管理员统管全局，全部由基于 JWT 的身份认证保障安全。

![GCSC 工作台截图](https://github.com/MakiWinster72/GCSC/blob/main/assets/2026-03-09-1.png?raw=true)

系统后端基于 **Spring Boot 3.3**，前端基于 **Vue 3 SPA**，双方仅通过 RESTful JSON API 通信。MySQL 8.0 负责所有数据的持久化，文件上传（图片、文档、视频）存储在本地文件系统并作为静态资源提供服务。整个应用不维护服务端会话状态——每次请求都通过 `Authorization` Header 中的 Bearer Token 完成身份认证。

> 来源：[README.md](README.md#L1-L8)、[CLAUDE.md](CLAUDE.md#L5-L11)、[StudentCenterApplication.java](backend/src/main/java/com/gcsc/studentcenter/StudentCenterApplication.java#L1-L14)

## 技术栈

| 层级 | 技术 | 版本 | 用途 |
|------|------|------|------|
| **运行时** | Java | 21 | 后端语言运行时 |
| **框架** | Spring Boot | 3.3.5 | Web、安全、数据、校验 |
| **ORM** | JPA / Hibernate | 6.x | 数据库映射，schema 自动创建 |
| **数据库** | MySQL | 8.0 | 关系型数据持久化 |
| **认证** | JJWT + Spring Security | 0.12.6 | 无状态 JWT Token 认证 |
| **构建** | Maven | 3.x | 依赖管理与打包 |
| **前端框架** | Vue 3（组合式 API） | 3.5 | 响应式组件化 UI |
| **构建工具** | Vite | 5.4 | 快速 HMR 开发服务器与生产构建 |
| **路由** | Vue Router | 4.4 | 带守卫的客户端路由 |
| **HTTP 客户端** | Axios | 1.7 | 带拦截器的 API 通信 |
| **数据表格** | AG Grid | 31.3 | 可排序筛选的表格视图 |
| **导出** | jsPDF + autoTable / xlsx | 2.5 / 0.18 | PDF 和 Excel 生成 |
| **预览** | docx-preview | 0.3 | 浏览器内 Word 文档渲染 |

> 来源：[pom.xml](backend/pom.xml#L1-L76)、[package.json](frontend/package.json#L1-L28)、[README.md](README.md#L28-L54)

## 系统架构

后端遵循经典的**分层架构**，前端采用**组合式 SPA** 模式。每个经过认证的请求都会经过 JWT 过滤器链，Vite 开发服务器将 `/api` 和 `/uploads` 路径反向代理到后端，开发阶段无需配置 CORS。

架构强制后端遵循严格的 **请求 → 过滤器 → Controller → Service → Repository → 数据库** 流程，前端将逻辑组织为可复用 Composable，封装响应式状态和 API 调用，与视图模板分离。

> 来源：[CLAUDE.md](CLAUDE.md#L43-L91)、[application.yml](backend/src/main/resources/application.yml#L1-L39)、[vite.config.js](frontend/vite.config.js#L1-L29)、[router/index.js](frontend/src/router/index.js#L1-L82)

## 核心功能

### 九种成就类型

学生可以记录九种不同类别的成就，每种类别背后都有独立的 JPA Entity、Repository，以及 `AchievementService` 中的多态分发层。前端通过 `achievementConstants.js` 中的字段映射驱动类别特定表单。

| # | 类别 Key | 中文名称 | 实体类 |
|---|---------|---------|--------|
| 1 | `contest` | 学科竞赛、文体艺术 | `AchievementContest` |
| 2 | `paper` | 发表学术论文 | `AchievementPaper` |
| 3 | `journal` | 发表期刊作品 | `AchievementJournal` |
| 4 | `patent` | 专利（著作权）授权 | `AchievementPatent` |
| 5 | `certificate` | 职业资格证书 | `AchievementCertificate` |
| 6 | `research` | 参与教师科研项目 | `AchievementResearch` |
| 7 | `works` | 创作表演代表性作品 | `AchievementWorks` |
| 8 | `doubleHundred` | 双百工程 | `AchievementDoubleHundred` |
| 9 | `ieerTraining` | 大学生创新创业训练计划 | `AchievementIeerTraining` |

每种类型共享统一结构——标题、描述、日期、媒体附件、用户关联——同时各自扩展类别特定字段（如竞赛有主办方、获奖等级、团队成员；论文有期刊名、作者排名、收录情况）。

> 来源：[achievementConstants.js](frontend/src/constants/achievementConstants.js#L18-L29)、[CLAUDE.md](CLAUDE.md#L57-L69)

### 基于角色的访问控制（RBAC）

系统定义了四个角色，权限逐级扩展。角色信息编码在 JWT Claims 中，由 `JwtAuthenticationFilter` 提取为 Spring Security 的 `SecurityContext`，带上 `ROLE_` 前缀。

| 角色 | 数据可见范围 | 可执行操作 |
|------|------------|----------|
| **STUDENT** | 仅自己的档案和成就 | 提交成就、申请档案修改、查看通知 |
| **CADRE** | 本班学生 + 自己的档案 | 审核班级同学的提交（班级审核面板） |
| **TEACHER** | 所管理班级的学生 + 自己的档案 | 审核成就和档案修改、导出学生数据（PDF/Excel） |
| **ADMIN** | 所有用户、所有数据 | 用户完整 CRUD、系统设置、备份恢复、所有审核 |

`router/index.js` 中的路由守卫在渲染受保护视图前检查 `meta.allowedRoles` 与存储的用户角色。`menu.js` 中的侧边栏菜单也通过 `filterMenuItemsByRole()` 按角色过滤导航项。

> 来源：[router/index.js](frontend/src/router/index.js#L58-L80)、[menu.js](frontend/src/constants/menu.js#L31-L50)、[build-run.md](skills/references/build-run.md#L26-L33)

### 双轨审核流程

成就提交和学生档案修改均遵循**先审后生效**模式。学生提交请求时附带 `payloadSnapshot`（拟变更数据）和 `changes`（差异描述），教师或管理员审批、通过（附理由）或驳回，申请人也可以取消待审请求。系统还支持通过 `ReviewSettings` 开启自动审批。

![通知审核截图](https://github.com/MakiWinster72/GCSC/blob/main/assets/2026-03-09-3.png?raw=true)

> 来源：[CLAUDE.md](CLAUDE.md#L132-L137)、[docs/index.md](docs/index.md#L36-L42)

### 导出与嵌入模式

教师和管理员可以从学生信息页面直接导出学生档案为 **PDF**（jsPDF + autoTable）或 **Excel**（SheetJS/xlsx）。导出逻辑封装在 Composable `useStudentPdfExport.js` 和工具函数 `studentProfileExport.js` 中。此外，每个工作台视图都支持**嵌入 iframe 模式**，通过在 URL 末尾附加 `?embed=1` 激活，可去除侧边栏、顶部栏和底部栏，嵌入外部门户。

> 来源：[CLAUDE.md](CLAUDE.md#L121-L122)、[package.json](frontend/package.json#L11-L22)

## 项目结构

仓库按后端、前端、共享资产、文档和工具脚本清晰分目录组织。

```
GCSC/
├── backend/                          # Spring Boot 3.3.5 (Java 21)
│   ├── pom.xml                       # Maven 依赖
│   └── src/main/
│       ├── java/com/gcsc/studentcenter/
│       │   ├── StudentCenterApplication.java
│       │   ├── config/               # SecurityConfig, JwtFilter, CORS, WebConfig
│       │   ├── controller/           # 10 个 REST Controller
│       │   ├── service/              # 13 个 Service 类
│       │   ├── repository/           # 16 个 JPA Repository
│       │   ├── entity/               # 18 个 JPA Entity
│       │   ├── dto/                  # 30+ 个请求/响应 DTO
│       │   └── exception/            # GlobalExceptionHandler
│       └── resources/
│           └── application.yml       # 配置（引入 .env）
│
├── frontend/                         # Vue 3 SPA (Vite 5.4)
│   ├── src/
│   │   ├── api/                      # 12 个 Axios 模块
│   │   ├── assets/styles/            # 18 个 CSS 文件（模块化设计）
│   │   ├── components/               # 16 个共享组件
│   │   ├── composables/              # 16 个 Composable 函数
│   │   ├── constants/                # 菜单与成就类型定义
│   │   ├── layouts/                  # DashboardLayout.vue（外壳）
│   │   ├── router/                   # 路由定义与守卫
│   │   ├── utils/                    # 导出、渲染、存储等工具函数
│   │   └── views/                    # 9 个页面级组件
│   ├── vite.config.js                # 开发服务器 + 代理配置
│   └── package.json
│
├── assets/                           # 产品截图与图标
├── docs/                             # VitePress 文档站点
├── scripts/                          # 工具脚本（测试数据生成）
└── skills/                           # AI 助手技能参考文件
```

> 来源：[README.md](README.md#L128-L181)、[CLAUDE.md](CLAUDE.md#L43-L91)

## 运行前准备

本地运行项目前，请确保开发环境满足以下前提条件：

| 依赖 | 最低版本 | 用途 |
|------|---------|------|
| **JDK** | 21+ | 编译并运行 Spring Boot 后端 |
| **Node.js** | 18+ | 运行 Vite 开发服务器并安装前端依赖 |
| **MySQL** | 8.0+ | 所有持久化数据的数据库 |
| **Maven** | 3.9+ | Java 后端构建工具 |

数据库 Schema 在首次启动时由 JPA 的 `ddl-auto: update` 自动创建——只需在启动后端前创建空数据库和用户即可。参见 [application.yml](backend/src/main/resources/application.yml#L9-L13)。

Vite 开发服务器自动将 `/api/**` 和 `/uploads/**` 请求代理到 `localhost:8080` 的后端，开发阶段无需配置 CORS。参见 [vite.config.js](frontend/vite.config.js#L15-L26)。

## 推荐阅读路径

本文档按从高层概述到实现细节的顺序组织。新接触本项目者，请按以下顺序阅读：

1. **[快速上手](./quick-start)** — 配置数据库、启动前后端服务、首次登录
2. **[项目结构](./project-structure)** — 了解文件组织方式及原因
3. **[架构总览](./architecture)** — 深入了解分层后端与组合式前端
4. **[安全与 JWT 认证](./jwt-auth)** — 了解认证流程端到端细节
5. **[九种成就类型](./achievement-system)** — 深入了解多态成就系统

专注特定领域的读者，可参考侧边栏 **深度探索** 部分——从 Axios 请求层到双轨审批流程，每个子系统均有专项指南。