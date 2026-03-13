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
              </div>
            </div>
            <div class="student-filter-field">
              <span class="info-label">学院</span>
              <select v-model="filters.college" class="info-input">
                <option value="">全部</option>
                <option v-for="college in collegeOptions" :key="college" :value="college">
                  {{ college }}
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

      <section v-if="hasActiveFilters" class="info-card student-results-card">
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
    </main>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { filterMenuItemsByRole, isMenuEnabled } from "../constants/menu";

const router = useRouter();
const API_BASE = "http://localhost:8080";

const profile = reactive(loadUser());
const activeMenu = ref("student-info");
const menuItems = computed(() => filterMenuItemsByRole(profile.role));
const selectedIds = ref([]);
const currentPage = ref(1);
const pageInput = ref(null);
const yearMenuOpen = ref(false);

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

const students = ref([
  {
    id: 1,
    name: "李思涵",
    gradeYear: "2024",
    college: "大数据与人工智能学院",
    major: "软件工程",
    classNo: "1",
    studentNo: "20240101",
    tags: [],
  },
  {
    id: 2,
    name: "张宇航",
    gradeYear: "2023",
    college: "工商学院",
    major: "市场营销",
    classNo: "2",
    studentNo: "20230218",
    tags: ["HK"],
  },
  {
    id: 3,
    name: "周雨晴",
    gradeYear: "2022",
    college: "会计学院",
    major: "会计学",
    classNo: "3",
    studentNo: "20220176",
    tags: ["SPECIAL"],
  },
  {
    id: 4,
    name: "陈亦南",
    gradeYear: "2024",
    college: "湾区与影视学院",
    major: "播音",
    classNo: "1",
    studentNo: "20240188",
    tags: [],
  },
  {
    id: 5,
    name: "林芷妍",
    gradeYear: "2023",
    college: "大数据与人工智能学院",
    major: "计算机科学与技术",
    classNo: "4",
    studentNo: "20230303",
    tags: [],
  },
  {
    id: 6,
    name: "王若凡",
    gradeYear: "2022",
    college: "会计学院",
    major: "国际会计（ACCA）",
    classNo: "2",
    studentNo: "20220229",
    tags: ["HK"],
  },
]);

const availableMajors = computed(() => {
  if (!filters.college) {
    const majors = Object.values(collegeMajorsMap).flat();
    return Array.from(new Set(majors));
  }
  return collegeMajorsMap[filters.college] || [];
});

const yearLabel = computed(() => filters.classYear || "全部");

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

const filteredStudents = computed(() => {
  if (!hasActiveFilters.value) {
    return [];
  }
  const keyword = filters.keyword.trim().toLowerCase();
  return students.value.filter((student) => {
    if (filters.classYear && student.gradeYear !== String(filters.classYear)) {
      return false;
    }
    if (filters.college && student.college !== filters.college) {
      return false;
    }
    if (filters.major && student.major !== filters.major) {
      return false;
    }
    if (filters.isHkMoTw && !student.tags.includes("HK")) {
      return false;
    }
    if (filters.isSpecial && !student.tags.includes("SPECIAL")) {
      return false;
    }
    if (keyword) {
      const haystack = `${student.name}${student.college}${student.major}${student.classNo}${student.studentNo}`.toLowerCase();
      if (!haystack.includes(keyword)) {
        return false;
      }
    }
    return true;
  });
});

const totalPages = computed(() => {
  if (!filteredStudents.value.length) {
    return 1;
  }
  return Math.ceil(filteredStudents.value.length / 5);
});

const pagedStudents = computed(() => {
  if (!filteredStudents.value.length) {
    return [];
  }
  const start = (currentPage.value - 1) * 5;
  return filteredStudents.value.slice(start, start + 5);
});

watch(filteredStudents, () => {
  currentPage.value = 1;
  pageInput.value = null;
});

function toggleYearMenu() {
  yearMenuOpen.value = !yearMenuOpen.value;
}

function selectYear(value) {
  filters.classYear = value;
  yearMenuOpen.value = false;
}

function handleDocumentClick(event) {
  if (event.target.closest(".student-select")) {
    return;
  }
  yearMenuOpen.value = false;
}

onMounted(() => {
  document.addEventListener("click", handleDocumentClick);
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
  border-radius: 12px;
  border: 1px solid rgba(3, 107, 114, 0.2);
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 18px 38px rgba(3, 107, 114, 0.12);
  z-index: 5;
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

.student-filter-inline {
  display: flex;
  gap: 18px;
  align-items: center;
}

.student-results-card {
  gap: 16px;
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
