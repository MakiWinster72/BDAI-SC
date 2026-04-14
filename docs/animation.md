# 窗口出入动画设计

## 概述

弹窗/面板采用底部滑入 + 模糊渐变动画，参考 iOS/macOS 系统的操作表风格，流畅且有层次感。

## 快速应用

项目已在 `src/styles.css` 中预置了通用类，只需两步即可使用：

**1. HTML 结构**

```html
<div :class="['sheet-overlay', { open: visible }]" @click.self="close">
  <div class="sheet-modal">
    <!-- 弹窗内容 -->
  </div>
</div>
```

**2. 逻辑控制**

```js
const visible = ref(false)
function open() { visible.value = true }
function close() { visible.value = false }
```

## 通用类说明

| 类名 | 作用 |
|------|------|
| `.sheet-overlay` | 遮罩层，含模糊背景和居中 flex 布局 |
| `.sheet-overlay.open` | 遮罩显示状态 |
| `.sheet-modal` | 弹窗本体，含滑入动画 |

直接在 `styles.css` 中使用这两个类即可，无需编写任何 CSS。弹窗内容样式由内部自行控制。

## 自定义样式

如果预设宽高/圆角不满足需求，可在组件 `<style scoped>` 中覆盖：

```css
/* 更大宽度 */
.sheet-modal {
  max-width: 36rem;
}

/* 更大圆角 */
.sheet-modal {
  border-radius: 32px;
}
```

## 动画参数

| 属性 | 值 |
|------|-----|
| 滑入距离 | `translateY(120%)` |
| 缩放 | `scale(0.98)` → `scale(1)` |
| 模糊 | `blur(6px)` → `blur(0)` |
| 透明度 | `opacity: 0` → `opacity: 1` |
| 过渡时间（位移/透明度） | `0.45s ease` |
| 过渡时间（模糊） | `0.75s ease` |
| 背景遮罩模糊 | `backdrop-filter: blur(8px)` |
| 背景透明度 | `rgba(52, 37, 22, 0.35)` |

## 设计要点

1. **底部滑入**：面板从视口底部外侧滑入，暗示内容从下层浮现
2. **多层渐变**：模糊 + 透明度 + 缩放三个属性同时过渡，层次丰富
3. **模糊渐变更慢**：filter 使用 0.75s 而非 0.45s，产生先清晰后模糊的梦幻感
4. **遮罩同步**：背景遮罩透明度渐变与面板滑入同步，整体感强
5. **pointer-events 控制**：未 open 时 `pointer-events: none`，避免遮罩阻挡点击

## 变体

### 顶部滑入（toast/通知）

将 `translateY` 方向反转：

```css
.sheet-modal {
  top: 0;
  bottom: auto;
  transform: translateX(-50%) translateY(-120%) scale(0.98);
}

.sheet-overlay.open .sheet-modal {
  transform: translateX(-50%) translateY(0) scale(1);
}
```

### 左侧滑入

```css
.sheet-modal {
  left: 0;
  right: auto;
  bottom: auto;
  top: 50%;
  transform: translateY(-50%) translateX(-120%) scale(0.98);
}

.sheet-overlay.open .sheet-modal {
  transform: translateY(-50%) translateX(0) scale(1);
}
```

### 底部 sheet（参考 achievement-sheet）

已存在于 `styles.css` 中的 `.achievement-sheet`，从视口底部滑入，适合多列表格等较宽内容。
