<h1 align="center">BDAI-SC</h1>

<p align="center">

[![Java 21](https://img.shields.io/badge/Java-21-5382a1?style=flat-square&logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 3.3](https://img.shields.io/badge/Spring%20Boot-3.3.5-6db33f?style=flat-square&logo=spring)](https://spring.io/projects/spring-boot)
[![Vue 3](https://img.shields.io/badge/Vue-3.5-4fc08d?style=flat-square&logo=vuedotjs)](https://vuejs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479a1?style=flat-square&logo=mysql)](https://www.mysql.com/)
[![JWT](https://img.shields.io/badge/JWT-0.12.6-000?style=flat-square&logo=jsonwebtokens)](https://github.com/jwtk/jjwt)

</p>

## QuickStart

### 前置条件

- JDK 21+
- Node.js 18+
- MySQL 8.0+
- Maven 3.9+

### 1. 配置环境变量

```bash
cp .env.example .env
```

按需修改 `.env` 中的关键配置：

- `BDAI_SC_DB_HOST` / `BDAI_SC_DB_PORT` / `BDAI_SC_DB_NAME` / `BDAI_SC_DB_USER` / `BDAI_SC_DB_PASSWORD` — 数据库连接
- `BDAI_SC_JWT_SECRET` — JWT 密钥，生产环境务必更换
- `BDAI_SC_INIT_ADMIN_USERNAME` / `BDAI_SC_INIT_ADMIN_PASSWORD` — 默认管理员账号（首次启动自动创建）

> ⚠️ 首次启动后系统会自动创建默认管理员账号（用户名 `bdai`，密码 `bdai2026`）。**部署后务必修改该账号密码**，或新建管理员后删除此默认账号。

加载环境变量：

```bash
source .env
```

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
```

访问 `http://localhost:8080`

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

访问 `http://localhost:5173`

> 后端启动前请在项目根目录执行 `source .env`，Maven 不会自动加载 `.env`。

## 开发与验证

合并或发布前，按改动范围至少执行下列检查之一；也可在项目根目录运行 `./scripts/verify.sh`（前端 build + 后端 test）。

| 改动范围 | 建议命令 |
| -------- | -------- |
| 前端任意改动 | `npm --prefix frontend run build` |
| 后端权限、审核、对象级访问、上传鉴权 | `source .env && cd backend && mvn test` |

当前后端测试覆盖服务层安全逻辑（非空）：

- `StudentProfileServiceSecurityTest` — 档案 IDOR、搜索范围、审核开启时禁止学生/班干部直写
- `AchievementServiceSecurityTest` — 成就对象权限、审核开启时禁止学生/班干部直写

### 环境变量与上传目录

完整列表见 `.env.example`。常用项：

| 变量 | 说明 |
| ---- | ---- |
| `BDAI_SC_DB_*` | MySQL 连接 |
| `BDAI_SC_JWT_SECRET` | JWT 签名密钥，生产务必更换 |
| `BDAI_SC_UPLOAD_DIR` | 上传根目录，默认 `backend/uploads`（相对 `backend/`） |
| `BDAI_SC_CORS_ALLOWED_ORIGINS` | 允许的前端 Origin |
| `VITE_API_BASE` | 前端 API 根地址，开发留空可走 Vite 代理 |

**上传与访问策略**

- 上传：`POST /api/upload`（需登录），文件落在 `BDAI_SC_UPLOAD_DIR`，接口返回 `/api/media/uploads/{userId}/{folder}/...` 形式 URL。
- 读取：`GET /api/media/uploads/{userId}/{folder}/{filename}`（需登录，含头像、成果附件、审核材料），按用户与角色做对象级校验；**没有** `/uploads/**` 公开静态目录。
- 前端私有 URL 通过 `ProtectedMediaImage` / `fetchMedia` 带 JWT 拉取。

### 认证与审核（与代码一致）

**无需 JWT 的接口**（节选）：`POST /api/auth/login`、`POST /api/auth/register`、`GET /api/auth/captcha`、`GET /api/auth/public-config`，以及前端静态资源。其余 API 默认需登录。

**成果类型（9 类）**：`contest`、`research`、`paper`、`patent`、`certificate`、`works`、`journal`、`double-hundred`、`ieer-training`（实体名 `AchievementIeerTraining`）。

**审核开启时的直写限制**

- 档案审核开启：学生、班干部不能通过 `PUT /api/student-profiles/me` 直接落库，须走档案审核申请；教师/管理员可直写。
- 成就审核开启：学生、班干部不能通过 `POST/PUT/DELETE /api/achievements/**` 直写，须走成就审核申请；教师/管理员可直写。
- 审核通过后的落库走服务层内部方法，不受上述直写拦截影响。

## 角色权限

| 功能         | STUDENT        | CADRE | TEACHER | ADMIN |
| ------------ | -------------- | ----- | ------- | ----- |
| 登录/注册    | ✅             | ✅    | ✅      | ✅    |
| 管理个人档案 | ✅（提交审核） | ✅    | ✅      | ✅    |
| 提交成果     | ✅             | ✅    | ✅      | ✅    |
| 审核成果     | ❌             | 本班  | ✅      | ✅    |
| 审核档案变更 | ❌             | 本班  | ✅      | ✅    |
| 查看学生档案 | 仅本人         | 本班  | 所管    | 全部  |
| 导出学生信息 | ❌             | ❌    | ✅      | ✅    |
| 系统设置     | ❌             | ❌    | ❌      | ✅    |
| 管理用户角色 | ❌             | ❌    | ❌      | ✅    |

---

## Git 提交规范

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
