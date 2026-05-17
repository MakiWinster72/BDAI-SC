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
