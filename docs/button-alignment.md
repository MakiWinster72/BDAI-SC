# 修复 Ghost Button 与 Action Button 不对齐问题

## 问题描述

`ghost-button` 和 `action-button` 在同一行并排使用时，会出现以下问题：

1. **高度不一致**：`ghost-button` 默认高度 `32px`，`action-button` 默认高度 `48px`
2. **垂直无法对齐**：按钮基线不在同一水平线上
3. **内边距不同**：两个按钮的 padding 默认值不一样

## 解决方案

为并排使用的按钮添加统一的对齐样式：

```html
<div class="btn-row">
  <button class="ghost-button btn-fixed" type="button">取消</button>
  <button class="action-button btn-fixed" type="button">保存</button>
</div>
```

```css
.btn-row {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  align-items: center; /* 垂直居中 */
}

.btn-fixed {
  height: 36px !important;      /* 统一高度 */
  padding: 0 18px !important;    /* 统一内边距 */
  border-radius: 10px !important; /* 统一圆角 */
  font-size: 14px !important;    /* 统一字号 */
  width: auto !important;        /* 宽度自适应 */
}

/* 可选：统一样式细节 */
.btn-fixed.ghost-button {
  background: rgba(255, 255, 255, 0.5) !important;
}

.btn-fixed.action-button {
  background: #036b72 !important;
  color: #fff !important;
  border: none !important;
}
```

## 关键点

- 使用 `align-items: center` 让按钮垂直居中对齐
- 使用 `!important` 强制覆盖全局样式的默认值
- 保持 `gap` 控制按钮间距，而非用 margin
- 统一高度和内边距是最重要的两步
