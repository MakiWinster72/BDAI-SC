# 设计规范

本文档是 BDAI-SC 学生中心的完整设计规范，涵盖色彩系统、字体排版、组件样式、动画规范等前端设计决策，是开发者和设计者的核心参考。

## 色彩系统

### 主色调（紫粉渐变）

系统采用 **Purple & Pink** 主色调，以深紫色 `#640c72` 为核心，衍生出一套完整的色彩体系。

| CSS 变量 | 色值 | 用途 |
|---------|------|------|
| `--primary` | `#640c72` | 主品牌色，用于按钮、链接、强调元素 |
| `--primary-light` | `#ff85bb` | 浅粉紫，hover 状态、渐变端点 |
| `--primary-dark` | `#4a0956` | 深紫，用于标题、重要文字、hover 端点 |
| `--primary-glow` | `rgba(100,12,114,0.3)` | 发光效果，box-shadow 氛围 |
| `--primary-surface` | `rgba(100,12,114,0.06)` | 极淡背景色，表单项、hover 背景 |

### 语义化别名

| CSS 变量 | 色值 | 用途 |
|---------|------|------|
| `--accent` | `var(--primary)` | 兼容性别名 |
| `--accent-strong` | `var(--primary-dark)` | 兼容性别名 |

### 背景渐变

| CSS 变量 | 色值 | 用途 |
|---------|------|------|
| `--bg-base` | `#f5f5f5` | 页面底色 |
| `--bg-deep` | `#640c72` | 渐变深端 |
| `--bg-mid` | `#ff85bb` | 渐变中端 |
| `--bg-soft` | `#ffcee3` | 渐变浅端，暖色调过渡 |

### 玻璃态与线条

| CSS 变量 | 色值 | 用途 |
|---------|------|------|
| `--card` | `rgba(255,255,255,0.82)` | 玻璃态卡片背景（backdrop-filter: blur） |
| `--card-hover` | `rgba(255,255,255,0.95)` | 卡片 hover 态 |
| `--line` | `rgba(100,12,114,0.1)` | 默认边框/分隔线 |
| `--line-strong` | `rgba(100,12,114,0.18)` | 强化边框/输入框边框 |

### 文本颜色

| CSS 变量 | 色值 | 用途 |
|---------|------|------|
| `--text-main` | `#2d1a3e` | 主文本，深紫色 |
| `--text-sub` | `#5a6a8a` | 次要文本，灰蓝色 |
| `--text-inverse` | `#ffffff` | 反色文本 |

### 状态色

| CSS 变量 | 色值 | 用途 |
|---------|------|------|
| `--danger` | `#c0392b` | 危险/错误/拒绝状态 |
| `--success` | `#27ae60` | 成功状态 |

### 阴影

| CSS 变量 | 用途 |
|---------|------|
| `--shadow` | `0 8px 32px rgba(100,12,114,0.08)` — 默认卡片阴影 |
| `--shadow-sm` | `0 4px 16px rgba(100,12,114,0.06)` — 轻量阴影 |
| `--shadow-glow` | `0 0 40px rgba(255,133,187,0.25)` — 发光效果 |

---

## 字体系统

### 字体族

系统使用 **鸿蒙黑体（HarmonyOS Sans SC）** 作为中文字体。对于英文和数字，使用系统默认无衬线字体。

```css
font-family: "HarmonyOS Sans SC", -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
```

### 字号规范

| 用途 | 字号 | 行高 | 字重 |
|------|------|------|------|
| 页面大标题 | 28px | 1.2 | 800 |
| 区块标题 | 20–22px | 1.3 | 700 |
| 卡片标题 | 16px | 1.35 | 700 |
| 正文 | 14px | 1.6–1.7 | 400/600 |
| 辅助文字 | 12–13px | 1.4–1.5 | 400/600 |
| 标签/小字 | 11–12px | 1.3 | 600–700 |

---

## 按钮系统

### 设计原则

- hover 时 `translateY(-1px)` 配合阴影提升，active 时 `translateY(0)` 提供按下感
- disabled 时 `opacity: 0.5`，`cursor: not-allowed`
- 所有过渡使用 `180ms ease` 作为标准时长

### 幽灵按钮（Ghost Button）

`.ghost-button` — 次要操作，描边风格，用于表单、对话框中的次要操作。

