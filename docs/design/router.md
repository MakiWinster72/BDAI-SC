# Vue Router 与路由守卫

## 概述


BDAI-SC 前端采用单文件 Vue Router 配置，通过单一的 beforeEach 守卫强制执行三种不同的导航策略。

## 路由架构概述

路由器采用嵌套路由组合：顶层 `DashboardLayout` 包裹器拥有所有经过身份验证的子路由，而未经验证的页面（登录、注册）则位于根层级。

## 路由定义映射

| 路径 | 名称 | 组件 | 守卫元数据 | 用途 |
|------|------|------|------------|------|
| / | — | DashboardLayout | requiresAuth | 外壳布局（重定向到 /myinfos） |
| /myinfos | myinfos | MyInfosView | (继承) | 学生个人资料编辑器 |
| /notifications | notifications | NotificationsView | (继承) | 审核/审批收件箱 |
| /achievements | achievements | AchievementsView | (继承) | 个人成就管理 |
| /settings | settings | SettingsView | (继承) | 账户与偏好设置 |
| /student-info | student-info | StudentInfoView | allowedRoles: ['TEACHER', 'ADMIN'] | 浏览其他学生的资料 |
| /admin | admin | AdminView | allowedRoles: ['ADMIN'] | 系统管理面板 |
| /login | login | LoginView | guestOnly | 身份验证入口 |
| /register | register | RegisterView | guestOnly | 账户注册 |

## 导航守卫管道

### 检查 1 - 身份验证门控

当路由声明了 `meta.requiresAuth` 时，守卫检查 `localStorage.getItem('gcsc_token')` 是否存在。

### 检查 2 - 访客隔离

带有 `meta.guestOnly` 的路由会将已通过身份验证的用户重定向到 `/myinfos`。

### 检查 3 - 角色授权

带有 `meta.allowedRoles` 的路由会检查 `gcsc_user.role` 是否在允许的角色数组中。

## 守卫行为摘要

| 场景 | 结果 |
|------|------|
| 未认证用户访问 /myinfos | → /login |
| 已认证用户访问 /login | → /myinfos |
| STUDENT 访问 /admin | → /myinfos |
| STUDENT 访问 /student-info | → /myinfos |
| ADMIN 访问任何路由 | ✓ 允许 |

## 状态来源：localStorage 契约

| 键 | 内容 | 守卫用途 |
|----|------|----------|
| gcsc_token | 原始 JWT 字符串 | requiresAuth 检查 |
| gcsc_user | JSON: role, username, displayName | allowedRoles 检查 |

## 双层角色保护

1. **UI 层 (menu.js)** - 过滤菜单项可见性
2. **路由守卫层 (index.js)** - 执行实际的授权检查

## 导航工具函数

`viewTransition.js` 中的 `navigateWithViewTransition` 辅助函数处理应用内导航。

## 后续步骤

- [Axios 请求层](./axios-layer) — JWT 附加与 401 拦截
- [JWT 身份验证流程](./jwt-auth) — Token 生命周期
