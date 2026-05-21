<template>
  <main class="dashboard-right">
    <MobileCapsule @open-sidebar="openDashboardSidebar">
      <template #right>
        <StudentInfoCapsuleToolbar
          :has-active-filters="hasActiveFilters"
          :grid-view-open="gridViewOpen"
          :grid-fullscreen="gridFullscreen"
          :select-menu-open="selectMenuOpen"
          :select-all-loading="selectAllLoading"
          @open-filter="openMobileFilter"
          @toggle-grid="toggleGridView"
          @toggle-fullscreen="toggleGridFullscreen"
          @toggle-select-menu="toggleSelectMenu"
          @close-select-menu="selectMenuOpen = false"
          @select-page="handleSelectPage"
          @select-all="handleSelectAll"
        />
      </template>
    </MobileCapsule>

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
          :locale-text="gridLocaleTextMap"
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

    <StudentFloatingExportActions
      :visible="hasSelection"
      :export-label="exportLabel"
      :bottom="floatingBottom"
      @cancel="cancelSelection"
      @export="openExportDialog"
    />

    <StudentGridFieldDialog
      :open="gridFieldDialogOpen"
      :closing="gridFieldDialogClosing"
      :export-selections="exportSelections"
      :export-groups="exportGroups"
      :family-rows="familyRows"
      :is-all-selected="isAllSelected"
      :is-group-checked="isGroupChecked"
      @close="closeGridFieldDialog"
      @toggle-all="toggleAllSelections"
      @toggle-group="toggleGroupSelection"
    />

    <StudentMobileFilterSheet
      :open="mobileFilterOpen"
      :sheet-style="filterSheetStyle"
      :filters="filters"
      :class-year-options="classYearOptions"
      :student-category-options="studentCategoryOptions"
      :available-majors="availableMajors"
      :special-student-type-options="specialStudentTypeOptions"
      @close="closeMobileFilter"
      @reset="resetMobileFilters"
      @touchstart="handleFilterTouchStart"
      @touchmove="handleFilterTouchMove"
      @touchend="handleFilterTouchEnd"
    />

    <StudentDetailDrawer
      v-if="viewOpen || viewClosing"
      ref="detailDrawerRef"
      :open="viewOpen"
      :closing="viewClosing"
      :loading="viewLoading"
      :student="viewItem"
      :editing="detailEditing"
      :can-edit="profile.role === 'ADMIN'"
      :achievements-open="achievementsOpen"
      :achievements-closing="achievementsClosing"
      :resolve-media-url="resolveMediaUrl"
      :save-profile="saveViewProfile"
      @close="closeView"
      @saved="handleViewProfileSaved"
      @open-achievements="openAchievements"
      @export-pdf="handleExportPdf"
      @start-edit="detailEditing = true"
      @cancel-edit="handleCancelEdit"
      @save="handleSaveProfile"
    />

    <StudentAchievementsPanel
      :open="achievementsOpen"
      :closing="achievementsClosing"
      :achievement-url="achievementUrl"
      :has-student="Boolean(viewItem)"
      @close="closeAchievements"
    />

    <StudentExportDialog
      :open="exportDialogOpen"
      preview-title="导出预览(仅显示前三人)"
      empty-message="没有获取到学生详情，请稍后再试。"
      :load-rows="loadExportRows"
      @close="closeExportDialog"
      @export-success="handleStudentExportSuccess"
    />

    <StudentGridViewConfirmSheet
      :open="gridViewConfirmOpen"
      :total-items="totalItems"
      @close="closeGridViewConfirm"
      @confirm="confirmGridView"
    />
  </main>
</template>

