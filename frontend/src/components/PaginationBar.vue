<script setup>
import { computed } from 'vue'

/**
 * PaginationBar - 分页导航组件
 *
 * Props:
 *   currentPage    - 当前页 (从1开始)
 *   totalPages     - 总页数
 *   pageSize       - 每页条数 (可选)
 *   pageSizeOptions - pageSize下拉选项 (可选，有值时显示选择器)
 *   mode           - 'full' | 'simple' (默认 'full')
 *     'full': 首页/上一页/页码/省略号/下一页/末页 + pageSize选择器
 *     'simple': 上一页/下一页 + 页码信息
 *
 * Emits:
 *   update:currentPage - 页码变化
 *   update:pageSize    - 每页条数变化 (仅当pageSizeOptions存在时)
 */

const props = defineProps({
  currentPage: {
    type: Number,
    required: true,
  },
  totalPages: {
    type: Number,
    required: true,
  },
  pageSize: {
    type: Number,
    default: null,
  },
  pageSizeOptions: {
    type: Array,
    default: null, // e.g. [10, 20, 50]
  },
  mode: {
    type: String,
    default: 'full',
  },
})

const emit = defineEmits(['update:currentPage', 'update:pageSize'])

// --- Computed ---

const hasPrev = computed(() => props.currentPage > 1)
const hasNext = computed(() => props.currentPage < props.totalPages)

const visibleItems = computed(() => {
  if (props.mode === 'simple') return []
  const total = props.totalPages
  const cur = props.currentPage
  if (total <= 7) {
    return Array.from({ length: total }, (_, i) => ({ type: 'num', value: i + 1 }))
  }
  const items = []
  items.push({ type: 'num', value: 1 })
  if (cur > 3) items.push({ type: 'sep' })
  for (let i = Math.max(2, cur - 1); i <= Math.min(total - 1, cur + 1); i++) {
    items.push({ type: 'num', value: i })
  }
  if (cur < total - 2) items.push({ type: 'sep' })
  items.push({ type: 'num', value: total })
  return items
})

// --- Methods ---

function setPage(page) {
  if (page < 1 || page > props.totalPages) return
  emit('update:currentPage', page)
}

function setPageSize(size) {
  emit('update:pageSize', size)
}
</script>

<template>
  <div class="pagination-bar" :class="{ 'pagination-bar--simple': mode === 'simple' }">
    <!-- Simple mode: prev / page info / next -->
    <template v-if="mode === 'simple'">
      <button
        class="page-btn"
        :disabled="!hasPrev"
        type="button"
        aria-label="上一页"
        @click="setPage(currentPage - 1)"
      >
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
          <path stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7" />
        </svg>
      </button>
      <div class="page-info">
        <span class="page-current">{{ currentPage }}</span>
        <span class="page-sep">/</span>
        <span class="page-total">{{ totalPages }}</span>
      </div>
      <button
        class="page-btn"
        :disabled="!hasNext"
        type="button"
        aria-label="下一页"
        @click="setPage(currentPage + 1)"
      >
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
          <path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7" />
        </svg>
      </button>
    </template>

    <!-- Full mode: first / prev / numbers / next / last / size selector -->
    <template v-else>
      <div class="pagination-pages">
        <!-- 首页 -->
        <button
          class="page-button page-nav"
          type="button"
          :disabled="currentPage === 1"
          aria-label="首页"
          @click="setPage(1)"
        >
          <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
            <path d="M15 4L8 10l7 6V4z" fill="currentColor"/>
            <rect x="4" y="4" width="2" height="12" fill="currentColor"/>
          </svg>
        </button>
        <!-- 上一页 -->
        <button
          class="page-button page-nav"
          type="button"
          :disabled="!hasPrev"
          aria-label="上一页"
          @click="setPage(currentPage - 1)"
        >
          <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
            <path d="M11 4L4 10l7 6V4z" fill="currentColor"/>
          </svg>
        </button>

        <!-- 页码 -->
        <template v-for="item in visibleItems" :key="item.value">
          <button
            v-if="item.type === 'num'"
            class="page-button"
            :class="{ active: item.value === currentPage }"
            type="button"
            @click="setPage(item.value)"
          >
            {{ item.value }}
          </button>
          <span v-else class="page-ellipsis" aria-hidden="true">···</span>
        </template>

        <!-- 下一页 -->
        <button
          class="page-button page-nav"
          type="button"
          :disabled="!hasNext"
          aria-label="下一页"
          @click="setPage(currentPage + 1)"
        >
          <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
            <path d="M9 4l7 6-7 6V4z" fill="currentColor"/>
          </svg>
        </button>
        <!-- 末页 -->
        <button
          class="page-button page-nav"
          type="button"
          :disabled="currentPage === totalPages"
          aria-label="末页"
          @click="setPage(totalPages)"
        >
          <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
            <path d="M5 4l7 6-7 6V4z" fill="currentColor"/>
            <rect x="14" y="4" width="2" height="12" fill="currentColor"/>
          </svg>
        </button>
      </div>

      <!-- Page size selector -->
      <div v-if="pageSizeOptions" class="page-size">
        <span class="info-label">每页</span>
        <select
          :value="pageSize"
          class="info-input page-size-select"
          @change="setPageSize(Number($event.target.value))"
        >
          <option v-for="opt in pageSizeOptions" :key="opt" :value="opt">{{ opt }}</option>
        </select>
        <span class="info-label">条</span>
      </div>
    </template>
  </div>
