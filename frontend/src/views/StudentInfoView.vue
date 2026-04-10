<template>
  <main class="dashboard-right">
    <header class="feed-header">
      <h1 class="feed-title">学生信息</h1>
    </header>

    <section class="info-shell student-right-stack">
      <section v-show="!gridFullscreen" class="info-card student-filter-card">
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
            <button
              class="student-grid-toggle"
              type="button"
              @click="toggleGridView"
              :title="gridViewOpen ? '切换列表视图' : '切换表格视图'"
            >
              <span class="grid-toggle-icon" aria-hidden="true">
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
              <div class="student-stepper">
                <button
                  class="stepper-button"
                  type="button"
                  :disabled="!canDecrementClass"
                  @click="decrementClass"
                >
                  −
                </button>
                <input
                  v-model="filters.classNo"
                  class="info-input stepper-input"
                  type="number"
                  step="1"
                  placeholder="全部"
                  @input="normalizeClassNo"
                />
                <button
                  class="stepper-button"
                  type="button"
                  :disabled="!canIncrementClass"
                  @click="incrementClass"
                >
                  +
                </button>
              </div>
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

      <section class="info-card student-results-card">
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

        <div v-if="!gridViewOpen" class="student-pagination">
          <div class="student-pages">
            <!-- 首页 -->
            <button
              class="page-button page-nav"
              type="button"
              :disabled="currentPage === 1"
              @click="setPage(1)"
              aria-label="首页"
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
              :disabled="currentPage === 1"
              @click="setPage(currentPage - 1)"
              aria-label="上一页"
            >
              <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
                <path d="M11 4L4 10l7 6V4z" fill="currentColor"/>
              </svg>
            </button>

            <!-- 页码 -->
            <template v-for="item in visiblePages" :key="item.value">
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
              :disabled="currentPage === totalPages"
              @click="setPage(currentPage + 1)"
              aria-label="下一页"
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
              @click="setPage(totalPages)"
              aria-label="末页"
            >
              <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
                <path d="M5 4l7 6-7 6V4z" fill="currentColor"/>
                <rect x="14" y="4" width="2" height="12" fill="currentColor"/>
              </svg>
            </button>
          </div>

          <div class="page-size">
            <span class="info-label">每页</span>
            <select
              v-model.number="pageSize"
              class="info-input page-size-input"
            >
              <option v-for="size in pageSizeOptions" :key="size" :value="size">
                {{ size }}
              </option>
            </select>
          </div>
          <button
            class="action-button"
            type="button"
            :disabled="exportDisabled"
            @click="openExportDialog"
          >
            {{ exportLabel }}
          </button>
        </div>
      </section>
    </section>

    <transition name="export-dialog-backdrop">
      <div
        v-if="gridFieldDialogOpen"
        class="grid-field-dialog-backdrop"
        @click="closeGridFieldDialog"
      ></div>
    </transition>
    <section
      class="grid-field-dialog"
      :class="{ open: gridFieldDialogOpen, closing: gridFieldDialogClosing }"
    >
      <header class="export-dialog-header">
        <div class="export-dialog-title">
          选择显示字段
          <label class="export-all-toggle">
            <input
              type="checkbox"
              :checked="isAllSelected"
              @change="toggleAllSelections($event.target.checked)"
            />
            <span>全选</span>
          </label>
        </div>
        <button
          class="ghost-button"
          type="button"
          @click="closeGridFieldDialog"
        >
          关闭
        </button>
      </header>
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
    </section>

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
import { navigateWithViewTransition } from "../utils/viewTransition";
import { useDashboardShell } from "../composables/useDashboardShell";

const router = useRouter();
const route = useRoute();
const { openSidebar: openDashboardSidebar } = useDashboardShell();
const API_BASE = "http://localhost:8080";

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
const exportLabel = computed(() => {
  const count = selectedIds.value.length;
  return count ? `导出(${count})` : "导出";
});

