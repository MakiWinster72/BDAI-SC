<script setup>
import { computed } from 'vue'

/**
 * StepperInput - 数字步进输入组件
 * 用于班级号、学年等数字的增减操作
 *
 * 两种模式：
 *   clamp 模式（默认）：到达 min/max 时停住
 *   wrap 模式：到达边界时 resetTo 空值
 *
 * Props:
 *   modelValue    - 当前值 (v-model, 支持 Number 或空字符串)
 *   min           - 最小值 (默认1)
 *   max           - 最大值 (默认10)
 *   step          - 步进值 (默认1)
 *   disabled      - 是否禁用
 *   placeholder   - 占位文本 (默认 '全部')
 *   wrap          - 超出边界时重置为空 (默认 false)
 *
 * Emits:
 *   update:modelValue - 值变化时
 */

const props = defineProps({
  modelValue: {
    type: [Number, String],
    default: null,
  },
  min: {
    type: Number,
    default: 1,
  },
  max: {
    type: Number,
    default: 10,
  },
  step: {
    type: Number,
    default: 1,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  placeholder: {
    type: String,
    default: '全部',
  },
  wrap: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:modelValue'])

const model = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
})

const current = computed(() => {
  const v = props.modelValue
  if (v === '' || v === null || v === undefined) return null
  const n = Number(v)
  return Number.isNaN(n) ? null : n
})

const canDecrement = computed(() => !props.disabled)
const canIncrement = computed(() => !props.disabled)

function decrement() {
  if (!canDecrement.value) return
  const cur = current.value
  if (cur === null) {
    emit('update:modelValue', props.max)
    return
  }
  const next = cur - props.step
  if (next < props.min) {
    emit('update:modelValue', props.wrap ? '' : props.min)
  } else {
    emit('update:modelValue', next)
  }
}

function increment() {
  if (!canIncrement.value) return
  const cur = current.value
  if (cur === null) {
    emit('update:modelValue', props.min)
    return
  }
  const next = cur + props.step
  if (next > props.max) {
    emit('update:modelValue', props.wrap ? '' : props.max)
  } else {
    emit('update:modelValue', next)
  }
}
</script>

<template>
  <div class="stepper" :class="{ 'is-disabled': disabled }">
    <button
      class="stepper__btn stepper__btn--dec"
      type="button"
      tabindex="-1"
      :disabled="!canDecrement"
      aria-label="减少"
      @click="decrement"
    >
      <svg width="12" height="12" viewBox="0 0 12 12" fill="none" aria-hidden="true">
        <path d="M2 6h8" stroke="currentColor" stroke-width="1.75" stroke-linecap="round"/>
      </svg>
    </button>
    <input
      v-model="model"
      class="stepper__input"
      type="number"
      :step="step"
      :min="min"
      :max="max"
      :disabled="disabled"
      :placeholder="placeholder"
      aria-label="数值"
    />
    <button
      class="stepper__btn stepper__btn--inc"
      type="button"
      tabindex="-1"
      :disabled="!canIncrement"
      aria-label="增加"
      @click="increment"
    >
      <svg width="12" height="12" viewBox="0 0 12 12" fill="none" aria-hidden="true">
        <path d="M6 2v8M2 6h8" stroke="currentColor" stroke-width="1.75" stroke-linecap="round"/>
      </svg>
    </button>
  </div>
</template>

<style scoped>
.stepper {
  display: inline-flex;
  align-items: center;
  gap: 0;
  height: 44px;
  border-radius: 10px;
  border: 1px solid var(--line-strong, #d0cfce);
  background: rgba(255, 255, 255, 0.96);
  overflow: hidden;
  transition: border-color 180ms ease, box-shadow 180ms ease;
  /* 在 field-card 内填满宽度 */
  width: 100%;
  box-sizing: border-box;
}

.stepper:focus-within {
  border-color: var(--primary, #640c72);
  box-shadow: 0 0 0 3px var(--primary-surface, rgba(100, 12, 114, 0.1));
}

.stepper.is-disabled {
  opacity: 0.5;
  pointer-events: none;
}

/* ── Buttons ── */
.stepper__btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 100%;
  border: none;
  background: transparent;
  color: var(--primary, #640c72);
  cursor: pointer;
  flex-shrink: 0;
  transition: background 150ms ease, color 150ms ease;
  /* 与输入框垂直居中，无需 padding */
  padding: 0;
}

.stepper__btn:hover:not(:disabled) {
  background: var(--primary-surface, rgba(100, 12, 114, 0.08));
}

.stepper__btn:active:not(:disabled) {
  background: var(--primary-dark, rgba(100, 12, 114, 0.14));
}

.stepper__btn:disabled {
  color: var(--text-sub, #aaa);
  cursor: not-allowed;
}

.stepper__btn--dec {
  border-right: 1px solid var(--line, rgba(0, 0, 0, 0.06));
}

.stepper__btn--inc {
  border-left: 1px solid var(--line, rgba(0, 0, 0, 0.06));
}

/* ── Input ── */
.stepper__input {
  flex: 1;
  min-width: 0;
  height: 100%;
  border: none;
  background: transparent;
  color: var(--primary-dark, #2d1a3e);
  font-size: 15px;
  font-weight: 600;
  text-align: center;
  outline: none;
  -moz-appearance: textfield;
  font-variant-numeric: tabular-nums;
  padding: 0 4px;
  box-sizing: border-box;
  /* 禁止手动输入，只允许通过按钮改变 */
  pointer-events: none;
  user-select: none;
}

.stepper__input::placeholder {
  color: var(--text-sub, #aaa);
  font-weight: 400;
}

.stepper__input::-webkit-outer-spin-button,
.stepper__input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}
</style>
