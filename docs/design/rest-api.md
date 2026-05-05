# REST API 设计

GCSC 后端暴露的是一个无状态的、面向资源的 REST API，基于 Spring Boot 构建，作为 Vue 前端的唯一集成面。每个端点遵循薄 Controller 约定：Controller 校验传入 DTO、委托业务逻辑给 Service 层、用 `ResponseEntity` 包装结果。本文档记录了架构模式、认证策略和开发者扩展或集成系统时交互的完整端点表面。

> 来源：[StudentCenterApplication.java](backend/src/main/java/com/gcsc/studentcenter/StudentCenterApplication.java)、[SecurityConfig.java](backend/src/main/java/com/gcsc/studentcenter/config/SecurityConfig.java)

## 请求处理管道

每个 HTTP 请求在到达任何 Controller 之前都经过一层过滤器链。理解这个管道对于调试认证失败、CORS 问题或定制请求行为至关重要。

1. `GlobalCorsFilter` 在 `Ordered.HIGHEST_PRECEDENCE` 首先执行，处理预检请求并设置 CORS Header
2. `JwtAuthenticationFilter` 提取 Bearer Token，通过 `JwtService` 解析，并在 Spring 的 `SecurityContextHolder` 中填充包含用户名和 `ROLE_{role}` 权限的 `UsernamePasswordAuthenticationToken`
3. `SecurityFilterChain` 检查路由级规则：受保护端点的未认证请求返回 JSON 401 并附 `"未登录或登录已过期"`

