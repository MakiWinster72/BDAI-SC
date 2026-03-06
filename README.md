# GCSC 学生中心（注册/登录）

当前实现：
- `backend/`：Java 17 + Spring Boot + Maven + MySQL
- `frontend/`：Vue 3 + Vite
- 功能：用户名/密码登录、显示名称+用户名+密码注册
- 鉴权：JWT（登录/注册返回 `token`，前端自动携带 `Authorization`）

## 1. 数据库准备

```sql
CREATE DATABASE IF NOT EXISTS gcsc DEFAULT CHARACTER SET utf8mb4;
CREATE USER IF NOT EXISTS 'gcsc'@'localhost' IDENTIFIED BY 'gcsc';
GRANT ALL PRIVILEGES ON gcsc.* TO 'gcsc'@'localhost';
FLUSH PRIVILEGES;
```

## 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端默认地址：`http://localhost:8080`

若启动时报 `Unable to determine Dialect without JDBC metadata`，按下面检查：
- 确认 MySQL 已启动，并且 `gcsc` 库存在。
- 确认连接用户是 `gcsc`，不是 `gcsc@localhost:3306` 这样的完整串。
- 用下面命令手工验证连接：

```bash
mysql -ugcsc -pgcsc -h127.0.0.1 -P3306 -e "use gcsc; show tables;"
```

## 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认地址：`http://localhost:5173`

## 4. 认证接口

- `POST /api/auth/register`
  - 请求体：`displayName`、`username`、`password`
- `POST /api/auth/login`
  - 请求体：`username`、`password`
- `GET /api/auth/me`
  - 需携带请求头：`Authorization: Bearer <token>`

## 5. 前后端如何连接

- 前端请求地址在 `frontend/src/api/auth.js`：
  - `baseURL` 指向 `http://localhost:8080`
  - 请求拦截器会读取 `localStorage` 的 `gcsc_token` 并自动加到 `Authorization` 头
- 后端 CORS 已放行 `http://localhost:5173`（`backend/src/main/java/com/gcsc/studentcenter/config/SecurityConfig.java`）
- 登录/注册成功后，后端返回 `token`，前端保存到 `localStorage` 并跳转首页
- 首页会调用 `/api/auth/me` 验证 token 是否有效，过期则跳回登录页

## 6. 实现说明（简要）

- 后端
  - 用户表：`backend/src/main/java/com/gcsc/studentcenter/entity/AppUser.java`
  - 注册/登录：`backend/src/main/java/com/gcsc/studentcenter/service/AuthService.java`
  - JWT：`backend/src/main/java/com/gcsc/studentcenter/service/JwtService.java`
  - 认证过滤器：`backend/src/main/java/com/gcsc/studentcenter/config/JwtAuthenticationFilter.java`
- 前端
  - 登录页：`frontend/src/views/LoginView.vue`
  - 注册页：`frontend/src/views/RegisterView.vue`
  - 首页：`frontend/src/views/HomeView.vue`

## 7. Git 提交规范（建议）

- `feat:` 新功能
- `style:` 仅样式改动
- `fix:` 修复问题
- `docs:` 文档改动
- `refactor:` 重构（不改功能）
- `chore:` 工程维护
- 前缀后建议使用中文描述，例如：`feat: 完成注册登录接口`
