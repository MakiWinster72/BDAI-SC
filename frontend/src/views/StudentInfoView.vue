<template>
  <main class="dashboard-right">
    <MobileCapsule @open-sidebar="openDashboardSidebar">
      <template #right>
        <button class="capsule-action student-capsule-btn is-filter" type="button" @click="openMobileFilter">
          <span class="capsule-icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
              <path d="M22 3H2l8 9.46V19l4 2v-8.54L22 3z"/>
            </svg>
          </span>
          <span class="student-capsule-label">筛选</span>
          <span v-if="hasActiveFilters" class="capsule-filter-dot"></span>
        </button>
        <button class="capsule-action student-capsule-btn hide-on-mobile" type="button" @click="toggleGridView">
          <span class="capsule-icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="3" width="7" height="7"/>
              <rect x="14" y="3" width="7" height="7"/>
              <rect x="3" y="14" width="7" height="7"/>
              <rect x="14" y="14" width="7" height="7"/>
            </svg>
          </span>
          <span class="student-capsule-label">{{ gridViewOpen ? "列表" : "表格" }}</span>
        </button>
        <button
          v-if="gridViewOpen"
          class="capsule-action student-capsule-btn hide-on-mobile"
          :class="{ 'capsule-active': gridFullscreen }"
          type="button"
          @click="toggleGridFullscreen"
        >
          <span class="capsule-icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
              <path d="M8 3H5a2 2 0 00-2 2v3m18 0V5a2 2 0 00-2-2h-3m0 18h3a2 2 0 002-2v-3M3 16v3a2 2 0 002 2h3"/>
            </svg>
          </span>
          <span class="student-capsule-label">{{ gridFullscreen ? "退出" : "全屏" }}</span>
        </button>
        <button
          v-if="!gridViewOpen"
          class="capsule-action student-capsule-btn"
          :class="{ 'capsule-active': selectMenuOpen }"
          type="button"
          aria-label="选择学生"
          @click.stop="toggleSelectMenu"
        >
          <span class="capsule-icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="3" width="18" height="18" rx="2"/>
              <path d="M9 12l2 2 4-4"/>
            </svg>
          </span>
          <span class="student-capsule-label">选择</span>
        </button>
      </template>
    </MobileCapsule>

    <Teleport to="body">
      <Transition name="select-float">
        <div v-if="selectMenuOpen && !gridViewOpen" class="select-float-menu" @click.stop>
          <button
            class="select-float-btn"
            type="button"
            @click="handleSelectPage"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M8 6h13M8 12h13M8 18h13M3 6h.01M3 12h.01M3 18h.01"/>
            </svg>
            选择本页
          </button>
          <button
            class="select-float-btn"
            type="button"
            :disabled="selectAllLoading"
            @click="handleSelectAll"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M9 11l3 3L22 4"/>
              <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
            </svg>
            {{ selectAllLoading ? "选择中..." : "选择全部" }}
          </button>
        </div>
      </Transition>
    </Teleport>

    <header class="feed-header">
      <h1 class="feed-title">学生信息</h1>
    </header>

    <section class="info-shell student-right-stack">
      <StudentFilterBar
        v-show="!gridFullscreen"
        :filters="filters"
        :class-year-options="classYearOptions"
        :student-category-options="studentCategoryOptions"
        :available-majors="availableMajors"
        :special-student-type-options="specialStudentTypeOptions"
        :has-active-filters="hasActiveFilters"
        :loading="loading"
        :total-items="totalItems"
        :grid-view-open="gridViewOpen"
        :grid-loading="gridLoading"
        :grid-sheets="gridSheets"
        :grid-active-sheet="gridActiveSheet"
        :grid-row-count="gridActiveSheetData.rowData.length"
        @update-filter="updateStudentFilter"
        @reset-filters="resetFilters"
        @toggle-hmt="toggleHmt"
        @open-grid-field-dialog="openGridFieldDialog"
        @update-grid-active-sheet="updateGridActiveSheet"
      />

      <section class="card student-results-card">
        <StudentGridPanel
          v-if="gridViewOpen"
          ref="gridPanelRef"
          :row-data="gridActiveSheetData.rowData"
          :column-defs="gridActiveSheetData.colDefs"
          :default-col-def="gridDefaultColDef"
          :locale-text="gridLocaleText"
          :locale-text-func="gridLocaleTextFunc"
          :fullscreen="gridFullscreen"
        />
        <StudentListPanel
          v-else
          v-model:selected-ids="selectedIds"
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :loading="loading"
          :total-items="totalItems"
          :has-active-filters="hasActiveFilters"
          :select-all-loading="selectAllLoading"
          :students="pagedStudents"
          :total-pages="totalPages"
          :page-size-options="pageSizeOptions"
          :get-hk-mo-tw-label="getHkMoTwLabel"
          :get-special-student-type-label="getSpecialStudentTypeLabel"
          @select-current-page="selectCurrentPage"
          @select-all-filtered="selectAllFiltered"
          @open-detail="openDetail"
        />
      </section>
    </section>

    <!-- Floating action buttons -->
    <div ref="floatingRef" class="floating-actions">
      <Transition name="floating-action">
        <button
          v-if="hasSelection"
          class="floating-btn floating-btn-cancel"
          type="button"
          @click="cancelSelection"
        >
          取消选择
        </button>
      </Transition>
      <button
        v-if="hasSelection"
        class="floating-btn floating-btn-export"
        type="button"
        @click="openExportDialog"
      >
        {{ exportLabel }}
      </button>
    </div>

    <OverlayPanel
      :open="gridFieldDialogOpen"
      :closing="gridFieldDialogClosing"
      title="选择显示字段"
      aria-label="选择显示字段"
      size="wide"
      @close="closeGridFieldDialog"
    >
      <template #header>
        <div class="overlay-custom-header">
          <span class="overlay-custom-title">选择显示字段</span>
          <label class="export-all-toggle">
            <input
              type="checkbox"
              :checked="isAllSelected"
              @change="toggleAllSelections($event.target.checked)"
            />
            <span>全选</span>
          </label>
        </div>
      </template>
      <div class="export-dialog-body">
        <div v-for="group in exportGroups" :key="group.id" class="export-group">
          <label class="export-group-title">
            <span>{{ group.label }}</span>
            <input
              type="checkbox"
              :checked="isGroupChecked(group)"
              @change="toggleGroupSelection(group, $event.target.checked)"
            />
          </label>
          <div class="export-group-options">
            <template v-if="group.id === 'family'">
              <div
                v-for="(row, index) in familyRows"
                :key="`grid-family-row-${index}`"
                class="export-option-row"
              >
                <label
                  v-for="field in row"
                  :key="field.key"
                  class="export-option"
                >
                  <input
                    v-model="exportSelections[field.key]"
                    type="checkbox"
                  />
                  <span>{{ field.label }}</span>
                </label>
              </div>
            </template>
            <template v-else>
              <label
                v-for="field in group.fields"
                :key="field.key"
                class="export-option"
              >
                <input v-model="exportSelections[field.key]" type="checkbox" />
                <span>{{ field.label }}</span>
              </label>
            </template>
          </div>
        </div>
      </div>
    </OverlayPanel>

    <!-- Mobile Filter Sheet -->
    <Teleport to="body">
      <div v-if="mobileFilterOpen" class="sheet-overlay open" @click.self="closeMobileFilter">
        <div class="mobile-filter-sheet" role="dialog" aria-modal="true" aria-label="筛选" :style="filterSheetStyle">
          <div
            class="mobile-filter-handle"
            @touchstart="handleFilterTouchStart"
            @touchmove="handleFilterTouchMove"
            @touchend="handleFilterTouchEnd"
          ></div>
          <header
            class="mobile-filter-header"
            @touchstart="handleFilterTouchStart"
            @touchmove="handleFilterTouchMove"
            @touchend="handleFilterTouchEnd"
          >
            <h2 class="mobile-filter-title">筛选学生</h2>
            <button class="mobile-filter-close" type="button" @click="closeMobileFilter">
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
            <button class="mobile-filter-reset" type="button" @click="resetMobileFilters">重置</button>
            <button class="mobile-filter-apply" type="button" @click="closeMobileFilter">完成</button>
          </div>
        </div>
      </div>
    </Teleport>

    <transition name="publisher-backdrop">
      <div
        v-if="viewOpen"
        class="student-detail-backdrop"
        @click="closeView"
      ></div>
    </transition>
    <section
      class="student-detail-view"
      :class="{
        open: viewOpen,
        closing: viewClosing,
        split: achievementsOpen || achievementsClosing,
      }"
      :aria-hidden="!viewOpen"
    >
      <div class="student-detail-handle"></div>
      <header class="student-detail-header">
        <div class="student-detail-title">学生详情</div>
        <button class="student-detail-close" type="button" @click="closeView">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
            <path d="M18 6L6 18M6 6l12 12"/>
          </svg>
        </button>
      </header>
      <div v-if="viewLoading" class="empty-tip">加载中...</div>
      <StudentProfileEditor
        v-else-if="viewItem"
        ref="profileEditorRef"
        :student="viewItem"
        :resolve-media-url="resolveMediaUrl"
        :save-profile="saveViewProfile"
        :can-edit="profile.role === 'ADMIN'"
        :show-achievements="true"
        :editing="detailEditing"
        @saved="handleViewProfileSaved"
        @open-achievements="openAchievements"
        @start-edit="detailEditing = true"
        @cancel-edit="detailEditing = false"
      />
      <div v-if="!viewLoading && viewItem" class="student-detail-capsule">
        <template v-if="!detailEditing">
          <button class="capsule-action" type="button" @click="openAchievements">
            成果
          </button>
          <button class="capsule-action" type="button" @click="handleExportPdf">
            PDF
          </button>
          <button class="capsule-action" type="button" @click="detailEditing = true">
            编辑
          </button>
        </template>
        <template v-else>
          <button class="capsule-action" type="button" @click="handleCancelEdit">
            取消
          </button>
          <button class="capsule-action capsule-action-primary" type="button" @click="handleSaveProfile">
            保存
          </button>
        </template>
      </div>
    </section>

    <section
      class="student-achievements-view"
      :class="{ open: achievementsOpen, closing: achievementsClosing }"
      :aria-hidden="!achievementsOpen"
    >
      <div class="student-detail-handle"></div>
      <header class="student-detail-header">
        <div class="student-detail-title">个人成就</div>
        <button
          class="student-detail-close"
          type="button"
          @click="closeAchievements"
        >
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
            <path d="M18 6L6 18M6 6l12 12"/>
          </svg>
        </button>
      </header>
      <div class="student-achievements-body" v-if="viewItem">
        <iframe
          class="student-achievements-frame"
          :key="achievementUrl"
          :src="achievementUrl"
          title="学生个人成就"
        ></iframe>
      </div>
    </section>

    <StudentExportDialog
      :open="exportDialogOpen"
      preview-title="导出预览(仅显示前三人)"
      empty-message="没有获取到学生详情，请稍后再试。"
      :load-rows="loadExportRows"
      @close="closeExportDialog"
      @export-success="handleStudentExportSuccess"
    />

    <div
      :class="['sheet-overlay', { open: gridViewConfirmOpen }]"
      @click.self="closeGridViewConfirm"
    >
      <div class="sheet-modal" @click.stop>
        <header class="sheet-modal-header">
          <div class="sheet-modal-title">提示</div>
        </header>
        <div class="sheet-modal-body">
          当前学生数量为 {{ totalItems }} 人，表格视图加载大量数据可能会造成卡顿。<br />
          建议使用导出功能下载表格查看。<br />
          是否继续切换到表格视图？
        </div>
        <div class="sheet-modal-actions">
          <button class="ghost-button" type="button" @click="closeGridViewConfirm">
            取消
          </button>
          <button class="action-button" type="button" @click="confirmGridView">
            继续
          </button>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref, watch } from "vue";
