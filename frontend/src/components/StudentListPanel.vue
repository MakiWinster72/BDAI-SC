<script setup>
import { computed } from "vue";
import PaginationBar from "./PaginationBar.vue";
import ProtectedMediaImage from "./ProtectedMediaImage.vue";
import { resolveMediaUrl } from "@/utils/media";

const props = defineProps({
  loading: {
    type: Boolean,
    default: false,
  },
  totalItems: {
    type: Number,
    default: 0,
  },
  hasActiveFilters: {
    type: Boolean,
    default: false,
  },
  selectAllLoading: {
    type: Boolean,
    default: false,
  },
  students: {
    type: Array,
    default: () => [],
  },
  selectedIds: {
    type: Array,
    default: () => [],
  },
  currentPage: {
    type: Number,
    default: 1,
  },
  pageSize: {
    type: Number,
    default: 10,
  },
  totalPages: {
    type: Number,
    default: 1,
  },
  pageSizeOptions: {
    type: Array,
    default: () => [],
  },
  getHkMoTwLabel: {
    type: Function,
    required: true,
  },
  getSpecialStudentTypeLabel: {
    type: Function,
    required: true,
  },
});

const emit = defineEmits([
  "select-current-page",
  "select-all-filtered",
  "open-detail",
  "update:selected-ids",
  "update:current-page",
  "update:page-size",
]);

const selectedIdsModel = computed({
  get: () => props.selectedIds,
  set: (value) => emit("update:selected-ids", value),
});
</script>

<template>
  <div class="student-results-header">
    <div class="info-section-title">
      筛选结果
      <span v-if="hasActiveFilters" class="student-results-count">已筛选</span>
    </div>
    <div class="student-results-meta">
      {{ loading ? "正在更新结果..." : `当前共 ${totalItems} 条学生记录` }}
    </div>
    <div class="student-results-actions">
      <button
        class="ghost-button"
        type="button"
        @click="emit('select-current-page')"
      >
        选择本页
      </button>
      <button
        class="ghost-button"
        type="button"
        :disabled="selectAllLoading"
        @click="emit('select-all-filtered')"
      >
        {{ selectAllLoading ? "选择中..." : "选择全部" }}
      </button>
    </div>
  </div>

  <div v-if="loading" class="empty-tip student-results-loading">
    加载学生信息中...
  </div>
  <div v-else-if="students.length" class="student-list">
    <div
      v-for="item in students"
      :key="item.id"
      class="student-row"
      :class="{ selected: selectedIds.includes(item.id) }"
      @click="emit('open-detail', item)"
    >
      <input
        v-model="selectedIdsModel"
        type="checkbox"
        :value="item.id"
        @click.stop
      />
      <div class="student-avatar">
        <ProtectedMediaImage
          v-if="item.avatarUrl"
          :src="resolveMediaUrl(item.avatarUrl)"
          :alt="item.name"
        />
        <span v-else>{{ (item.name || "?")[0] }}</span>
      </div>
      <div class="student-main">
        <div class="student-name-row">
          <span class="student-name">{{ item.name }}</span>
          <span class="student-no-inline">{{ item.studentNo }}</span>
        </div>
        <div class="student-meta">
          {{ item.gradeYear }}级 {{ item.major }}{{ item.classNo }}班
        </div>
        <div v-if="item.isHk || item.isMo || item.isTw" class="student-hkmo-badge">
          {{ getHkMoTwLabel(item) }}
        </div>
        <div v-if="item.specialStudentType" class="student-special-badge">
          {{ getSpecialStudentTypeLabel(item.specialStudentType) }}
        </div>
      </div>
    </div>
  </div>
  <div v-else class="empty-tip">没有匹配的学生。</div>

  <PaginationBar
    :current-page="currentPage"
    :page-size="pageSize"
    :total-pages="totalPages"
    :page-size-options="pageSizeOptions"
    mode="full"
    @update:current-page="emit('update:current-page', $event)"
    @update:page-size="emit('update:page-size', $event)"
  />
</template>