</template>

<style scoped>
.pagination-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.pagination-bar--simple {
  justify-content: center;
}

.pagination-pages {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
}

/* Full mode buttons */
.page-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 36px;
  height: 36px;
  padding: 0 8px;
  border-radius: 999px;
  border: 1px solid var(--line-strong, #d0cfce);
  background: rgba(255, 255, 255, 0.7);
  color: var(--primary-dark, #4a084f);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 150ms ease, border-color 150ms ease;
}

.page-button:hover:not(:disabled):not(.active) {
  background: var(--card-hover, #f5f0fa);
  border-color: var(--primary, #640c72);
}

.page-button.active {
  background: var(--primary-surface, rgba(100, 12, 114, 0.1));
  border-color: var(--primary, #640c72);
  color: var(--primary, #640c72);
  font-weight: 700;
}

.page-button:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.page-nav svg {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.page-ellipsis {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 28px;
  height: 36px;
  color: var(--text-sub, #6a8087);
  font-size: 14px;
  font-weight: 600;
  user-select: none;
}

/* Simple mode buttons */
.page-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: 1.5px solid var(--line-strong, #d0cfce);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.9);
  color: var(--primary, #640c72);
  cursor: pointer;
  transition: background 160ms ease, border-color 160ms ease, transform 120ms ease;
}

.page-btn:hover:not(:disabled) {
  background: var(--primary-surface, rgba(100, 12, 114, 0.08));
  border-color: var(--primary, #640c72);
}

.page-btn:active:not(:disabled) {
  transform: scale(0.93);
}

.page-btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.page-btn svg {
  width: 14px;
  height: 14px;
}

.page-info {
  display: flex;
  align-items: baseline;
  gap: 3px;
  font-size: 14px;
  font-variant-numeric: tabular-nums;
}

.page-current {
  font-weight: 800;
  color: var(--primary-dark, #4a084f);
  font-size: 16px;
}

.page-sep {
  color: var(--text-sub, #6a8087);
  font-size: 12px;
}

.page-total {
  color: var(--text-sub, #6a8087);
  font-size: 13px;
}

/* Page size selector */
.page-size {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-size-select {
  width: 72px;
  height: 36px;
  padding: 0 10px;
  border: 1.5px solid var(--line-strong, #d0cfce);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.88);
  color: var(--text-main, #2d1a3e);
  font-size: 13px;
  font-weight: 600;
  outline: none;
  cursor: pointer;
  transition: border-color 180ms ease, box-shadow 180ms ease;
  -moz-appearance: textfield;
}

.page-size-select:focus {
  border-color: var(--primary, #640c72);
  box-shadow: 0 0 0 3px var(--primary-surface, rgba(100, 12, 114, 0.08));
}
</style>