import { useRouter, useRoute } from "vue-router";

import harmonyFontUrl from "@/assets/fonts/HarmonyOS_Sans_SC_Regular.ttf?url";
import harmonyFontBlackUrl from "@/assets/fonts/HarmonyOS_Sans_SC_Black.ttf?url";
import { getMenuLocation, isMenuEnabled } from "@/constants/menu";
import {
  getStudentProfileById,
  saveStudentProfileById,
  searchStudentProfiles,
} from "@/api/profile";
import MobileCapsule from "@/components/MobileCapsule.vue";
import StudentExportDialog from "@/components/StudentExportDialog.vue";
import StudentFilterBar from "@/components/StudentFilterBar.vue";
import StudentGridPanel from "@/components/StudentGridPanel.vue";
import StudentListPanel from "@/components/StudentListPanel.vue";
import { createAuditLog } from "@/api/auditLog";
import StudentProfileEditor from "@/components/StudentProfileEditor.vue";
import OverlayPanel from "@/components/OverlayPanel.vue";
import { navigateWithViewTransition } from "@/utils/viewTransition";
import { useDashboardShell } from "@/composables/useDashboardShell";
import { useToast } from "@/composables/useToast";
import { useStudentSearch } from "@/composables/useStudentSearch";
import { resolveMediaUrl } from "@/utils/media";
import { loadUser } from "@/utils/userStorage";
import {
  ACHIEVEMENT_EXPORT_CATEGORIES,
  buildPreviewSheets,
  checkStudentProfileNeedsDetail,
  createExportSelections,
  exportGroups,
  familyRows,
  fetchAchievementsForStudents,
  getSelectedExportKeys,
} from "@/utils/studentProfileExport";

