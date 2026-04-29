<script setup>
import { Teleport } from 'vue'
import { useToast } from '../composables/useToast'

const { toasts, removeToast } = useToast()

const ICONS = {
  success: `<path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />`,
  error: `<path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-2.694-.833-3.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z" />`,
  warn: `<path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-2.694-.833-3.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z" />`,
  info: `<path stroke-linecap="round" stroke-linejoin="round" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />`,
  progress: `<path stroke-linecap="round" stroke-linejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />`,
}
</script>

<template>
  <Teleport to="body">
    <div class="toast-container" aria-live="polite" aria-atomic="false">
      <TransitionGroup name="toast-anim">
        <div
          v-for="item in toasts"
          :key="item.id"
          :class="['toast', 'toast-' + item.type]"
          role="alert"
        >
          <svg
            class="toast-icon"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            aria-hidden="true"
            v-html="ICONS[item.type] || ICONS.info"
          />
          <span class="toast-msg">{{ item.message }}</span>
          <div v-if="item.progress !== null && item.progress !== undefined" class="toast-progress-wrap">
            <div class="toast-progress-bar" :style="{ width: Math.max(0, Math.min(100, item.progress)) + '%' }"></div>
          </div>
          <button
            class="toast-close"
            type="button"
            aria-label="关闭"
            @click="removeToast(item.id)"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
              <path d="M18 6L6 18M6 6l12 12" />
            </svg>
          </button>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<style scoped>
.toast-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 8px;
  pointer-events: none;
  max-width: 360px;
}

.toast {
  position: static;
  transform: none;
  display: flex;
  align-items: flex-start;
  gap: 9px;
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  box-shadow: 0 8px 32px rgba(100, 12, 114, 0.18), 0 2px 8px rgba(100, 12, 114, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.5);
  font-size: 13.5px;
  font-weight: 600;
  color: var(--text-main, #1a0a2e);
  pointer-events: auto;
  line-height: 1.5;
}

.toast-success {
  background: rgba(239, 246, 241, 0.95);
  border-color: rgba(39, 174, 96, 0.25);
  color: #166534;
}

.toast-error {
  background: rgba(253, 242, 242, 0.95);
  border-color: rgba(192, 57, 43, 0.2);
  color: #991b1b;
}

.toast-warn {
  background: rgba(255, 251, 235, 0.95);
  border-color: rgba(234, 179, 8, 0.3);
  color: #92400e;
}

.toast-info {
  background: rgba(248, 245, 253, 0.95);
  border-color: rgba(100, 12, 114, 0.15);
  color: var(--primary-dark, #1a0a2e);
}

.toast-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
  margin-top: 1px;
}

.toast-msg {
  flex: 1;
  word-break: break-word;
}

.toast-close {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border: none;
  background: transparent;
  color: currentColor;
  cursor: pointer;
  opacity: 0.5;
  border-radius: 5px;
  flex-shrink: 0;
  transition: opacity 150ms ease;
  padding: 0;
  margin-top: -1px;
}

.toast-close:hover {
  opacity: 1;
}

.toast-close svg {
  width: 12px;
  height: 12px;
}

.toast-progress-wrap {
  margin-top: 8px;
  height: 4px;
  border-radius: 999px;
  background: rgba(100, 12, 114, 0.1);
  overflow: hidden;
}

.toast-progress .toast-progress-wrap {
  background: rgba(100, 12, 114, 0.12);
}

.toast-progress-bar {
  height: 100%;
  border-radius: 999px;
  background: var(--primary, var(--primary));
  transition: width 180ms linear;
}

.toast-progress {
  background: rgba(248, 245, 253, 0.95);
  border-color: rgba(100, 12, 114, 0.18);
}

/* Transition */
.toast-anim-enter-active {
  transition: opacity 280ms cubic-bezier(0.22, 1, 0.36, 1),
              transform 280ms cubic-bezier(0.22, 1, 0.36, 1);
}
.toast-anim-leave-active {
  transition: opacity 220ms ease,
              transform 220ms ease;
}
.toast-anim-enter-from {
  opacity: 0;
  transform: translateX(24px) scale(0.96);
}
.toast-anim-leave-to {
  opacity: 0;
  transform: translateX(16px) scale(0.96);
}

@media (max-width: 840px) {
  .toast-container {
    top: 16px;
    left: 50%;
    right: auto;
    transform: translateX(-50%);
    max-width: 80%;
  }
  .toast {
    font-size: 12px;
    padding: 8px 10px;
  }
}
.toast-anim-move {
  transition: transform 280ms cubic-bezier(0.22, 1, 0.36, 1);
}
</style>
