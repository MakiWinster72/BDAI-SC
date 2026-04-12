<template>
  <main class="dashboard-right">
    <header class="feed-header">
      <h1 class="feed-title">学生信息</h1>
    </header>

    <section class="info-shell student-right-stack">
      <section v-show="!gridFullscreen" class="card student-filter-card">
        <div class="student-filter-toolbar">
          <div class="student-filter-intro">
            <div class="info-section-title">搜索与筛选</div>
            <div class="student-filter-caption">
              {{ hasActiveFilters ? "已应用筛选条件" : "按姓名、学号、班级快速定位学生" }}
            </div>
          </div>
          <div class="student-filter-search-wrap">
            <input
              v-model="filters.keyword"
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
              @click="resetFilters"
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
              <select v-model="filters.classYear" class="info-input">
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
              <select v-model="filters.studentCategory" class="info-input">
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
              <select v-model="filters.major" class="info-input" :disabled="!filters.studentCategory">
                <option value="">全部</option>
                <option
                  v-for="major in availableMajors"
                  :key="major"
                  :value="major"
                >
                  {{ major }}
                </option>
              </select>
            </div>

            <div class="student-filter-panel student-filter-field-class">
              <div class="student-filter-panel-head">
                <span class="info-label">班级</span>
              </div>
              <StepperInput
                v-model="filters.classNo"
                :min="1"
                :max="10"
                wrap
                placeholder="全部"
              />
            </div>
          </div>

          <div class="student-filter-meta">
            <div class="student-filter-flags">
              <label class="info-choice">
                <input v-model="filters.isHkMoTw" type="checkbox" />
                港澳台
              </label>
              <label class="info-choice">
                <input v-model="filters.isSpecial" type="checkbox" />
                特殊学生
              </label>
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
            @click="openGridFieldDialog"
            title="选择字段"
          >
            +
          </button>
          <button
            v-for="sheet in gridSheets"
            :key="sheet.id"
            class="student-grid-tab"
            :class="{ active: sheet.id === gridActiveSheet }"
            type="button"
            @click="gridActiveSheet = sheet.id"
          >
            {{ sheet.label }}
          </button>
          <span v-if="gridLoading" class="student-grid-status">加载中...</span>
          <span v-else class="student-grid-status"
            >共 {{ gridActiveSheetData.rowData.length }} 条</span
          >
        </div>
      </section>

      <section class="card student-results-card">
        <div v-if="!gridViewOpen" class="student-results-header">
          <div class="info-section-title">筛选结果</div>
          <div class="student-results-actions">
            <button
              class="ghost-button"
              type="button"
              @click="selectCurrentPage"
            >
              选择本页
            </button>
            <button
              class="ghost-button"
              type="button"
              :disabled="selectAllLoading"
              @click="selectAllFiltered"
            >
              {{ selectAllLoading ? "选择中..." : "选择全部" }}
            </button>
          </div>
        </div>
        <div
          v-if="gridViewOpen"
          ref="gridWrapRef"
          class="student-grid-wrap"
          :class="{ fullscreen: gridFullscreen }"
        >
          <AgGridVue
            class="ag-theme-quartz student-grid"
            :row-data="gridActiveSheetData.rowData"
            :column-defs="gridActiveSheetData.colDefs"
            :default-col-def="gridDefaultColDef"
            :locale-text="gridLocaleText"
            :locale-text-func="gridLocaleTextFunc"
            :animate-rows="true"
            :pagination="true"
            :pagination-page-size="100"
            :suppress-cell-focus="true"
          />
        </div>
        <div v-else-if="loading" class="empty-tip student-results-loading">
          加载学生信息中...
        </div>
        <div v-else-if="pagedStudents.length" class="student-list">
          <div v-for="item in pagedStudents" :key="item.id" class="student-row">
            <input v-model="selectedIds" type="checkbox" :value="item.id" />
            <div class="student-main">
              <div class="student-name">{{ item.name }}</div>
              <div class="student-meta">
                {{ item.gradeYear }}级 {{ item.major }}{{ item.classNo }}班
                {{ item.studentNo }}
              </div>
            </div>
            <button
              class="ghost-button"
              type="button"
              @click="openDetail(item)"
            >
              详情
            </button>
          </div>
        </div>
        <div v-else class="empty-tip">没有匹配的学生。</div>

        <div v-if="!gridViewOpen">
          <PaginationBar
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total-pages="totalPages"
            :page-size-options="pageSizeOptions"
            mode="full"
          />
        </div>
      </section>
    </section>

    <!-- Floating action buttons -->
    <div class="floating-actions">
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
      <button
        class="floating-btn floating-btn-toggle"
        type="button"
        @click="toggleGridView"
        :title="gridViewOpen ? '切换列表视图' : '切换表格视图'"
      >
        <span class="floating-toggle-icon" aria-hidden="true">
          <svg viewBox="0 0 24 24" focusable="false">
            <path
              d="M7 7h10v3h2V5H5v5h2V7zm10 10H7v-3H5v5h14v-5h-2v3zM9 10l-3 2 3 2v-4zm6 4 3-2-3-2v4z"
              fill="currentColor"
            />
          </svg>
        </span>
        {{ gridViewOpen ? "切换列表" : "切换表格" }}
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
      <header class="publisher-header">
        <div class="publisher-title">学生详情</div>
        <button class="publisher-close" type="button" @click="closeView">
          关闭
        </button>
      </header>
      <div v-if="viewLoading" class="empty-tip">加载中...</div>
      <StudentProfileEditor
        v-else-if="viewItem"
        :student="viewItem"
        :resolve-media-url="resolveMediaUrl"
        :save-profile="saveViewProfile"
        :can-edit="profile.role === 'ADMIN'"
        :show-achievements="true"
        @saved="handleViewProfileSaved"
        @open-achievements="openAchievements"
      />
    </section>

    <section
      class="student-achievements-view"
      :class="{ open: achievementsOpen, closing: achievementsClosing }"
      :aria-hidden="!achievementsOpen"
    >
      <header class="publisher-header">
        <div class="publisher-title">个人成就</div>
        <button
          class="publisher-close"
          type="button"
          @click="closeAchievements"
        >
          关闭
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
      filename-prefix="students_export"
      preview-title="导出预览(仅显示前三人)"
      empty-message="没有获取到学生详情，请稍后再试。"
      :load-rows="loadExportRows"
      @close="closeExportDialog"
      @export-success="toastSuccess('学生信息已导出')"
    />

    <MobileCapsule @open-sidebar="openDashboardSidebar">
      <template #right>
        <button class="capsule-action" type="button" @click="toggleGridView">
          {{ gridViewOpen ? "列表" : "表格" }}
        </button>
        <button
          v-if="gridViewOpen"
          class="capsule-action"
          :class="{ 'capsule-active': gridFullscreen }"
          type="button"
          @click="toggleGridFullscreen"
        >
          {{ gridFullscreen ? "退出" : "全屏" }}
        </button>
      </template>
    </MobileCapsule>

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
import { computed, onMounted, reactive, ref, watch } from "vue";
import { AgGridVue } from "ag-grid-vue3";
import "ag-grid-community/styles/ag-grid.css";
import "ag-grid-community/styles/ag-theme-quartz.css";
import { useRouter, useRoute } from "vue-router";

