<script setup>
/**
 * OverlayPanel - 通用叠加面板组件
 *
 * 用于详情视图、成就视图、字段选择对话框等叠加层
 * 包含毛玻璃背景遮罩 + 动画面板
 *
 * Props:
 *   open        - 是否显示 (v-model)
 *   closing     - 是否处于关闭动画中
 *   title       - 面板标题
 *   ariaLabel  - 无障碍标签
 *   size        - 'normal' | 'wide' | 'full' (宽度预设)
 *   placement   - 'bottom' | 'center' (面板出现位置，默认 bottom)
 *
 * Events:
 *   close - 用户请求关闭（点击遮罩或关闭按钮）
 *
 * 注意: open/closing 状态由父组件控制，此组件仅负责 UI 渲染和事件通知
 */

const props = defineProps({
  open: {
    type: Boolean,
    required: true,
  },
  closing: {
    type: Boolean,
    default: false,
  },
  title: {
    type: String,
    default: '',
  },
  ariaLabel: {
    type: String,
    default: '',
  },
  size: {
    type: String,
    default: 'normal',
  },
  placement: {
    type: String,
    default: 'bottom',
  },
})

const emit = defineEmits(['close'])

function handleBackdropClick() {
  emit('close')
}

function handleClose() {
  emit('close')
}
</script>

<template>
  <Teleport to="body">
    <div v-if="open || closing" class="overlay-panel-root" aria-live="polite">
      <!-- Backdrop -->
      <div
        class="overlay-backdrop"
        :class="{ closing: closing }"
        @click="handleBackdropClick"
        aria-hidden="true"
      />

      <!-- Panel -->
      <div
        class="overlay-panel"
        :class="[size, placement, { open, closing }]"
        :aria-label="ariaLabel || title"
        role="dialog"
      >
        <!-- Header -->
        <div v-if="title || $slots.header" class="overlay-header">
          <slot name="header">
            <h2 class="overlay-title">{{ title }}</h2>
          </slot>
          <button
            class="overlay-close"
            type="button"
            :aria-label="ariaLabel ? `关闭 ${ariaLabel}` : '关闭'"
            @click="handleClose"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
              <path d="M18 6L6 18M6 6l12 12"/>
            </svg>
          </button>
        </div>

        <!-- Body -->
        <div class="overlay-body">
          <slot />
        </div>

        <!-- Footer -->
        <div v-if="$slots.footer" class="overlay-footer">
          <slot name="footer" />
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.overlay-panel-root {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  pointer-events: none;
}

.overlay-panel-root[data-placement="center"] {
  align-items: center;
}

/* Backdrop */
.overlay-backdrop {
  position: absolute;
  inset: 0;
  background: rgba(45, 26, 62, 0.35);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  pointer-events: auto;
  opacity: 1;
  transition: opacity 250ms ease;
}

.overlay-backdrop.closing {
  opacity: 0;
}

/* Panel */
.overlay-panel {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  max-height: 84vh;
  overflow: hidden;
  border-radius: 22px 22px 0 0;
  border: 1px solid rgba(255, 255, 255, 0.5);
  background: var(--card, #fff);
  box-shadow: var(--shadow-glow, 0 8px 32px rgba(100, 12, 114, 0.15));
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  pointer-events: auto;

  /* Animation base states */
  transform: translateY(120%) scale(0.98);
  opacity: 0;
  transition:
    transform 300ms cubic-bezier(0.22, 1, 0.36, 1),
    opacity 250ms ease;
}

.overlay-panel.open {
  transform: translateY(0) scale(1);
  opacity: 1;
}

.overlay-panel.closing {
  transform: translateY(120%) scale(0.98);
  opacity: 0;
}

/* Placement: center */
.overlay-panel.placement-center {
  align-items: center;
  border-radius: 22px;
  transform: scale(0.94) translateY(12px);
  opacity: 0;
}

.overlay-panel.placement-center.open {
  transform: scale(1) translateY(0);
  opacity: 1;
}

.overlay-panel.placement-center.closing {
  transform: scale(0.94) translateY(12px);
  opacity: 0;
}

/* Size presets */
.overlay-panel.size-normal {
  width: min(720px, calc(100vw - 32px));
}

.overlay-panel.size-wide {
  width: min(980px, calc(100vw - 32px));
}

.overlay-panel.size-full {
  width: calc(100vw - 32px);
}

/* Header */
.overlay-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 18px 14px;
  border-bottom: 1px solid var(--line, rgba(100, 12, 114, 0.1));
  flex-shrink: 0;
}

.overlay-title {
  margin: 0;
  font-size: 16px;
  font-weight: 800;
  color: var(--primary-dark, #4a084f);
  line-height: 1.3;
}

.overlay-close {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 9px;
  background: var(--primary-surface, rgba(100, 12, 114, 0.08));
  color: var(--primary, #640c72);
  cursor: pointer;
  flex-shrink: 0;
  transition: background 160ms ease, transform 120ms ease;
}

.overlay-close:hover {
  background: rgba(100, 12, 114, 0.16);
}

.overlay-close:active {
  transform: scale(0.93);
}

.overlay-close svg {
  width: 15px;
  height: 15px;
}

/* Body */
.overlay-body {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  scrollbar-width: none;
  padding: 12px 14px 16px;
}

.overlay-body::-webkit-scrollbar {
  width: 0;
  height: 0;
}

/* Footer */
.overlay-footer {
  border-top: 1px solid var(--line, rgba(100, 12, 114, 0.1));
  padding: 14px 18px;
  flex-shrink: 0;
}

/* Responsive */
@media (max-width: 640px) {
  .overlay-panel {
    width: 100%;
    max-height: 92vh;
    border-radius: 22px 22px 0 0;
    transform: translateY(100%);
  }

  .overlay-panel.open {
    transform: translateY(0);
  }

  .overlay-panel.closing {
    transform: translateY(100%);
  }

  .overlay-panel.placement-center {
    width: 100%;
    max-height: 92vh;
    border-radius: 22px 22px 0 0;
    transform: translateY(100%);
  }

  .overlay-panel.placement-center.open {
    transform: translateY(0);
  }

  .overlay-panel.placement-center.closing {
    transform: translateY(100%);
  }
}

@media (prefers-reduced-motion: reduce) {
  .overlay-backdrop,
  .overlay-panel {
    transition: none !important;
  }

  .overlay-panel {
    transform: translateY(0) scale(1) !important;
    opacity: 1 !important;
  }

  .overlay-panel.closing {
    transform: translateY(0) scale(1) !important;
    opacity: 1 !important;
  }
}
</style>
