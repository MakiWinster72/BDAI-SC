<script setup>
import { computed } from "vue";
import LoadingIndicator from "@/components/LoadingIndicator.vue";

const props = defineProps({
  filters: {
    type: Object,
    required: true,
  },
  classYearOptions: {
    type: Array,
    default: () => [],
  },
  studentCategoryOptions: {
    type: Array,
    default: () => [],
  },
  availableMajors: {
    type: Array,
    default: () => [],
  },
  specialStudentTypeOptions: {
    type: Array,
    default: () => [],
  },
  hasActiveFilters: {
    type: Boolean,
    default: false,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  totalItems: {
    type: Number,
    default: 0,
  },
  gridViewOpen: {
    type: Boolean,
    default: false,
  },
  gridLoading: {
    type: Boolean,
    default: false,
  },
  gridSheets: {
    type: Array,
    default: () => [],
  },
  gridActiveSheet: {
    type: String,
    default: "main",
  },
  gridRowCount: {
    type: Number,
    default: 0,
  },
});

const emit = defineEmits([
  "update-filter",
  "reset-filters",
  "toggle-hmt",
  "open-grid-field-dialog",
  "update-grid-active-sheet",
]);

function createFilterModel(key) {
  return computed({
    get: () => props.filters[key],
    set: (value) => emit("update-filter", { key, value }),
  });
}

const keyword = createFilterModel("keyword");
const classYear = createFilterModel("classYear");
const studentCategory = createFilterModel("studentCategory");
const major = createFilterModel("major");
const classNo = createFilterModel("classNo");
const specialStudentType = createFilterModel("specialStudentType");
</script>

<template>
  <section class="card student-filter-card">
    <div class="student-filter-toolbar">
      <div class="student-filter-intro">
        <div class="info-section-title">搜索与筛选</div>
      </div>
      <div class="student-filter-search-wrap">
        <input
          v-model="keyword"
          class="info-input student-search"
          type="text"
          placeholder="搜索姓名 / 班别 / 学院 / 学号"
        />
      </div>
      <div class="student-filter-toolbar-actions">
        <button
          v-if="hasActiveFilters"
          class="student-filter-reset"
          type="button"
          @click="emit('reset-filters')"
        >
          清空筛选
        </button>
      </div>
    </div>

    <div v-if="!gridViewOpen" class="student-filter-body">
      <div class="student-filter-grid">
        <div class="student-filter-panel student-filter-field-year">
          <div class="student-filter-panel-head">
            <span class="info-label">年级</span>
          </div>
          <select v-model="classYear" class="info-input">
            <option value="">全部</option>
            <option
              v-for="year in classYearOptions"
              :key="year"
              :value="String(year)"
            >
              {{ year }}
            </option>
          </select>
        </div>

        <div class="student-filter-panel student-filter-field-category">
          <div class="student-filter-panel-head">
            <span class="info-label">学生类型</span>
          </div>
          <select v-model="studentCategory" class="info-input">
            <option value="">全部</option>
            <option
              v-for="cat in studentCategoryOptions"
              :key="cat"
              :value="cat"
            >
              {{ cat }}
            </option>
          </select>
        </div>

        <div class="student-filter-panel student-filter-field-major">
          <div class="student-filter-panel-head">
            <span class="info-label">专业</span>
          </div>
          <select v-model="major" class="info-input" :disabled="!filters.studentCategory">
            <option value="">全部</option>
            <option
              v-for="item in availableMajors"
              :key="item"
              :value="item"
            >
              {{ item }}
            </option>
          </select>
        </div>

        <div class="student-filter-panel student-filter-field-class">
          <div class="student-filter-panel-head">
            <span class="info-label">班级</span>
          </div>
          <select v-model="classNo" class="info-input">
            <option value="">全部</option>
            <option
              v-for="n in 10"
              :key="n"
              :value="String(n)"
            >
              {{ n }}
            </option>
          </select>
        </div>
      </div>

      <div class="student-filter-meta">
        <div class="student-filter-flags">
          <label class="info-choice">
            <input
              type="checkbox"
              :checked="filters.isHk"
              @change="emit('toggle-hmt', 'isHk')"
            />
            香港
          </label>
          <label class="info-choice">
            <input
              type="checkbox"
              :checked="filters.isMo"
              @change="emit('toggle-hmt', 'isMo')"
            />
            澳门
          </label>
          <label class="info-choice">
            <input
              type="checkbox"
              :checked="filters.isTw"
              @change="emit('toggle-hmt', 'isTw')"
            />
            台湾
          </label>
          <div class="student-special-filter">
            <span class="info-label" style="margin-right: 6px;">特殊学生</span>
            <select v-model="specialStudentType" class="info-input">
              <option
                v-for="opt in specialStudentTypeOptions"
                :key="opt.value"
                :value="opt.value"
              >
                {{ opt.label }}
              </option>
            </select>
          </div>
        </div>
        <div class="student-filter-status">
          {{ loading ? "正在更新结果..." : `当前共 ${totalItems} 条学生记录` }}
        </div>
      </div>
    </div>

    <div v-if="gridViewOpen" class="student-grid-tabs">
      <button
        class="student-grid-tab student-grid-tab-add"
        type="button"
        title="选择字段"
        @click="emit('open-grid-field-dialog')"
      >
        +
      </button>
      <button
        v-for="sheet in gridSheets"
        :key="sheet.id"
        class="student-grid-tab"
        :class="{ active: sheet.id === gridActiveSheet }"
        type="button"
        @click="emit('update-grid-active-sheet', sheet.id)"
      >
        {{ sheet.label }}
      </button>
      <LoadingIndicator
        v-if="gridLoading"
        class="student-grid-status"
        label="加载表格中…"
        size="sm"
      />
      <span v-else class="student-grid-status">共 {{ gridRowCount }} 条</span>
    </div>
  </section>
</template>