const router = useRouter();
const route = useRoute();
const { openSidebar: openDashboardSidebar } = useDashboardShell();
const { success: toastSuccess } = useToast();

const profile = reactive(loadUser());
const activeMenu = ref("student-info");
const selectedIds = ref([]);
const currentPage = ref(1);
const pageInput = ref(null);
const students = ref([]);
const totalPages = ref(1);
const totalItems = ref(0);
const loading = ref(false);
const pageSizeOptions = [10, 20, 30, 50];
const pageSize = ref(10);
const viewOpen = ref(false);
const viewClosing = ref(false);
const viewItem = ref(null);
const viewLoading = ref(false);
const selectAllLoading = ref(false);
const exportDialogOpen = ref(false);
const gridViewOpen = ref(false);
const gridLoading = ref(false);
const gridDetailRows = ref([]);
const gridAchievementData = ref([]);
const gridFieldDialogOpen = ref(false);
const gridFieldDialogClosing = ref(false);
const gridActiveSheet = ref("main");
const gridFullscreen = ref(false);
const gridViewConfirmOpen = ref(false);
const gridPanelRef = ref(null);
const gridHasFullDetail = ref(false);
let gridRequestId = 0;
const achievementsOpen = ref(false);
const achievementsClosing = ref(false);
const sidebarOpen = ref(false);
const activeCategory = ref("all");
const mobileFilterOpen = ref(false);
const selectMenuOpen = ref(false);
const {
  filters,
  classYearOptions,
  studentCategoryOptions,
  specialStudentTypeOptions,
  availableMajors,
  hasActiveFilters,
  toggleHmt,
  resetFilters,
  buildSearchParams,
  getSpecialStudentTypeLabel,
} = useStudentSearch();

function updateStudentFilter({ key, value }) {
  filters[key] = value;
}

function updateGridActiveSheet(sheetId) {
  gridActiveSheet.value = sheetId;
}

// Mobile filter sheet drag to close
const filterTouchStartY = ref(0);
const filterTouchCurrentY = ref(0);
const filterIsDragging = ref(false);
const filterDragTranslateY = ref(0);
const FILTER_DRAG_THRESHOLD = 80;

function toggleSelectMenu() {
  selectMenuOpen.value = !selectMenuOpen.value;
}

function closeSelectMenu() {
  selectMenuOpen.value = false;
}

function handleSelectPage() {
  closeSelectMenu();
  selectCurrentPage();
}

function handleSelectAll() {
  closeSelectMenu();
  selectAllFiltered();
}

function onDocumentClick(e) {
  if (!selectMenuOpen.value) return;
  const menu = document.querySelector(".select-float-menu");
  if (menu && !menu.contains(e.target)) {
    closeSelectMenu();
  }
}

function openMobileFilter() {
  mobileFilterOpen.value = true;
  document.body.style.overflow = "hidden";
}

function closeMobileFilter() {
  mobileFilterOpen.value = false;
  document.body.style.overflow = "";
}

function resetMobileFilters() {
  resetFilters();
}

const filterSheetStyle = computed(() => ({
  transform: filterDragTranslateY.value > 0
    ? `translateY(${filterDragTranslateY.value}px) scale(${1 - filterDragTranslateY.value / 2000})`
    : "",
  transition: filterIsDragging.value ? "none" : "",
  "transform-origin": "bottom center",
}));

function handleFilterTouchStart(e) {
  filterTouchStartY.value = e.touches[0].clientY;
  filterTouchCurrentY.value = filterTouchStartY.value;
  filterIsDragging.value = true;
}

function handleFilterTouchMove(e) {
  if (!filterIsDragging.value) return;
  const delta = e.touches[0].clientY - filterTouchStartY.value;
  if (delta > 0) {
    filterDragTranslateY.value = delta;
  }
}

function handleFilterTouchEnd() {
  if (!filterIsDragging.value) return;
  if (filterDragTranslateY.value > FILTER_DRAG_THRESHOLD) {
    closeMobileFilter();
  }
  filterIsDragging.value = false;
  filterDragTranslateY.value = 0;
}

const gridDefaultColDef = {
  sortable: true,
  filter: true,
  resizable: true,
  minWidth: 90,
  flex: 1,
};