import harmonyFontUrl from "../assets/fonts/HarmonyOS_Sans_SC_Regular.ttf?url";
import harmonyFontBlackUrl from "../assets/fonts/HarmonyOS_Sans_SC_Black.ttf?url";
import { getMenuLocation, isMenuEnabled } from "../constants/menu";
import {
  getStudentProfileById,
  saveStudentProfileById,
  searchStudentProfiles,
} from "../api/profile";
import { listAchievements } from "../api/achievement";
import MobileCapsule from "../components/MobileCapsule.vue";
import StudentExportDialog from "../components/StudentExportDialog.vue";
import StudentProfileEditor from "../components/StudentProfileEditor.vue";
import PaginationBar from "../components/PaginationBar.vue";
import StepperInput from "../components/StepperInput.vue";
import OverlayPanel from "../components/OverlayPanel.vue";
import { navigateWithViewTransition } from "../utils/viewTransition";
import { useDashboardShell } from "../composables/useDashboardShell";
import { useToast } from "../composables/useToast";
import { resolveMediaUrl } from "../utils/media";
import { loadUser } from "../utils/userStorage";

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
const gridWrapRef = ref(null);
const gridHasFullDetail = ref(false);
let gridRequestId = 0;
const achievementsOpen = ref(false);
const achievementsClosing = ref(false);
const sidebarOpen = ref(false);
const activeCategory = ref("all");

const classYearOptions = Array.from({ length: 11 }, (_, index) => 2020 + index);
const majorOptions = [
  "计算机科学与技术",
  "计算机科学与技术（实验区）",
  "软件工程",
  "电子商务",
  "大数据管理与应用（佛山校区全学段）",
  "大数据管理与应用（数字治理）",
];

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