```css
border: 1px solid var(--line-strong);
background: var(--card);
color: var(--primary);
border-radius: 12px;
height: 40px;
padding: 0 20px;
```

尺寸变体：
- `.ghost-button--full`：高度 50px，用于表单底部
- `.ghost-button--xs`：高度 32px，用于关闭按钮等超小场景

### 主要操作按钮（Action Button）

`.action-button` — 主要操作，渐变填充，用于表单提交、重要操作入口。

```css
background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
color: #ffffff;
border-radius: 12px;
height: 40px;
box-shadow: 0 4px 14px rgba(100, 12, 114, 0.3);
```

尺寸变体：`.action-button--full` 高度 50px，配 inset 高光。

### 通用工具栏按钮（Btn）

`.btn` / `.btn-primary` / `.btn-ghost` — 管理后台工具栏操作，高度 38px，最小宽度 96px。

危险按钮：`.btn-danger`（实心红色）和 `.btn-danger-ghost`（幽灵红色）。

### 图标按钮

`.icon-btn` — 表格行操作、工具栏，34×34px，透明背景，hover 时显示紫色圆形背景。

```css
.icon-btn:hover {
  background: rgba(100, 12, 114, 0.1);
  color: var(--primary);
}
```

### 通知操作按钮

`.notif-btn` — 通知审批面板专用，内置四种状态颜色（cancel/approve/reject/info）。

### 分页按钮

`.page-btn` — 34×34px，分页导航，与 `.icon-btn` 尺寸一致。

### 胶囊按钮

`.chip-btn` — 品牌头部胶囊按钮，两种变体：`.chip-btn--primary`（粉紫渐变实心）和 `.chip-btn--ghost`（透明幽灵）。

### 移动端底部胶囊导航

`.capsule-action` — 移动端底部固定导航胶囊，内含图标按钮和分隔线。`.capsule-primary` 是中间的渐变主操作按钮。

### 响应式隐匿规则

`@media (max-width: 840px)` 下，`.footer-action`、`.achievement-add`、`.edit-dock` 自动隐藏。

---

## 卡片系统

### 规范卡片（Canonical Card）

所有卡片类型共享统一视觉基础：

```css
.card {
  border-radius: 20px;
  border: 1px solid var(--line);
  background: var(--card);         /* rgba(255,255,255,0.82) + backdrop-filter */
  backdrop-filter: blur(12px) saturate(115%);
  box-shadow: var(--shadow);        /* 0 8px 32px rgba(100,12,114,0.08) */
  padding: var(--card-padding, 16px);
}
```

卡片内高光：`.card::after` 使用 `inset 0 1px 0 rgba(255,255,255,0.5)` 和 `inset 0 -1px 0 rgba(255,255,255,0.1)` 创造玻璃边缘质感。

### 菜单卡片（Menu Card）

`.menu-card` — 侧边栏导航，padding 6px 14px 12px，grid 布局，`box-shadow: none`（侧边栏不需要投影）。

### 通知菜单项

`.menu-notification-item` — 通知列表项，hover 时显示紫色边框和阴影，active 时显示紫色背景。内置 `.menu-notification-badge` 支持 `pending`（橙）、`approved`（绿）、`rejected`（红）三种状态。

### 成就卡片

`.achievement-card` — 瀑布流布局，180px 左侧缩略图 + 右侧信息区。Hover 时 `translateY(-3px)` + 阴影扩大 + 紫色边框。

内置卡片入场动画：`animation: cardEnter var(--duration-slow) var(--ease-page)`，使用 nth-child 错开延迟（60ms 递增）。

---

## 对话框与弹窗

### 通用对话框（Dialog Card）

居中固定弹出，宽度 `min(420px, calc(100vw - 32px))`，背景 `var(--card)` 玻璃态。

```css
.dialog-backdrop {
  background: rgba(61, 7, 112, 0.35);
  backdrop-filter: blur(6px);
  z-index: 75;
}
```

### 过渡动画

两种模式：`dialog-fade`（淡入淡出 + 背景模糊）和 `dialog-pop`（缩放弹出 + 模糊）。