const gridLocaleText = {
  // 过滤器与菜单
  page: "页",
  more: "更多",
  to: "至",
  of: "共",
  next: "下一页",
  last: "末页",
  first: "首页",
  previous: "上一页",
  loadingOoo: "加载中...",
  selectAll: "全选",
  searchOoo: "搜索...",
  blank: "空值",
  notBlank: "非空",
  filterOoo: "筛选...",
  applyFilter: "应用筛选",
  equals: "等于",
  notEqual: "不等于",
  contains: "包含",
  notContains: "不包含",
  startsWith: "以...开头",
  endsWith: "以...结尾",
  lessThan: "小于",
  greaterThan: "大于",
  lessThanOrEqual: "小于等于",
  greaterThanOrEqual: "大于等于",
  inRange: "范围",
  setFilter: "集合筛选",
  columns: "列",
  filters: "筛选",
  reset: "重置",
  group: "分组",
  rowGroupColumnsEmptyMessage: "拖拽列到这里进行分组",
  pivotColumnsEmptyMessage: "拖拽列到这里进行透视",
  noRowsToShow: "暂无数据",
  // TODO: 翻译“Page Size”
  // 聚合
  sum: "求和",
  min: "最小值",
  max: "最大值",
  none: "无",
  count: "计数",
  avg: "平均值",
  // 其他
  copy: "复制",
  copyWithHeaders: "复制（含表头）",
  paste: "粘贴",
  export: "导出",
  csvExport: "导出 CSV",
  excelExport: "导出 Excel",
};

const gridLocaleTextFunc = (key, defaultValue) => {
  if (key in gridLocaleText) {
    return gridLocaleText[key];
  }
  return defaultValue;
};

const exportSelections = reactive(createExportSelections());

const pagedStudents = computed(() => students.value);
const exportDisabled = computed(() => selectedIds.value.length === 0);
const hasSelection = computed(() => selectedIds.value.length > 0);
const exportLabel = computed(() => {
  const count = selectedIds.value.length;
  return count ? `导出(${count})` : "导出";
});

function cancelSelection() {
  selectedIds.value = [];
}

const achievementUrl = computed(() => {
  if (!viewItem.value) {
    return "/achievements?category=all";
  }
  const params = new URLSearchParams();
  params.set("category", "all");
  if (viewItem.value.studentNo) {
    params.set("studentNo", viewItem.value.studentNo);
  }
  if (viewItem.value.fullName) {
    params.set("studentName", viewItem.value.fullName);
  }
  params.set("embed", "1");
  return `/achievements?${params.toString()}`;
});

watch(
  () => ({
    classYear: filters.classYear,
    studentCategory: filters.studentCategory,
    major: filters.major,
    classNo: filters.classNo,
    isHk: filters.isHk,
    isMo: filters.isMo,
    isTw: filters.isTw,
    specialStudentType: filters.specialStudentType,
    keyword: filters.keyword,
  }),
  () => {
    currentPage.value = 1;
    pageInput.value = null;
    selectedIds.value = [];
    if (gridViewOpen.value) {
      fetchGridStudents();
      return;
    }
    fetchStudents();
  },
  { deep: true },
);

watch(currentPage, () => {
  if (!gridViewOpen.value) {
    fetchStudents();
  }
});

watch(pageSize, () => {
  pageInput.value = null;
  if (gridViewOpen.value) {
    return;
  }
  if (currentPage.value === 1) {
    fetchStudents();
    return;
  }
  currentPage.value = 1;
});

function resetResults() {
  students.value = [];
  totalPages.value = 1;
  totalItems.value = 0;
}

async function fetchStudents() {
  loading.value = true;
  try {
    const { data } = await searchStudentProfiles(
      buildSearchParams(currentPage.value, pageSize.value),
    );
    students.value = (data?.items || []).map((item) => ({
      id: item.id,
      name: item.fullName || "未命名",
      avatarUrl: item.avatarUrl || "",
      className: buildClassName(item),
      gradeYear: item.classYear || "",
      college: item.college || "",
      major: item.classMajor || "",
      classNo: item.classNo || "",
      studentNo: item.studentNo || "",
      isHk: item.isHk || false,
      isMo: item.isMo || false,
      isTw: item.isTw || false,
      specialStudent: item.specialStudent || false,
      specialStudentType: item.specialStudentType || "",
    }));
    totalPages.value = Math.max(1, data?.totalPages || 1);
    totalItems.value = data?.total || 0;
  } catch {
    resetResults();
  } finally {
    loading.value = false;
  }
}

async function fetchGridStudents() {
  gridLoading.value = true;
  const requestId = ++gridRequestId;
  try {
    const size = 200;
    const { data } = await searchStudentProfiles(buildSearchParams(1, size));
    if (requestId !== gridRequestId) {
      return;
    }
    const items = [...(data?.items || [])];
    const pages = data?.totalPages || 1;
    for (let page = 2; page <= pages; page += 1) {
      const { data: pageData } = await searchStudentProfiles(
        buildSearchParams(page, size),
      );
      if (requestId !== gridRequestId) {
        return;
      }
      items.push(...(pageData?.items || []));
    }
    gridDetailRows.value = items;
    gridHasFullDetail.value = false;
    gridAchievementData.value = [];
    const selectedKeys = getSelectedExportKeys(exportSelections);
    if (checkStudentProfileNeedsDetail(selectedKeys) && items.length) {
      await fetchGridDetails(items, requestId);
    }
  } catch {
    gridDetailRows.value = [];
    gridAchievementData.value = [];
    gridHasFullDetail.value = false;
  } finally {
    if (requestId === gridRequestId) {
      gridLoading.value = false;
    }
  }
}

