# 快速开始

## 环境要求

Node.js 18+  
Java 21+  
Maven 3.8+  
MySQL 8.0+

## 1. 配置环境变量

项目使用 `.env` 文件集中管理所有配置。

```bash
# 复制配置文件
cp .env.example .env
```

按需修改 `.env` 中的以下关键配置：

```bash
# 数据库连接
BDAI_SC_DB_URL=jdbc:mysql://127.0.0.1:3306/bdai_sc?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
BDAI_SC_DB_USER=bdai_sc
BDAI_SC_DB_PASSWORD=bdai_sc

# JWT 密钥
BDAI_SC_JWT_SECRET=your-secret-key-here

# 默认管理员账号（首次启动自动创建）
BDAI_SC_INIT_ADMIN_USERNAME=bdai
BDAI_SC_INIT_ADMIN_PASSWORD=bdai2026
BDAI_SC_INIT_ADMIN_DISPLAY_NAME=管理员
```

> ⚠️ 首次启动后系统会自动创建一个默认管理员账号（用户名 `bdai`，密码 `bdai2026`）。**请务必在部署后修改该账号的密码**，或登录后新建一个管理员账号并删除此默认账号，以确保安全。

## 2. 初始化数据库

```sql
CREATE DATABASE IF NOT EXISTS bdai_sc DEFAULT CHARACTER SET utf8mb4;
CREATE USER IF NOT EXISTS 'bdai_sc'@'localhost' IDENTIFIED BY 'bdai_sc';
GRANT ALL PRIVILEGES ON bdai_sc.* TO 'bdai_sc'@'localhost';
FLUSH PRIVILEGES;
```

表结构由 JPA 自动创建（`ddl-auto: update`）。

## 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端启动时会自动读取项目根目录的 `.env` 文件。

启动成功后默认运行于 <http://localhost:8080>

## 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端启动时会自动读取 `.env` 中的 `VITE_*` 变量。

启动成功后运行于 <http://localhost:5173>

## 后续步骤

- [使用手册](../manual/) — 了解每个页面的功能与使用方法
- [详细设计概述](../design/) — 了解系统架构与核心模块
