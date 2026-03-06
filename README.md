# GCSC 学生中心（注册/登录）

当前实现：
- `backend/`：Java 17 + Spring Boot + Maven + MySQL
- `frontend/`：Vue 3 + Vite
- 功能：用户名/密码登录、显示名称+用户名+密码注册

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

## 5. Git 提交规范（建议）

- `feat:` 新功能
- `style:` 仅样式改动
- `fix:` 修复问题
- `docs:` 文档改动
- `refactor:` 重构（不改功能）
- `chore:` 工程维护
- 前缀后建议使用中文描述，例如：`feat: 完成注册登录接口`