/**
 * Sliding window pagination: always shows current page ± 2,
 * plus first/last and ellipsis when needed.
 * e.g. [1] [...] [3 4] 5 [6 7] [...] [20]
 */
const WINDOW = 2;
const visiblePages = computed(() => {
  const total = totalPages.value;
  const cur = currentPage.value;
  if (total <= 7) {
    return Array.from({ length: total }, (_, i) => ({ type: "num", value: i + 1 }));
  }
  const pages = [];
  const addNum = (n) => pages.push({ type: "num", value: n });
  const addEllipsis = (key) => pages.push({ type: "ellipsis", value: key });

  addNum(1);
  if (cur - WINDOW > 2) addEllipsis("left");
  for (let i = Math.max(2, cur - WINDOW); i <= Math.min(total - 1, cur + WINDOW); i++) {
    addNum(i);
  }
  if (cur + WINDOW < total - 1) addEllipsis("right");
  addNum(total);

  return pages;
});
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
  gridViewOpen.value = !gridViewOpen.value;
  if (gridViewOpen.value) {
    fetchGridStudents();
  }
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

function normalizeClassNo() {
  if (filters.classNo === "") {
    return;
  }
  const next = Number(filters.classNo);
  if (Number.isNaN(next)) {
    filters.classNo = "";
    return;
  }
  const clamped = Math.min(Math.max(1, next), 10);
  filters.classNo = String(clamped);
}

const canDecrementClass = computed(() => true);
const canIncrementClass = computed(() => true);

function decrementClass() {
  const current = Number(filters.classNo);
  if (!filters.classNo || Number.isNaN(current)) {
    filters.classNo = "10";
    return;
  }
  if (current <= 1) {
    filters.classNo = "";
    return;
  }
  filters.classNo = String(current - 1);
}

function incrementClass() {
  const current = Number(filters.classNo);
  if (!filters.classNo || Number.isNaN(current)) {
    filters.classNo = "1";
    return;
  }
  if (current >= 10) {
    filters.classNo = "";
    return;
  }
  filters.classNo = String(current + 1);
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

function resolveMediaUrl(url) {
  if (!url) {
    return "";
  }
  if (url.startsWith("http://") || url.startsWith("https://")) {
    return url;
  }
  return `${API_BASE}${url}`;
}

function goToSettings() {
  navigateWithViewTransition(router, "/settings");
}

function loadUser() {
  try {
    const raw = JSON.parse(localStorage.getItem("gcsc_user") || "{}");
    return {
      username: raw.username || "",
      displayName: raw.displayName || "",
      avatarUrl: raw.avatarUrl || "",
      role: raw.role || "STUDENT",
      studentNo: raw.studentNo || "",
      className: raw.className || "",
      college: raw.college || "",
    };
  } catch {
    return {
      username: "",
      displayName: "",
      avatarUrl: "",
      role: "STUDENT",
      studentNo: "",
      className: "",
      college: "",
    };
  }
}
</script>

<style scoped>
.student-filter-card {
  gap: 16px;
  position: relative;
  z-index: 40;
  padding: 0 0 18px;
  border-bottom: none;
  min-height: 0;
}

.student-right-stack {
  display: grid;
  gap: 14px;
}

.student-filter-toolbar {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(280px, 1.15fr) auto;
  align-items: center;
  gap: 16px;
  padding: 18px 18px 16px;
  border-radius: 18px;
  border: 1px solid var(--line);
  background: var(--card);
  box-shadow: var(--shadow-sm);
}

.student-filter-intro {
  display: grid;
  gap: 6px;
  min-width: 0;
}

.student-filter-caption {
  font-size: 13px;
  color: var(--text-sub);
  line-height: 1.45;
}

.student-filter-search-wrap {
  min-width: 0;
}

.student-filter-toolbar-actions {
  display: inline-flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  flex-wrap: wrap;
}

.student-filter-reset {
  height: 44px;
  min-width: 44px;
  border-radius: 999px;
  border: 1px solid var(--line-strong);
  background: rgba(255, 255, 255, 0.86);
  color: var(--primary-dark);
  padding: 0 14px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition:
    background 0.2s ease,
    border-color 0.2s ease,
    color 0.2s ease;
}

.student-filter-reset:hover {
  background: rgba(255, 255, 255, 0.98);
  border-color: var(--primary);
  color: var(--primary-dark);
}

.student-filter-reset:focus-visible {
  outline: 2px solid var(--primary);
  outline-offset: 2px;
}

.student-filter-body {
  display: grid;
  gap: 14px;
  padding: 0 18px;
}

.student-grid-toggle {
  border: none;
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
  color: #fff;
  padding: 0 16px;
  height: 44px;
  min-width: 44px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 4px 12px var(--primary-glow);
  transition:
    box-shadow 0.2s ease,
    transform 0.2s ease;
}

.student-grid-toggle:hover {
  box-shadow: 0 6px 16px var(--primary-glow);
}

.student-grid-toggle:active {
  transform: scale(0.96);
}

.student-grid-toggle:focus-visible {
  outline: 2px solid var(--primary);
  outline-offset: 2px;
}

.grid-toggle-icon {
  width: 18px;
  height: 18px;
  display: inline-flex;
}

.grid-toggle-icon svg {
  width: 100%;
  height: 100%;
}

.student-grid-wrap {
  margin-top: 14px;
}

.student-grid-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
  color: var(--text-sub);
  font-size: 13px;
}

