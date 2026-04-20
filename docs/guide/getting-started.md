# 快速开始

## 环境要求

- Node.js 18+
- Java 21+
- MySQL 8.0+

## 数据库配置

```sql
CREATE DATABASE IF NOT EXISTS gcsc DEFAULT CHARACTER SET utf8mb4;
CREATE USER IF NOT EXISTS 'gcsc'@'localhost' IDENTIFIED BY 'gcsc';
GRANT ALL PRIVILEGES ON gcsc.* TO 'gcsc'@'localhost';
FLUSH PRIVILEGES;
```

## 后端启动

```bash
cd backend
mvn spring-boot:run
```

## 前端启动

```bash
cd frontend
npm install
npm run dev
```
