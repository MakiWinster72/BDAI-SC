<script setup>
import { onMounted, onUnmounted } from "vue";

const props = defineProps({
  hasActiveFilters: { type: Boolean, default: false },
  gridViewOpen: { type: Boolean, default: false },
  gridFullscreen: { type: Boolean, default: false },
  selectMenuOpen: { type: Boolean, default: false },
  selectAllLoading: { type: Boolean, default: false },
  hasSelection: { type: Boolean, default: false },
  selectedCount: { type: Number, default: 0 },
  actionDockStyle: {
    type: Object,
    default: () => ({
      bottom: "calc(88px + env(safe-area-inset-bottom, 0px))",
      top: "auto",
    }),
  },
});

const emit = defineEmits([
  "open-filter",
  "toggle-grid",
  "toggle-fullscreen",
  "toggle-select-menu",
  "close-select-menu",
  "select-page",
  "select-all",
]);

function onDocumentClick(event) {
  if (!props.selectMenuOpen) {
    return;
  }
  const menu = document.querySelector(".select-float-menu");
  if (menu && !menu.contains(event.target)) {
    emit("close-select-menu");
  }
}

onMounted(() => {
  document.addEventListener("click", onDocumentClick);
});

onUnmounted(() => {
  document.removeEventListener("click", onDocumentClick);
});
</script>

<template>
  <Teleport to="body">
    <Transition name="select-float">
      <div
        v-if="selectMenuOpen && !gridViewOpen && !hasSelection"
        class="student-action-dock select-float-menu"
        :style="actionDockStyle"
        @click.stop
      >
        <button class="select-float-btn" type="button" @click="emit('select-page')">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M8 6h13M8 12h13M8 18h13M3 6h.01M3 12h.01M3 18h.01" />
          </svg>
          选择本页
        </button>
        <button
          class="select-float-btn"
          type="button"
          :disabled="selectAllLoading"
          @click="emit('select-all')"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M9 11l3 3L22 4" />
            <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" />
          </svg>
          {{ selectAllLoading ? "选择中..." : "选择全部" }}
        </button>
      </div>
    </Transition>
  </Teleport>

  <button
    class="capsule-action student-capsule-btn is-filter"
    type="button"
    @click="emit('open-filter')"
  >
    <span class="capsule-icon" aria-hidden="true">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
        <path d="M22 3H2l8 9.46V19l4 2v-8.54L22 3z" />
      </svg>
    </span>
    <span class="student-capsule-label">筛选</span>
    <span v-if="hasActiveFilters" class="capsule-filter-dot" />
  </button>
  <button class="capsule-action student-capsule-btn hide-on-mobile" type="button" @click="emit('toggle-grid')">
    <span class="capsule-icon" aria-hidden="true">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
        <rect x="3" y="3" width="7" height="7" />
        <rect x="14" y="3" width="7" height="7" />
        <rect x="3" y="14" width="7" height="7" />
        <rect x="14" y="14" width="7" height="7" />
      </svg>
    </span>
    <span class="student-capsule-label">{{ gridViewOpen ? "列表" : "表格" }}</span>
  </button>
  <button
    v-if="gridViewOpen"
    class="capsule-action student-capsule-btn hide-on-mobile"
    :class="{ 'capsule-active': gridFullscreen }"
    type="button"
    @click="emit('toggle-fullscreen')"
  >
    <span class="capsule-icon" aria-hidden="true">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
        <path d="M8 3H5a2 2 0 00-2 2v3m18 0V5a2 2 0 00-2-2h-3m0 18h3a2 2 0 002-2v-3M3 16v3a2 2 0 002 2h3" />
      </svg>
    </span>
    <span class="student-capsule-label">{{ gridFullscreen ? "退出" : "全屏" }}</span>
  </button>
  <button
    v-if="!gridViewOpen"
    class="capsule-action student-capsule-btn"
    :class="{ 'capsule-active': selectMenuOpen || hasSelection }"
    type="button"
    :aria-label="hasSelection ? '取消选择' : '选择学生'"
    @click.stop="emit('toggle-select-menu')"
  >
    <span class="capsule-icon" aria-hidden="true">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
        <rect x="3" y="3" width="18" height="18" rx="2" />
        <path d="M9 12l2 2 4-4" />
      </svg>
    </span>
    <span class="student-capsule-label">
      {{ hasSelection ? `已选 ${selectedCount}` : "选择" }}
    </span>
  </button>
</template>

<style scoped>
@import "@/assets/styles/student-info-view-scoped.css";
</style>