.student-grid-actions {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.student-grid-title {
  font-weight: 600;
  color: var(--text-main);
}

.student-grid-status {
  color: var(--text-sub);
}

.student-grid-field-group {
  display: grid;
  gap: 6px;
}

.student-grid-field-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  color: var(--text-main);
}

.student-grid-field-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-sub);
  font-size: 13px;
}

.student-grid-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
  align-items: center;
}

.student-filter-card .student-grid-tabs {
  margin-top: 12px;
}

.student-grid-tab {
  border: 1px solid var(--line-strong);
  background: var(--card);
  color: var(--primary-dark);
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
  cursor: pointer;
  transition:
    background 0.15s ease,
    border-color 0.15s ease,
    color 0.15s ease;
  min-height: 44px;
  min-width: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.student-grid-tab:hover:not(.active) {
  background: var(--card-hover);
  border-color: var(--primary);
  color: var(--primary-dark);
}

.student-grid-tab.active {
  background: var(--primary-surface);
  border-color: var(--primary);
  color: var(--primary-dark);
  font-weight: 600;
}

.student-grid-tab:focus-visible {
  outline: 2px solid var(--primary);
  outline-offset: 2px;
}

.grid-field-dialog-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(45, 26, 62, 0.35);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  z-index: 60;
}

.grid-field-dialog {
  position: fixed;
  left: 50%;
  bottom: 16px;
  width: min(860px, calc(100vw - 32px));
  max-height: 84vh;
  transform: translate(-50%, 120%) scale(0.98);
  opacity: 0;
  pointer-events: none;
  border-radius: 22px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  background: var(--card);
  box-shadow: var(--shadow-glow);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  z-index: 70;
  transition:
    transform 300ms cubic-bezier(0.22, 1, 0.36, 1),
    opacity 250ms ease;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow: auto;
  scrollbar-width: none;
  padding: 12px 14px 16px;
}

.grid-field-dialog.open {
  transform: translate(-50%, 0) scale(1);
  opacity: 1;
  pointer-events: auto;
}

.grid-field-dialog.closing {
  transform: translate(-50%, 120%) scale(0.98);
  opacity: 0;
}

.grid-field-dialog::-webkit-scrollbar {
  width: 0;
  height: 0;
}

.student-grid {
  width: 100%;
  height: 520px;
  border-radius: 12px;
  overflow: hidden;
}

.student-grid-wrap {
  position: relative;
}

