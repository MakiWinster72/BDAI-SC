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

      <div class="student-right-stack">
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
            <div class="student-filter-row student-filter-two">
              <div class="student-filter-field">
                <span class="info-label">年级</span>
                <div class="student-select" @click.stop="toggleYearMenu">
                  <button class="info-input student-select-trigger" type="button">
                    {{ yearLabel }}
                  </button>
                <transition name="student-dropdown">
                  <div v-if="yearMenuOpen" class="student-select-menu">
                    <button
                      class="student-select-option"
                      type="button"
                      @click="selectYear('')"
                    >
                      全部
                    </button>
                    <button
                      v-for="year in classYearOptions"
                      :key="year"
                      class="student-select-option"
                      type="button"
                      @click="selectYear(String(year))"
                    >
                      {{ year }}
                    </button>
                  </div>
                </transition>
              </div>
            </div>
            <div class="student-filter-field">
              <span class="info-label">学院</span>
              <div class="student-select" @click.stop="toggleCollegeMenu">
                <button class="info-input student-select-trigger" type="button">
                  {{ collegeLabel }}
                </button>
                <transition name="student-dropdown">
                  <div v-if="collegeMenuOpen" class="student-select-menu">
                    <button
                      class="student-select-option"
                      type="button"
                      @click="selectCollege('')"
                    >
                      全部
                    </button>
                    <button
                      v-for="college in collegeOptions"
                      :key="college"
                      class="student-select-option"
                      type="button"
                      @click="selectCollege(college)"
                    >
                      {{ college }}
                    </button>
                  </div>
                </transition>
              </div>
            </div>
          </div>

          <div class="student-filter-row">
            <span class="info-label">班级</span>
            <div class="student-select" @click.stop="toggleMajorMenu">
              <button class="info-input student-select-trigger" type="button">
                {{ majorLabel }}
              </button>
              <transition name="student-dropdown">
                <div v-if="majorMenuOpen" class="student-select-menu">
                  <button
                    class="student-select-option"
                    type="button"
                    @click="selectMajor('')"
                  >
                    全部
                  </button>
                  <button
                    v-for="major in availableMajors"
                    :key="major"
                    class="student-select-option"
                    type="button"
                    @click="selectMajor(major)"
                  >
                    {{ major }}
                  </button>
                </div>
              </transition>
            </div>
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
              <button class="ghost-button" type="button">详情</button>
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
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { filterMenuItemsByRole, isMenuEnabled } from "../constants/menu";
import { searchStudentProfiles } from "../api/profile";

const router = useRouter();
const API_BASE = "http://localhost:8080";

const profile = reactive(loadUser());
const activeMenu = ref("student-info");
const menuItems = computed(() => filterMenuItemsByRole(profile.role));
const selectedIds = ref([]);
const currentPage = ref(1);
const pageInput = ref(null);
const yearMenuOpen = ref(false);
const collegeMenuOpen = ref(false);
const majorMenuOpen = ref(false);
const students = ref([]);
const totalPages = ref(1);
const totalItems = ref(0);
const loading = ref(false);
const pageSize = 5;

const classYearOptions = Array.from({ length: 19 }, (_, index) => 2022 + index);
const collegeOptions = [
  "大数据与人工智能学院",
  "工商学院",
  "会计学院",
  "湾区与影视学院",
];
const collegeMajorsMap = {
  大数据与人工智能学院: [
    "软件工程",
    "计算机科学与技术",
    "计算机科学与技术（实验区）",
    "电子商务",
  ],
  工商学院: [
    "市场营销",
    "工商管理",
    "创业管理",
    "工商管理（实验区）",
  ],
  会计学院: ["会计学", "审计学", "国际会计（ACCA）"],
  湾区与影视学院: ["播音"],
};

const filters = reactive({
  classYear: "",
  college: "",
  major: "",
  isHkMoTw: false,
  isSpecial: false,
  keyword: "",
});