async function fetchGridDetails(items, requestId) {
  const results = await Promise.all(
    items.map((item) =>
      getStudentProfileById(item.id)
        .then(({ data }) => data || null)
        .catch(() => null),
    ),
  );
  if (requestId !== gridRequestId) {
    return;
  }
  const detailRows = results.filter(Boolean);
  if (detailRows.length) {
    gridDetailRows.value = detailRows;
    gridHasFullDetail.value = true;
  }
}

function toggleGridView() {
  if (!gridViewOpen.value && totalItems.value > 100) {
    gridViewConfirmOpen.value = true;
    return;
  }
  gridViewOpen.value = !gridViewOpen.value;
  if (gridViewOpen.value) {
    fetchGridStudents();
  }
}

function confirmGridView() {
  gridViewConfirmOpen.value = false;
  gridViewOpen.value = true;
  fetchGridStudents();
}

function closeGridViewConfirm() {
  gridViewConfirmOpen.value = false;
}

function toggleGridFullscreen() {
  const el = gridPanelRef.value?.element;
  if (!el) {
    gridFullscreen.value = !gridFullscreen.value;
    return;
  }
  const start = el.getBoundingClientRect();
  gridFullscreen.value = !gridFullscreen.value;
  nextTick(() => {
    const end = el.getBoundingClientRect();
    const dx = start.left - end.left;
    const dy = start.top - end.top;
    const sx = start.width / end.width;
    const sy = start.height / end.height;
    el.style.transformOrigin = "top left";
    el.style.transition = "transform 420ms cubic-bezier(0.22, 1, 0.36, 1)";
    el.style.transform = `translate(${dx}px, ${dy}px) scale(${sx}, ${sy})`;
    requestAnimationFrame(() => {
      el.style.transform = "";
    });
    const cleanup = () => {
      el.style.transition = "";
      el.style.transform = "";
      el.style.transformOrigin = "";
      el.removeEventListener("transitionend", cleanup);
    };
    el.addEventListener("transitionend", cleanup);
  });
}

function openGridFieldDialog() {
  gridFieldDialogOpen.value = true;
  gridFieldDialogClosing.value = false;
}

function closeGridFieldDialog() {
  gridFieldDialogOpen.value = false;
  gridFieldDialogClosing.value = true;
  setTimeout(() => {
    gridFieldDialogClosing.value = false;
  }, 260);
}

watch(
  () => gridActiveSheet.value,
  (sheetId) => {
    if (!sheetId.startsWith("achievement")) {
      return;
    }
    if (!gridDetailRows.value.length || gridAchievementData.value.length) {
      return;
    }
    fetchAchievementsForStudents(gridDetailRows.value).then((data) => {
      gridAchievementData.value = Array.isArray(data) ? data : [];
    });
  },
);

watch(
  exportSelections,
  () => {
    if (!gridViewOpen.value) {
      return;
    }
    const selectedKeys = getSelectedExportKeys(exportSelections);
    if (!checkStudentProfileNeedsDetail(selectedKeys)) {
      return;
    }
    if (gridHasFullDetail.value || !gridDetailRows.value.length) {
      return;
    }
    fetchGridDetails(gridDetailRows.value, gridRequestId);
  },
  { deep: true },
);

function openDetail(item) {
  viewOpen.value = true;
  viewClosing.value = false;
  viewLoading.value = true;
  viewItem.value = null;
  achievementsOpen.value = false;
  achievementsClosing.value = false;
  getStudentProfileById(item.id)
    .then(({ data }) => {
      viewItem.value = data || null;
    })
    .catch(() => {
      viewItem.value = null;
    })
    .finally(() => {
      viewLoading.value = false;
    });
}

function closeView() {
  if (achievementsOpen.value) {
    closeAchievements();
  }
  viewOpen.value = false;
  viewClosing.value = true;
  detailEditing.value = false;
  setTimeout(() => {
    viewItem.value = null;
    viewLoading.value = false;
    viewClosing.value = false;
  }, 260);
}

function openAchievements() {
  if (!viewItem.value) {
    return;
  }
  achievementsOpen.value = true;
  achievementsClosing.value = false;
}

function closeAchievements() {
  achievementsOpen.value = false;
  achievementsClosing.value = true;
  setTimeout(() => {
    achievementsClosing.value = false;
  }, 260);
}

function saveViewProfile(payload) {
  if (!viewItem.value?.id) {
    throw new Error("缺少学生档案 ID");
  }
  return saveStudentProfileById(viewItem.value.id, payload);
}

function getHkMoTwLabel(item) {
  const parts = [];
  if (item.isHk) parts.push("香港");
  if (item.isMo) parts.push("澳门");
  if (item.isTw) parts.push("台湾");
  return parts.join(" / ");
}

function handleViewProfileSaved(data) {
  if (!data) {
    return;
  }
  detailEditing.value = false;
  viewItem.value = data;
  const nextClassName = buildClassName(data);
  students.value = students.value.map((item) => {
    if (String(item.id) !== String(data.id)) {
      return item;
    }
    return {
      ...item,
      name: data.fullName || "未命名",
      className: nextClassName,
      gradeYear: data.classYear || "",
      college: data.college || "",
      major: data.classMajor || "",
      classNo: data.classNo || "",
      studentNo: data.studentNo || "",
      isHk: data.isHk || false,
      isMo: data.isMo || false,
      isTw: data.isTw || false,
      specialStudent: data.specialStudent || false,
      specialStudentType: data.specialStudentType || "",
    };
  });
}

onMounted(async () => {
  document.addEventListener("click", onDocumentClick);
  const keywordParam = route.query.keyword;
  if (keywordParam && typeof keywordParam === "string") {
    filters.keyword = keywordParam;
    await fetchStudents();
    if (students.value.length === 1) {
      openDetail(students.value[0]);
    }
    return;
  }
  fetchStudents();
});

function setPage(page) {
  const p = Math.min(Math.max(1, page), totalPages.value);
  currentPage.value = p;
  pageInput.value = null;
}

function applyPageInput() {
  if (!pageInput.value) {
    return;
  }
  const safePage = Math.min(Math.max(1, pageInput.value), totalPages.value);
  currentPage.value = safePage;
}

