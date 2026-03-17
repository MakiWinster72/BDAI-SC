<template>
  <div class="dashboard-layout">
    <aside class="dashboard-left">
      <section class="profile-card">
        <div class="profile-row profile-main">
          <div class="profile-avatar">
            <img
              v-if="profile.avatarUrl"
              :src="resolveMediaUrl(profile.avatarUrl)"
              alt="头像"
            />
            <span v-else>{{ avatarText }}</span>
          </div>
          <div class="profile-name-wrap">
            <p class="profile-name">
              {{ profile.displayName || profile.username || "同学" }}
            </p>
            <p class="profile-role">{{ roleLabel }}</p>
          </div>
          <button
            class="profile-settings"
            type="button"
            aria-label="设置"
            @click="goToSettings"
          >
            <img src="/assets/icons/settings.svg" alt="设置" />
          </button>
        </div>
        <div class="profile-row">学号：{{ profile.studentNo || "未填写" }}</div>
        <div class="profile-row">班级：{{ profile.className || "未填写" }}</div>
        <div class="profile-row">学院：{{ profile.college || "未填写" }}</div>
      </section>

      <section class="menu-card">
        <button
          v-for="item in menuItems"
          :key="item.key"
          class="menu-item"
          :class="{
            active: activeMenu === item.key,
            disabled: !isMenuEnabled(item.key),
          }"
          type="button"
          :disabled="!isMenuEnabled(item.key)"
          @click="handleMenuClick(item.key)"
        >
          {{ item.label }}
        </button>
      </section>
    </aside>

    <main class="dashboard-right">
      <header class="feed-header">
        <h1 class="feed-title">学生信息</h1>
      </header>

      <section class="info-shell student-right-stack">
        <section class="info-card student-filter-card">
          <div class="student-filter-header">
            <div class="info-section-title">搜索</div>
            <input
              v-model="filters.keyword"
              class="info-input student-search"
              type="text"
              placeholder="搜索姓名 / 班别 / 学院 / 学号"
            />
          </div>
          <div class="student-filter-grid">
            <div class="student-filter-row">
              <div class="student-filter-field">
                <span class="info-label">年级</span>
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
            </div>

            <div class="student-filter-row">
              <span class="info-label">班级</span>
              <select v-model="filters.major" class="info-input">
                <option value="">全部</option>
                <option v-for="major in availableMajors" :key="major" :value="major">
                  {{ major }}
                </option>
              </select>
            </div>

            <div class="student-filter-row student-filter-inline">
              <label class="info-choice">
                <input v-model="filters.isHkMoTw" type="checkbox" />
                港澳台
              </label>
              <label class="info-choice">
                <input v-model="filters.isSpecial" type="checkbox" />
                特殊学生
              </label>
            </div>
          </div>
        </section>

        <section class="info-card student-results-card">
          <div class="info-section-title">筛选结果</div>
          <div v-if="pagedStudents.length" class="student-list">
            <div v-for="item in pagedStudents" :key="item.id" class="student-row">
              <input v-model="selectedIds" type="checkbox" :value="item.id" />
              <div class="student-main">
                <div class="student-name">{{ item.name }}</div>
                <div class="student-meta">
                  {{ item.gradeYear }}级 {{ item.college }} {{ item.major }}{{ item.classNo }}班 ·
                  {{ item.studentNo }}
                </div>
              </div>
              <button class="ghost-button" type="button" @click="openDetail(item)">详情</button>
            </div>
          </div>
          <div v-else class="empty-tip">没有匹配的学生。</div>

          <div class="student-pagination">
            <div class="student-pages">
              <button
                v-for="page in totalPages"
                :key="page"
                class="page-button"
                :class="{ active: page === currentPage }"
                type="button"
                @click="setPage(page)"
              >
                {{ page }}
              </button>
              <input
                v-model.number="pageInput"
                class="info-input page-input"
                type="number"
                :min="1"
                :max="totalPages"
                placeholder="页码"
                @change="applyPageInput"
              />
            </div>
            <button class="action-button" type="button">导出</button>
          </div>
        </section>
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
        :class="{ open: viewOpen, closing: viewClosing }"
        :aria-hidden="!viewOpen"
      >
        <header class="publisher-header">
          <div class="publisher-title">学生详情</div>
          <button class="publisher-close" type="button" @click="closeView">
            关闭
          </button>
        </header>
        <div v-if="viewLoading" class="empty-tip">加载中...</div>
        <div v-else-if="viewItem" class="info-shell student-detail-body">
          <div class="info-hero">
            <button class="avatar-square" type="button" disabled>
              <img
                v-if="viewItem.avatarUrl"
                :src="resolveMediaUrl(viewItem.avatarUrl)"
                alt="头像"
              />
              <span v-else>{{ (viewItem.fullName || "同学").slice(0, 1) }}</span>
            </button>
            <div class="info-hero-text">
              <div class="info-hero-title">{{ viewItem.fullName || "未命名" }}</div>
              <div class="info-hero-subtitle">学号：{{ viewItem.studentNo || "-" }}</div>
            </div>
          </div>

          <div class="info-card">
            <div class="info-section-title">个人信息</div>
            <div class="info-form-grid">
              <div class="field-card">
                <span class="info-label">名字</span>
                <div class="info-input info-static">{{ viewItem.fullName || "-" }}</div>
              </div>
              <div class="field-card">
                <span class="info-label">学号</span>
                <div class="info-input info-static">{{ viewItem.studentNo || "-" }}</div>
              </div>
              <div class="field-card field-full">
                <span class="info-label">班别</span>
                <div class="info-input info-static">
                  {{ viewItem.classYear ? viewItem.classYear + "级" : "" }}{{ viewItem.classMajor || "" }}{{ viewItem.classNo ? viewItem.classNo + "班" : "" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">学院</span>
                <div class="info-input info-static">{{ viewItem.college || "-" }}</div>
              </div>
            </div>
          </div>

          <div class="info-card">
            <div class="info-section-title">联系方式</div>
            <div class="info-form-grid">
              <div class="field-card">
                <span class="info-label">电话号码</span>
                <div class="info-input info-static">{{ viewItem.phone || "-" }}</div>
              </div>
              <div class="field-card">
                <span class="info-label">紧急联系电话</span>
                <div class="info-input info-static">{{ viewItem.emergencyPhone || "-" }}</div>
              </div>
              <div class="field-card field-full">
                <span class="info-label">紧急联系人关系</span>
                <div class="info-input info-static">{{ viewItem.emergencyRelation || "-" }}</div>
              </div>
              <div class="field-card field-full">
                <span class="info-label">现住址</span>
                <div class="info-input info-static">{{ viewItem.address || "-" }}</div>
              </div>
            </div>
          </div>

          <div class="info-card">
            <div class="info-section-title">党团信息</div>
            <div class="info-form-grid">
              <div class="field-card">
                <span class="info-label">团员编号</span>
                <div class="info-input info-static">{{ viewItem.leagueNo || "-" }}</div>
              </div>
              <div class="field-card">
                <span class="info-label">入党申请</span>
                <div class="info-input info-static">
                  {{ viewItem.partyApplied ? "是" : "否" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">积极分子日期</span>
                <div class="info-input info-static">{{ viewItem.activistDate || "-" }}</div>
              </div>
              <div class="field-card">
                <span class="info-label">入党申请日期</span>
                <div class="info-input info-static">{{ viewItem.applicationDate || "-" }}</div>
              </div>
              <div class="field-card">
                <span class="info-label">是否未发展党员</span>
                <div class="info-input info-static">
                  {{ viewItem.notDeveloped ? "是" : "否" }}
                </div>
              </div>
            </div>
          </div>

          <div class="info-card">
            <div class="info-section-title">其他信息</div>
            <div class="info-form-grid">
              <div class="field-card">
                <span class="info-label">身份证号</span>
                <div class="info-input info-static">{{ viewItem.idNo || "-" }}</div>
              </div>
              <div class="field-card">
                <span class="info-label">籍贯</span>
                <div class="info-input info-static">{{ viewItem.nativePlace || "-" }}</div>
              </div>
              <div class="field-card">
                <span class="info-label">港澳台</span>
                <div class="info-input info-static">
                  {{ viewItem.hkMoTw ? "是" : "否" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">特殊学生</span>
                <div class="info-input info-static">
                  {{ viewItem.specialStudent ? "是" : "否" }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { filterMenuItemsByRole, isMenuEnabled } from "../constants/menu";
import { getStudentProfileById, searchStudentProfiles } from "../api/profile";

const router = useRouter();
const API_BASE = "http://localhost:8080";

const profile = reactive(loadUser());
const activeMenu = ref("student-info");
const menuItems = computed(() => filterMenuItemsByRole(profile.role));
const selectedIds = ref([]);
const currentPage = ref(1);
const pageInput = ref(null);
const students = ref([]);
const totalPages = ref(1);
const totalItems = ref(0);
const loading = ref(false);
const pageSize = 5;
const viewOpen = ref(false);
const viewClosing = ref(false);
const viewItem = ref(null);
const viewLoading = ref(false);

const classYearOptions = Array.from({ length: 19 }, (_, index) => 2022 + index);
const majorOptions = [
  "软件工程",
  "计算机科学与技术",
  "计算机科学与技术（实验区）",
  "电子商务",
  "市场营销",
  "工商管理",
  "创业管理",
  "工商管理（实验区）",
  "会计学",
  "审计学",
  "国际会计（ACCA）",
  "播音",
];

const filters = reactive({
  classYear: "",
  major: "",
  isHkMoTw: false,
  isSpecial: false,
  keyword: "",
});

const availableMajors = computed(() => {
  return majorOptions;
});


const hasActiveFilters = computed(() => {
  return Boolean(
    filters.classYear ||
      filters.major ||
      filters.isHkMoTw ||
      filters.isSpecial ||
      filters.keyword,
  );
});

const pagedStudents = computed(() => students.value);

watch(
  () => ({
    classYear: filters.classYear,
    major: filters.major,
    isHkMoTw: filters.isHkMoTw,
    isSpecial: filters.isSpecial,
    keyword: filters.keyword,
  }),
  () => {
    currentPage.value = 1;
    pageInput.value = null;
    fetchStudents();
  },
  { deep: true },
);

watch(currentPage, () => {
  fetchStudents();
});

function resetResults() {
  students.value = [];
  totalPages.value = 1;
  totalItems.value = 0;
}

async function fetchStudents() {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize,
    };
    if (filters.classYear) {
      params.classYear = filters.classYear;
    }
    if (filters.major) {
      params.major = filters.major;
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
    const { data } = await searchStudentProfiles(params);
    students.value = (data?.items || []).map((item) => ({
      id: item.id,
      name: item.fullName || "未命名",
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

function openDetail(item) {
  viewOpen.value = true;
  viewClosing.value = false;
  viewLoading.value = true;
  viewItem.value = null;
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
  viewOpen.value = false;
  viewClosing.value = true;
  setTimeout(() => {
    viewItem.value = null;
    viewLoading.value = false;
    viewClosing.value = false;
  }, 260);
}

onMounted(() => {
  fetchStudents();
});

function setPage(page) {
  currentPage.value = page;
  pageInput.value = null;
}

function applyPageInput() {
  if (!pageInput.value) {
    return;
  }
  const safePage = Math.min(Math.max(1, pageInput.value), totalPages.value);
  currentPage.value = safePage;
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

function handleMenuClick(key) {
  if (!isMenuEnabled(key)) {
    return;
  }
  if (key === "achievements") {
    router.push("/achievements");
    return;
  }
  if (key === "my-info") {
    router.push("/myinfos");
    return;
  }
  if (key === "student-info") {
    router.push("/student-info");
    return;
  }
  router.push("/myinfos");
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
  router.push("/settings");
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
  gap: 18px;
  position: relative;
  z-index: 40;
}

.student-right-stack {
  display: grid;
  gap: 14px;
}

.student-filter-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.student-search {
  min-width: 220px;
}

.student-filter-grid {
  display: grid;
  gap: 14px;
}

.student-filter-row {
  display: grid;
  gap: 10px;
}

.student-filter-two {
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.student-filter-field {
  display: grid;
  gap: 8px;
}


.student-filter-inline {
  display: flex;
  gap: 18px;
  align-items: center;
}

.student-results-card {
  gap: 16px;
  position: relative;
  z-index: 1;
}

.student-list {
  display: grid;
  gap: 10px;
}

.student-row {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 12px;
  align-items: center;
  padding: 12px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(3, 107, 114, 0.1);
}

.student-main {
  display: grid;
  gap: 4px;
}

.student-name {
  font-weight: 700;
  color: #0f4d55;
}

.student-meta {
  font-size: 12px;
  color: #5c7178;
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

.page-button {
  border-radius: 999px;
  border: 1px solid rgba(3, 107, 114, 0.25);
  background: rgba(255, 255, 255, 0.7);
  color: #0f4d55;
  height: 32px;
  padding: 0 12px;
  cursor: pointer;
  font-weight: 600;
}

.page-button.active {
  background: rgba(205, 255, 249, 0.9);
  border-color: rgba(3, 107, 114, 0.45);
}

.page-input {
  width: 90px;
  height: 32px;
}

.student-detail-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(3, 18, 20, 0.35);
  backdrop-filter: blur(8px);
  z-index: 60;
}

.student-detail-view {
  position: fixed;
  left: 50%;
  bottom: 16px;
  width: min(980px, calc(100vw - 32px));
  transform: translate(-50%, 120%) scale(0.98);
  opacity: 0;
  filter: blur(6px);
  pointer-events: none;
  border-radius: 22px;
  border: 1px solid rgba(255, 255, 255, 0.35);
  background: linear-gradient(
    140deg,
    rgba(205, 255, 249, 0.92),
    rgba(197, 217, 226, 0.78)
  );
  box-shadow: 0 28px 70px rgba(3, 107, 114, 0.22);
  backdrop-filter: blur(12px);
  z-index: 70;
  transition:
    transform 0.9s cubic-bezier(0.22, 1, 0.36, 1),
    opacity 0.75s ease,
    filter 0.75s ease;
  max-height: 84vh;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow: auto;
  scrollbar-width: none;
  padding: 12px 14px 16px;
}

.student-detail-view.open {
  transform: translate(-50%, 0) scale(1);
  opacity: 1;
  filter: blur(0px);
  pointer-events: auto;
}

.student-detail-view.closing {
  transform: translate(-50%, 120%) scale(0.98);
  opacity: 0;
  filter: blur(6px);
}

.student-detail-view::-webkit-scrollbar {
  width: 0;
  height: 0;
}

.student-detail-body {
  display: grid;
  gap: 14px;
}

.info-static {
  display: flex;
  align-items: center;
}
</style>
