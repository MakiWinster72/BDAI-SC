# AGENTS.md - BDAI_SC Student Center

## 首次运行
```bash
cp .env.example .env && source .env
```

## 后端（Java 21 + Spring Boot 3.3.5）

```bash
source .env          # 必须！Maven 不自动加载 .env
cd backend
mvn spring-boot:run            # 启动
mvn clean package -DskipTests  # 构建 JAR
mvn test                       # 全部测试
mvn test -Dtest=ClassName     # 单测类
mvn test -Dtest=ClassName#methodName  # 单测方法
```
- **验证改动**：从仓库根目录运行 `./scripts/verify.sh`（前端 build + 后端 test）
- **后端任何改动**都要跑 `mvn test`，尤其是安全/权限/审核逻辑（覆盖 `StudentProfileServiceSecurityTest`、`AchievementServiceSecurityTest`）
- 上传文件落 `backend/uploads/` 或 `$BDAI_SC_UPLOAD_DIR`（相对 `backend/` 而非项目根）

## 前端（Vue 3 + Vite）

```bash
cd frontend
npm install
npm run dev      # http://localhost:5173
npm run build    # 从仓库根：npm --prefix frontend run build
```
- `VITE_API_BASE` 留空走 Vite 反代到 8080；填了就直连（生产行为）
- 无 lint/typecheck/test 脚本

## 数据库（MySQL 8）

```sql
CREATE DATABASE IF NOT EXISTS bdai_sc DEFAULT CHARACTER SET utf8mb4;
CREATE USER IF NOT EXISTS 'bdai_sc'@'localhost' IDENTIFIED BY 'bdai_sc';
GRANT ALL PRIVILEGES ON bdai_sc.* TO 'bdai_sc'@'localhost';
FLUSH PRIVILEGES;
```
- JPA `ddl-auto: update`，首次运行自动建表

## 配置要点

- `backend/src/main/resources/application.yml` 通过 `spring.config.import` 加载 `.env`（搜索 `backend/` 再 `../`）
- JWT token 在 frontend localStorage 为 `bdai_sc_token`
- JWT 用户名校验：`^[a-zA-Z0-9_]{4,32}$`（4-32 位字母、数字、下划线）
- 默认管理员首次启动自动创建：用户名 `bdai`，密码 `bdai2026`（部署后必改）

## 审核流程关键规则

- 档案审核开启（`reviewSettings.profileReviewAutoApprove=false`）：STUDENT/CADRE 不能直写 `PUT /api/student-profiles/me`，须提交审核申请；TEACHER/ADMIN 可直写
- 成就审核开启（`reviewSettings.achievementReviewAutoApprove=false`）：STUDENT/CADRE 不能直写 `POST/PUT/DELETE /api/achievements/**`，须提交审核申请；TEACHER/ADMIN 可直写
- 审核通过后落库走服务层内部方法，不受直写拦截影响

## 文件上传

- 上传：`POST /api/upload`（需登录），返回 `/api/media/uploads/{userId}/{folder}/...`
- 读取：`GET /api/media/uploads/{userId}/{folder}/{filename}`（需登录，对象级权限检查）
- **没有** `/uploads/**` 公开静态目录
- 前端私有 URL 通过 `ProtectedMediaImage` / `fetchMedia` 带 JWT 拉取

## 架构速览

- 包名：`com.gcsc.studentcenter`（controller/service/repository/entity/dto/config/exception）
- 9 类成就实体：contest, research, paper, patent, certificate, works, journal, double-hundred, ieer-training
- 4 角色：STUDENT（默认）/ CADRE / TEACHER / ADMIN
- DB 表名/列名：snake_case；角色存 DB 为 `ROLE_<role>`（Spring Security）

## Git

- 分支：`feat/feature-name`、`fix/bug-description`、`page/page-name`
- 提交：中文描述，如 `feat: 添加XX功能`、`fix: 修复XX问题`
