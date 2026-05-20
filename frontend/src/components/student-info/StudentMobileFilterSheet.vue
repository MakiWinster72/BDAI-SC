<script setup>
defineProps({
  open: { type: Boolean, default: false },
  sheetStyle: { type: Object, default: () => ({}) },
  filters: { type: Object, required: true },
  classYearOptions: { type: Array, default: () => [] },
  studentCategoryOptions: { type: Array, default: () => [] },
  availableMajors: { type: Array, default: () => [] },
  specialStudentTypeOptions: { type: Array, default: () => [] },
});
const emit = defineEmits(["close", "reset", "touchstart", "touchmove", "touchend"]);
</script>

<template>
    <!-- Mobile Filter Sheet -->
    <Teleport to="body">
      <div v-if="open" class="sheet-overlay open" @click.self="emit('close')">
        <div class="mobile-filter-sheet" role="dialog" aria-modal="true" aria-label="筛选" :style="sheetStyle">
          <div
            class="mobile-filter-handle"
            @touchstart="$emit('touchstart', $event)"
            @touchmove="$emit('touchmove', $event)"
            @touchend="$emit('touchend')"
          ></div>
          <header
            class="mobile-filter-header"
            @touchstart="$emit('touchstart', $event)"
            @touchmove="$emit('touchmove', $event)"
            @touchend="$emit('touchend')"
          >
            <h2 class="mobile-filter-title">筛选学生</h2>
            <button class="mobile-filter-close" type="button" @click="emit('close')">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M18 6L6 18M6 6l12 12"/>
              </svg>
            </button>
          </header>
          <div class="mobile-filter-body">
            <div class="mobile-filter-section">
              <label class="mobile-filter-label">关键词搜索</label>
              <div class="mobile-filter-search">
                <svg class="mobile-filter-search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/>
                </svg>
                <input
                  v-model="filters.keyword"
                  class="mobile-filter-input"
                  type="text"
                  placeholder="姓名、学号、班级..."
                />
              </div>
            </div>

            <div class="mobile-filter-row">
              <div class="mobile-filter-section half">
                <label class="mobile-filter-label">年级</label>
                <div class="mobile-filter-select-wrap">
                  <select v-model="filters.classYear" class="mobile-filter-select">
                    <option value="">全部年级</option>
                    <option v-for="year in classYearOptions" :key="year" :value="String(year)">{{ year }}级</option>
                  </select>
                  <svg class="mobile-filter-select-arrow" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M6 9l6 6 6-6"/>
                  </svg>
                </div>
              </div>
              <div class="mobile-filter-section half">
                <label class="mobile-filter-label">班级</label>
                <div class="mobile-filter-select-wrap">
                  <select v-model="filters.classNo" class="mobile-filter-select">
                    <option value="">全部班级</option>
                    <option v-for="n in 10" :key="n" :value="String(n)">{{ n }}班</option>
                  </select>
                  <svg class="mobile-filter-select-arrow" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M6 9l6 6 6-6"/>
                  </svg>
                </div>
              </div>
            </div>

            <div class="mobile-filter-section">
              <label class="mobile-filter-label">学生类型</label>
              <div class="mobile-filter-select-wrap">
                <select v-model="filters.studentCategory" class="mobile-filter-select">
                  <option value="">全部类型</option>
                  <option v-for="cat in studentCategoryOptions" :key="cat" :value="cat">{{ cat }}</option>
                </select>
                <svg class="mobile-filter-select-arrow" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M6 9l6 6 6-6"/>
                </svg>
              </div>
            </div>

            <div class="mobile-filter-section">
              <label class="mobile-filter-label">专业</label>
              <div class="mobile-filter-select-wrap">
                <select v-model="filters.major" class="mobile-filter-select" :disabled="!filters.studentCategory">
                  <option value="">全部专业</option>
                  <option v-for="major in availableMajors" :key="major" :value="major">{{ major }}</option>
                </select>
                <svg class="mobile-filter-select-arrow" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M6 9l6 6 6-6"/>
                </svg>
              </div>
            </div>

            <div class="mobile-filter-section">
              <label class="mobile-filter-label">港澳台学生</label>
              <div class="mobile-filter-chips">
                <label class="mobile-filter-chip" :class="{ active: filters.isHk }" @click.prevent="filters.isHk = !filters.isHk">
                  <input :checked="filters.isHk" type="radio" name="hkmo" hidden />
                  <span>香港</span>
                </label>
                <label class="mobile-filter-chip" :class="{ active: filters.isMo }" @click.prevent="filters.isMo = !filters.isMo">
                  <input :checked="filters.isMo" type="radio" name="hkmo" hidden />
                  <span>澳门</span>
                </label>
                <label class="mobile-filter-chip" :class="{ active: filters.isTw }" @click.prevent="filters.isTw = !filters.isTw">
                  <input :checked="filters.isTw" type="radio" name="hkmo" hidden />
                  <span>台湾</span>
                </label>
              </div>
            </div>

            <div class="mobile-filter-section">
              <label class="mobile-filter-label">特殊学生</label>
              <div class="mobile-filter-select-wrap">
                <select v-model="filters.specialStudentType" class="mobile-filter-select">
                  <option value="">无</option>
                  <option v-for="opt in specialStudentTypeOptions.slice(1)" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
                </select>
                <svg class="mobile-filter-select-arrow" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M6 9l6 6 6-6"/>
                </svg>
              </div>
            </div>
          </div>
          <div class="mobile-filter-footer">
            <button class="mobile-filter-reset" type="button" @click="emit('reset')">重置</button>
            <button class="mobile-filter-apply" type="button" @click="emit('close')">完成</button>
          </div>
        </div>
      </div>
    </Teleport>
</template>

<style scoped>
@import "@/assets/styles/student-info-view-scoped.css";
</style>
