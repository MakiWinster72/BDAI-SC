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
  readonly: {
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
  <div class="stepper-input-root">
    <button
      class="stepper-btn stepper-btn--dec"
      type="button"
      :disabled="!canDecrement"
      aria-label="减少"
      @click="decrement"
    >
      −
    </button>
    <input
      v-model="model"
      class="stepper-value"
      type="number"
      :step="step"
      :min="min"
      :max="max"
      :disabled="disabled"
      :readonly="readonly"
      :placeholder="placeholder"
      aria-label="数值"
    />
    <button
      class="stepper-btn stepper-btn--inc"
      type="button"
      :disabled="!canIncrement"
      aria-label="增加"
      @click="increment"
    >
      +
    </button>
  </div>
</template>

<style scoped>
.stepper-input-root {
  display: inline-flex;
  align-items: center;
  border: 1.5px solid var(--line-strong, #d0cfce);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.88);
  overflow: hidden;
  transition: border-color 180ms ease, box-shadow 180ms ease;
}

.stepper-input-root:focus-within {
  border-color: var(--primary, #640c72);
  box-shadow: 0 0 0 3px var(--primary-surface, rgba(100, 12, 114, 0.08));
}

.stepper-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border: none;
  background: transparent;
  color: var(--primary-dark, #4a084f);
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
  flex-shrink: 0;
  transition: background 150ms ease, color 150ms ease;
  line-height: 1;
}

.stepper-btn:hover:not(:disabled) {
  background: var(--primary-surface, rgba(100, 12, 114, 0.08));
  color: var(--primary, #640c72);
}

.stepper-btn:active:not(:disabled) {
  transform: scale(0.92);
}

.stepper-btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.stepper-value {
  width: 52px;
  min-width: 0;
  border: none;
  background: transparent;
  color: var(--text-main, #2d1a3e);
  font-size: 15px;
  font-weight: 600;
  text-align: center;
  outline: none;
  -moz-appearance: textfield;
  padding: 0 2px;
  font-variant-numeric: tabular-nums;
}

.stepper-value::-webkit-outer-spin-button,
.stepper-value::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}
</style>
