# 快速开始

## 获取代码

```bash
git clone https://github.com/MakiWinster72/BDAI-SC

# 中国大陆加速
git clone https://gh-proxy.com/https://github.com/MakiWinster72/BDAI-SC
```

## 环境要求

### 直接部署

Node.js 18+  
Java 21+  
Maven 3.8+  
MySQL 8.0+

### Docker 部署

Docker 24+  
Docker Compose v2+

## 部署方式

### 方式一：Docker 部署（推荐）

#### 1. 配置环境变量

```bash
# 复制配置文件
cp .env.example .env
```

按需修改 `.env` 中的关键配置：

```bash
# 数据库连接
BDAI_SC_DB_HOST=127.0.0.1
BDAI_SC_DB_PORT=3306
BDAI_SC_DB_NAME=bdai_sc
BDAI_SC_DB_USER=bdai_sc
BDAI_SC_DB_PASSWORD=bdai_sc

# JWT 密钥
BDAI_SC_JWT_SECRET=your-secret-key-here

# 默认管理员账号（首次启动自动创建）
BDAI_SC_INIT_ADMIN_USERNAME=bdai
BDAI_SC_INIT_ADMIN_PASSWORD=bdai2026
BDAI_SC_INIT_ADMIN_DISPLAY_NAME=管理员
```

> 首次启动后系统会自动创建一个默认管理员账号（用户名 `bdai`，密码 `bdai2026`）。**请务必在部署后修改该账号的密码**，或登录后新建一个管理员账号并删除此默认账号，以确保安全。

#### 2. 启动服务

```bash
docker compose up -d
```

容器启动后会：

- 自动创建 MySQL 数据库
- 自动初始化表结构（`ddl-auto: update`）
- 自动创建默认管理员账号

启动成功后：

- 前端：<http://localhost:5173>
- 后端：<http://localhost:8080>

#### 3. 停止服务

```bash
docker compose down
```

#### 4. 重新构建（代码更新后）

```bash
docker compose down && docker compose build --no-cache && docker compose up -d
```

---

### 方式二：直接部署

#### 1. 配置环境变量

```bash
# 复制配置文件
cp .env.example .env
```

按需修改 `.env` 中的关键配置：

```bash
# 数据库连接
BDAI_SC_DB_HOST=127.0.0.1
BDAI_SC_DB_PORT=3306
BDAI_SC_DB_NAME=bdai_sc
BDAI_SC_DB_USER=bdai_sc
BDAI_SC_DB_PASSWORD=bdai_sc

# JWT 密钥
BDAI_SC_JWT_SECRET=your-secret-key-here

# 默认管理员账号（首次启动自动创建）
BDAI_SC_INIT_ADMIN_USERNAME=bdai
BDAI_SC_INIT_ADMIN_PASSWORD=bdai2026
BDAI_SC_INIT_ADMIN_DISPLAY_NAME=管理员
```

> 首次启动后系统会自动创建一个默认管理员账号（用户名 `bdai`，密码 `bdai2026`）。**请务必在部署后修改该账号的密码**，或登录后新建一个管理员账号并删除此默认账号，以确保安全。

加载环境变量：

```bash
source .env
```

#### 2. 初始化数据库

```sql
CREATE DATABASE IF NOT EXISTS bdai_sc DEFAULT CHARACTER SET utf8mb4;
CREATE USER IF NOT EXISTS 'bdai_sc'@'localhost' IDENTIFIED BY 'bdai_sc';
GRANT ALL PRIVILEGES ON bdai_sc.* TO 'bdai_sc'@'localhost';
FLUSH PRIVILEGES;
```

表结构由 JPA 自动创建（`ddl-auto: update`）。

#### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端启动时会自动读取项目根目录的 `.env` 文件。

启动成功后默认运行于 <http://localhost:8080>

#### 4. 启动前端

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