.student-grid-wrap.fullscreen {
  position: fixed;
  inset: 0;
  bottom: 100px;
  z-index: 80;
  background: var(--bg-base);
  padding: 16px;
  display: flex;
  flex-direction: column;
}

.student-grid-wrap.fullscreen .student-grid {
  flex: 1;
  border-radius: 16px;
  min-height: 0;
}

.student-grid-fullscreen {
  border: 1px solid var(--line-strong);
  background: var(--card);
  color: var(--primary-dark);
  padding: 8px 14px;
  border-radius: 999px;
  font-size: 12px;
  cursor: pointer;
  transition: background 0.2s, border-color 0.2s;
  min-height: 44px;
  min-width: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.student-grid-fullscreen:hover {
  background: var(--card-hover);
  border-color: var(--primary);
}

.student-grid-fullscreen:focus-visible {
  outline: 2px solid var(--primary);
  outline-offset: 2px;
}

.student-grid-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

.student-search {
  min-width: 0;
  width: 100%;
  height: 42px;
}

.student-filter-panel {
  display: grid;
  gap: 8px;
  padding: 14px;
  min-width: 0;
  border-radius: 18px;
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.82);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.45);
}

.student-filter-panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 20px;
}

.student-filter-grid {
  display: grid;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  gap: 14px 16px;
  align-items: end;
}

.student-filter-field {
  min-width: 0;
}

.student-filter-field-year {
  grid-column: span 3;
}

.student-filter-field-category {
  grid-column: span 3;
}

.student-filter-field-major {
  grid-column: span 4;
}

.student-filter-field-class {
  grid-column: span 2;
}

.student-stepper {
  display: grid;
  grid-template-columns: 36px minmax(0, 1fr) 36px;
  gap: 10px;
  align-items: center;
}

.stepper-button {
  height: 44px;
  width: 44px;
  border-radius: 12px;
  border: 1px solid var(--line-strong);
  background: rgba(255, 255, 255, 0.6);
  color: var(--primary-dark);
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
  transition:
    background 0.15s ease,
    border-color 0.15s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.stepper-button:hover:not(:disabled) {
  background: var(--card-hover);
  border-color: var(--primary);
}

.stepper-button:focus-visible {
  outline: 2px solid var(--primary);
  outline-offset: 2px;
}

.stepper-button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.stepper-input {
  text-align: center;
  -moz-appearance: textfield;
}

.stepper-input::-webkit-outer-spin-button,
.stepper-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.student-filter-flags {
  display: flex;
  gap: 10px 18px;
  align-items: center;
  flex-wrap: wrap;
}

.student-filter-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  padding: 2px 4px 0;
}

.student-filter-status {
  color: var(--text-sub);
  font-size: 13px;
  font-weight: 600;
}

@media (max-width: 980px) {
  .student-filter-toolbar {
    grid-template-columns: 1fr;
  }

  .student-filter-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .student-filter-field-year,
  .student-filter-field-category,
  .student-filter-field-major,
  .student-filter-field-class {
    grid-column: auto;
  }
}

.student-results-card {
  gap: 16px;
  position: relative;
  z-index: 1;
  min-height: 420px;
}

.student-results-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.student-results-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.student-list {
  display: grid;
  gap: 10px;
}

.student-results-loading {
  display: grid;
  place-items: center;
  min-height: 240px;
}

.student-row {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 12px;
  align-items: center;
  padding: 12px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.6);
  border: 1.5px solid transparent;
  transition:
    background 0.15s ease,
    border-color 0.15s ease,
    transform 0.12s ease;
}

.student-row:hover {
  background: var(--card-hover);
  border-color: var(--primary);
}

.student-row:active {
  transform: scale(0.99);
}

.student-main {
  display: grid;
  gap: 4px;
}

.student-name {
  font-weight: 700;
  color: var(--primary-dark);
}

.student-meta {
  font-size: 12px;
  color: var(--text-sub);
}

