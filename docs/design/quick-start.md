# 快速上手

本指南将带你完成 BDAI-SC 学生中心的本地开发环境搭建——从全新克隆到三个服务全部正常通信。如果尚未阅读项目概述，请先阅读 [概述](./overview) 了解 BDAI-SC 是什么以及为什么存在。

## 架构概览

在敲任何命令之前，先理解本地开发所基于的**三端口模型**会大有裨益。搞清楚哪个服务与哪个端口通信，是贯穿整个开发过程中最重要的调试思路。

![登录后的工作台首页](https://github.com/MakiWinster72/GCSC/blob/main/assets/2026-03-09-1.png?raw=true)

浏览器只和一个地址通信——Vite 开发服务器，端口 5173。其他一切都在幕后发生。

Vite 开发服务器扮演透明反向代理的角色。当前端代码调用 `/api/auth/login` 时，Vite 将请求转发至 `http://127.0.0.1:8080/api/auth/login`——浏览器从不直接访问 8080 端口，这在开发阶段完全消除了 CORS 问题。此代理配置位于 [vite.config.js](frontend/vite.config.js#L14-L26)。

> 来源：[vite.config.js](frontend/vite.config.js#L14-L26)、[application.yml](backend/src/main/resources/application.yml#L1-L39)、[request.js](frontend/src/api/request.js#L1-L36)

## 环境准备

BDAI-SC 只需要四款软件。不存在可选运行时或特殊依赖——每款工具都是主流、社区文档完善的技术。

| 软件 | 最低版本 | 用途 | 验证命令 |
|------|---------|------|---------|
| JDK | 21 | 后端运行时 | `java -version` |
| Maven | 3.8+ | 后端构建工具 | `mvn -version` |
| Node.js | 18+ | 前端运行时 | `node -v` |
| MySQL | 8.0+ | 持久化存储 | `mysql -V` |

**JDK 21 是硬性要求。** [pom.xml](backend/pom.xml#L13-L15) 声明了 `21`，任何更低版本都会导致 `invalid source release: 21` 错误。如果使用了多 JDK 版本管理工具（如 SDKMAN），在运行 Maven 前切换到 `sdk use java 21.x.x-tem` 或显式设置 `JAVA_HOME`。

> 来源：[pom.xml](backend/pom.xml#L13-L15)、[package.json](frontend/package.json#L1-L28)、[CLAUDE.md](CLAUDE.md#L24-L28)

## 步骤 1 — 克隆并确认

克隆仓库并确认顶级目录符合预期：

```bash
git clone https://github.com/MakiWinster72/GCSC.git
cd GCSC
ls -d backend/ frontend/ scripts/ docs/ assets/
```

应看到全部五个目录。若有缺失说明克隆中断，重新运行 `git clone` 即可。

> 来源：[CLAUDE.md](CLAUDE.md#L1-L5)

## 步骤 2 — 初始化数据库

BDAI-SC 使用名为 `bdai_sc` 的 MySQL 数据库，字符集为 `utf8mb4` 以支持完整 Unicode（包括成就描述中的 Emoji）。表结构由 Hibernate 的 `ddl-auto: update` 自动创建——无需手动运行 SQL 迁移脚本。

连接到 MySQL 实例并执行以下语句：

```sql
CREATE DATABASE IF NOT EXISTS bdai_sc DEFAULT CHARACTER SET utf8mb4;
CREATE USER IF NOT EXISTS 'bdai_sc'@'localhost' IDENTIFIED BY 'bdai_sc';
GRANT ALL PRIVILEGES ON bdai_sc.* TO 'bdai_sc'@'localhost';
FLUSH PRIVILEGES;
```

验证连接是否正常：

```bash
mysql -ubdai_sc -pbdai_sc -h127.0.0.1 -P3306 -e "USE bdai_sc; SELECT 'Connection OK' AS status;"
```

若返回 `Connection OK`，数据库已就绪。表将在后端首次启动时自动创建。

> 来源：[application.yml](backend/src/main/resources/application.yml#L4-L10)、[CLAUDE.md](CLAUDE.md#L30-L36)、[docs/guide/getting-started.md](docs/guide/getting-started.md#L33-L39)

## 步骤 3 — 配置环境变量

后端和前端均从项目根目录的共享 `.env` 文件读取配置。后端通过 `application.yml` 中的 `spring.config.import` 引入，Vite 通过 [vite.config.js](frontend/vite.config.js#L7-L8) 中的 `envDir` 设置指向父目录来加载。

```bash
cp .env.example .env
```

本地开发场景下，默认值已经正确。以下是所有可配置项及其修改场景：

| 环境变量 | 默认值 | 修改场景 |
|---------|--------|---------|
| `BDAI_SC_DB_URL` | `jdbc:mysql://127.0.0.1:3306/bdai_sc?...` | MySQL 运行在不同主机或端口 |
| `BDAI_SC_DB_USER` | `bdai_sc` | 使用了不同的 MySQL 用户 |
| `BDAI_SC_DB_PASSWORD` | `bdai_sc` | 使用了不同的密码 |
| `BDAI_SC_JWT_SECRET` | `bdai-sc-student-center-jwt-secret-key-change-in-production-2026` | **生产环境务必修改** |
| `BDAI_SC_JWT_EXPIRES_MINUTES` | `120` | 需要更短/更长的会话时长 |
| `BDAI_SC_BACKEND_PORT` | `8080` | 8080 端口已被占用 |
| `BDAI_SC_FRONTEND_PORT` | `5173` | 5173 端口已被占用 |
| `BDAI_SC_UPLOAD_DIR` | `./uploads` | 希望将上传文件存到其他位置 |

初次运行可以跳过创建 `.env`——application.yml 的默认值配合步骤 2 的数据库凭据即可正常工作。

**忘记创建 `.env` 是最常见的第一步错误。** 如果 MySQL 明明在运行但后端启动失败并报数据库连接错误，请先确认 `.env` 文件存在于项目根目录（不是 `backend/` 或 `frontend/` 内部）。

> 来源：[application.yml](backend/src/main/resources/application.yml#L1-L39)、[vite.config.js](frontend/vite.config.js#L6-L8)、[config-reference.md](docs/design/config-reference.md#L1-L89)

## 步骤 4 — 启动后端

进入后端目录，用 Maven 启动 Spring Boot：

```bash
cd backend
mvn spring-boot:run
```

首次运行会下载所有 Maven 依赖，可能需要几分钟。后续运行由于本地缓存，速度几乎即时。看到以下日志说明启动成功：

```
Started StudentCenterApplication in X.XXX seconds (JVM running for Y.YYY)
```

如果看到 Hibernate 输出了 `create table` 语句，属于正常现象——说明 JPA 正在首次自动创建数据库表。

后端现在监听在 `http://localhost:8080`。可以用以下命令验证：

```bash
curl -s http://localhost:8080/api/auth/login | head -c 100
```

应返回一段 JSON 错误响应（因为未发送有效凭据），这证明服务器在正常运行。

> 来源：[pom.xml](backend/pom.xml#L1-L77)、[CLAUDE.md](CLAUDE.md#L24-L28)、[CLAUDE.md](CLAUDE.md#L30-L36)

## 步骤 5 — 启动前端

**打开一个新的终端窗口**（保持后端运行），然后安装依赖并启动 Vite：

```bash
cd frontend
npm install
npm run dev
```

`npm install` 解析 [package.json](frontend/package.json#L1-L28) 中列出的所有依赖——Vue 3、Vite、Axios、AG Grid、jsPDF 等。首次安装约下载 150MB 包文件。看到以下日志说明一切正常：

```
  VITE v5.4.9  ready in XXXms

  ➜  Local:   http://localhost:5173/
  ➜  Network: http://192.168.x.x:5173/
```

在浏览器打开 `http://localhost:5173`，应看到登录页面。

> 来源：[package.json](frontend/package.json#L5-L8)、[vite.config.js](frontend/vite.config.js#L1-L30)、[CLAUDE.md](CLAUDE.md#L24-L28)

## 步骤 6 — 注册并探索

两个服务都运行起来后，就可以创建第一个账户了。访问注册页面并填写表单：

| 字段 | 规则 | 示例 |
|------|------|------|
| Display Name | 必填字符串 | `张三` |
| Username | 4–32 个字符，仅字母、数字和下划线 | `zhangsan_01` |
| Password | 必填字符串（后端未强制最短长度） | `mySecret123` |

![我的信息页面——学生登录后看到的第一个页面](https://github.com/MakiWinster72/GCSC/blob/main/assets/2026-03-09-5.png?raw=true)

注册成功后，后端返回 JWT Token。前端将其存储在 `localStorage` 的 `bdai_sc_token` 键下，并通过 [request.js](frontend/src/api/request.js#L13-L19) 中定义的 Axios 拦截器自动为每次后续 API 请求附加该 Token。首次登录后，系统会引导用户填写学生档案。

进入工作台后，可通过侧边栏探索主要页面：

| 菜单项 | 路由 | 可见角色 |
|--------|------|---------|
| 我的信息 | `/myinfos` | 所有已认证用户 |
| 个人成就 | `/achievements` | 所有已认证用户 |
| 通知 | `/notifications` | 所有已认证用户 |
| 学生信息 | `/student-info` | 仅教师和管理员 |
| 后台管理 | `/admin` | 仅管理员 |

![成就页面——证书展示的画廊风格](https://github.com/MakiWinster72/GCSC/blob/main/assets/2026-03-09-3.png?raw=true)

> 来源：[request.js](frontend/src/api/request.js#L13-L19)、[CLAUDE.md](CLAUDE.md#L55-L60)、[docs/design/getting-started.md](docs/design/getting-started.md#L111-L122)

## 常见问题排查

大多数首次运行问题都属于以下几类。对照下表先行自检，再深入查看日志。

| 现象 | 最可能原因 | 解决方案 |
|------|-----------|---------|
| `invalid source release: 21` | JDK 版本不对 | 安装 JDK 21，用 `java -version` 验证，设置 `JAVA_HOME` |
| `Unable to determine Dialect without JDBC metadata` | MySQL 未运行或数据库不存在 | 启动 MySQL，重新执行步骤 2 的 SQL |
| `Access denied for user 'bdai_sc'@'localhost'` | 凭据不匹配 | 重新执行步骤 2 的 CREATE USER 和 GRANT |
| 后端启动了但前端显示空白页 | Vite 未运行 | 确认 `npm run dev` 在独立终端中运行 |
| API 调用返回 `Network Error` | 后端未运行或端口错误 | 确认 `mvn spring-boot:run` 显示 "Started" 且监听 8080 |
| 登录成功但后续调用返回 401 | Token 过期或未存储 | 在浏览器控制台检查 `localStorage.getItem('bdai_sc_token')` |
| 文件上传失败 | 上传目录不存在 | 运行 `mkdir -p backend/uploads/` |

深入调试时，查看后端控制台（Spring Boot 日志）和浏览器 DevTools Network 标签。由于代理转发机制，任何失败的 API 调用在浏览器 Network 面板中显示为来自 `localhost:5173` 而非 `localhost:8080`。

> 来源：[README.md](README.md#L68-L89)、[CLAUDE.md](CLAUDE.md#L24-L28)

## 技术栈汇总

以下是为 BDAI-SC 提供支撑的完整技术栈：

| 层级 | 技术 | 版本 | 职责 |
|------|------|------|------|
| 后端运行时 | Java | 21 | 应用运行时，含现代语言特性 |
| 后端框架 | Spring Boot | 3.3.5 | Web 服务器、依赖注入、自动配置 |
| ORM | Spring Data JPA / Hibernate | — | 实体映射、自动 CRUD |
| 安全 | Spring Security + JWT (jjwt) | 0.12.6 | 无状态身份认证与基于角色的授权 |
| 数据库 | MySQL | 8.0+ | 持久化存储，utf8mb4 字符集 |
| 前端框架 | Vue 3（组合式 API） | ^3.5.12 | 响应式 UI，采用 `<script setup>` 语法 |
| 构建工具 | Vite | ^5.4.9 | 开发服务器 HMR 与生产构建 |
| HTTP 客户端 | Axios | ^1.7.7 | 带 JWT 拦截器的 API 请求 |
| 数据表格 | AG Grid | ^31.3.2 | 面向教师/管理员的学生信息表格 |
| 导出 | jsPDF + xlsx | ^2.5.1 / ^0.18.5 | PDF 和 Excel 文件生成 |

> 来源：[pom.xml](backend/pom.xml#L1-L77)、[package.json](frontend/package.json#L1-L28)

## 下一步

你现在拥有完整可用的本地开发环境了。根据你的目标，以下是推荐的后续阅读顺序：

- **[项目结构](./project-structure)** — 获取每个目录和文件的详细地图，自如导航代码库。
- **[架构总览](./architecture)** — 深入了解分层架构、请求生命周期和前后端通信细节。
- **[安全与 JWT 认证](./jwt-auth)** — 了解从注册到受保护 API 调用的完整 Token 生命周期。
- **[REST API 设计](./rest-api)** — 参考全部 9 个 Controller 的所有可用端点。