const filters = reactive({
  classYear: "",
  studentCategory: "",
  major: "",
  classNo: "",
  isHkMoTw: false,
  isSpecial: false,
  keyword: "",
});

const studentCategoryOptions = ["本科生", "研究生"];

const majorOptionsByCategory = {
  本科生: [
    "计算机科学与技术",
    "计算机科学与技术（实验区）",
    "计算机科学与技术(中外联合培养项目班)",
    "2025计算机科学与技术（中外联合培养项目班未赴国外学习）",
    "软件工程",
    "人工智能",
    "电子商务",
    "电子商务（大数据决策分析）",
    "大数据管理与应用",
    "大数据管理与应用（佛山校区全学段）",
    "大数据管理与应用（数字治理）",
  ],
  研究生: [
    "管理科学与工程",
    "技术经济及管理",
    "智能科学与技术",
    "计算机技术",
    "图书情报",
  ],
};

const exportGroups = [
  {
    id: "base",
    label: "基础信息",
    fields: [
      { key: "name", label: "姓名", defaultSelected: true },
      { key: "className", label: "班级", defaultSelected: true },
      { key: "studentNo", label: "学号", defaultSelected: true },
    ],
  },
  {
    id: "school",
    label: "学籍信息",
    fields: [
      { key: "enrollmentDate", label: "入学时间" },
      { key: "studentCategory", label: "学生类别" },
      { key: "classTeacher", label: "班主任" },
      { key: "counselor", label: "辅导员" },
    ],
  },
  {
    id: "identity",
    label: "个人证件与联系方式",
    fields: [
      { key: "ethnicity", label: "民族" },
      { key: "politicalStatus", label: "政治面貌" },
      { key: "phone", label: "手机号码" },
      { key: "backupContact", label: "备用联系方式（QQ/邮箱）" },
      { key: "idNo", label: "身份证号" },
      { key: "nativePlace", label: "籍贯" },
      { key: "address", label: "住址" },
    ],
  },
  {
    id: "dorm",
    label: "住宿信息",
    fields: [
      { key: "offCampusLiving", label: "是否在外居住" },
      { key: "offCampusAddress", label: "外居住地址" },
      { key: "dormCampus", label: "住宿校区" },
      { key: "dormBuilding", label: "住宿楼栋" },
      { key: "dormRoom", label: "住宿房间" },
    ],
  },
  {
    id: "family",
    label: "家庭信息",
    fields: [
      { key: "fatherName", label: "父亲姓名" },
      { key: "fatherPhone", label: "父亲电话" },
      { key: "fatherWorkUnit", label: "父亲工作单位" },
      { key: "fatherTitle", label: "父亲职务" },
      { key: "motherName", label: "母亲姓名" },
      { key: "motherPhone", label: "母亲电话" },
      { key: "motherWorkUnit", label: "母亲工作单位" },
      { key: "motherTitle", label: "母亲职务" },
      { key: "hkMoTw", label: "港澳台" },
      { key: "specialStudent", label: "特殊学生" },
    ],
  },
  {
    id: "emergency",
    label: "紧急联系人",
    fields: [
      { key: "emergencyPhone", label: "紧急联系人电话" },
      { key: "emergencyRelation", label: "紧急联系人关系" },
    ],
  },
  {
    id: "education",
    label: "教育经历",
    fields: [
      { key: "eduPeriod", label: "时间段" },
      { key: "eduSchoolName", label: "学校名称" },
      { key: "eduEducationLevel", label: "学历" },
      { key: "eduWitness", label: "证明人" },
    ],
  },
  {
    id: "party",
    label: "团组织与入党信息",
    fields: [
      { key: "leagueJoined", label: "是否入团" },
      { key: "leagueApplicationDate", label: "提交入团申请书时间" },
      { key: "leagueJoinDate", label: "入团时间" },
      { key: "leagueNo", label: "团号" },
      { key: "partyApplied", label: "是否申请入党" },
      { key: "applicationDate", label: "提交入党申请书时间" },
      { key: "activistDate", label: "确定积极分子时间" },
      { key: "partyTrainingDate", label: "上党课时间" },
      { key: "developmentTargetDate", label: "确定发展对象时间" },
      { key: "probationaryMemberDate", label: "接收为预备党员时间" },
      { key: "fullMemberDate", label: "转为正式党员时间" },
    ],
  },
  {
    id: "achievement",
    label: "个人成就",
    fields: [
      { key: "ach_contest", label: "学科竞赛、文体艺术" },
      { key: "ach_paper", label: "发表学术论文" },
      { key: "ach_journal", label: "发表期刊作品" },
      { key: "ach_patent", label: "专利（著作权）授权数（项）" },
      { key: "ach_certificate", label: "职业资格证书" },
      { key: "ach_research", label: "学生参与教师科研项目情况" },
      { key: "ach_works", label: "创作、表演的代表性作品" },
    ],
  },
];