.student-pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.student-pages {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.page-size {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-size-input {
  width: 90px;
  height: 44px;
}

.page-button {
  border-radius: 999px;
  border: 1px solid var(--line-strong);
  background: rgba(255, 255, 255, 0.7);
  color: var(--primary-dark);
  height: 44px;
  min-width: 44px;
  padding: 0 12px;
  cursor: pointer;
  font-weight: 600;
  transition:
    background 0.15s ease,
    border-color 0.15s ease;
}

.page-button:hover:not(.active) {
  background: var(--card-hover);
  border-color: var(--primary);
}

.page-button:focus-visible {
  outline: 2px solid var(--primary);
  outline-offset: 2px;
}

.page-button.active {
  background: var(--primary-surface);
  border-color: var(--primary);
  color: var(--primary-dark);
}

.page-nav {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  gap: 0;
}

.page-nav svg {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.page-nav:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.page-nav:focus-visible {
  outline: 2px solid var(--primary);
  outline-offset: 2px;
}

.page-ellipsis {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 44px;
  height: 44px;
  color: var(--text-sub);
  font-size: 14px;
  font-weight: 600;
  pointer-events: none;
  user-select: none;
}

.student-detail-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(45, 26, 62, 0.35);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  z-index: 55;
}

.student-detail-view {
  position: fixed;
  left: 50%;
  bottom: 16px;
  width: min(980px, calc(100vw - 32px));
  transform: translate(-50%, 120%) scale(0.98);
  opacity: 0;
  pointer-events: none;
  border-radius: 22px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  background: var(--card);
  box-shadow: var(--shadow-glow);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  z-index: 65;
  transition:
    transform 300ms cubic-bezier(0.22, 1, 0.36, 1),
    opacity 250ms ease,
    left 300ms cubic-bezier(0.22, 1, 0.36, 1),
    width 300ms cubic-bezier(0.22, 1, 0.36, 1);
  max-height: 84vh;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow: auto;
  scrollbar-width: none;
  padding: 12px 14px 16px;
}

.student-detail-view.split {
  left: 16px;
  width: min(720px, calc(50vw - 24px));
  transform: translate(0, 120%) scale(0.98);
}

.student-detail-view.open {
  transform: translate(-50%, 0) scale(1);
  opacity: 1;
  pointer-events: auto;
}

.student-detail-view.split.open {
  transform: translate(0, 0) scale(1);
}

.student-detail-view.closing {
  transform: translate(-50%, 120%) scale(0.98);
  opacity: 0;
}

.student-detail-view.split.closing {
  transform: translate(0, 120%) scale(0.98);
}

.student-detail-view::-webkit-scrollbar {
  width: 0;
  height: 0;
}

.export-dialog-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(45, 26, 62, 0.35);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  z-index: 60;
}

.export-dialog {
  position: fixed;
  left: 50%;
  bottom: 16px;
  width: min(860px, calc(100vw - 32px));
  transform: translate(-50%, 120%) scale(0.98);
  opacity: 0;
  pointer-events: none;
  border-radius: 22px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  background: var(--card);
  box-shadow: var(--shadow-glow);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  z-index: 70;
  transition:
    transform 300ms cubic-bezier(0.22, 1, 0.36, 1),
    opacity 250ms ease,
    left 300ms cubic-bezier(0.22, 1, 0.36, 1),
    width 300ms cubic-bezier(0.22, 1, 0.36, 1);
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.export-dialog.open {
  transform: translate(-50%, 0) scale(1);
  opacity: 1;
  pointer-events: auto;
}

.export-dialog.closing {
  transform: translate(-50%, 120%) scale(0.98);
  opacity: 0;
}

.export-dialog.split {
  left: 8px;
  width: calc(50vw - 16px);
  transform: translate(0, 120%) scale(0.98);
}

.export-dialog.split.open {
  transform: translate(0, 0) scale(1);
}

.export-dialog.split.closing {
  transform: translate(0, 120%) scale(0.98);
}

.export-dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 18px 10px;
  border-bottom: 1px solid var(--line);
}

.export-dialog-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--primary-dark);
  display: inline-flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
}

