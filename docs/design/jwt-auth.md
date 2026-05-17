# 安全与 JWT 身份认证

本系统通过无状态 JWT 架构保障每个请求的安全——无服务端会话，无 Cookie。用户从登录表单提交凭据到 Token 过期瞬间，栈中每一层都强制执行单一事实来源：签名后的 JWT 载荷。本文追踪跨越四个协作层的完整认证生命周期——Spring Security 过滤器链、JWT 服务、认证业务逻辑和前端的拦截器-守卫对——展示一个 `Bearer` Token 如何将一个匿名 HTTP 请求转变为经过认证的 `SecurityContext` 主体。

> 来源：[SecurityConfig.java](backend/src/main/java/com/gcsc/studentcenter/config/SecurityConfig.java#L1-L49)、[JwtAuthenticationFilter.java](backend/src/main/java/com/gcsc/studentcenter/config/JwtAuthenticationFilter.java#L1-L67)、[JwtService.java](backend/src/main/java/com/gcsc/studentcenter/service/JwtService.java#L1-L49)

## 架构概览

认证系统组织为四个明确分工的层次。**Spring Security 过滤器链**作为守门人，决定哪些请求可以未认证通过、哪些必须携带有效 Token。**JWT 服务**处理签发和验证 Token 的加密工作。**认证服务**封装所有凭据相关业务逻辑——注册、登录、修改密码。最后**前端层**配对 Axios 请求拦截器（为每个出站请求附加 Token）和 Vue Router 导航守卫（强制客户端路由保护）。

> 来源：[SecurityConfig.java](backend/src/main/java/com/gcsc/studentcenter/config/SecurityConfig.java#L24-L42)、[GlobalCorsFilter.java](backend/src/main/java/com/gcsc/studentcenter/config/GlobalCorsFilter.java#L13-L15)、[request.js](frontend/src/api/request.js#L1-L36)

## 安全过滤器链配置

`SecurityConfig` 在一个 `@Bean` 方法中确立整个后端安全态势。明确禁用 CSRF 防护，因为系统基于 Token 而非 Cookie——不存在可供攻击者伪造的 Cookie。会话管理设置为 `STATELESS`，确保 Spring Security 永不创建 `HttpSession`。自定义 `authenticationEntryPoint` 替换默认的 HTML 重定向行为，在未认证请求访问受保护端点时返回结构化 JSON 响应：`{"success":false,"message":"未登录或登录已过期"}`，HTTP 状态码 401。

过滤器链明确将 `JwtAuthenticationFilter` 插入 `UsernamePasswordAuthenticationFilter` 之前，在 Spring Security 内置表单登录机制有机会运行之前拦截每个请求。端点白名单使用 `requestMatchers` 授予五组路由未认证访问权限：认证端点（`/api/auth/register`、`/api/auth/login`）、系统设置公开端点（`/api/settings/system`）、所有上传文件资源（`/uploads/**`）和所有成就公开视图（`/api/achievements/**`）。

`JwtAuthenticationFilter` 通过 `addFilterBefore` 而非 `addFilterAt` 添加。这确保它对每个请求都执行——而非仅对通常触发基于表单认证的请求执行。结合 `STATELESS` 会话管理，这创建了一种纯粹的 Token 门控架构，过滤器是**唯一**填充 `SecurityContext` 的机制。

> 来源：[SecurityConfig.java](backend/src/main/java/com/gcsc/studentcenter/config/SecurityConfig.java#L24-L42)

## CORS 过滤器——第一道防线

`GlobalCorsFilter` 在 `Ordered.HIGHEST_PRECEDENCE` 上运行，在链中每个其他过滤器之前执行，包括 Spring Security 内置的 CORS 处理。它从每个传入请求的 `Origin` Header 中读取来源，与 `security.cors.allowed-origins` 配置属性（默认为 `*`）比对。当通配符激活时，设置 `Access-Control-Allow-Origin: *` 并附带 `Access-Control-Allow-Credentials: false`。当配置了特定来源时，执行精确匹配或子域通配符匹配（如 `https://*.example.com`），并为匹配的来源启用凭证。

关键的是，过滤器通过立即返回 HTTP 200 来短路 `OPTIONS` 预检请求，防止它们到达安全过滤器链或任何 Controller。这意味着 CORS 协商在认证被评估之前就完成了。允许的方法硬编码为 `GET, POST, PUT, DELETE, OPTIONS`，允许的 Header 限制为 `Authorization, Content-Type`。

> 来源：[GlobalCorsFilter.java](backend/src/main/java/com/gcsc/studentcenter/config/GlobalCorsFilter.java#L13-L59)

## JWT Token 结构与生命周期

`JwtService` 刻意保持最小化——仅两个公开方法。`generateToken` 创建带四个 Claims 的签名 JWT：`sub`（用户名）、`displayName`、`role` 和标准 `iat`/`exp` 时间戳。签名密钥通过 `Keys.hmacShaKeyFor()` 从 `security.jwt.secret` 配置属性派生，将 UTF-8 字符串转换为适合 HMAC-SHA 的 `SecretKey`。Token 在 `security.jwt.expires-minutes`（默认为 120 分钟）后过期。

`parseToken` 反向执行此过程：验证签名、检查过期时间并返回解析后的 `Claims` 载荷。任何篡改或过期都会抛出 `JwtException`，过滤器静默捕获——清除 `SecurityContext` 而非返回错误，允许下游安全规则统一处理未认证状态。

| 组成部分 | Claim / 字段 | 来源 | 示例 |
|---------|-------------|------|------|
| **Header** | `alg` | `signWith()` 固定 | HS256 |
| **Payload** | `sub` | `username` 参数 | `2024001` |
| **Payload** | `displayName` | `displayName` 参数 | `张三` |
| **Payload** | `role` | `role.name()` | `STUDENT` |
| **Payload** | `iat` | `Instant.now()` | Unix 时间戳 |
| **Payload** | `exp` | `now + expiresMinutes` | Unix 时间戳（+120 分钟） |
| **Signature** | HMAC-SHA | `Keys.hmacShaKeyFor(secret)` | Base64 编码 |

当前实现中没有 Refresh Token 机制。当 Token 在 120 分钟后过期，用户必须重新认证。修改密码端点不会使现有 Token 失效，这意味着单纯修改密码不会终止活跃会话。

> 来源：[JwtService.java](backend/src/main/java/com/gcsc/studentcenter/service/JwtService.java#L15-L48)

## 请求级认证过滤器

`JwtAuthenticationFilter` 继承 `OncePerRequestFilter`，保证其 `doFilterInternal` 对每个 HTTP 请求恰好执行一次（无论调度器类型是 forward、include 还是 async）。过滤器遵循严格的决策顺序：首先，读取 `Authorization` Header。若 Header 缺失或不以 `"Bearer "` 开头，立即调用 `filterChain.doFilter()` 并返回——这是静默透传，不是错误。若存在 Bearer Token，调用 `jwtService.parseToken()` 提取并验证 Claims。

解析成功时，过滤器提取 `sub`（用户名）和 `role` Claims，构造带 `ROLE_` 前缀的 `SimpleGrantedAuthority`（如 `ROLE_ADMIN`），并将一切包装在 `UsernamePasswordAuthenticationToken` 中。然后将此 Token 存储在 `SecurityContextHolder.getContext()` 中，使认证主体可通过 `@AuthenticationPrincipal` 或 `Authentication.getName()` 对所有下游 Controller 可用。若 `parseToken` 抛出 `JwtException`（过期、篡改、格式错误），过滤器静默捕获，调用 `SecurityContextHolder.clearContext()` 并继续过滤器链——请求以匿名身份继续。

> 来源：[JwtAuthenticationFilter.java](backend/src/main/java/com/gcsc/studentcenter/config/JwtAuthenticationFilter.java#L32-L65)

## 注册流程

注册是 `AuthService` 中一个 `@Transactional` 方法，在一次原子操作中同时创建 `AppUser` 和关联的 `StudentProfile`。流程从校验开始：`displayName` 在去空格后非空检查，`username` 用正则表达式 `^[a-zA-Z0-9_]{4,32}$` 校验，唯一性通过 `appUserRepository.existsByUsername()` 强制。`RegisterRequest` DTO 通过 Jakarta Bean Validation 注解（`@NotBlank`、`@Size`、`@Pattern`）添加第二层校验，在 Controller 方法体执行前先运行。

校验成功后，密码用 `BCryptPasswordEncoder` 哈希，用户实体以固定学院值 `"大数据与人工智能学院"` 持久化。随后创建 `StudentProfile` 并与用户建立链接——这个一对一关系在注册时就已建立，意味着每个用户立即拥有一个可编辑的档案骨架。方法以生成 JWT 并返回捆绑了 Token 和所有用户元数据（username、displayName、role、studentNo、className、college、avatarUrl）的 `AuthResponse` 结束。

Controller 层在调用 `AuthService.register()` 之前额外做一次检查：查询 `SystemSettingsService.isRegistrationAllowed()`，若注册在系统级别被禁用，返回 HTTP 403 和 `{"success":false,"message":"当前未开放注册"}`。这让管理员可以在运行时开关自助注册而无需重新部署。

| 校验目标 | 规则 | 强制层 |
|---------|------|--------|
| `displayName` | `@NotBlank`，`@Size(2-64)` | DTO + 手动去空格检查 |
| `username` | `@NotBlank`，`@Size(4-32)`，`@Pattern(^[a-zA-Z0-9_]{4,32}$)` | DTO + 手动正则检查 |
| `password` | `@NotBlank`，`@Size(6-32)` | 仅 DTO |
| `role` | `STUDENT` / `TEACHER` / `ADMIN` / `CADRE` | `parseRole()` 回退到 `STUDENT` |
| `username` 唯一性 | 数据库约束 | `existsByUsername()` |
| 注册开关 | 系统设置 | Controller 门控（`isRegistrationAllowed`） |

> 来源：[AuthService.java](backend/src/main/java/com/gcsc/studentcenter/service/AuthService.java#L46-L103)、[AuthController.java](backend/src/main/java/com/gcsc/studentcenter/controller/AuthController.java#L41-L48)、[RegisterRequest.java](backend/src/main/java/com/gcsc/studentcenter/dto/RegisterRequest.java#L1-L83)

## 登录流程

登录端点遵循防止用户名枚举的设计：`username not found` 和 `wrong password` 两种情况返回完全相同的错误消息 `"用户名或密码错误"`。在提取客户端 IP（通过 `X-Forwarded-For` → `X-Real-IP` → `request.getRemoteAddr()` 回退链）和 `User-Agent` 后，服务校验用户名格式、从数据库检索用户，并使用 `PasswordEncoder.matches()` 将提交密码与存储的 BCrypt 哈希比对。

登录成功时，服务查询 `LoginHistoryService.getPreviousLogin()` 以获取用户前一次会话的元数据（IP、设备名、时间戳），然后记录当前登录。响应同时包含新 JWT 和 `lastLoginInfo` 对象，使前端能显示"上次登录：设备 X，时间 Y"的通知。`roleOrDefault` 辅助方法确保任何未显式分配角色的用户都被视为 `STUDENT`，为遗留数据提供安全的回退。

> 来源：[AuthService.java](backend/src/main/java/com/gcsc/studentcenter/service/AuthService.java#L105-L140)、[AuthController.java](backend/src/main/java/com/gcsc/studentcenter/controller/AuthController.java#L50-L57)、[AuthController.java](backend/src/main/java/com/gcsc/studentcenter/controller/AuthController.java#L82-L92)

## 补充认证端点

除注册和登录外，`AuthController` 还暴露三个补充端点来完成认证面。`GET /api/auth/me` 接收注入的 `Authentication` 对象（由 JWT 过滤器填充），返回新鲜的 `UserProfileResponse`——这是前端无需新登录即可刷新过期用户元数据的机制。`POST /api/auth/change-password` 在哈希并持久化新密码之前校验旧密码，但**不会使任何现有 JWT Token 失效**。

`GET /api/auth/login-history` 返回分页的登录记录列表，通过 `authentication.getName()` 自动限定为当前认证用户。底层 `LoginHistoryService` 将 `User-Agent` 字符串解析为人类可读的设备名、操作系统和浏览器，使用手工字符串匹配。定时 Cron 作业（`0 0 3 * * ?`，每天凌晨 3 点）自动清理超过 30 天的登录记录。

| 端点 | 方法 | 需要认证 | 用途 |
|------|------|---------|------|
| `/api/auth/register` | POST | 否 | 创建账户 + 颁发 JWT |
| `/api/auth/login` | POST | 否 | 验证凭据 + 颁发 JWT |
| `/api/auth/me` | GET | 是 | 刷新当前用户档案 |
| `/api/auth/change-password` | POST | 是 | 更新密码（Token 仍然有效） |
| `/api/auth/login-history` | GET | 是 | 分页登录审计记录 |

> 来源：[AuthController.java](backend/src/main/java/com/gcsc/studentcenter/controller/AuthController.java#L59-L80)、[LoginHistoryService.java](backend/src/main/java/com/gcsc/studentcenter/service/LoginHistoryService.java#L21-L87)

## 前端 Token 持久化与注入

前端将认证状态存储在两个 `localStorage` 键中：`bdai_sc_token` 保存原始 JWT 字符串，`bdai_sc_user` 保存包含用户档案元数据（`username`、`displayName`、`avatarUrl`、`role`、`studentNo`、`className`、`college`）的 JSON 对象。这种双键设计让路由守卫无需解析 JWT 即可检查认证状态，而 Axios 拦截器将原始 Token 注入 HTTP Header。

`LoginView.vue` 组件编排登录后存储序列：成功的 `login()` API 调用返回后，将用户对象写入 `bdai_sc_user`（306–317 行），将 Token 写入 `bdai_sc_token`（318 行），然后执行 `router.push("/myinfos")` 导航。如果响应包含 `lastLoginInfo`，显示带有前一次会话设备、IP 和时间戳的 Toast 通知。首次登录（无前一次会话）显示欢迎消息，引导用户填写档案。

> 来源：[LoginView.vue](frontend/src/views/LoginView.vue#L282-L326)、[request.js](frontend/src/api/request.js#L12-L18)

## Axios 拦截器层

`request.js` 中的 Axios 实例应用两个拦截器，构成前端的 HTTP 层安全边界。**请求拦截器**在每个出站请求上读取 `bdai_sc_token` 并附加为 `Authorization: Bearer <token>` Header。**响应拦截器**捕获任何 HTTP 401 响应——Token 过期、无效或缺失的通用信号——并按顺序执行三个操作：从 `localStorage` 删除 `bdai_sc_token` 和 `bdai_sc_user`，检查用户是否已在 `/login` 页面（避免无限重定向循环），并通过 `window.location.href = '/login'` 强制硬导航。

使用 `window.location.href` 而非 Vue Router 的 `router.push()` 是有意为之：拦截器运行在 Vue 组件生命周期之外，全页重新加载确保所有内存状态（响应式存储、组件实例）被干净重置。API 基础 URL 由环境变量决定（生产环境为 `VITE_API_BASE`，开发环境为空字符串以使用 Vite 代理），在前后端以独立端口运行的配置中回退到 `http://{hostname}:{VITE_BACKEND_PORT}`。

> 来源：[request.js](frontend/src/api/request.js#L1-L36)

## Vue Router 导航守卫

`router/index.js` 中的 `beforeEach` 守卫强制三类路由保护。**需要认证的路由**（根路径 `/` 下所有带 `meta: { requiresAuth: true }` 的子路由）在 `bdai_sc_token` 缺失时重定向到 `/login`。**仅游客路由**（`/login`、`/register` 带 `meta: { guestOnly: true }`）在已认证用户访问时重定向到 `/myinfos`。**角色受限路由**将 `allowedRoles` 元数组与存储在 `bdai_sc_user` 中的用户角色比对，在缺失时回退到 `STUDENT`。

| 路由 | 守卫类型 | 条件 | 重定向 |
|------|---------|------|--------|
| `/` 及所有子路由 | `requiresAuth` | 无 `bdai_sc_token` | `/login` |
| `/student-info` | `allowedRoles: ['TEACHER', 'ADMIN']` | 角色不在列表中 | `/myinfos` |
| `/admin` | `allowedRoles: ['ADMIN']` | 角色不是 ADMIN | `/myinfos` |
| `/class-reviews` | `allowedRoles: ['CADRE']` | 角色不是 CADRE | `/myinfos` |
| `/login`, `/register` | `guestOnly` | 存在 `bdai_sc_token` | `/myinfos` |
| `/register` | 注册开关 | `gcsc_allowRegistration !== '1'` | `/login` |

注册开关是服务器端 `isRegistrationAllowed()` 检查的客户端补充。`LoginView.vue` 在挂载时获取系统设置并将 `allowRegistration` 标志缓存到 localStorage 为 `gcsc_allowRegistration`。路由守卫读取此缓存值以决定是否允许导航到 `/register`，当注册关闭时防止用户仅通过直接 URL 输入到达注册表单。

> 来源：[router/index.js](frontend/src/router/index.js#L58-L80)、[LoginView.vue](frontend/src/views/LoginView.vue#L263-L273)

## 错误处理策略

认证错误遵循统一响应契约。`GlobalExceptionHandler` 捕获两种异常类型：`IllegalArgumentException`（由 `AuthService` 抛出，违反业务规则）返回 HTTP 400 及 `{"success": false, "message": "..."}`，`MethodArgumentNotValidException`（由 DTO 上的 `@Valid` 触发）返回 HTTP 400 及第一个字段级校验消息。Token 相关失败完全绕过异常处理器——由 JWT 过滤器的静默捕获块和自定义 `authenticationEntryPoint` 处理，返回 HTTP 401。

| 异常类型 | HTTP 状态 | 响应体 | 来源 |
|---------|----------|--------|------|
| `IllegalArgumentException` | 400 | `{"success": false, "message": "用户名或密码错误"}` | `AuthService.login()` / `register()` |
| `MethodArgumentNotValidException` | 400 | `{"success": false, "message": "<第一个字段错误>"}` | DTO 上的 `@Valid` |
| 缺失/无效 JWT | 401 | `{"success":false,"message":"未登录或登录已过期"}` | `SecurityConfig.authenticationEntryPoint` |
| 注册已关闭 | 403 | `{"success": false, "message": "当前未开放注册"}` | `AuthController.register()` |

> 来源：[GlobalExceptionHandler.java](backend/src/main/java/com/gcsc/studentcenter/exception/GlobalExceptionHandler.java#L13-L38)、[SecurityConfig.java](backend/src/main/java/com/gcsc/studentcenter/config/SecurityConfig.java#L29-L33)、[AuthController.java](backend/src/main/java/com/gcsc/studentcenter/controller/AuthController.java#L41-L48)

## 配置参考

所有认证相关设置都外部化在 `application.yml` 的 `security` 命名空间下。JWT 密钥在编码为 UTF-8 时必须至少 256 位（32 字节）以满足 HMAC-SHA256 密钥长度要求。过期时间以分钟指定，CORS 来源支持通配符 `*` 和逗号分隔的模式（含可选子域通配符）。

| 属性 | 默认值 | 描述 |
|------|--------|------|
| `security.jwt.secret` | `gcsc-student-center-jwt-secret-key-change-in-production-2026` | HMAC-SHA 签名密钥（≥256 位） |
| `security.jwt.expires-minutes` | `120` | Token 生命周期（分钟） |
| `security.cors.allowed-origins` | `*` | 逗号分隔来源白名单；支持 `*` 和 `https://*.domain.com` 模式 |

> 来源：[JwtService.java](backend/src/main/java/com/gcsc/studentcenter/service/JwtService.java#L21-L27)、[GlobalCorsFilter.java](backend/src/main/java/com/gcsc/studentcenter/config/GlobalCorsFilter.java#L17-L18)

## 继续探索

认证层端到端映射完成后，下一步自然是通过嵌入每个 JWT Token 中的 `role` Claim 理解它如何驱动整个 RBAC 系统上基于角色的授权决策。了解后端视角——`@AuthenticationPrincipal` 和角色检查如何传播到 Controller——继续读 [REST API 设计](./rest-api)。了解前端 Axios 实例和错误传播策略如何与认证层协同工作，读 [Axios 请求层](./axios-layer)。查看全部四个角色到其允许操作的完整映射，移步 [RBAC 角色模型](./rbac)。