<script setup>import { computed, onMounted, onUnmounted, reactive, ref, watch, nextTick } from "vue";
import { useRoute } from "vue-router";
import MobileCapsule from "@/components/MobileCapsule.vue";
import StudentExportDialog from "@/components/StudentExportDialog.vue";
import StudentFilterBar from "@/components/student-info/StudentFilterBar.vue";
import StudentGridPanel from "@/components/student-info/StudentGridPanel.vue";
import StudentListPanel from "@/components/student-info/StudentListPanel.vue";
import StudentAchievementsPanel from "@/components/student-info/StudentAchievementsPanel.vue";
import StudentDetailDrawer from "@/components/student-info/StudentDetailDrawer.vue";
import StudentFloatingExportActions from "@/components/student-info/StudentFloatingExportActions.vue";
import StudentGridFieldDialog from "@/components/student-info/StudentGridFieldDialog.vue";
import StudentGridViewConfirmSheet from "@/components/student-info/StudentGridViewConfirmSheet.vue";
import StudentInfoCapsuleToolbar from "@/components/student-info/StudentInfoCapsuleToolbar.vue";
import StudentMobileFilterSheet from "@/components/student-info/StudentMobileFilterSheet.vue";
import { createAuditLog } from "@/api/auditLog";
import {
  getStudentProfileById,
  saveStudentProfileById,
  searchStudentProfiles,
} from "@/api/profile";
import { useDashboardShell } from "@/composables/useDashboardShell";
import { useMobileFilterSheet } from "@/composables/useMobileFilterSheet";
import { useStudentExportFields } from "@/composables/useStudentExportFields";
import { useStudentSearch } from "@/composables/useStudentSearch";
import { useToast } from "@/composables/useToast";
import {
  gridDefaultColDef,
  gridLocaleTextFunc,
  gridLocaleTextMap,
} from "@/config/studentGridConfig";
import {
  buildPreviewSheets,
  checkStudentProfileNeedsDetail,
  fetchAchievementsForStudents,
  getSelectedExportKeys,
} from "@/utils/studentProfileExport";
import {
  buildStudentClassName,
  getHkMoTwLabel,
  mapStudentListItem,
} from "@/utils/studentListDisplay";
import { resolveMediaUrl } from "@/utils/media";
import { loadUser } from "@/utils/userStorage";

const route = useRoute();
const { openSidebar: openDashboardSidebar } = useDashboardShell();
const { success: toastSuccess } = useToast();

const profile = reactive(loadUser());
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
const selectMenuOpen = ref(false);
const detailEditing = ref(false);
const detailDrawerRef = ref(null);
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

const {
  open: mobileFilterOpen,
  sheetStyle: filterSheetStyle,
  openSheet: openMobileFilter,
  closeSheet: closeMobileFilter,
  handleTouchStart: handleFilterTouchStart,
  handleTouchMove: handleFilterTouchMove,
  handleTouchEnd: handleFilterTouchEnd,
} = useMobileFilterSheet();

const {
  exportSelections,
  exportGroups,
  familyRows,
  isAllSelected,
  isGroupChecked,
  toggleGroupSelection,
  toggleAllSelections,
} = useStudentExportFields();

function resetMobileFilters() {
  resetFilters();
}

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

function updateStudentFilter({ key, value }) {
  filters[key] = value;
}

function updateGridActiveSheet(sheetId) {
  gridActiveSheet.value = sheetId;
}

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
    students.value = (data?.items || []).map(mapStudentListItem);
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


function handleViewProfileSaved(data) {
  if (!data) {
    return;
  }
  detailEditing.value = false;
  viewItem.value = data;
  const nextClassName = buildStudentClassName(data);
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

function handleExportPdf() {
  detailDrawerRef.value?.triggerPdfExport?.();
}

function handleSaveProfile() {
  detailDrawerRef.value?.triggerSave?.();
}

function handleCancelEdit() {
  detailDrawerRef.value?.cancelEdit?.();
  detailEditing.value = false;
}

const floatingBottom = ref("24px");

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
  window.removeEventListener("scroll", updateFloatingBottom);
  window.removeEventListener("resize", updateFloatingBottom);
});
</script>

<style>
@import '@/assets/styles/student-info-view.css';
</style>

<style scoped>
@import "@/assets/styles/student-info-view-scoped.css";
</style>