function formatDateOrEmpty(dateValue, statusFlag, statusText) {
  if (statusFlag) {
    return statusText;
  }
  return dateValue || "";
}

function buildClassName(item) {
  if (!item) {
    return "";
  }
  if (item.className) {
    return item.className;
  }
  const safeYear = item.classYear ? `${item.classYear}级` : "";
  const safeMajor = item.classMajor || "";
  const safeNo = item.classNo ? `${item.classNo}班` : "";
  return `${safeYear}${safeMajor}${safeNo}`.trim();
}

function selectCurrentPage() {
  selectedIds.value = pagedStudents.value.map((item) => item.id);
}

async function selectAllFiltered() {
  if (selectAllLoading.value) {
    return;
  }
  selectAllLoading.value = true;
  try {
    const total = totalItems.value || 0;
    if (!total) {
      selectedIds.value = [];
      return;
    }
    const size = Math.min(total, 500);
    const { data } = await searchStudentProfiles(buildSearchParams(1, size));
    const ids = (data?.items || []).map((item) => item.id);
    const pages = data?.totalPages || 1;
    if (pages > 1) {
      const restPages = [];
      for (let page = 2; page <= pages; page += 1) {
        restPages.push(
          searchStudentProfiles(buildSearchParams(page, size)).then(
            ({ data: pageData }) => pageData?.items || [],
          ),
        );
      }
      const moreItems = (await Promise.all(restPages)).flat();
      ids.push(...moreItems.map((item) => item.id));
    }
    selectedIds.value = Array.from(new Set(ids));
  } finally {
    selectAllLoading.value = false;
  }
}

const achievementEntries = computed(() => [
  { key: "all", label: "全部" },
  ...ACHIEVEMENT_EXPORT_CATEGORIES,
]);

const gridSelectedKeys = computed(() => getSelectedExportKeys(exportSelections));
const gridSheets = computed(() =>
  buildPreviewSheets(
    gridDetailRows.value,
    gridSelectedKeys.value,
    gridAchievementData.value,
  ),
);

const gridActiveSheetData = computed(() => {
  const sheet =
    gridSheets.value.find((item) => item.id === gridActiveSheet.value) ||
    gridSheets.value[0];
  if (!sheet || !Array.isArray(sheet.table) || !sheet.table.length) {
    return { colDefs: [], rowData: [] };
  }
  const [header, ...rows] = sheet.table;
  const colDefs = header.map((label, index) => ({
    field: `col${index}`,
    headerName: label,
    minWidth: 120,
    flex: 1,
    sortable: true,
    filter: true,
  }));
  const rowData = rows.map((row) => {
    const obj = {};
    header.forEach((_, index) => {
      obj[`col${index}`] = row[index] ?? "";
    });
    return obj;
  });
  return { colDefs, rowData };
});

watch(
  () => gridSheets.value,
  (sheets) => {
    if (!sheets.length) {
      gridActiveSheet.value = "main";
      return;
    }
    if (!sheets.some((item) => item.id === gridActiveSheet.value)) {
      gridActiveSheet.value = sheets[0].id;
    }
  },
  { deep: true },
);

function openExportDialog() {
  exportDialogOpen.value = true;
}

function closeExportDialog() {
  exportDialogOpen.value = false;
}

async function handleStudentExportSuccess() {
  toastSuccess('学生信息已导出');
  try {
    await createAuditLog({ action: 'EXPORT_STUDENTS', detail: '导出学生信息' });
  } catch (e) {
    // Silently ignore — export succeeded regardless
  }
}

function isGroupSelected(group) {
  return group.fields.every((field) => exportSelections[field.key]);
}

function isGroupChecked(group) {
  return isGroupSelected(group);
}

const isAllSelected = computed(() =>
  exportGroups.every((group) =>
    group.fields.every((field) => exportSelections[field.key]),
  ),
);

function toggleGroupSelection(group, checked) {
  group.fields.forEach((field) => {
    exportSelections[field.key] = checked;
  });
}

function toggleAllSelections(checked) {
  exportGroups.forEach((group) => {
    group.fields.forEach((field) => {
      exportSelections[field.key] = checked;
    });
  });
}

async function loadExportRows(limit) {
  const ids =
    typeof limit === "number"
      ? selectedIds.value.slice(0, limit)
      : [...selectedIds.value];
  if (!ids.length) {
    return [];
  }
  const results = await Promise.all(
    ids.map((id) =>
      getStudentProfileById(id)
        .then(({ data }) => data || null)
        .catch(() => null),
    ),
  );
  return results.filter(Boolean);
}

function formatEducationText(items) {
  if (!Array.isArray(items) || items.length === 0) {
    return "";
  }
  return items
    .filter(Boolean)
    .map((item) => {
      const start = item.startDate || "";
      const end = item.isCurrent ? "至今" : item.endDate || "";
      const period = [start, end].filter(Boolean).join("~");
      const parts = [
        period,
        item.schoolName || "",
        item.educationLevel || "",
        item.witness || "",
      ].filter(Boolean);
      return parts.join(" / ");
    })
    .filter(Boolean)
    .join(" | ");
}

const roleLabelMap = {
  STUDENT: "学生",
  TEACHER: "教师",
  ADMIN: "管理员",
};

const roleLabel = computed(() => roleLabelMap[profile.role] || "学生");
const avatarText = computed(() => {
  const name = profile.displayName || profile.username || "同学";
  return name.slice(0, 1).toUpperCase();
});

const activeCategoryIndex = computed(() => {
  const index = achievementEntries.value.findIndex(
    (entry) => entry.key === activeCategory.value,
  );
  return index === -1 ? 0 : index;
});

const drawerIndicatorStyle = computed(() => ({
  transform: `translateY(calc(${activeCategoryIndex.value} * (var(--drawer-item-height) + var(--drawer-item-gap))))`,
}));

