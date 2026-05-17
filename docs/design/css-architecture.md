# CSS 架构与约定

## 概述


本页面记录了指导 BDAI-SC 前端所有样式表的架构决策、命名模式和结构约定。

## 架构概述

CSS 架构遵循**集中式全局样式表 + 作用域组件覆盖**模型：
- 绝大多数样式位于 `frontend/src/styles.css`（5311 行）
- Vue 组件仅在需要严格封装时使用 `<style scoped>`

## 设计令牌系统

```css
:root {
  --bg-deep: #036b72;      /* 主按钮、激活状态 */
  --bg-mid: #a5ffd6;       /* 渐变中点 */
  --bg-soft: #cdfff9;      /* 页面背景 */
  --card: rgba(3,107,114,0.78);  /* 卡片背景 */
  --danger: #b35b6a;       /* 错误状态 */
  --success: #2f8f75;       /* 成功反馈 */
}
```

## styles.css 内部的文件组织

| 分区 | 大致行号 | 范围 |
|------|----------|------|
| :root 令牌 + 重置 | 1 – 44 | 设计令牌、盒模型 |
| Auth 布局 | 46 – 281 | .auth-layout、表单基元 |
| Dashboard 外壳 | 283 – 399 | .dashboard-shell、胶囊栏 |
| 卡片系统 | 461 – 598 | 毛玻璃卡片效果 |
| 菜单与导航 | 621 – 1227 | 面板过渡动画 |
| 成就卡片 | 1661 – 1800 | 媒体画廊 |
| 响应式断点 | 4378 – 4599+ | @media 查询 |

## 命名约定

域前缀命名约定：`{domain}-{element}` 或 `{domain}-{element}--{modifier}`

| 前缀 | 域 | 示例 |
|------|-----|------|
| auth- | 登录与注册 | .auth-layout、.auth-card |
| dashboard- | 外壳布局 | .dashboard-shell、.dashboard-layout |
| menu- | 侧边栏导航 | .menu-card、.menu-item |
| achievement- | 成就功能 | .achievement-grid |
| form- | 共享表单 | .form-row、.form-label |
| ghost-/action- | 按钮基元 | .ghost-button、.action-button |

## 布局模式

- 完全基于 **CSS Grid** 构建主要布局
- **Flexbox** 用于行内组件对齐
- 无基于浮动的布局

### Dashboard 网格外壳

```css
.dashboard-layout {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 18px;
  align-items: start;
}
```

## 毛玻璃卡片系统

四个卡片类共享基础定义：

```css
.profile-card, .menu-card, .composer-card, .panel-card {
  border-radius: 20px;
  background: linear-gradient(145deg, rgba(255,255,255,0.72), ...);
  backdrop-filter: blur(12px) saturate(115%);
}
```

## 响应式设计策略

桌面优先，具有四个主要断点：

| 断点 | 关键变化 |
|------|----------|
| ≤ 1080px | 侧边栏缩窄、三列变两列 |
| ≤ 840px | 仪表盘变单列、胶囊栏出现 |
| ≤ 720px | 学生信息垂直堆叠 |
| ≤ 480px | Auth 卡片全宽 |

## 动画约定

| 持续时间 | 用途 |
|----------|------|
| 120–180ms | 微反馈：悬停、按下 |
| 200–400ms | 可见性过渡 |
| 450–900ms | 结构性运动：抽屉、模态框 |

## 后续步骤

- [动画与过渡模式](./animation) — 动态导航体验
- [架构概述](./architecture) — 更广泛的应用结构
