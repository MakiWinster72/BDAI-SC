# 仪表盘布局与导航

## 概述


控制面板是包裹 BDAI-SC 中所有登录后页面的已认证外壳。

## 布局外壳架构

整个已认证体验存在于一个单一的父路由下，其 component 为 `DashboardLayout`。六个子路由（myinfos、achievements、notifications、settings、student-info、admin）都在该外壳内的 `<RouterView />` 中渲染。

## 结构组成

| 组件 | 用途 |
|------|------|
| BrandHeader | 机构标志、个人资料芯片 |
| DashboardSidebar | 侧边栏导航 |
| RouterView | 路由内容区域 |
| AppFooter | 机构链接和版权信息 |
| ToastContainer | 全局通知 |

## BrandHeader 与滚动联动

| 阶段 | 滚动范围 | 视觉行为 |
|------|----------|----------|
| 粘性区域 | 0 – 20vh | 头部完全可见 |
| 滑出区域 | 20vh – 100vh | translateY 和 opacity 按比例变化 |
| 隐藏状态 | > 100vh | 完全隐藏 |

## 侧边栏菜单

| 菜单键 | 标签 | 可见角色 | 路由路径 |
|--------|------|----------|----------|
| my-info | 我的信息 | 所有角色 | /myinfos |
| achievements | 个人成就 | 所有角色 | /achievements?category=all |
| student-info | 学生信息 | TEACHER, ADMIN | /student-info |
| notifications | 通知 | 所有角色 | /notifications |
| admin | 后台管理 | 仅限 ADMIN | /admin |

## CardMenu 子面板系统

三面板导航模型：
- 主菜单面板
- 成就子面板（10 个类别）
- 通知子面板

## 嵌入模式

当使用 `?embed=true` 或 `?embed=1` 访问时：
- BrandHeader 隐藏
- DashboardSidebar 隐藏
- AppFooter 隐藏
- 外壳内边距缩小至 12px

## 后续步骤

- [Vue Router 与路由守卫](./router) — 路由层与守卫
- [动画与过渡模式](./animation) — 侧边栏过渡动画