```css
/* fade */
.dialog-fade-enter-active { transition: opacity 0.35s ease, backdrop-filter 0.35s ease; }

/* pop — 带 scale 和 translate */
.dialog-pop-enter-from { opacity: 0; transform: translate(-50%, -48%) scale(0.96); }
.dialog-pop-enter-to   { opacity: 1; transform: translate(-50%, -50%) scale(1); }
```

### 底部弹出面板（Sheet Modal）

移动端默认从底部滑出（`align-items: flex-end`），桌面端居中。CSS Transition 驱动的 `.open` 类切换显隐，无需 Vue Transition。

```css
.sheet-modal {
  transform: translateX(-50%) translateY(calc(-50% + 40px)) scale(0.97);
  transition: transform 0.42s cubic-bezier(0.22, 1, 0.36, 1), opacity 0.4s ease;
}
.sheet-overlay.open .sheet-modal {
  transform: translateX(-50%) translateY(-50%) scale(1);
}
```

### Toast 轻提示

固定在右上角，`position: fixed; right: 24px; top: 20px;`，玻璃态，z-index 90。

---

## 表单系统

### 表单输入框（Form Input）

通用 `.form-input`：高度 40px，圆角 12px，透明背景带 1px 边框，focus 时边框变紫并显示 `box-shadow: 0 0 0 3px rgba(100,12,114,0.08)`。

认证页专用 `.form-input`（在 `.auth-card` 内）：高度 48px，圆角 14px，背景略不透明。

信息页 `.info-input`：背景 `#faf9fb`，信息页专用灰底，focus 时背景变白。

### 成就动态字段

`.achievement-dynamic-fields` — 成就编辑器中的双列网格字段容器，每个字段行 `.field-row` 自带内边距、圆角边框和背景，focus-within 时边框加深。

### 日期选择

`input[type="date"].info-input`：右侧有日历图标（CSS background-image SVG），webkit 原生日历 picker 通过 `opacity: 0` 隐藏。

### 单选/复选

`input[type="radio"]` 和 `input[type="checkbox"]`：使用 `accent-color: rgba(100, 12, 114, 0.7)` 统一主题色。

---

## 成就系统样式

### 成就卡片（瀑布流）

`.feed-waterfall` 使用 CSS `column-gap: 12px; column-count: 1`（响应式断点控制列数）。`.post-card` 是瀑布流内的子卡片。

### 成就分类标签栏

`.category-tab` — 成就列表顶部分类标签，激活态使用主色渐变背景。

### 成就类型徽章

- `.tag`：默认，紫色小圆角标签
- `.tag.good`：橙色，优秀标签
- `.tag.achie`：绿色，成就类
- `.tag.private`：紫色，私有标签

### 成就标签页

`.achievement-section-label`：全大写 + 字母间距 + 紫色，作为动态表单每个类别的分组标题。

### 成就详情视图（Sheet 和 FullView）

底部滑出 `.achievement-sheet`（最大宽度 960px）和全屏居中 `.achievement-view`（最大宽度 980px），共用水印式左侧类型标签（如论文的 `PAPER` 斜体大标）。

---

## 动画系统

### 缓动函数

| CSS 变量 | 缓动函数 | 用途 |
|---------|---------|------|
| `--ease-out` | `cubic-bezier(0.2, 0.8, 0.2, 1)` | 通用退出动画 |
| `--ease-page` | `cubic-bezier(0.22, 1, 0.36, 1)` | 页面过渡/卡片入场 |
| `--ease-dialog` | `cubic-bezier(0.22, 1, 0.36, 1)` | 对话框弹出 |
| `--ease-backdrop` | `ease` | 背景过渡 |

### 动画时长

| CSS 变量 | 时长 | 用途 |
|---------|------|------|
| `--duration-fast` | 160ms | 微交互、按钮 hover |
| `--duration-base` | 240ms | 标准过渡 |
| `--duration-slow` | 420ms | 页面级入场、卡片动画 |

### 关键帧

| 动画名 | 效果 | 来源 |
|--------|------|------|
| `cardEnter` | `translateY(20px) → 0` + 淡入 | achievements.css / base.css |
| `floatOrb` | 装饰球体悬浮上下位移动画（16s，循环） | auth.css |
| `itemActivePop` | `scale(1) → scale(0.96) → scale(1)`（弹性按下反馈） | cards.css |

### 菜单面板过渡

