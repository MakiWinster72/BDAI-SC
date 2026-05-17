# 动画与过渡模式

## 概述


BDAI-SC 采用分层动画系统，涵盖从可复用的 CSS token 原语到 Vue `<Transition>` 类，再到组件级 composable 编排。

## 动画 Token 架构

| Token | 值 | 用途 |
|-------|-----|------|
| `--duration-fast` | 160ms | 微交互：按钮按下、图标悬停 |
| `--duration-base` | 240ms | 标准 UI 反馈：焦点环、边框颜色 |
| `--duration-slow` | 420ms | 结构性过渡：面板滑入、卡片入场 |
| `--ease-out` | cubic-bezier(0.2, 0.8, 0.2, 1) | 减速 |
| `--ease-page` | cubic-bezier(0.22, 1, 0.36, 1) | 页面级动效 |
| `--ease-dialog` | cubic-bezier(0.22, 1, 0.36, 1) | 弹窗弹出 |

## 关键帧库

| 名称 | 效果 |
|------|------|
| cardEnter | 向上平移 20px + 淡入 |
| floatOrb | 无限循环呼吸效果（16s） |
| glowMove | 背景渐变位置动画 |
| spin | 加载旋转动画 |

## OverlayPanel 组件

两种放置策略：

| 放置 | 入场效果 |
|------|----------|
| bottom（默认） | 从底部向上滑入 translateY(120%) |
| center | 缩放入场 scale(0.94) + translateY(12px) |

## 模式速查表

| 模式 | 缓动 |
|------|------|
| 登录卡片入场 | --ease-out |
| OverlayPanel 底部 | --ease-page |
| 抽屉模态框 | --ease-page |
| Toast 进入 | --ease-page |
| Toast 离开 | ease |
| 按钮悬停 | ease |
| 按钮按下 | ease |

## 后续步骤

- [CSS 架构与约定](./css-architecture) — 样式表组织
- [仪表盘布局与导航](./dashboard-layout) — 布局结构
