<template>
  <div class="year-picker" ref="rootRef">
    <button
      class="year-picker__trigger"
      type="button"
      :disabled="disabled"
      @click.stop="toggle"
    >
      <span :class="{ 'year-picker__placeholder': !props.modelValue }">
        {{ props.modelValue || placeholder }}
      </span>
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
        <line x1="16" y1="2" x2="16" y2="6" />
        <line x1="8" y1="2" x2="8" y2="6" />
        <line x1="3" y1="10" x2="21" y2="10" />
      </svg>
    </button>
    <Teleport to="body">
      <div v-if="open" class="year-picker__backdrop" @click.stop="close" />
      <Transition name="year-picker-drop">
        <div
          v-if="open"
          class="year-picker__popover"
          :style="popoverStyle"
          @click.stop
          @mousemove="resetIdleTimer"
          @touchstart="resetIdleTimer"
        >
          <div class="year-picker__nav">
            <button type="button" class="year-picker__nav-btn" @click="prevDecade">&lt;</button>
            <span class="year-picker__decade-label">{{ decadeStart }} - {{ decadeStart + 9 }}</span>
            <button type="button" class="year-picker__nav-btn" @click="nextDecade">&gt;</button>
          </div>
          <div class="year-picker__grid">
            <button
              v-for="year in yearGrid"
              :key="year"
              class="year-picker__cell"
              :class="{ active: year === props.modelValue, outside: !year }"
              type="button"
              :disabled="!year"
              @click="selectYear(year)"
            >
              {{ year || "" }}
            </button>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";

const props = defineProps({
  modelValue: { type: Number, default: null },
  placeholder: { type: String, default: "选择年份" },
  disabled: { type: Boolean, default: false },
});

const emit = defineEmits(["update:modelValue"]);

const open = ref(false);
const rootRef = ref(null);
const decadeOffset = ref(0);

const currentYear = new Date().getFullYear();
const decadeStart = computed(() => {
  const base = props.modelValue || currentYear;
  const decade = Math.floor(base / 10) * 10;
  return decade + decadeOffset.value * 10;
});

const yearGrid = computed(() => {
  const start = decadeStart.value - 1;
  return Array.from({ length: 12 }, (_, i) => {
    const y = start + i;
    return y > 0 ? y : null;
  });
});

const positionTick = ref(0);
function bumpPosition() { positionTick.value++; }

const popoverStyle = computed(() => {
  if (!rootRef.value) return {};
  void positionTick.value;
  const rect = rootRef.value.getBoundingClientRect();
  return {
    position: "fixed",
    top: rect.bottom + 6 + "px",
    left: rect.left + "px",
  };
});

let idleTimer = null;
const IDLE_MS = 3000;

function resetIdleTimer() {
  clearTimeout(idleTimer);
  idleTimer = setTimeout(close, IDLE_MS);
}

function clearIdleTimer() {
  clearTimeout(idleTimer);
  idleTimer = null;
}

watch(open, (val) => {
  if (val) {
    bumpPosition();
    resetIdleTimer();
    window.addEventListener("scroll", bumpPosition, true);
    window.addEventListener("resize", bumpPosition);
  } else {
    clearIdleTimer();
    window.removeEventListener("scroll", bumpPosition, true);
    window.removeEventListener("resize", bumpPosition);
  }
});

function toggle() {
  if (props.disabled) return;
  decadeOffset.value = 0;
  open.value = !open.value;
}

function close() {
  open.value = false;
}

function selectYear(year) {
  if (!year) return;
  clearIdleTimer();
  emit("update:modelValue", year);
  open.value = false;
}

function prevDecade() {
  decadeOffset.value--;
  resetIdleTimer();
}

function nextDecade() {
  decadeOffset.value++;
  resetIdleTimer();
}

function onKeydown(e) {
  if (e.key === "Escape") close();
}

function onClickOutside(e) {
  if (!rootRef.value) return;
  if (rootRef.value.contains(e.target)) return;
  if (e.target.closest(".year-picker__popover")) return;
  if (e.target.closest(".year-picker__backdrop")) return;
  close();
}

onMounted(() => {
  document.addEventListener("keydown", onKeydown);
  document.addEventListener("click", onClickOutside);
});

onBeforeUnmount(() => {
  document.removeEventListener("keydown", onKeydown);
  document.removeEventListener("click", onClickOutside);
  window.removeEventListener("scroll", bumpPosition, true);
  window.removeEventListener("resize", bumpPosition);
});
</script>

<style>
.year-picker__backdrop {
  position: fixed;
  inset: 0;
  z-index: 998;
}

.year-picker__popover {
  z-index: 999;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-radius: 14px;
  border: 1px solid var(--line, rgba(0, 0, 0, 0.08));
  box-shadow: 0 8px 32px rgba(100, 12, 114, 0.12);
  padding: 12px;
  min-width: 240px;
}

.year-picker__nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  padding: 0 2px;
}

.year-picker__nav-btn {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: none;
  background: transparent;
  color: var(--primary-dark, #2d1a3e);
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s ease;
}

.year-picker__nav-btn:hover {
  background: rgba(100, 12, 114, 0.08);
}

.year-picker__decade-label {
  font-size: 13px;
  font-weight: 700;
  color: var(--primary-dark, #2d1a3e);
}

.year-picker__grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 6px;
}

.year-picker__cell {
  height: 40px;
  border-radius: 10px;
  border: none;
  background: transparent;
  color: var(--primary-dark, #2d1a3e);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.12s ease, color 0.12s ease;
  font-family: inherit;
}

.year-picker__cell:hover:not(.active):not(.outside) {
  background: rgba(100, 12, 114, 0.06);
}

.year-picker__cell.active {
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
  color: #fff;
}

.year-picker__cell.outside {
  cursor: default;
  opacity: 0;
}

/* Transition */
.year-picker-drop-enter-active {
  transition: opacity 0.18s ease, transform 0.2s cubic-bezier(0.22, 1, 0.36, 1);
}

.year-picker-drop-leave-active {
  transition: opacity 0.12s ease, transform 0.14s ease;
}

.year-picker-drop-enter-from,
.year-picker-drop-leave-to {
  opacity: 0;
  transform: translateY(-6px) scale(0.96);
}
</style>

<style scoped>
.year-picker {
  position: relative;
  width: 100%;
}

.year-picker__trigger {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  width: 100%;
  height: 44px;
  border-radius: 10px;
  border: 1px solid var(--line-strong, #d0cfce);
  background: rgba(255, 255, 255, 0.96);
  color: var(--primary-dark, #2d1a3e);
  font-size: 13px;
  padding: 0 13px;
  cursor: pointer;
  font-family: inherit;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}

.year-picker__trigger:hover:not(:disabled) {
  border-color: var(--primary);
}

.year-picker__trigger:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.year-picker__placeholder {
  color: var(--text-sub, #aaa);
}
</style>