菜单嵌套切换使用 `menu-panel-forward/back` 和 `menu-title-forward/back`，`240ms cubic-bezier(0.22, 1, 0.36, 1)` 配合 `translateX(±12px) scale(0.98)` 滑入滑出。

### 减少动画

所有交互组件均支持 `@media (prefers-reduced-motion: reduce)`，自动禁用动画和过渡。

---

## 布局规范

### Dashboard 外壳

`.dashboard-shell` — 主应用容器，`padding: 28px`，最小高度 `100vh`。背景为从透明到 `--bg-soft` 的渐变。嵌入模式（`?embed=1`）下 padding 收缩为 14px，侧边栏自动隐藏。

### Dashboard 网格

`.dashboard-layout` — CSS Grid 双栏布局：

```css
grid-template-columns: 320px minmax(0, 1fr);
gap: 20px;
```

左侧 `.dashboard-left` 粘性定位 `position: sticky; top: 28px`，最大高度 `calc(100vh - 56px)`。

### 移动端断点

| 断点 | 行为 |
|------|------|
| `≤1080px` | 侧边栏列宽 280px |
| `≤840px` | 侧边栏隐藏，显示移动端底部胶囊导航；`.dashboard-shell` padding 收缩至 20px |
| `≤480px` | `.dashboard-shell` padding 收缩至 14px |

### 移动端底部胶囊导航

`.mobile-capsule` — 固定于底部居中的胶囊导航，内部分为左右操作区（`.capsule-left` / `.capsule-right`）和中间分隔线（`.capsule-divider`）。激活态 `.capsule-active` 使用渐变背景。

入场/退场动画 `.capsule-slide`：向上 80px 淡出/滑入，`.42s cubic-bezier(0.22, 1, 0.36, 1)`。

---

## 媒体查看器

### 全屏查看器（Media Viewer）

`.viewer-backdrop` — 固定全屏，深色半透明背景（`rgba(3,14,18,0.85)`）加 `backdrop-filter: blur(12px)`。

顶部显示计数器（当前/总数）和关闭按钮，关闭按钮 hover 时 `rotate(90deg)`（顺时针旋转）。

底部点指示器 `.viewer-dots`：当前点为圆角矩形（`border-radius: 4px`），其余为圆点。

### 文档查看器

支持四类文档：PPTX（提示下载）、PDF（iframe 渲染）、DOCX（docx-preview 渲染）、XLSX（表格标签 + 表格内容）。

---

## 空状态与加载

### 空状态

`.empty-tip` — 居中图标 + 文字提示，灰色虚线边框，用于成就列表空、无数据等场景。使用 SVG data-URI 图标。

### 加载状态

| 组件 | 尺寸 | 用途 |
|------|------|------|
| `.spinner` | 20px | 默认页内加载 |
| `.spinner-sm` | 14px | 按钮内联加载 |
| `.spinner-lg` | 32px | 页面级加载 |
| `.spinner-white` | — | 深色背景上白色 |
| `.loading-container` | — | 居中容器（spinner + 文字） |
| `.loading-text` | — | 内联文字加载指示 |

---

## 响应式设计策略

### 优先移动端

系统以移动端为优先设计目标，通过 **渐进增强** 的方式向大屏幕扩展：

- 移动端使用底部胶囊导航 + 单栏布局
- 平板（≤840px）开始显示侧边栏隐藏逻辑
- 桌面（>840px）完整双栏布局
- 超宽屏（>1080px）侧边栏列宽从 280px 扩展到 320px

### 触摸友好

- 所有可点击元素最小触摸区域 44×44px
- iOS `safe-area-inset-bottom` 支持刘海屏底部边距
- `-webkit-tap-highlight-color: transparent` 消除移动端点击高亮

---

## 辅助功能

- 所有交互元素 `:focus-visible` 时显示紫色 2px 外边框（`outline-offset: 2px`）
- 支持 `prefers-reduced-motion` 媒体查询，所有动画和过渡禁用
- 颜色对比度符合 WCAG AA 标准（`--text-sub: #5a6a8a` 在白底上可读）
- 按钮和链接均使用语义化标签，支持键盘导航

---

**相关文档**：[CSS 架构](./css-architecture) · [动画与过渡](./animation) · [成就编辑器](./achievement-editor) · [Axios 请求层](./axios-layer)