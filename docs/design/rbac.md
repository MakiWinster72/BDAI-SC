# 基于角色的访问控制

## 概述


本页面介绍了 BDAI-SC 如何在后端 API 和前端 UI 中实施基于角色的权限控制。

## 角色层级与定义

| 角色 | 作用域 | 典型职责 |
|------|--------|----------|
| STUDENT | 仅限个人数据 | 提交成就和个人资料变更以供审核，管理个人记录 |
| TEACHER | 所有学生数据 + 审核 | 查看/搜索任意学生的个人资料和成就，批准或驳回审核请求 |
| ADMIN | 完整系统访问权限 | 拥有 TEACHER 的所有权限，外加用户管理、备份/恢复以及审核设置 |

> 来源：UserRole.java、AppUser.java

## JWT 声明嵌入

角色在创建令牌时进入授权管道：

1. `JwtService.generateToken()` 将角色作为自定义声明写入 JWT 载荷
2. `JwtAuthenticationFilter` 从已验证的令牌中提取 role 声明
3. 封装为带有 `ROLE_` 前缀的 `SimpleGrantedAuthority`（如 `ROLE_ADMIN`）
4. 设置到 `SecurityContextHolder` 中

> 来源：JwtService.java、JwtAuthenticationFilter.java

## 后端授权层

### 第一层 —— Spring Security URL 过滤

`SecurityConfig.java` 定义了基础的 URL 策略：

- `permitAll()`: `/api/auth/register`、`/api/auth/login`、`/uploads/**`、`/api/achievements/**`
- `authenticated()`: 所有其他端点

### 第二层 —— 控制器中的手动角色校验

`AdminController` 提供了最直观的角色实施示例。每个端点调用 `isAdmin()` 辅助方法，验证失败返回 403 Forbidden。

::: tip 设计冗余
`isAdmin()` 执行自行 JWT 解析，而不是从 `SecurityContextHolder` 读取。这是一种刻意的冗余设计。
:::

### 第三层 —— 服务级角色逻辑

| 授权模式 | 允许的角色 | 应用位置 |
|----------|-----------|----------|
| 审核人门控 | TEACHER, ADMIN | AchievementReviewRequestService、ProfileReviewRequestService |
| 特权数据访问 | TEACHER, ADMIN | AchievementService（搜索/浏览他人的记录） |
| 仅限管理员设置 | ADMIN | ReviewSettingsService |
| 仅限学生提交 | STUDENT | 审核提交端点 |
| 管理员控制器守卫 | ADMIN | AdminController（全部 6 个端点） |

> 来源：SecurityConfig.java、AdminController.java、AchievementReviewRequestService.java

## 前端路由守卫与菜单可见性

### 路由级守卫

```javascript
// meta: { allowedRoles: ['TEACHER', 'ADMIN'] }
```

守卫从 `localStorage`（`gcsc_user` 对象）中读取用户角色，与 `to.meta.allowedRoles` 比对。

> 来源：index.js

### 菜单可见性过滤

`CardMenu.vue` 通过 `filterMenuItemsByRole(props.profile.role)` 计算可见项。

```javascript
// menu.js
ROLE_MENU_VISIBILITY = {
  "student-info": new Set(['TEACHER', 'ADMIN']),
  "admin": new Set(['ADMIN'])
}
```

> 来源：CardMenu.vue、menu.js

## 角色数据的端到端流转

1. **注册** — `AuthService.parseRole()` 验证并默认设为 STUDENT
2. **令牌生成** — `jwtService.generateToken()` 嵌入角色声明
3. **前端存储** — 角色存储在 `localStorage.gcsc_user.role`
4. **每次请求** — `JwtAuthenticationFilter` 提取 role，创建 `SimpleGrantedAuthority("ROLE_" + role)`
5. **服务级实施** — 从 `AppUser` 实体读取角色并应用授权模式

::: warning 安全边界
前端守卫只是为了提升用户体验，而非安全边界。后端在每次请求时都会独立从 JWT 验证角色。
:::

## 扩展角色系统

添加新的受保护功能时的检查清单：

| 步骤 | 位置 | 操作内容 |
|------|------|----------|
| 1 | `frontend/src/router/index.js` | 添加 `meta: { allowedRoles: [...] }` |
| 2 | `frontend/src/constants/menu.js` | 添加到 `ROLE_MENU_VISIBILITY` |
| 3 | 控制器 | 添加 `isAdmin()` 或 `isReviewer()` 门控 |
| 4 | 服务 | 根据 `user.getRole()` 检查权限 |

## 下一步阅读

- [JWT 身份验证流程](./jwt-auth) — 令牌创建与验证的详细演练
- [Axios 请求层](./axios-layer) — 前端如何将令牌附加到每次请求
- [Vue Router 与路由守卫](./router) — 完整路由配置