.export-all-toggle {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-sub);
}

.export-dialog-body {
  padding: 14px 18px 8px;
  overflow: auto;
  display: grid;
  gap: 14px;
}

.export-group {
  border-radius: 14px;
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.6);
  padding: 12px;
  display: grid;
  gap: 10px;
}

.export-group-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--primary-dark);
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.export-group-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 14px;
}

.export-option-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 14px;
  width: 100%;
}

.export-option {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-main);
}

.export-dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 12px 18px 16px;
  border-top: 1px solid var(--line);
  align-items: center;
}

.export-dialog-actions .ghost-button,
.export-dialog-actions .export-confirm {
  width: auto;
  min-width: 110px;
  height: 40px;
}

.export-confirm {
  padding: 0 22px;
}

.export-preview-view {
  position: fixed;
  right: 8px;
  bottom: 16px;
  width: calc(50vw - 16px);
  height: 80vh;
  transform: translate(0, 120%) scale(0.98);
  opacity: 0;
  pointer-events: none;
  border-radius: 22px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  background: var(--card);
  box-shadow: var(--shadow-glow);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  z-index: 75;
  transition:
    transform 300ms cubic-bezier(0.22, 1, 0.36, 1),
    opacity 250ms ease;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 12px 14px 16px;
}

.export-preview-view.open {
  transform: translate(0, 0) scale(1);
  opacity: 1;
  pointer-events: auto;
}

.export-preview-view.closing {
  transform: translate(0, 120%) scale(0.98);
  opacity: 0;
}

.export-preview-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.export-preview-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--primary-dark);
}

.export-preview-body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 10px;
}

.export-preview-grid {
  flex: 1;
  min-height: 0;
  border-radius: 16px;
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.92);
  overflow: auto;
}

.export-preview-table {
  border-collapse: collapse;
  width: 100%;
  font-size: 12px;
  color: var(--text-main);
}

.export-preview-table th,
.export-preview-table td {
  border: 1px solid var(--line);
  padding: 6px 8px;
  white-space: nowrap;
}

.export-preview-table th {
  background: var(--primary-surface);
  font-weight: 700;
  position: sticky;
  top: 0;
  z-index: 1;
}

