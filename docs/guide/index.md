---
title: 介绍
description: BDAI-SC Student Center 使用指南
---

# 介绍

欢迎使用 **BDAI-SC 学生信息管理中心** 文档，为大数据与人工智能学院量身打造。

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端 | Java 21 + Spring Boot | 3.3.5 |
| 前端 | Vue 3 (Composition API) | 3.5.x |
| 数据库 | MySQL | 8.x |
| 认证 | Spring Security + JWT | — |
| 构建 | Maven + Vite | — |

## 核心功能

| 功能 | 说明 |
|------|------|
| 学生档案管理 | 个人信息、教育经历、干部经历、党团关系 |
| 成就记录 | 竞赛、论文、专利、证书、科研、作品、双百、培训 |
| 双轨审核 | 成就提交与档案修改均需教师/管理员审核 |
| 文件上传 | 支持图片、视频、文档等媒体附件 |
| 数据导出 | Excel、PDF、Word 格式导出 |

## 快速开始

推荐阅读顺序：

1. [快速开始](./getting-started) — 本地环境搭建与运行
2. [架构概述](../design/architecture) — 系统整体结构
3. [JWT 认证流程](../design/jwt-auth) — 认证机制
4. [学生档案数据模型](../design/student-profile-model) — 核心数据
5. [多实体成就系统](../design/achievement-system) — 成就管理