const exportSelections = reactive(initExportSelections());

function initExportSelections() {
  const selections = {};
  exportGroups.forEach((group) => {
    group.fields.forEach((field) => {
      selections[field.key] = true;
    });
  });
  return selections;
}

const familyRows = computed(() => {
  const group = exportGroups.find((item) => item.id === "family");
  if (!group) {
    return [];
  }
  const byKey = Object.fromEntries(
    group.fields.map((field) => [field.key, field]),
  );
  return [
    ["fatherName", "fatherPhone", "fatherWorkUnit", "fatherTitle"]
      .map((key) => byKey[key])
      .filter(Boolean),
    ["motherName", "motherPhone", "motherWorkUnit", "motherTitle"]
      .map((key) => byKey[key])
      .filter(Boolean),
    ["hkMoTw", "specialStudent"].map((key) => byKey[key]).filter(Boolean),
  ];
});

const availableMajors = computed(() => {
  if (!filters.studentCategory) {
    return [];
  }
  return majorOptionsByCategory[filters.studentCategory] || [];
});

const hasActiveFilters = computed(() => {
  return Boolean(
    filters.classYear ||
    filters.studentCategory ||
    filters.major ||
    filters.classNo ||
    filters.isHkMoTw ||
    filters.isSpecial ||
    filters.keyword,
  );
});

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
    isHkMoTw: filters.isHkMoTw,
    isSpecial: filters.isSpecial,
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

