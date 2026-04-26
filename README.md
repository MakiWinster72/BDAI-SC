# BDAI_SC 学生中心（注册/登录）

当前实现：
- `backend/`：Java 21 + Spring Boot + Maven + MySQL
- `frontend/`：Vue 3 + Vite
- 功能：用户名/密码登录、显示名称+用户名+密码注册
- 鉴权：JWT（登录/注册返回 `token`，前端自动携带 `Authorization`）

## quickStart！
### 0. 统一配置（推荐先做）

在项目根目录使用一份配置文件管理前后端参数（地址、端口、密钥等）：

```bash
cp .env.example .env
```

按需修改 `.env` 中的关键项：
- `BDAI_SC_FRONTEND_PORT` / `BDAI_SC_BACKEND_PORT`
- `BDAI_SC_DB_URL` / `BDAI_SC_DB_USER` / `BDAI_SC_DB_PASSWORD`
- `BDAI_SC_JWT_SECRET`
- `VITE_API_BASE`

### 1. 数据库准备

```sql
# 或使用自己的账号密码
CREATE DATABASE IF NOT EXISTS bdai_sc DEFAULT CHARACTER SET utf8mb4;
CREATE USER IF NOT EXISTS 'bdai_sc'@'localhost' IDENTIFIED BY 'bdai_sc';
GRANT ALL PRIVILEGES ON bdai_sc.* TO 'bdai_sc'@'localhost';
FLUSH PRIVILEGES;
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端默认地址：`http://localhost:8080`

若启动时报 `Unable to determine Dialect without JDBC metadata`，按下面检查：
- 确认 MySQL 已启动，并且 `bdai_sc` 库存在。
- 确认连接用户是 `bdai_sc`，不是 `bdai_sc@localhost:3306` 这样的完整串。
- 用下面命令手工验证连接：

```bash
mysql -ubdai_sc -pbdai_sc -h127.0.0.1 -P3306 -e "use bdai_sc; show tables;"
```

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认地址：`http://localhost:5173`

## 实现说明

- 后端
  - 用户表：`backend/src/main/java/com/gcsc/studentcenter/entity/AppUser.java`
  - 注册/登录：`backend/src/main/java/com/gcsc/studentcenter/service/AuthService.java`
  - JWT：`backend/src/main/java/com/gcsc/studentcenter/service/JwtService.java`
  - 认证过滤器：`backend/src/main/java/com/gcsc/studentcenter/config/JwtAuthenticationFilter.java`
- 前端
  - 登录页：`frontend/src/views/LoginView.vue`
  - 注册页：`frontend/src/views/RegisterView.vue`
  - 首页：`frontend/src/views/HomeView.vue`

## Git 提交规范

- `feat:` 新功能
- `style:` 仅样式改动
- `fix:` 修复问题
- `docs:` 文档改动
- `refactor:` 重构（不改功能）
- `chore:` 工程维护
- 前缀后建议使用中文描述，例如：`feat: 完成注册登录接口`

## TODO:
1. [x] 注册登录 ✅ 2026-03-06
2. [ ] 分角色（学生/教师/管理员）
3. [ ] 填写信息（学生）
4. [ ] 导出信息（教师）
5. [ ] 发布近期成就/动态（教师/学生）
6. [ ] 社区互动（教师/学生）
7. [ ] 分学院（看情况）
8. TODO: 页面设计增加logo的位置

## 页面设计预览

### 主页：

登录进来就看见的页面，左上为目前登录信息小卡片，左下为不同分区的菜单栏，右侧为学院内动态/置顶公告。
![](assets/2026-03-09-1.png)

### 我的信息

个人信息页，学生在该页面填写个人信息，初次登录会提示进入该页面填写，后续更改需要请求变更（或取消该限制，直接更改）
![](assets/2026-03-09-5.png)

### 学生信息（仅教师/管理员）

教师/管理员可以在此页面筛选学生，并选择对应的学生导出信息。
![](assets/2026-03-09-10.png)

### 设置页

设置页，点击小卡片右上角可进入，目前还没想好有什么可以设置的。
![](assets/2026-03-09-2.png)

### 成就页

成就页，学生可在成就页记录自己的成就。成就可配图（证书，记录等），形成画廊展示。可以为该成就补充信息，如名字，经历时间，获得成就的日期，该成就的描述，个人感想等。
![](assets/2026-03-09-3.png)

### 记录页

记录页，该页面为学生记录的隐私动态，仅自己可见。
![](assets/2026-03-09-4.png)

### 教师联系方式

联系页，学生可在该页面查看老师/部门的联系方式。
![](assets/2026-03-09-9.png)