.export-preview-tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.export-preview-tab {
  border-radius: 10px;
  border: 1px solid var(--line-strong);
  background: var(--card);
  color: var(--primary-dark);
  height: 44px;
  min-width: 44px;
  padding: 0 12px;
  cursor: pointer;
  font-weight: 600;
  font-size: 12px;
  transition:
    background 0.15s ease,
    border-color 0.15s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.export-preview-tab:hover:not(.active) {
  background: var(--card-hover);
  border-color: var(--primary);
}

.export-preview-tab:focus-visible {
  outline: 2px solid var(--primary);
  outline-offset: 2px;
}

.export-preview-tab.active {
  background: var(--primary-surface);
  border-color: var(--primary);
  color: var(--primary-dark);
}

@media (max-width: 1100px) {
  .export-dialog.split {
    left: 50%;
    width: min(980px, calc(100vw - 32px));
    transform: translate(-50%, 120%) scale(0.98);
  }

  .export-dialog.split.open {
    transform: translate(-50%, 0) scale(1);
  }

  .export-dialog.split.closing {
    transform: translate(-50%, 120%) scale(0.98);
  }

  .export-preview-view {
    left: 50%;
    right: auto;
    width: min(980px, calc(100vw - 32px));
    height: 80vh;
    transform: translate(-50%, 120%) scale(0.98);
  }

  .export-preview-view.open {
    transform: translate(-50%, 0) scale(1);
  }

  .export-preview-view.closing {
    transform: translate(-50%, 120%) scale(0.98);
  }
}

.student-detail-body {
  display: grid;
  gap: 14px;
}

.info-static {
  display: flex;
  align-items: center;
}

.info-actions-single {
  grid-template-columns: minmax(0, 1fr);
}

.info-actions-double {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.student-achievements-view {
  position: fixed;
  right: 16px;
  bottom: 16px;
  width: min(720px, calc(50vw - 24px));
  height: 84vh;
  transform: translate(0, 120%) scale(0.98);
  opacity: 0;
  pointer-events: none;
  border-radius: 22px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  background: var(--card);
  box-shadow: var(--shadow-glow);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  z-index: 66;
  transition:
    transform 300ms cubic-bezier(0.22, 1, 0.36, 1),
    opacity 250ms ease;
  max-height: 84vh;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow: hidden;
  padding: 12px 14px 16px;
}

.student-achievements-view.open {
  transform: translate(0, 0) scale(1);
  opacity: 1;
  pointer-events: auto;
}

.student-achievements-view.closing {
  transform: translate(0, 120%) scale(0.98);
  opacity: 0;
}

.student-achievements-body {
  flex: 1;
  min-height: 0;
  display: flex;
}

.student-achievements-frame {
  border: none;
  width: 100%;
  height: 100%;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.92);
}

@media (max-width: 1100px) {
  .student-detail-view.split {
    left: 50%;
    width: min(980px, calc(100vw - 32px));
    transform: translate(-50%, 120%) scale(0.98);
  }

  .student-detail-view.split.open {
    transform: translate(-50%, 0) scale(1);
  }

  .student-detail-view.split.closing {
    transform: translate(-50%, 120%) scale(0.98);
  }

  .student-achievements-view {
    left: 50%;
    right: auto;
    width: min(980px, calc(100vw - 32px));
    height: 84vh;
    transform: translate(-50%, 120%) scale(0.98);
  }

  .student-achievements-view.open {
    transform: translate(-50%, 0) scale(1);
  }

  .student-achievements-view.closing {
    transform: translate(-50%, 120%) scale(0.98);
  }
}

@media (max-width: 640px) {
  .student-right-stack {
    padding-bottom: 88px;
    gap: 10px;
  }

  .student-filter-card {
    padding-bottom: 14px;
  }

  .student-filter-toolbar {
    grid-template-columns: 1fr;
    gap: 8px;
    padding: 14px;
  }

  .student-search {
    width: 100%;
  }

  .student-filter-body {
    padding: 0 14px;
  }

  .student-filter-grid {
    grid-template-columns: 1fr;
  }

  .student-filter-field-year,
  .student-filter-field-category,
  .student-filter-field-major,
  .student-filter-field-class {
    grid-column: auto;
  }

  .student-results-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .student-results-actions {
    width: 100%;
  }

  .student-pagination {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }

  .student-pages {
    justify-content: center;
  }

  .page-size {
    justify-content: center;
  }

  .student-list {
    gap: 8px;
  }

  .student-row {
    grid-template-columns: auto 1fr auto;
    gap: 10px;
    padding: 14px;
    border-radius: 16px;
    background: rgba(255, 255, 255, 0.8);
    border: 1.5px solid transparent;
    transition:
      background 0.15s ease,
      transform 0.12s ease,
      border-color 0.15s ease;
  }

  .student-row:active {
    background: rgba(255, 255, 255, 0.95);
    transform: scale(0.99);
  }

  .student-main {
    gap: 6px;
  }

  .student-name {
    font-size: 16px;
    font-weight: 800;
    color: var(--primary-dark);
    line-height: 1.3;
  }

  .student-meta {
    font-size: 12px;
    color: var(--text-sub);
    line-height: 1.4;
    word-break: break-all;
  }

  .student-row .ghost-button {
    align-self: center;
    padding: 6px 14px;
    font-size: 12px;
    border-radius: 50px;
    white-space: nowrap;
    flex-shrink: 0;
  }

  .student-list {
    gap: 12px;
  }

  /* ── 表格视图（切换表格后）────────────── */
  .student-filter-card {
    gap: 12px;
    padding: 14px;
  }

  .student-grid-toggle {
    height: 44px;
    padding: 0 14px;
    font-size: 13px;
  }

  .student-grid-tabs {
    gap: 6px;
    flex-wrap: wrap;
  }

  .student-filter-toolbar-actions {
    justify-content: flex-start;
  }

  .student-filter-reset {
    height: 44px;
    min-width: 44px;
    font-size: 12px;
  }

  /* 隐藏 toolbar 里的切换按钮（已移至胶囊） */
  .student-filter-toolbar .student-grid-toggle {
    display: none;
  }

  .student-grid-tab {
    flex-shrink: 0;
    scroll-snap-align: start;
    padding: 5px 10px;
    font-size: 11px;
    border-radius: 8px;
  }

  .student-grid-tab.student-grid-tab-add {
    padding: 5px 10px;
  }

  .student-results-card {
    gap: 12px;
    padding: 14px;
  }

  .student-results-header {
    flex-direction: row;
    align-items: center;
    flex-wrap: nowrap;
    overflow: hidden;
  }

  .student-results-header .info-section-title {
    flex-shrink: 0;
  }

  .student-results-actions {
    flex-shrink: 0;
    gap: 6px;
  }

  .student-results-actions .ghost-button {
    padding: 5px 10px;
    font-size: 11px;
    border-radius: 50px;
  }

  .student-grid-wrap {
    margin-top: 10px;
    border-radius: 12px;
    overflow: hidden;
  }

  .student-grid {
    height: 380px;
    border-radius: 0;
  }

  .student-grid-fullscreen {
    padding: 7px 12px;
    font-size: 11px;
  }

  .student-grid-wrap.fullscreen {
    padding: 12px;
    z-index: 200;
    bottom: 86px;
  }

  .student-grid-wrap.fullscreen .student-grid-toolbar {
    margin-top: 0;
    margin-bottom: 8px;
  }

  .student-grid-wrap.fullscreen .student-grid {
    border-radius: 12px;
    flex: 1;
    min-height: 0;
  }

  .student-detail-view {
    bottom: 0;
    left: 0;
    right: 0;
    width: 100%;
    max-height: 92vh;
    border-radius: 22px 22px 0 0;
    transform: translateY(100%);
  }

  .student-detail-view.open {
    transform: translateY(0);
  }

  .student-detail-view.closing {
    transform: translateY(100%);
  }

  .student-detail-view.split {
    width: 100%;
    max-height: 92vh;
  }

  .student-achievements-view {
    bottom: 0;
    left: 0;
    right: 0;
    width: 100%;
    height: 92vh;
    max-height: 92vh;
    border-radius: 22px 22px 0 0;
    transform: translateY(100%);
  }

  .student-achievements-view.open {
    transform: translateY(0);
  }

  .student-achievements-view.closing {
    transform: translateY(100%);
  }

  .grid-field-dialog {
    bottom: 0;
    left: 0;
    right: 0;
    width: 100%;
    max-height: 92vh;
    border-radius: 22px 22px 0 0;
    transform: translateY(100%);
  }

  .grid-field-dialog.open {
    transform: translateY(0);
  }

  .grid-field-dialog.closing {
    transform: translateY(100%);
  }
}

@media (prefers-reduced-motion: reduce) {
  .student-row,
  .student-grid-toggle,
  .student-grid-tab,
  .stepper-button,
  .page-button,
  .export-preview-tab,
  .student-filter-reset,
  .student-grid-fullscreen,
  .grid-field-dialog,
  .export-dialog,
  .student-detail-view,
  .student-achievements-view,
  .export-preview-view {
    transition: none !important;
    animation: none !important;
  }

  .grid-field-dialog,
  .export-dialog,
  .student-detail-view,
  .student-achievements-view,
  .export-preview-view {
    transform: translate(-50%, 0) scale(1);
    opacity: 1;
    pointer-events: auto;
  }
}
</style>