> 来源：[GlobalCorsFilter.java](backend/src/main/java/com/gcsc/studentcenter/config/GlobalCorsFilter.java#L13-L59)、[JwtAuthenticationFilter.java](backend/src/main/java/com/gcsc/studentcenter/config/JwtAuthenticationFilter.java#L23-L66)、[SecurityConfig.java](backend/src/main/java/com/gcsc/studentcenter/config/SecurityConfig.java#L24-L42)

## 两种认证策略

该 API 的一个鲜明架构特点是 11 个 Controller 中共存两种认证解析模式。两种策略最终消耗同一个 JWT Token，但它们在如何解析调用者身份和权限方面有所不同。

### 策略 A：Spring Security `Authentication` 注入

六个 Controller——`AuthController`、`StudentProfileController`、`AchievementReviewRequestController`、`ProfileReviewRequestController`、`ReviewSettingsController` 和 `AchievementUploadSettingsController`——接受 Spring 的 `Authentication` 对象作为方法参数，通过 `authentication.getName()` 获取用户名。

```java
// 典型策略 A 模式（来自 StudentProfileController）
@GetMapping("/me")
public ResponseEntity<StudentProfileResponse> me(Authentication authentication) {
    return ResponseEntity.ok(studentProfileService.getProfile(authentication.getName()));
}
```

### 策略 B：手动解析 JWT Header

四个 Controller——`AchievementController`、`AdminController`、`SystemSettingsController` 和 `UploadController`——通过 `@RequestHeader` 手动注入 `Authorization` Header，并自行使用 `JwtService.parseToken()` 解析。这使它们能直接访问 JWT Claims 中 Spring Security 不存储在主体上的信息，最关键的是 `role` Claim。

```java
// 典型策略 B 模式（来自 AdminController）
private boolean isAdmin(String authHeader) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) return false;
    try {
        Claims claims = jwtService.parseToken(authHeader.substring(7));
        return appUserRepository.findByUsername(claims.getSubject())
            .map(user -> user.getRole() == UserRole.ADMIN)
            .orElse(false);
    } catch (JwtException ex) { return false; }
}
```

`UploadController` 直接使用 `SecurityContextHolder.getContext().getAuthentication()` 而非通过 `@RequestHeader` 注入。

| 方面 | 策略 A | 策略 B |
|------|--------|--------|
| **Controller** | Auth、StudentProfile、AchievementReview、ProfileReview、ReviewSettings、AchievementUploadSettings | Achievement、Admin、SystemSettings、Upload |
| **身份来源** | `Authentication` 参数注入 | `@RequestHeader(AUTHORIZATION)` + `JwtService.parseToken()` |
| **角色访问** | 通过 `SecurityContext` 权限（未使用） | 直接 `claims.get("role")` |
| **管理员守卫** | 不适用（Service 层处理） | Controller 中手动 `isAdmin()` 检查 |
| **认证失败** | Spring Security 自动返回 401 | Controller 返回 `HttpStatus.UNAUTHORIZED` / `FORBIDDEN` |

策略 B Controller 在框架级别绕过 Spring Security 的授权机制——它们允许请求通过过滤器链（因为 `/api/achievements/**` 在框架级别被标记为 `permitAll`），但随后在内部强制认证。Controller 自身必须检查 auth header 是否为空并返回 401。

> 来源：[AchievementController.java](backend/src/main/java/com/gcsc/studentcenter/controller/AchievementController.java#L99-L121)、[AdminController.java](backend/src/main/java/com/gcsc/studentcenter/controller/AdminController.java#L49-L77)、[AuthController.java](backend/src/main/java/com/gcsc/studentcenter/controller/AuthController.java#L69-L79)、[UploadController.java](backend/src/main/java/com/gcsc/studentcenter/controller/UploadController.java#L34-L43)、[SystemSettingsController.java](backend/src/main/java/com/gcsc/studentcenter/controller/SystemSettingsController.java#L26-L48)

## 安全配置与路由授权

`SecurityConfig` 类在 API 周围建立了严格的边界。CSRF 被禁用，Session 设置为 `STATELESS`，授权遵循白名单优先方式。

| 路由模式 | HTTP 方法 | 授权 | 用途 |
|---------|---------|------|------|
| `/api/auth/register` | POST | `permitAll` | 用户自助注册（受系统设置门控） |
| `/api/auth/login` | POST | `permitAll` | 基于凭据的身份认证 |
| `/api/settings/system` | GET | `permitAll` | 公开系统设置（注册状态、品牌信息） |
| `/api/achievements/**` | 全部 | `permitAll` | 公开浏览成就（Controller 层强制认证） |
| `/uploads/**` | GET | `permitAll` | 上传媒体的静态文件服务 |
| `OPTIONS /**` | OPTIONS | `permitAll` | CORS 预检处理 |
| 其他所有 `/api/**` | 全部 | `authenticated` | 受保护端点 |

一个关键细节：`/api/achievements/**` 在框架级别标记为 `permitAll`，意味着 Spring Security 不会因 Token 有效性而阻止任何发往成就端点的请求。`AchievementController` 通过在每个请求上手动检查 `Authorization` Header 来补偿，在 Token 缺失或无效时返回 `401`。

> 来源：[SecurityConfig.java](backend/src/main/java/com/gcsc/studentcenter/config/SecurityConfig.java#L24-L42)

## 错误处理契约

`GlobalExceptionHandler`（标注了 `@RestControllerAdvice`）为所有 Controller 提供统一错误响应格式。

| 异常 | HTTP 状态 | 响应结构 |
|------|----------|---------|
| `IllegalArgumentException` | 400 Bad Request | `{"success": false, "message": "<异常消息>"}` |
| `MethodArgumentNotValidException` | 400 Bad Request | `{"success": false, "message": "<字段级校验消息>"}` |

对于 `MethodArgumentNotValidException`，处理程序从绑定结果中提取第一个 `FieldError` 并使用其 `defaultMessage`。若不存在字段错误，回退到通用 `"参数校验失败"`。

未认证请求访问受保护路由时收到自定义 401 响应：

```json
{"success": false, "message": "未登录或登录已过期"}
```

不存在全局回退处理器。若 Service 抛出的 `RuntimeException` 不是 `IllegalArgumentException`，它将作为 500 Internal Server Error 传播。

> 来源：[GlobalExceptionHandler.java](backend/src/main/java/com/gcsc/studentcenter/exception/GlobalExceptionHandler.java#L13-L38)、[SecurityConfig.java](backend/src/main/java/com/gcsc/studentcenter/config/SecurityConfig.java#L29-L33)

## 完整端点参考

### 身份认证 — `/api/auth`

| 方法 | 端点 | 认证 | 描述 |
|------|------|------|------|
| POST | `/api/auth/register` | 否 | 创建新用户账户（遵守注册开关） |
| POST | `/api/auth/login` | 否 | 身份认证并接收 JWT Token + 用户档案 |
| GET | `/api/auth/me` | 是 | 获取当前用户档案 |
| POST | `/api/auth/change-password` | 是 | 修改当前用户密码 |
| GET | `/api/auth/login-history` | 是 | 分页登录历史（page、size 参数） |

> 来源：[AuthController.java](backend/src/main/java/com/gcsc/studentcenter/controller/AuthController.java#L41-L92)

### 学生档案 — `/api/student-profiles`

| 方法 | 端点 | 认证 | 描述 |
|------|------|------|------|
| GET | `/api/student-profiles/me` | 是 | 获取自己的学生档案 |
| GET | `/api/student-profiles/{id}` | 是 | 按 ID 获取档案 |
| PUT | `/api/student-profiles/me` | 是 | 创建或更新自己的档案 |
| PUT | `/api/student-profiles/{id}` | 是 | 按 ID 更新某个档案 |
| GET | `/api/student-profiles/search` | 是 | 多条件分页搜索 |

搜索端点接受 11 个可选查询参数：`page`（默认 1）、`size`（默认 5）、`classYear`、`classNo`、`college`、`major`、`isHk`、`isMo`、`isTw`、`specialStudent`、`specialStudentType`、`studentCategory` 和 `keyword`。返回带 `items`、`page`、`size`、`total` 和 `totalPages` 字段的 `StudentSearchResponse` DTO。

> 来源：[StudentProfileController.java](backend/src/main/java/com/gcsc/studentcenter/controller/StudentProfileController.java#L38-L123)

### 成就 — `/api/achievements`

| 方法 | 端点 | 认证 | 描述 |
|------|------|------|------|
| GET | `/api/achievements` | 手动 | 按类别、studentNo、studentName 列出/筛选成就 |
| GET | `/api/achievements/{category}/{id}` | 手动 | 按类别和 ID 获取单个成就 |
| POST | `/api/achievements/{category}` | 手动 | 在特定类别中创建成就 |
| PUT | `/api/achievements/{category}/{id}` | 手动 | 更新现有成就 |
| DELETE | `/api/achievements/{category}/{id}` | 手动 | 删除成就 |

`{category}` 路径变量对应九种成就类型（如 `paper`、`patent`、`contest`）。所有端点使用策略 B 认证。

> 来源：[AchievementController.java](backend/src/main/java/com/gcsc/studentcenter/controller/AchievementController.java#L16-L122)

### 成就审核 — `/api/achievement-review-requests`

| 方法 | 端点 | 认证 | 描述 |
|------|------|------|------|
| GET | `/api/achievement-review-requests` | 是 | 列出当前用户的所有审核请求 |
| POST | `/api/achievement-review-requests` | 是 | 提交新成就供审核 |
| POST | `/api/achievement-review-requests/{id}/approve` | 是 | 批准待审请求 |
| POST | `/api/achievement-review-requests/{id}/reject` | 是 | 附理由驳回 |
| DELETE | `/api/achievement-review-requests/{id}` | 是 | 取消自己的待审请求 |
| PUT | `/api/achievement-review-requests/{id}/supporting-documents` | 是 | 附加证明材料 |

> 来源：[AchievementReviewRequestController.java](backend/src/main/java/com/gcsc/studentcenter/controller/AchievementReviewRequestController.java#L15-L84)

### 档案审核 — `/api/profile-review-requests`

| 方法 | 端点 | 认证 | 描述 |
|------|------|------|------|
| GET | `/api/profile-review-requests` | 是 | 列出所有档案审核请求 |
| POST | `/api/profile-review-requests` | 是 | 提交档案修改供审核 |
| POST | `/api/profile-review-requests/{id}/approve` | 是 | 批准档案修改 |
| POST | `/api/profile-review-requests/{id}/reject` | 是 | 附理由驳回 |
| DELETE | `/api/profile-review-requests/{id}` | 是 | 取消自己的待审请求 |
| PUT | `/api/profile-review-requests/{id}/supporting-documents` | 是 | 附加证明材料 |

> 来源：[ProfileReviewRequestController.java](backend/src/main/java/com/gcsc/studentcenter/controller/ProfileReviewRequestController.java#L15-L84)

### 管理后台 — `/api/admin`

| 方法 | 端点 | 认证 | 描述 |
|------|------|------|------|
| POST | `/api/admin/users` | 管理员 | 创建新用户 |
| GET | `/api/admin/users` | 管理员 | 分页用户列表（搜索、角色、className 筛选） |
| GET | `/api/admin/users/ids` | 管理员 | 带可选筛选的所有用户 ID 列表 |
| PUT | `/api/admin/users/{id}` | 管理员 | 更新用户详情 |
| DELETE | `/api/admin/users/{id}` | 管理员 | 删除用户账户 |
| GET | `/api/admin/classes` | 管理员 | 列出所有不同班级名 |
| PUT | `/api/admin/teachers/{id}/assigned-classes` | 管理员 | 设置教师的所管理班级 |
| GET | `/api/admin/teachers/{id}/assigned-classes` | 管理员 | 获取教师的所管理班级 |
| GET | `/api/admin/backup/db` | 管理员 | 下载数据库 SQL 转储（二进制） |
| POST | `/api/admin/restore/db` | 管理员 | 从 `.sql` 文件上传恢复数据库 |
| GET | `/api/admin/backup/attachments` | 管理员 | 下载附件为 ZIP（binary） |
| GET | `/api/admin/storage-analysis` | 管理员 | 每人存储使用量报告（前 30 名） |

所有管理员端点使用策略 B 并带 `isAdmin()` 守卫。备份/恢复端点返回二进制载荷（`APPLICATION_OCTET_STREAM`）并带 `Content-Disposition` Header。

> 来源：[AdminController.java](backend/src/main/java/com/gcsc/studentcenter/controller/AdminController.java#L30-L353)

### 文件上传 — `/api/upload`

| 方法 | 端点 | 认证 | 描述 |
|------|------|------|------|
| POST | `/api/upload` | 是 | 上传文件（multipart `file` + 可选 `context`） |

上传文件存储在 `{uploadDir}/{userId}/` 下，通过 `WebConfig` 资源处理器在 `/uploads/{userId}/{filename}` 提供静态服务。

> 来源：[UploadController.java](backend/src/main/java/com/gcsc/studentcenter/controller/UploadController.java#L13-L44)、[WebConfig.java](backend/src/main/java/com/gcsc/studentcenter/config/WebConfig.java#L11-L27)

### 设置 — `/api/settings/*` 和 `/api/admin/settings/*`

| 方法 | 端点 | 认证 | 描述 |
|------|------|------|------|
| GET | `/api/settings/review` | 否 | 获取审核工作流设置 |
| PUT | `/api/settings/review` | 是 | 更新审核设置 |
| GET | `/api/settings/achievement-upload` | 否 | 获取成就上传设置 |
| PUT | `/api/settings/achievement-upload` | 是 | 更新成就上传设置 |
| GET | `/api/settings/system` | 否 | 获取公开系统设置 |
| PUT | `/api/admin/settings/system` | 管理员 | 更新系统设置 |

注意 `SystemSettingsController` 使用基础 `@RequestMapping("/api")` 而非 `/api/settings`，因此其 PUT 端点映射到 `/api/admin/settings/system`。

> 来源：[ReviewSettingsController.java](backend/src/main/java/com/gcsc/studentcenter/controller/ReviewSettingsController.java#L14-L38)、[AchievementUploadSettingsController.java](backend/src/main/java/com/gcsc/studentcenter/controller/AchievementUploadSettingsController.java#L14-L40)、[SystemSettingsController.java](backend/src/main/java/com/gcsc/studentcenter/controller/SystemSettingsController.java#L14-L71)

## DTO 与响应模式

`dto` 包包含 30 个类，构成严格的请求/响应契约。每个 DTO 是一个纯 Java 对象，有构造器和 Getter——无 Lombok，无继承层次结构。

| DTO | 使用方 | 值得注意的字段 |
|-----|--------|--------------|
| `AuthResponse` | 登录 | `success`、`token`、`tokenType`、`username`、`role`、`displayName`、`lastLoginInfo` |
| `StudentProfileResponse` | 档案 CRUD | 含教育/干部经历的完整档案 |
| `StudentSearchResponse` | 搜索 | 手动分页：`items`、`page`、`size`、`total`、`totalPages` |
| `AchievementRecordResponse` | 成就 CRUD | 跨全部 9 种成就类型的统一结构 |
| `UploadResponse` | 上传 | `success`、`url`、`mediaType`、`originalName` |
| `Page<LoginHistoryResponse>` | 登录历史 | Spring Data `Page` 包装器 |

前端通过 `frontend/src/api/` 下 11 个专用 Axios 模块消费这些 DTO，全部构建在共享的 `request.js` 实例之上。

> 来源：[AuthResponse.java](backend/src/main/java/com/gcsc/studentcenter/dto/AuthResponse.java#L3-L91)、[StudentSearchResponse.java](backend/src/main/java/com/gcsc/studentcenter/dto/StudentSearchResponse.java#L5-L45)、[UploadResponse.java](backend/src/main/java/com/gcsc/studentcenter/dto/UploadResponse.java#L3-L31)、[request.js](frontend/src/api/request.js#L1-L36)

## 下一步

了解了 API 表面及其认证模式后，以下页面提供更深入的上下文：

- [安全与 JWT 认证](./jwt-auth) — 深潜水 Token 生成、验证和 JWT 过滤器内部原理
- [数据模型与 JPA 实体](./student-profile-model) — 这些 DTO 底层实体层以及数据如何从 Controller 流到数据库
- [审核与审批工作流](./review-workflow) — 成就和档案审核端点如何参与审批工作流
- [RBAC 角色模型](./rbac) — 角色如何在 Controller 级检查之外强制执行