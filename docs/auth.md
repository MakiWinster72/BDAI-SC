# 注册/登录/验证实现说明

## 总体流程

1. 前端在注册或登录页收集表单信息，调用 `/api/auth/register` 或 `/api/auth/login`。
2. 后端校验请求数据，注册时写入用户并生成 JWT，登录时校验密码并生成 JWT。
3. 前端保存 `bdai_sc_token` 与 `bdai_sc_user` 到 `localStorage`。
4. 之后所有请求通过 Axios 请求拦截器自动加上 `Authorization: Bearer <token>`。
5. 后端 `JwtAuthenticationFilter` 解析 JWT，写入 Spring Security 上下文，完成鉴权。
6. 若后端返回 401，前端清除本地 token 并跳转到登录页。

## 后端实现

### 接口入口

- `POST /api/auth/register` 注册
- `POST /api/auth/login` 登录
- `GET /api/auth/me` 获取当前用户信息
- 文件位置：`backend/src/main/java/com/gcsc/studentcenter/controller/AuthController.java`

### 注册逻辑

- 文件位置：`backend/src/main/java/com/gcsc/studentcenter/service/AuthService.java`
- 校验 `displayName`、`username`、`password`，用户名规则 `^[a-zA-Z0-9_]{4,32}$`。
- 校验用户名唯一性。
- 密码使用 `BCryptPasswordEncoder` 加密后存储到 `passwordHash`。
- 角色为空时默认为 `STUDENT`。
- 生成 JWT 并返回 `AuthResponse`。

### 登录逻辑

- 文件位置：`backend/src/main/java/com/gcsc/studentcenter/service/AuthService.java`
- 校验用户名格式与非空。
- 根据用户名读取用户并校验密码哈希。
- 生成 JWT 并返回 `AuthResponse`。

### JWT 生成与解析

- 文件位置：`backend/src/main/java/com/gcsc/studentcenter/service/JwtService.java`
- `generateToken` 在 JWT 中写入 `sub`、`displayName`、`role` 等 claims。
- 过期时间由 `application.yml` 的 `security.jwt.expires-minutes` 控制，默认 120 分钟。
- 使用 `security.jwt.secret` 作为签名密钥。

### 鉴权过滤器

- 文件位置：`backend/src/main/java/com/gcsc/studentcenter/config/JwtAuthenticationFilter.java`
- 从 `Authorization` 头读取 `Bearer` token。
- 解析 token，构造 `UsernamePasswordAuthenticationToken` 写入 `SecurityContext`。
- 解析失败时清理上下文但不抛异常。

### 安全配置

- 文件位置：`backend/src/main/java/com/gcsc/studentcenter/config/SecurityConfig.java`
- 关闭 CSRF，启用 CORS。
- `/api/auth/register` 与 `/api/auth/login` 放行。
- 其他接口默认需要鉴权。
- 未登录或 token 失效时返回 401，并输出统一的 JSON 错误。

## 前端实现

### 注册页

- 文件位置：`frontend/src/views/RegisterView.vue`
- 提交表单调用 `register` API。
- 注册成功后写入 `bdai_sc_user` 与 `bdai_sc_token` 到 `localStorage`。
- 目前表单只包含 `displayName`、`username`、`password`。

### 登录页

- 文件位置：`frontend/src/views/LoginView.vue`
- 提交表单调用 `login` API。
- 登录成功后写入 `bdai_sc_user` 与 `bdai_sc_token` 到 `localStorage`。

### API 与请求拦截

- API 定义：`frontend/src/api/auth.js`
- Axios 拦截器：`frontend/src/api/request.js`
- 请求拦截器自动附加 `Authorization: Bearer <token>`。
- 响应拦截器捕获 401，清理本地登录态并跳转 `/login`。

### 路由守卫

- 文件位置：`frontend/src/router/index.js`
- 需要登录的页面通过 `meta.requiresAuth` 保护。
- 未登录访问受限页面自动跳转登录。
- `allowedRoles` 做基础角色限制，依赖本地 `bdai_sc_user.role`。

## 相关配置

- JWT 配置：`backend/src/main/resources/application.yml`
- 关键字段：`security.jwt.secret`、`security.jwt.expires-minutes`

