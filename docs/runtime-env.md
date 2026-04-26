# BDAI_SC Student Center 运行环境与最低配置

## 项目简介

BDAI_SC Student Center 是一个学生成绩管理全栈 Web 应用。

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.3.5 |
| Java | OpenJDK / OracleJDK | 21 |
| ORM | Spring Data JPA | - |
| 安全 | Spring Security + JWT | - |
| 数据库 | MySQL | 8.0+ |
| 前端框架 | Vue | 3.5.12 |
| 构建工具 | Vite | 5.4.9 |
| HTTP 客户端 | Axios | 1.7.7 |

## 最低硬件配置

### 开发 / 部署服务器

| 资源 | 最低配置 | 推荐配置 |
|------|----------|----------|
| CPU | 2 核 | 4 核+ |
| 内存 | 4 GB | 8 GB+ |
| 磁盘 | 10 GB | 20 GB+（含上传文件存储） |
| 网络 | 100 Mbps | 1 Gbps |

> 单台 2 核 4 GB 服务器可同时运行前后端并支持 10-20 人并发访问。

## 软件依赖

### 必须安装

| 软件 | 版本要求 | 说明 |
|------|----------|------|
| JDK | 21 | Spring Boot 3.3 要求 JDK 17+，本项目使用 JDK 21 |
| Maven | 3.8+ | 后端构建工具 |
| Node.js | 18+ | 前端运行时 |
| npm | 9+ | 前端包管理（随 Node.js 安装） |
| MySQL | 8.0+ | 字符集需支持 utf8mb4 |

### 可选

| 软件 | 版本要求 | 说明 |
|------|----------|------|
| Git | 任意版本 | 代码版本控制 |

## 运行时端口

| 服务 | 端口 | 说明 |
|------|------|------|
| 后端 API | 8080 | Spring Boot 默认端口 |
| 前端开发服务器 | 5173 | Vite 默认端口 |
| MySQL | 3306 | MySQL 默认端口（可自定义） |

## 环境变量

### 后端（application.yml 或系统环境变量）

| 变量名 | 必填 | 默认值 | 说明 |
|--------|------|--------|------|
| `spring.datasource.url` | 是 | - | MySQL 连接 URL，如 `jdbc:mysql://localhost:3306/bdai_sc` |
| `spring.datasource.username` | 是 | - | MySQL 用户名 |
| `spring.datasource.password` | 是 | - | MySQL 密码 |
| `security.jwt.secret` | 是 | - | JWT 签名密钥（至少 32 字符） |
| `server.port` | 否 | 8080 | 后端监听端口 |

### 前端（.env 或系统环境变量）

| 变量名 | 必填 | 默认值 | 说明 |
|--------|------|--------|------|
| `VITE_API_BASE` | 否 | `http://localhost:8080` | 后端 API 基础地址 |

## 数据库初始化

```sql
CREATE DATABASE IF NOT EXISTS bdai_sc DEFAULT CHARACTER SET utf8mb4;
CREATE USER IF NOT EXISTS 'bdai_sc'@'localhost' IDENTIFIED BY 'bdai_sc';
GRANT ALL PRIVILEGES ON bdai_sc.* TO 'bdai_sc'@'localhost';
FLUSH PRIVILEGES;
```

## 快速启动

### 1. 启动 MySQL

确保 MySQL 服务运行中，数据库已创建。

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端启动后监听 `http://localhost:8080`。

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端启动后访问 `http://localhost:5173`。

## 文件上传存储

上传文件存储在 `backend/uploads/` 目录，最大支持 200MB 文件。该目录需在启动前已存在且后端进程有读写权限。

## 浏览器兼容性

| 浏览器 | 最低版本 |
|--------|----------|
| Chrome | 90+ |
| Firefox | 90+ |
| Safari | 15+ |
| Edge | 90+ |
