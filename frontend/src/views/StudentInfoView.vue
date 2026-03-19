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
              <div class="student-filter-field">
                <span class="info-label">专业</span>
                <select v-model="filters.major" class="info-input">
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
            </div>

            <div class="student-filter-row">
              <div class="student-filter-field">
                <span class="info-label">班级</span>
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
                    min="1"
                    max="10"
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
            <div
              v-for="item in pagedStudents"
              :key="item.id"
              class="student-row"
            >
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
        <div v-else-if="viewItem" class="info-shell student-detail-body">
          <div class="info-hero">
            <button class="avatar-square" type="button" disabled>
              <img
                v-if="viewItem.avatarUrl"
                :src="resolveMediaUrl(viewItem.avatarUrl)"
                alt="头像"
              />
              <span v-else>{{
                (viewItem.fullName || "同学").slice(0, 1)
              }}</span>
            </button>
            <div class="info-hero-text">
              <div class="info-hero-title">
                {{ viewItem.fullName || "未命名" }}
              </div>
              <div class="info-hero-subtitle">
                学号：{{ viewItem.studentNo || "-" }}
              </div>
            </div>
            <div class="info-actions info-actions-single">
              <button
                class="action-button"
                type="button"
                @click="openAchievements"
              >
                个人成就
              </button>
            </div>
          </div>

          <div class="info-card">
            <div class="info-section-title">学籍信息</div>
            <div class="info-form-grid">
              <div class="field-card">
                <span class="info-label">名字</span>
                <div class="info-input info-static">
                  {{ viewItem.fullName || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">学号</span>
                <div class="info-input info-static">
                  {{ viewItem.studentNo || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">年级</span>
                <div class="info-input info-static">
                  {{ viewItem.classYear ? viewItem.classYear + "级" : "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">学院</span>
                <div class="info-input info-static">
                  {{ viewItem.college || "-" }}
                </div>
              </div>
              <div class="field-card field-full">
                <span class="info-label">班级</span>
                <div class="info-input info-static">
                  {{ buildClassName(viewItem) || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">入学时间</span>
                <div class="info-input info-static">
                  {{ viewItem.enrollmentDate || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">学生类别</span>
                <div class="info-input info-static">
                  {{ viewItem.studentCategory || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">班主任</span>
                <div class="info-input info-static">
                  {{ viewItem.classTeacher || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">辅导员</span>
                <div class="info-input info-static">
                  {{ viewItem.counselor || "-" }}
                </div>
              </div>
            </div>
          </div>

          <div class="info-card">
            <div class="info-section-title">个人证件与联系方式</div>
            <div class="info-form-grid">
              <div class="field-card">
                <span class="info-label">民族</span>
                <div class="info-input info-static">
                  {{ viewItem.ethnicity || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">政治面貌</span>
                <div class="info-input info-static">
                  {{ viewItem.politicalStatus || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">手机号码</span>
                <div class="info-input info-static">
                  {{ viewItem.phone || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">身份证件号</span>
                <div class="info-input info-static">
                  {{ viewItem.idNo || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">籍贯</span>
                <div class="info-input info-static">
                  {{ viewItem.nativePlace || "-" }}
                </div>
              </div>
              <div class="field-card field-full">
                <span class="info-label">住址</span>
                <div class="info-input info-static">
                  {{ viewItem.address || "-" }}
                </div>
              </div>
            </div>
          </div>

          <div class="info-card">
            <div class="info-section-title">住宿信息</div>
            <div class="info-form-grid">
              <div class="field-card field-full">
                <span class="info-label">是否在外居住</span>
                <div class="info-input info-static">
                  {{ viewItem.offCampusLiving ? "是" : "否" }}
                </div>
              </div>
              <div
                class="field-card field-full"
                v-if="viewItem.offCampusLiving"
              >
                <span class="info-label">外居住详细地址</span>
                <div class="info-input info-static">
                  {{ viewItem.offCampusAddress || "-" }}
                </div>
              </div>
              <div class="field-card" v-if="!viewItem.offCampusLiving">
                <span class="info-label">住宿校区</span>
                <div class="info-input info-static">
                  {{ viewItem.dormCampus || "-" }}
                </div>
              </div>
              <div class="field-card" v-if="!viewItem.offCampusLiving">
                <span class="info-label">住宿楼栋</span>
                <div class="info-input info-static">
                  {{ viewItem.dormBuilding || "-" }}
                </div>
              </div>
              <div class="field-card" v-if="!viewItem.offCampusLiving">
                <span class="info-label">住宿房间</span>
                <div class="info-input info-static">
                  {{ viewItem.dormRoom || "-" }}
                </div>
              </div>
            </div>
          </div>

          <div class="info-card">
            <div class="info-section-title">团组织与入党信息</div>
            <div class="info-form-grid three">
              <div class="field-card field-full">
                <span class="info-label">是否入团</span>
                <div class="info-input info-static">
                  {{ viewItem.leagueJoined ? "是" : "否" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">提交入团申请书时间</span>
                <div class="info-input info-static">
                  {{ viewItem.leagueApplicationDate || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">入团时间</span>
                <div class="info-input info-static">
                  {{
                    formatDateOrStatus(
                      viewItem.leagueJoinDate,
                      viewItem.leagueDeveloping,
                      "正在发展",
                    )
                  }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">团号</span>
                <div class="info-input info-static">
                  {{ viewItem.leagueNo || "-" }}
                </div>
              </div>
              <div class="field-card field-full">
                <span class="info-label">是否申请入党</span>
                <div class="info-input info-static">
                  {{ viewItem.partyApplied ? "是" : "否" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">提交入党申请书时间</span>
                <div class="info-input info-static">
                  {{ viewItem.applicationDate || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">确定积极分子时间</span>
                <div class="info-input info-static">
                  {{
                    formatDateOrStatus(
                      viewItem.activistDate,
                      viewItem.activistDeveloping,
                      "正在发展",
                    )
                  }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">上党课时间</span>
                <div class="info-input info-static">
                  {{
                    formatDateOrStatus(
                      viewItem.partyTrainingDate,
                      viewItem.partyTrainingPending,
                      "暂未报名",
                    )
                  }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">确定发展对象时间</span>
                <div class="info-input info-static">
                  {{
                    formatDateOrStatus(
                      viewItem.developmentTargetDate,
                      viewItem.developmentTargetDeveloping,
                      "正在发展",
                    )
                  }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">接收为预备党员时间</span>
                <div class="info-input info-static">
                  {{
                    formatDateOrStatus(
                      viewItem.probationaryMemberDate,
                      viewItem.probationaryDeveloping,
                      "正在发展",
                    )
                  }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">转为正式党员时间</span>
                <div class="info-input info-static">
                  {{
                    formatDateOrStatus(
                      viewItem.fullMemberDate,
                      viewItem.fullMemberDeveloping,
                      "正在发展",
                    )
                  }}
                </div>
              </div>
            </div>
          </div>

          <div class="info-card">
            <div class="info-section-title">教育经历</div>
            <div v-if="educationRows.length" class="education-table-wrap">
              <table class="education-table">
                <thead>
                  <tr>
                    <th>时间段</th>
                    <th>学校名称</th>
                    <th>学历</th>
                    <th>证明人</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="(item, index) in educationRows"
                    :key="`edu-view-${index}`"
                  >
                    <td>
                      <div class="education-period">
                        <div class="info-input info-static">
                          {{ item.startDate || "-" }}
                        </div>
                        <span class="education-sep">至</span>
                        <div class="info-input info-static">
                          {{ item.isCurrent ? "至今" : item.endDate || "-" }}
                        </div>
                      </div>
                    </td>
                    <td>
                      <div class="info-input info-static">
                        {{ item.schoolName || "-" }}
                      </div>
                    </td>
                    <td>
                      <div class="info-input info-static">
                        {{ item.educationLevel || "-" }}
                      </div>
                    </td>
                    <td>
                      <div class="info-input info-static">
                        {{ item.witness || "-" }}
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div v-else class="empty-tip">暂无教育经历</div>
          </div>

          <div class="info-card">
            <div class="info-section-title">家庭信息</div>
            <div class="info-form-grid family-grid">
              <div class="family-section-title">父亲</div>
              <div class="field-card">
                <span class="info-label">姓名</span>
                <div class="info-input info-static">
                  {{ viewItem.fatherName || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">手机号码</span>
                <div class="info-input info-static">
                  {{ viewItem.fatherPhone || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">工作单位</span>
                <div class="info-input info-static">
                  {{ viewItem.fatherWorkUnit || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">职务</span>
                <div class="info-input info-static">
                  {{ viewItem.fatherTitle || "-" }}
                </div>
              </div>
              <div class="family-section-title">母亲</div>
              <div class="field-card">
                <span class="info-label">姓名</span>
                <div class="info-input info-static">
                  {{ viewItem.motherName || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">手机号码</span>
                <div class="info-input info-static">
                  {{ viewItem.motherPhone || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">工作单位</span>
                <div class="info-input info-static">
                  {{ viewItem.motherWorkUnit || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">职务</span>
                <div class="info-input info-static">
                  {{ viewItem.motherTitle || "-" }}
                </div>
              </div>
            </div>
          </div>

          <div class="info-card">
            <div class="info-section-title">紧急联系人</div>
            <div class="info-form-grid">
              <div class="field-card">
                <span class="info-label">紧急联系人电话</span>
                <div class="info-input info-static">
                  {{ viewItem.emergencyPhone || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">紧急联系人的关系</span>
                <div class="info-input info-static">
                  {{ viewItem.emergencyRelation || "-" }}
                </div>
              </div>
            </div>
          </div>
        </div>
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
const achievementsOpen = ref(false);
const achievementsClosing = ref(false);

const classYearOptions = Array.from({ length: 11 }, (_, index) => 2020 + index);
const majorOptions = [
  "计算机科学与技术",
  "计算机科学与技术（实验区）",
  "软件工程",
  "电子商务",
  "大数据管理与应用（佛山校区全学段）",
  "大数据管理与应用（数字治理）",
];

const filters = reactive({
  classYear: "",
  major: "",
  classNo: "",
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
    filters.classNo ||
    filters.isHkMoTw ||
    filters.isSpecial ||
    filters.keyword,
  );
});

const pagedStudents = computed(() => students.value);
const educationRows = computed(() => {
  const items = viewItem.value?.educationExperiences;
  if (!Array.isArray(items)) {
    return [];
  }
  return items.filter((item) => {
    if (!item) {
      return false;
    }
    return (
      item.startDate ||
      item.endDate ||
      item.schoolName ||
      item.educationLevel ||
      item.witness ||
      item.isCurrent
    );
  });
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
    major: filters.major,
    classNo: filters.classNo,
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

function formatDateOrStatus(dateValue, statusFlag, statusText) {
  if (statusFlag) {
    return statusText;
  }
  return dateValue || "-";
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
    router.push({ path: "/achievements", query: { category: "all" } });
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
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
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

.student-stepper {
  display: grid;
  grid-template-columns: 36px minmax(0, 1fr) 36px;
  gap: 10px;
  align-items: center;
}

.stepper-button {
  height: 38px;
  border-radius: 12px;
  border: 1px solid rgba(3, 107, 114, 0.25);
  background: rgba(255, 255, 255, 0.6);
  color: #0f4d55;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}

.stepper-button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.stepper-input {
  text-align: center;
}

.student-filter-inline {
  display: flex;
  gap: 18px;
  align-items: center;
  grid-column: 1 / -1;
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
    filter 0.75s ease,
    left 0.75s cubic-bezier(0.22, 1, 0.36, 1),
    width 0.75s cubic-bezier(0.22, 1, 0.36, 1);
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
  filter: blur(0px);
  pointer-events: auto;
}

.student-detail-view.split.open {
  transform: translate(0, 0) scale(1);
}

.student-detail-view.closing {
  transform: translate(-50%, 120%) scale(0.98);
  opacity: 0;
  filter: blur(6px);
}

.student-detail-view.split.closing {
  transform: translate(0, 120%) scale(0.98);
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

.info-actions-single {
  grid-template-columns: minmax(0, 1fr);
}

.student-achievements-view {
  position: fixed;
  right: 16px;
  bottom: 16px;
  width: min(720px, calc(50vw - 24px));
  height: 84vh;
  transform: translate(0, 120%) scale(0.98);
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
  z-index: 72;
  transition:
    transform 0.9s cubic-bezier(0.22, 1, 0.36, 1),
    opacity 0.75s ease,
    filter 0.75s ease;
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
  filter: blur(0px);
  pointer-events: auto;
}

.student-achievements-view.closing {
  transform: translate(0, 120%) scale(0.98);
  opacity: 0;
  filter: blur(6px);
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
</style>