// Clear major when student category changes (since major options depend on it)
watch(() => filters.studentCategory, () => {
  filters.major = "";
});

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
      className: buildClassName(item),
      gradeYear: item.classYear || "",
      college: item.college || "",
      major: item.classMajor || "",
      classNo: item.classNo || "",
      studentNo: item.studentNo || "",
      hkMoTw: item.hkMoTw || false,
      specialStudent: item.specialStudent || false,
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
    const selectedKeys = getSelectedExportKeys();
    if (gridNeedsDetail(selectedKeys) && items.length) {
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
  const el = gridWrapRef.value;
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
    const selectedKeys = getSelectedExportKeys();
    if (!gridNeedsDetail(selectedKeys)) {
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
      hkMoTw: data.hkMoTw || false,
      specialStudent: data.specialStudent || false,
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

function resetFilters() {
  filters.classYear = "";
  filters.studentCategory = "";
  filters.major = "";
  filters.classNo = "";
  filters.isHkMoTw = false;
  filters.isSpecial = false;
  filters.keyword = "";
}

function buildSearchParams(page, size) {
  const params = {
    page,
    size,
  };
  if (filters.classYear) {
    params.classYear = filters.classYear;
  }
  if (filters.studentCategory) {
    params.studentCategory = filters.studentCategory;
  }
  if (filters.major) {
    params.major = filters.major;
  }
  if (filters.classNo) {
    params.classNo = Number(filters.classNo);
  }
  if (filters.isHkMoTw) {
    params.hkMoTw = true;
  }
  if (filters.isSpecial) {
    params.specialStudent = true;
  }
  if (filters.keyword && filters.keyword.trim()) {
    params.keyword = filters.keyword.trim();
  }
  return params;
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

const IDENTITY_FIELDS = [
  {
    key: "name",
    label: "姓名",
    getter: (item) => item.fullName || item.name || "",
  },
  {
    key: "className",
    label: "班级",
    getter: (item) => buildClassName(item) || "",
  },
  { key: "studentNo", label: "学号", getter: (item) => item.studentNo || "" },
];

const IDENTITY_KEYS = IDENTITY_FIELDS.map((field) => field.key);

const MAIN_FIELD_ORDER = [
  "name",
  "className",
  "studentNo",
  "enrollmentDate",
  "studentCategory",
  "classTeacher",
  "counselor",
  "ethnicity",
  "politicalStatus",
  "phone",
  "backupContact",
  "idNo",
  "nativePlace",
  "address",
  "offCampusLiving",
  "offCampusAddress",
  "dormCampus",
  "dormBuilding",
  "dormRoom",
  "fatherName",
  "fatherPhone",
  "fatherWorkUnit",
  "fatherTitle",
  "motherName",
  "motherPhone",
  "motherWorkUnit",
  "motherTitle",
  "hkMoTw",
  "specialStudent",
  "emergencyPhone",
  "emergencyRelation",
];

const MAIN_FIELD_META = {
  name: { label: "姓名", getter: (item) => item.fullName || item.name || "" },
  className: { label: "班级", getter: (item) => buildClassName(item) || "" },
  studentNo: { label: "学号", getter: (item) => item.studentNo || "" },
  enrollmentDate: {
    label: "入学时间",
    getter: (item) => item.enrollmentDate || "",
  },
  studentCategory: {
    label: "学生类别",
    getter: (item) => item.studentCategory || "",
  },
  classTeacher: { label: "班主任", getter: (item) => item.classTeacher || "" },
  counselor: { label: "辅导员", getter: (item) => item.counselor || "" },
  ethnicity: { label: "民族", getter: (item) => item.ethnicity || "" },
  politicalStatus: {
    label: "政治面貌",
    getter: (item) => item.politicalStatus || "未填写",
  },
  phone: { label: "手机号码", getter: (item) => item.phone || "" },
  backupContact: {
    label: "备用联系方式（QQ/邮箱）",
    getter: (item) => item.backupContact || "",
  },
  idNo: { label: "身份证号", getter: (item) => item.idNo || "" },
  nativePlace: { label: "籍贯", getter: (item) => item.nativePlace || "" },
  address: { label: "住址", getter: (item) => item.address || "" },
  offCampusLiving: {
    label: "是否在外居住",
    getter: (item) => formatYesNo(item.offCampusLiving),
  },
  offCampusAddress: {
    label: "外居住地址",
    getter: (item) => item.offCampusAddress || "",
  },
  dormCampus: { label: "住宿校区", getter: (item) => item.dormCampus || "" },
  dormBuilding: {
    label: "住宿楼栋",
    getter: (item) => item.dormBuilding || "",
  },
  dormRoom: { label: "住宿房间", getter: (item) => item.dormRoom || "" },
  hkMoTw: { label: "港澳台", getter: (item) => (item.hkMoTw ? "是" : "否") },
  specialStudent: {
    label: "特殊学生",
    getter: (item) => (item.specialStudent ? "是" : "否"),
  },
  fatherName: { label: "父亲姓名", getter: (item) => item.fatherName || "" },
  fatherPhone: { label: "父亲电话", getter: (item) => item.fatherPhone || "" },
  fatherWorkUnit: {
    label: "父亲工作单位",
    getter: (item) => item.fatherWorkUnit || "",
  },
  fatherTitle: { label: "父亲职务", getter: (item) => item.fatherTitle || "" },
  motherName: { label: "母亲姓名", getter: (item) => item.motherName || "" },
  motherPhone: { label: "母亲电话", getter: (item) => item.motherPhone || "" },
  motherWorkUnit: {
    label: "母亲工作单位",
    getter: (item) => item.motherWorkUnit || "",
  },
  motherTitle: { label: "母亲职务", getter: (item) => item.motherTitle || "" },
  emergencyPhone: {
    label: "紧急联系人电话",
    getter: (item) => item.emergencyPhone || "",
  },
  emergencyRelation: {
    label: "紧急联系人关系",
    getter: (item) => item.emergencyRelation || "",
  },
};

const EDUCATION_FIELD_ORDER = [
  "eduPeriod",
  "eduSchoolName",
  "eduEducationLevel",
  "eduWitness",
];

const EDUCATION_FIELD_META = {
  eduPeriod: {
    label: "时间段",
    getter: (item, edu) => {
      const start = edu?.startDate || "";
      const end = edu?.isCurrent ? "至今" : edu?.endDate || "";
      return [start, end].filter(Boolean).join("~");
    },
  },
  eduSchoolName: {
    label: "学校名称",
    getter: (item, edu) => edu?.schoolName || "",
  },
  eduEducationLevel: {
    label: "学历",
    getter: (item, edu) => edu?.educationLevel || "",
  },
  eduWitness: { label: "证明人", getter: (item, edu) => edu?.witness || "" },
};

const PARTY_FIELD_ORDER = [
  "leagueJoined",
  "leagueApplicationDate",
  "leagueJoinDate",
  "leagueNo",
  "partyApplied",
  "applicationDate",
  "activistDate",
  "partyTrainingDate",
  "developmentTargetDate",
  "probationaryMemberDate",
  "fullMemberDate",
];

const PARTY_FIELD_META = {
  leagueJoined: {
    label: "是否入团",
    getter: (item) => formatYesNo(item.leagueJoined),
  },
  leagueApplicationDate: {
    label: "提交入团申请书时间",
    getter: (item) => item.leagueApplicationDate || "",
  },
  leagueJoinDate: {
    label: "入团时间",
    getter: (item) =>
      formatDateOrEmpty(item.leagueJoinDate, item.leagueDeveloping, "正在发展"),
  },
  leagueNo: { label: "团号", getter: (item) => item.leagueNo || "" },
  partyApplied: {
    label: "是否申请入党",
    getter: (item) => formatYesNo(item.partyApplied),
  },
  applicationDate: {
    label: "提交入党申请书时间",
    getter: (item) => item.applicationDate || "",
  },
  activistDate: {
    label: "确定积极分子时间",
    getter: (item) =>
      formatDateOrEmpty(item.activistDate, item.activistDeveloping, "正在发展"),
  },
  partyTrainingDate: {
    label: "上党课时间",
    getter: (item) =>
      formatDateOrEmpty(
        item.partyTrainingDate,
        item.partyTrainingPending,
        "暂未报名",
      ),
  },
  developmentTargetDate: {
    label: "确定发展对象时间",
    getter: (item) =>
      formatDateOrEmpty(
        item.developmentTargetDate,
        item.developmentTargetDeveloping,
        "正在发展",
      ),
  },
  probationaryMemberDate: {
    label: "接收为预备党员时间",
    getter: (item) =>
      formatDateOrEmpty(
        item.probationaryMemberDate,
        item.probationaryDeveloping,
        "正在发展",
      ),
  },
  fullMemberDate: {
    label: "转为正式党员时间",
    getter: (item) =>
      formatDateOrEmpty(
        item.fullMemberDate,
        item.fullMemberDeveloping,
        "正在发展",
      ),
  },
};

const ACHIEVEMENT_CATEGORIES = [
  { key: "contest", label: "学科竞赛、文体艺术", selectKey: "ach_contest" },
  { key: "paper", label: "发表学术论文", selectKey: "ach_paper" },
  { key: "journal", label: "发表期刊作品", selectKey: "ach_journal" },
  {
    key: "patent",
    label: "专利（著作权）授权数（项）",
    selectKey: "ach_patent",
  },
  { key: "certificate", label: "职业资格证书", selectKey: "ach_certificate" },
  {
    key: "research",
    label: "学生参与教师科研项目情况",
    selectKey: "ach_research",
  },
  { key: "works", label: "创作、表演的代表性作品", selectKey: "ach_works" },
];

const achievementEntries = computed(() => [
  { key: "all", label: "全部" },
  ...ACHIEVEMENT_CATEGORIES,
]);

const ACHIEVEMENT_FIELDS = {
  certificate: [
    { key: "studentName", label: "学生姓名" },
    { key: "studentNo", label: "学号" },
    { key: "certificateType", label: "证书类型" },
    { key: "certificateName", label: "证书名称" },
    { key: "obtainDate", label: "获得日期" },
  ],
  contest: [
    { key: "studentName", label: "学生姓名" },
    { key: "studentNo", label: "学号" },
    { key: "contestName", label: "竞赛名称" },
    { key: "organizer", label: "主办方" },
    { key: "contestCategory", label: "竞赛类别" },
    { key: "awardCategory", label: "奖项类别" },
    { key: "awardLevel", label: "奖项等级" },
    { key: "contestType", label: "竞赛类型" },
    { key: "awardDate", label: "获奖日期" },
    { key: "awardCount", label: "获奖人数/数量" },
    { key: "teamMembers", label: "团队成员" },
    { key: "instructors", label: "指导教师" },
    { key: "remark", label: "备注" },
  ],
  journal: [
    { key: "studentName", label: "学生姓名" },
    { key: "studentNo", label: "学号" },
    { key: "workTitle", label: "作品题目" },
    { key: "publicationName", label: "刊物名称" },
    { key: "publishDate", label: "发表日期" },
  ],
  paper: [
    { key: "studentName", label: "学生姓名" },
    { key: "studentNo", label: "学号" },
    { key: "paperTitle", label: "论文题目" },
    { key: "journalName", label: "期刊名称" },
    { key: "publishDate", label: "发表日期" },
    { key: "authorOrder", label: "作者排序" },
    { key: "indexed", label: "收录情况" },
  ],
  patent: [
    { key: "studentName", label: "学生姓名" },
    { key: "studentNo", label: "学号" },
    { key: "patentName", label: "专利名称" },
    { key: "patentType", label: "专利类型" },
    { key: "grantNo", label: "授权号" },
    { key: "grantDate", label: "授权日期" },
    { key: "firstInventor", label: "第一发明人" },
  ],
  research: [
    { key: "studentName", label: "学生姓名" },
    { key: "studentNo", label: "学号" },
    { key: "projectName", label: "项目名称" },
    { key: "teacherNo", label: "教师工号" },
    { key: "projectLeader", label: "项目负责人" },
  ],
  works: [
    { key: "studentName", label: "学生姓名" },
    { key: "studentNo", label: "学号" },
    { key: "workName", label: "作品名称" },
    { key: "workCategory", label: "作品类别" },
    { key: "workType", label: "作品类型" },
    { key: "publishDate", label: "发布日期" },
    { key: "publishOccasion", label: "发布场合" },
    { key: "organizer", label: "主办方" },
    { key: "impactScope", label: "影响范围" },
    { key: "note", label: "备注说明" },
  ],
};

function shouldIncludeMainSheet(selectedKeys) {
  const hasEdu = EDUCATION_FIELD_ORDER.some((key) => selectedKeys.has(key));
  const hasParty = PARTY_FIELD_ORDER.some((key) => selectedKeys.has(key));
  const hasAchievement = ACHIEVEMENT_CATEGORIES.some((item) =>
    selectedKeys.has(item.selectKey),
  );
  const hasNonBaseMain = MAIN_FIELD_ORDER.some(
    (key) => !IDENTITY_KEYS.includes(key) && selectedKeys.has(key),
  );
  const hasAnyMain = MAIN_FIELD_ORDER.some((key) => selectedKeys.has(key));
  if (hasEdu || hasParty || hasAchievement) {
    return hasNonBaseMain;
  }
  return hasAnyMain;
}

function gridNeedsDetail(selectedKeys) {
  const hasEdu = EDUCATION_FIELD_ORDER.some((key) => selectedKeys.has(key));
  const hasParty = PARTY_FIELD_ORDER.some((key) => selectedKeys.has(key));
  const hasNonBaseMain = MAIN_FIELD_ORDER.some(
    (key) => !IDENTITY_KEYS.includes(key) && selectedKeys.has(key),
  );
  return hasEdu || hasParty || hasNonBaseMain;
}

const gridSelectedKeys = computed(() => getSelectedExportKeys());
const gridSheets = computed(() => {
  const keys = gridSelectedKeys.value;
  const sheets = [];
  if (shouldIncludeMainSheet(keys)) {
    const table = buildStudentTable(gridDetailRows.value, keys);
    if (table) {
      sheets.push({ id: "main", label: "全部", table });
    }
  }
  const educationTable = buildEducationTable(gridDetailRows.value, keys);
  if (educationTable) {
    sheets.push({ id: "education", label: "教育经历", table: educationTable });
  }
  const partyTable = buildPartyTable(gridDetailRows.value, keys);
  if (partyTable) {
    sheets.push({ id: "party", label: "团组织与入党信息", table: partyTable });
  }
  const activeAchievementCategories = ACHIEVEMENT_CATEGORIES.filter((item) =>
    keys.has(item.selectKey),
  );
  if (activeAchievementCategories.length) {
    const overview = buildAchievementOverview(
      gridDetailRows.value,
      keys,
      gridAchievementData.value,
    );
    sheets.push({
      id: "achievement-overview",
      label: "成就总览",
      table: overview,
    });
    activeAchievementCategories.forEach((category) => {
      const detailTable = buildAchievementDetailTable(
        category.key,
        gridAchievementData.value,
      );
      sheets.push({
        id: `achievement-${category.key}`,
        label: category.label,
        table: detailTable,
      });
    });
  }
  return sheets;
});

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

function getSelectedExportKeys() {
  return new Set(
    Object.entries(exportSelections)
      .filter(([, value]) => Boolean(value))
      .map(([key]) => key),
  );
}

function openExportDialog() {
  exportDialogOpen.value = true;
}

function closeExportDialog() {
  exportDialogOpen.value = false;
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

function buildStudentTable(rows, selectedKeys) {
  const activeFields = MAIN_FIELD_ORDER.filter((key) => selectedKeys.has(key));
  if (!activeFields.length) {
    return null;
  }
  const header = activeFields.map((key) => MAIN_FIELD_META[key].label);
  const body = rows.map((item) =>
    activeFields.map((key) => MAIN_FIELD_META[key].getter(item)),
  );
  return [header, ...body];
}

function computeColumnWidths(table) {
  const widths = [];
  table.forEach((row) => {
    row.forEach((cell, index) => {
      const text = cell == null ? "" : String(cell);
      const length = text.length;
      const minWidth = 8;
      const maxWidth = 40;
      const width = Math.min(Math.max(length + 2, minWidth), maxWidth);
      if (!widths[index] || width > widths[index].wch) {
        widths[index] = { wch: width };
      }
    });
  });
  return widths;
}

function buildEducationTable(rows, selectedKeys) {
  const activeIdentity = IDENTITY_FIELDS.filter((field) =>
    selectedKeys.has(field.key),
  );
  const activeFields = EDUCATION_FIELD_ORDER.filter((key) =>
    selectedKeys.has(key),
  );
  if (!activeFields.length) {
    return null;
  }
  const header = [
    ...activeIdentity.map((field) => field.label),
    ...activeFields.map((key) => EDUCATION_FIELD_META[key].label),
  ];
  const lines = [header];
  rows.forEach((item) => {
    const identityValues = activeIdentity.map((field) => field.getter(item));
    const experiences = Array.isArray(item.educationExperiences)
      ? item.educationExperiences.filter(Boolean)
      : [];
    if (!experiences.length) {
      lines.push([...identityValues, ...activeFields.map(() => "")]);
      return;
    }
    experiences.forEach((edu) => {
      lines.push([
        ...identityValues,
        ...activeFields.map((key) =>
          EDUCATION_FIELD_META[key].getter(item, edu),
        ),
      ]);
    });
  });
  return lines;
}

function buildPartyTable(rows, selectedKeys) {
  const activeIdentity = IDENTITY_FIELDS.filter((field) =>
    selectedKeys.has(field.key),
  );
  const activeFields = PARTY_FIELD_ORDER.filter((key) => selectedKeys.has(key));
  if (!activeFields.length) {
    return null;
  }
  const header = [
    ...activeIdentity.map((field) => field.label),
    ...activeFields.map((key) => PARTY_FIELD_META[key].label),
  ];
  const lines = [header];
  rows.forEach((item) => {
    lines.push([
      ...activeIdentity.map((field) => field.getter(item)),
      ...activeFields.map((key) => PARTY_FIELD_META[key].getter(item)),
    ]);
  });
  return lines;
}

async function fetchAchievementsForStudents(rows) {
  const tasks = rows.map((item) => {
    const studentNo = item.studentNo || "";
    const studentName = item.fullName || item.name || "";
    if (!studentNo && !studentName) {
      return Promise.resolve({ studentNo, studentName, records: [] });
    }
    return listAchievements({ studentNo, studentName })
      .then(({ data }) => ({
        studentNo,
        studentName,
        records: Array.isArray(data) ? data : [],
      }))
      .catch(() => ({ studentNo, studentName, records: [] }));
  });
  return Promise.all(tasks);
}

function buildAchievementOverview(rows, selectedKeys, achievementData) {
  const baseFields = IDENTITY_FIELDS.filter((field) =>
    selectedKeys.has(field.key),
  );
  const activeCategories = ACHIEVEMENT_CATEGORIES.filter((item) =>
    selectedKeys.has(item.selectKey),
  );
  const header = [
    ...baseFields.map((field) => field.label),
    ...activeCategories.map((item) => item.label),
  ];
  const body = rows.map((item) => {
    const baseValues = baseFields.map((field) => field.getter(item));
    const studentNo = item.studentNo || "";
    const studentName = item.fullName || item.name || "";
    const recordEntry = achievementData.find(
      (entry) =>
        (studentNo && entry.studentNo === studentNo) ||
        (studentName && entry.studentName === studentName),
    );
    const records = recordEntry?.records || [];
    const categoryFlags = activeCategories.map((category) => {
      const count = records.filter(
        (record) => record.category === category.key,
      ).length;
      return count > 0 ? String(count) : "";
    });
    return [...baseValues, ...categoryFlags];
  });
  return [header, ...body];
}

function buildAchievementDetailTable(categoryKey, achievementData) {
  const fields = ACHIEVEMENT_FIELDS[categoryKey] || [];
  const header = fields.map((field) => field.label);
  const rows = [];
  achievementData.forEach((entry) => {
    entry.records
      .filter((record) => record.category === categoryKey)
      .forEach((record) => {
        const values = fields.map((field) => record.fields?.[field.key] || "");
        rows.push(values);
      });
  });
  return [header, ...rows];
}

function formatYesNo(value) {
  if (value === true) {
    return "是";
  }
  if (value === false) {
    return "否";
  }
  return "";
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
</script>

<style scoped>
@import '../assets/styles/student-info-view.css';
</style>