function handleMenuClick(key) {
  if (!isMenuEnabled(key)) {
    return;
  }
  if (key === "achievements") {
    navigateWithViewTransition(router, getMenuLocation(key));
    return;
  }
  navigateWithViewTransition(router, getMenuLocation(key));
}

function toggleAchievements() {
  if (!isMenuEnabled("achievements")) {
    return;
  }
  achievementsOpen.value = !achievementsOpen.value;
  activeMenu.value = "achievements";
  if (achievementsOpen.value) {
    handleAchievementEntry("all");
  }
}

function handleAchievementEntry(key) {
  if (!isMenuEnabled("achievements")) {
    return;
  }
  const safeKey = achievementEntries.value.some((entry) => entry.key === key)
    ? key
    : "all";
  activeCategory.value = safeKey;
  achievementsOpen.value = true;
  activeMenu.value = "achievements";
  sidebarOpen.value = false;
  navigateWithViewTransition(router, {
    path: "/achievements",
    query: { category: safeKey },
  });
}

function openSidebar() {
  sidebarOpen.value = true;
}

function closeSidebar() {
  sidebarOpen.value = false;
}

function goToSettings() {
  navigateWithViewTransition(router, "/settings");
}

function handleExportPdf() {
  if (profileEditorRef.value?.triggerPdfExport) {
    profileEditorRef.value.triggerPdfExport();
  }
}

function handleSaveProfile() {
  if (profileEditorRef.value?.triggerSave) {
    profileEditorRef.value.triggerSave();
  }
}

function handleCancelEdit() {
  if (profileEditorRef.value?.cancelEdit) {
    profileEditorRef.value.cancelEdit();
  }
  detailEditing.value = false;
}

const floatingRef = ref(null);
const floatingBottom = ref("24px");
const detailEditing = ref(false);
const profileEditorRef = ref(null);

function updateFloatingBottom() {
  const footer = document.querySelector(".dashboard-footer-wrap");
  if (!footer) return;
  const footerRect = footer.getBoundingClientRect();
  const viewH = window.innerHeight;
  if (footerRect.top < viewH) {
    floatingBottom.value = `${viewH - footerRect.top + 16}px`;
  } else {
    floatingBottom.value = "24px";
  }
}

onMounted(() => {
  window.addEventListener("scroll", updateFloatingBottom, { passive: true });
  window.addEventListener("resize", updateFloatingBottom);
  updateFloatingBottom();
});

onUnmounted(() => {
  document.removeEventListener("click", onDocumentClick);
  window.removeEventListener("scroll", updateFloatingBottom);
  window.removeEventListener("resize", updateFloatingBottom);
});
</script>

<style>
@import '@/assets/styles/student-info-view.css';
</style>

<style scoped>
/* Mobile capsule buttons — vertical icon+label layout matching AdminView */
.capsule-action.is-filter {
  position: relative;
}

.student-capsule-btn {
  flex-shrink: 0;
  flex-direction: column;
  gap: 1px;
  color: var(--primary);
  padding: 6px clamp(3px, 2.2vw, 10px);
  border: 1px solid rgba(100, 12, 114, 0.12);
}
.student-capsule-btn .capsule-icon {
  flex-shrink: 0;
}

.student-capsule-label {
  font-size: 9px;
  font-weight: 600;
  line-height: 1;
  letter-spacing: 0.02em;
}

.capsule-filter-dot {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--primary, #e74c3c);
}

/* Mobile filter sheet - bottom sheet style */
.mobile-filter-sheet {
  position: fixed;
  bottom: 16px;
  left: 16px;
  right: 16px;
  top: auto;
  max-width: 100%;
  max-height: 80vh;
  margin: 0 auto;
  background: var(--card, #fff);
  border-radius: 24px;
  display: flex;
  flex-direction: column;
  animation: sheet-slide-up 0.4s cubic-bezier(0.22, 1, 0.36, 1);
  box-shadow: 0 -4px 24px rgba(0, 0, 0, 0.12);
  will-change: transform;
}

@keyframes sheet-slide-up {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}

.mobile-filter-handle {
  width: 36px;
  height: 4px;
  background: var(--line, #ddd);
  border-radius: 2px;
  margin: 12px auto;
  flex-shrink: 0;
}

.mobile-filter-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px 16px;
  border-bottom: 1px solid var(--line, #f0f0f0);
  flex-shrink: 0;
}

.mobile-filter-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text, #1a1a1a);
  margin: 0;
}

.mobile-filter-close {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--bg-secondary, #f5f5f5);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary, #666);
  cursor: pointer;
}

.mobile-filter-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.mobile-filter-row {
  display: flex;
  gap: 12px;
}

.mobile-filter-section {
  margin-bottom: 20px;
}

.mobile-filter-section.half {
  flex: 1;
}

.mobile-filter-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary, #888);
  margin-bottom: 8px;
  display: block;
}

.mobile-filter-search {
  position: relative;
}

.mobile-filter-search-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-secondary, #aaa);
}

