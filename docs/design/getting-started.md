# 快速开始

## 概述


本指南将引导你完成 BDAI-SC Student Center 的配置与运行，从全新克隆到搭建出完整的本地开发环境。

## 架构概览

BDAI-SC 是一个经典的双服务架构：Spring Boot REST API 与 Vue 3 单页应用通过 HTTP 进行通信。MySQL 数据库负责持久化所有数据，JWT 令牌则在无服务端会话的情况下处理身份验证。

理解这三个端口是本地开发中最重要的一项调试洞察：

- 浏览器与 **5173** 端口通信（前端）
- Vite 开发服务器将 API 调用代理至 **8080** 端口（后端）
- Spring Boot 与 **3306** 端口上的 MySQL 通信

> 来源：application.yml、request.js、vite.config.js

## 前置条件

| 软件 | 最低版本 | 用途 | 验证命令 |
|------|----------|------|----------|
| JDK | 21 | 后端运行时 | `java -version` |
| Maven | 3.8+ | 后端构建工具 | `mvn -version` |
| Node.js | 18+ | 前端运行时 | `node -v` |
| npm | 9+ | 前端包管理器 | `npm -v` |
| MySQL | 8.0+ | 数据库 | `mysql -V` |

::: warning 注意
JDK 21 不可妥协。`pom.xml` 中声明了 `21`。如果你的 `java -version` 输出低于 21，Maven 编译将失败并提示 `invalid source release: 21`。请使用 SDKMAN 等版本管理器或显式设置 `JAVA_HOME`。
:::

> 来源：pom.xml、runtime-env.md

## 步骤 1 — 克隆代码仓库

```bash
git clone https://github.com/MakiWinster72/BDAI-SC.git
cd BDAI-SC
```

验证克隆：

```bash
ls -d backend/ frontend/ scripts/ docs/
```

> 来源：CLAUDE.md

## 步骤 2 — 配置数据库

```sql
CREATE DATABASE IF NOT EXISTS gcsc DEFAULT CHARACTER SET utf8mb4;
CREATE USER IF NOT EXISTS 'gcsc'@'localhost' IDENTIFIED BY 'gcsc';
GRANT ALL PRIVILEGES ON gcsc.* TO 'gcsc'@'localhost';
FLUSH PRIVILEGES;
```

验证连接：

```bash
mysql -ugcsc -pgcsc -h127.0.0.1 -P3306 -e "use gcsc; show tables;"
```

> 来源：application.yml、README.md

## 步骤 3 — 配置并启动后端

配置项 | 默认值 | 何时修改
------|--------|--------
`spring.datasource.url` | `jdbc:mysql://localhost:3306/gcsc` | 仅当 MySQL 运行在不同的主机/端口时
`spring.datasource.username` | `gcsc` | 仅当你使用了不同的 MySQL 用户时
`spring.datasource.password` | `gcsc` | 仅当你使用了不同的 MySQL 密码时
`security.jwt.secret` | `gcsc-student-center-jwt-secret-key-...` | 生产环境中必须更改
`server.port` | `8080` | 仅当 8080 端口已被占用时

启动后端：

```bash
cd backend
mvn spring-boot:run
```

成功标志：

```
Started StudentCenterApplication in X.XXX seconds
```

> 来源：application.yml、pom.xml、CLAUDE.md

## 步骤 4 — 安装依赖并启动前端

```bash
cd frontend
npm install
npm run dev
```

成功标志：

```
  VITE v5.4.9  ready in XXXms

  ➜  Local:   http://localhost:5173/
  ➜  Network: http://192.168.x.x:5173/
```

> 来源：package.json、vite.config.js、index.js (router)

## 步骤 5 — 注册你的首个账号

| 字段 | 规则 | 示例 |
|------|------|------|
| 显示名称 | 必填字符串 | 张三 |
| 用户名 | 4–32 个字符，仅限字母数字和下划线 | `zhangsan_01` |
| 密码 | 必填字符串 | `mySecret123` |

> 来源：auth.md、RegisterView.vue、LoginView.vue

## 步骤 6 — 探索仪表盘

| 菜单项 | 路由 | 可见性 |
|--------|------|--------|
| 我的信息 | `/myinfos` | 所有已认证用户 |
| 个人成就 | `/achievements` | 所有已认证用户 |
| 通知 | `/notifications` | 所有已认证用户 |
| 学生信息 | `/student-info` | 仅限教师和管理员 |
| 后台管理 | `/admin` | 仅限管理员 |

> 来源：menu.js、index.js (router)、README.md

## 故障排除

| 症状 | 可能的原因 | 修复方法 |
|------|----------|----------|
| `mvn spring-boot:run` 失败并提示 `invalid source release: 21` | JDK 版本错误 | 安装 JDK 21 并设置 `JAVA_HOME` |
| `Unable to determine Dialect without JDBC metadata` | MySQL 未运行或缺少数据库 | 启动 MySQL，运行步骤 2 中的 SQL 初始化脚本 |
| `Access denied for user 'gcsc'@'localhost'` | 用户凭据不匹配 | 重新执行 CREATE USER 和 GRANT SQL 语句 |
| 前端显示空白页或 404 | Vite 未运行 | 确保 `npm run dev` 正在运行 |
| API 调用返回 `Network Error` | 后端未运行或存在 CORS 问题 | 确认后端正在 8080 端口运行 |
| 登录成功但所有 API 调用均返回 401 | 令牌已过期或未存储 | 检查 localStorage 里是否存在 `gcsc_token` |
| 文件上传失败 | 缺少 `uploads/` 目录 | `mkdir -p backend/uploads/` |

> 来源：README.md、runtime-env.md

## 生成测试数据（可选）

```bash
pip install faker
python scripts/generate_test_data.py --users 20 --achievements 50 -o test_data.sql
mysql -ugcsc -pgcsc gcsc < test_data.sql
```

> 来源：generate_test_data.py

## 下一步

- [概述](./quick-start) — 了解项目的目标和当前状态
- [项目结构指南](./project-structure) — 获取每个目录及其职责的详细映射
- [架构概述](./architecture) — 了解完整的后端/前端交互模型
- [JWT 身份验证流程](./jwt-auth) — 掌握从注册到受保护 API 调用的完整令牌生命周期
- [基于角色的访问控制](./rbac) — 了解 STUDENT/TEACHER/ADMIN 权限系统