const availableMajors = computed(() => {
  if (!filters.college) {
    const majors = Object.values(collegeMajorsMap).flat();
    return Array.from(new Set(majors));
  }
  return collegeMajorsMap[filters.college] || [];
});

const yearLabel = computed(() => filters.classYear || "全部");
const collegeLabel = computed(() => filters.college || "全部");
const majorLabel = computed(() => filters.major || "全部");

const hasActiveFilters = computed(() => {
  return Boolean(
    filters.classYear ||
      filters.college ||
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
    college: filters.college,
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
    if (filters.college) {
      params.college = filters.college;
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
    }));
    totalPages.value = Math.max(1, data?.totalPages || 1);
    totalItems.value = data?.total || 0;
  } catch {
    resetResults();
  } finally {
    loading.value = false;
  }
}

function toggleYearMenu() {
  yearMenuOpen.value = !yearMenuOpen.value;
  if (yearMenuOpen.value) {
    collegeMenuOpen.value = false;
    majorMenuOpen.value = false;
  }
}

function selectYear(value) {
  filters.classYear = value;
  yearMenuOpen.value = false;
}

function toggleCollegeMenu() {
  collegeMenuOpen.value = !collegeMenuOpen.value;
  if (collegeMenuOpen.value) {
    yearMenuOpen.value = false;
    majorMenuOpen.value = false;
  }
}

function selectCollege(value) {
  filters.college = value;
  if (filters.college && !availableMajors.value.includes(filters.major)) {
    filters.major = "";
  }
  collegeMenuOpen.value = false;
}

function toggleMajorMenu() {
  majorMenuOpen.value = !majorMenuOpen.value;
  if (majorMenuOpen.value) {
    yearMenuOpen.value = false;
    collegeMenuOpen.value = false;
  }
}

function selectMajor(value) {
  filters.major = value;
  majorMenuOpen.value = false;
}

function handleDocumentClick(event) {
  if (event.target.closest(".student-select")) {
    return;
  }
  yearMenuOpen.value = false;
  collegeMenuOpen.value = false;
  majorMenuOpen.value = false;
}

onMounted(() => {
  document.addEventListener("click", handleDocumentClick);
  fetchStudents();
});

onBeforeUnmount(() => {
  document.removeEventListener("click", handleDocumentClick);
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
  if (key === "good-news") {
    router.push("/congra");
    return;
  }
  if (key === "records") {
    router.push("/memory");
    return;
  }
  router.push("/home");
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

.student-select {
  position: relative;
  overflow: visible;
}

.student-select-trigger {
  width: 100%;
  text-align: left;
  cursor: pointer;
}

.student-select-menu {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  right: 0;
  max-height: 190px;
  overflow-y: auto;
  border-radius: 16px;
  border: 1px solid rgba(3, 107, 114, 0.18);
  background-color: rgba(255, 255, 255, 0.6);
  background-image: linear-gradient(
    130deg,
    rgba(205, 255, 249, 0.9),
    rgba(197, 217, 226, 0.7)
  );
  box-shadow: 0 18px 38px rgba(3, 107, 114, 0.18);
  -webkit-backdrop-filter: blur(14px);
  backdrop-filter: blur(14px);
  z-index: 30;
  padding: 6px;
  display: grid;
  gap: 4px;
}

.student-select-option {
  border: none;
  background: transparent;
  text-align: left;
  padding: 8px 10px;
  border-radius: 10px;
  cursor: pointer;
  color: #0f4d55;
  font-size: 13px;
}

.student-select-option:hover {
  background: rgba(205, 255, 249, 0.9);
}

.student-dropdown-enter-active,
.student-dropdown-leave-active {
  transition: opacity 160ms ease, transform 160ms ease;
}

.student-dropdown-enter-from,
.student-dropdown-leave-to {
  opacity: 0;
  transform: translateY(-6px) scale(0.98);
}

.student-dropdown-enter-to,
.student-dropdown-leave-from {
  opacity: 1;
  transform: translateY(0) scale(1);
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
</style>
