---
title: 详细设计
description: BDAI-SC Student Center 详细设计文档
---

# 详细设计

本部分包含 BDAI-SC Student Center 的详细设计文档，涵盖架构、认证、数据模型、API 设计等核心主题。

## 后端核心

### 架构

- [架构概述](./architecture) — 系统整体架构与技术栈
- [JWT 认证流程](./jwt-auth) — 身份验证生命周期
- [基于角色的访问控制](./rbac) — 三级权限模型

### API 与数据

- [REST API 设计](./rest-api) — 九个控制器的端点参考
- [学生档案数据模型](./student-profile-model) — 核心实体与 DTO
- [多实体成就系统](./achievement-system) — 九种成就类型
- [审核与审批工作流](./review-workflow) — 双轨审核系统
- [数据库 Schema 约定](./database-schema) — 表结构、命名规则

### 运维

- [管理面板与备份](./admin-backup) — 用户管理与数据备份

## 前端核心

- [Vue Router 与路由守卫](./router) — 导航守卫与权限控制
- [Axios 请求层](./axios-layer) — HTTP 客户端与拦截器
- [仪表盘布局与导航](./dashboard-layout) — Shell 布局与侧边栏
- [成就编辑器组合式函数](./achievement-editor) — 表单状态机
- [学生档案导出工具](./export-tools) — Excel / PDF 导出

## 设计与体验

- [设计规范](./design-spec) — 色彩系统、字体排版、按钮卡片、动画、布局等完整设计规范
- [CSS 架构与约定](./css-architecture) — 样式系统与命名规范
- [动画与过渡模式](./animation) — 动画 token 与过渡类
- [内嵌 Iframe 模式](./iframe-embed) — 成就页嵌入外部页面

## 参考

- [配置参考](./config-reference) — 所有配置项汇总
- [项目结构](./project-structure) — 目录组织
- [快速开始](./quick-start) — 快速启动指南