.mobile-filter-input {
  width: 100%;
  height: 44px;
  padding: 0 14px 0 40px;
  border: 1px solid var(--line, #e5e5e5);
  border-radius: 12px;
  font-size: 15px;
  background: var(--bg-secondary, #f8f8f8);
  color: var(--text, #1a1a1a);
  transition: border-color 0.2s, box-shadow 0.2s;
}

.mobile-filter-input:focus {
  outline: none;
  border-color: var(--primary, var(--primary));
  box-shadow: 0 0 0 3px rgba(100, 12, 114, 0.1);
  background: var(--card, #fff);
}

.mobile-filter-select-wrap {
  position: relative;
}

.mobile-filter-select {
  width: 100%;
  height: 44px;
  padding: 0 36px 0 14px;
  border: 1px solid var(--line, #e5e5e5);
  border-radius: 12px;
  font-size: 15px;
  background: var(--bg-secondary, #f8f8f8);
  color: var(--text, #1a1a1a);
  appearance: none;
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.mobile-filter-select:focus {
  outline: none;
  border-color: var(--primary, var(--primary));
  box-shadow: 0 0 0 3px rgba(100, 12, 114, 0.1);
  background: var(--card, #fff);
}

.mobile-filter-select:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.mobile-filter-select-arrow {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-secondary, #aaa);
  pointer-events: none;
}

.mobile-filter-chips {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.mobile-filter-chip {
  display: inline-flex;
  align-items: center;
  padding: 8px 16px;
  background: var(--bg-secondary, #f0f0f0);
  border-radius: 20px;
  font-size: 14px;
  color: var(--text-secondary, #666);
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.mobile-filter-chip:hover {
  background: var(--bg-secondary, #e8e8e8);
}

.mobile-filter-chip.active {
  background: var(--primary, var(--primary));
  color: #fff;
}

.mobile-filter-footer {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  padding-bottom: max(16px, env(safe-area-inset-bottom));
  border-top: 1px solid var(--line, #f0f0f0);
  border-radius: 0 0 24px 24px;
  flex-shrink: 0;
  background: var(--card, #fff);
}

.mobile-filter-reset {
  flex: 0 0 auto;
  height: 46px;
  padding: 0 20px;
  border: 1px solid var(--line, #e5e5e5);
  border-radius: 23px;
  background: var(--card, #fff);
  font-size: 15px;
  font-weight: 500;
  color: var(--text-secondary, #666);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
}

.mobile-filter-reset:hover {
  border-color: var(--text-secondary, #999);
  color: var(--text, #333);
}

.mobile-filter-apply {
  flex: 1;
  height: 46px;
  border: none;
  border-radius: 23px;
  background: linear-gradient(135deg, var(--primary, var(--primary)) 0%, var(--primary-dark, var(--primary-dark)) 100%);
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  box-shadow: 0 4px 12px rgba(100, 12, 114, 0.3);
}

.mobile-filter-apply:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(100, 12, 114, 0.4);
}

.mobile-filter-apply:active {
  transform: translateY(0);
}

/* Floating select menu - right side, selection actions */
.select-float-menu {
  position: fixed;
  bottom: calc(90px + env(safe-area-inset-bottom, 0px));
  right: 20px;
  display: flex;
  flex-direction: row;
  gap: 8px;
  z-index: 56;
}

.select-float-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 40px;
  padding: 0 16px;
  border: 1px solid rgba(100, 12, 114, 0.15);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  color: var(--primary);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  box-shadow: 0 2px 12px rgba(100, 12, 114, 0.1);
  transition: all 0.2s;
  -webkit-tap-highlight-color: transparent;
}

.select-float-btn:hover {
  background: rgba(255, 255, 255, 0.98);
  border-color: var(--primary);
  box-shadow: 0 4px 16px rgba(100, 12, 114, 0.15);
}

.select-float-btn:active {
  transform: scale(0.96);
}

.select-float-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Transition */
.select-float-enter-active {
  transition:
    opacity 0.22s ease,
    transform 0.28s cubic-bezier(0.22, 1, 0.36, 1);
}

.select-float-leave-active {
  transition:
    opacity 0.18s ease,
    transform 0.22s cubic-bezier(0.22, 1, 0.36, 1);
}

.select-float-enter-from {
  opacity: 0;
  transform: translateY(8px) scale(0.94);
}

.select-float-leave-to {
  opacity: 0;
  transform: translateY(4px) scale(0.96);
}

/* Hide desktop filter and results actions on mobile */
@media (max-width: 768px) {
  .student-filter-card {
    display: none;
  }
  .student-results-actions {
    display: none;
  }
}

/* Mobile capsule: hide grid/fullscreen buttons, wrap filter & select */
@media (max-width: 768px) {
  .capsule-right {
    flex-wrap: wrap;
    justify-content: flex-end;
    gap: 8px;
    padding: 8px 12px 8px 4px;
  }

  .hide-on-mobile {
    display: none !important;
  }

  .student-capsule-btn {
    flex-direction: row;
    gap: 6px;
    padding: 8px 14px;
    border-radius: 999px;
  }

  .student-capsule-label {
    font-size: 13px;
    font-weight: 600;
    letter-spacing: 0;
  }

  .student-capsule-btn .capsule-icon {
    width: 18px;
    height: 18px;
  }

  .student-capsule-btn .capsule-icon svg {
    width: 18px;
    height: 18px;
  }
}

.student-results-meta {
  display: none;
}

@media (max-width: 768px) {
  .student-results-meta {
    display: block;
    font-size: 13px;
    color: var(--text-sub);
    padding: 0 0 12px;
  }
}

/* Student detail capsule - hidden on desktop */
.student-detail-capsule {
  display: none;
}

@media (max-width: 768px) {
  .student-detail-capsule {
    position: fixed;
    bottom: calc(20px + env(safe-area-inset-bottom));
    left: 50%;
    transform: translateX(-50%);
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 8px 12px;
    background: var(--card);
    backdrop-filter: blur(15px) saturate(140%);
    -webkit-backdrop-filter: blur(15px) saturate(140%);
    border-radius: 50px;
    border: 2px solid rgba(100, 12, 114, 0.15);
    box-shadow: 0 4px 20px rgba(100, 12, 114, 0.15);
    z-index: 56;
  }

  .student-detail-capsule .capsule-action {
    height: 36px;
    padding: 0 16px;
    border-radius: 999px;
    border: none;
    background: transparent;
    color: var(--primary);
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
    display: inline-flex;
    align-items: center;
    justify-content: center;
  }

  .student-detail-capsule .capsule-action:active {
    transform: scale(0.95);
  }

  .student-detail-capsule .capsule-action-primary {
    background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
    color: #fff;
    box-shadow: 0 2px 8px rgba(100, 12, 114, 0.25);
  }
}

/* Student profile editor bottom padding for capsule */
@media (max-width: 768px) {
  .info-shell.student-profile-editor {
    padding-bottom: calc(80px + env(safe-area-inset-bottom, 0px));
  }
}
</style>
