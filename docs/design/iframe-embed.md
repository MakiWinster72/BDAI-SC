# 内嵌 Iframe 模式

## 概述


BDAI-SC 应用支持内嵌 iframe 模式，允许将"成就"页面作为简洁、独立的组件嵌入到任何外部 HTML 页面中。

## 工作原理

当访问者通过带有 `?embed=1`（或 `?embed=true`）的 URL 访问成就路由时，布局组件会检测该标志并切换到极简渲染模式。

```javascript
// DashboardLayout.vue
const isEmbedded = computed(() => {
  return route.name === 'achievements' &&
    ['1', 'true'].includes(route.query.embed?.toLowerCase()?.trim())
})
```

## 内嵌模式下隐藏的元素

| UI 元素 | 隐藏方式 | 作用 |
|---------|----------|------|
| BrandHeader | v-if | 顶部品牌栏 |
| DashboardSidebar | v-if | 左侧导航 |
| MobileCapsule | CSS display:none | 移动端悬浮栏 |
| AppFooter | v-if | 页脚 |

## CSS 转换

```css
/* 内嵌模式覆盖 */
.dashboard-shell.dashboard-shell-embedded { padding: 14px; }
.dashboard-layout-embedded { grid-template-columns: 1fr !important; }
```

## 外部页面嵌入示例

```html
<iframe
  src="https://your-domain.com/achievements?embed=1"
  width="100%"
  height="800"
  frameborder="0"
  title="Student Achievements"
></iframe>
```

## 查询参数参考

| 参数 | 接受的值 | 默认值 | 描述 |
|------|----------|--------|------|
| embed | "1", "true" | 未设置 | 激活内嵌模式 |
| category | "all", "paper", "patent"... | "all" | 按类型筛选成就 |

## 快速参考

| 方面 | 详情 |
|------|------|
| 激活 URL | /achievements?embed=1 |
| 隐藏元素 | BrandHeader, DashboardSidebar, MobileCapsule, AppFooter |
| 需要身份验证 | 是 |
| 适用路由 | 仅限 achievements |

## 后续步骤

- [仪表盘布局与导航](./dashboard-layout) — 布局外壳
- [CSS 架构与约定](./css-architecture) — CSS 网格系统
