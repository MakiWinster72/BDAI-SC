<template>
  <div class="dashboard-layout">
    <transition name="publisher-backdrop">
      <div
        v-if="sidebarOpen"
        class="mobile-sidebar-backdrop"
        @click="closeSidebar"
      ></div>
    </transition>
    <aside class="dashboard-left" :class="{ open: sidebarOpen }">
      <section class="profile-card">
        <div class="profile-row profile-main">
          <div class="profile-avatar">
            <img
              v-if="info.avatarUrl"
              :src="resolveMediaUrl(info.avatarUrl)"
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
        <h1 class="feed-title">我的信息</h1>
      </header>

      <section class="info-shell">
        <div class="info-hero">
          <button
            class="avatar-square"
            type="button"
            :disabled="!isEditing"
            @click="triggerAvatarUpload"
          >
            <img
              v-if="info.avatarUrl"
              :src="resolveMediaUrl(info.avatarUrl)"
              alt="头像"
            />
            <span v-else>点击设置头像</span>
          </button>
          <input
            ref="avatarInput"
            type="file"
            accept="image/*"
            hidden
            @change="onAvatarChange"
          />
          <div class="info-hero-text">
            <div class="info-hero-title">基础信息</div>
            <div class="info-hero-subtitle">请确保信息完整准确</div>
          </div>
          <div class="info-actions">
            <button class="ghost-button" type="button" @click="enterEdit">
              编辑
            </button>
            <button class="action-button" type="button" @click="confirmEdit">
              确认
            </button>
            <button class="ghost-button" type="button">
              请求变更
            </button>
          </div>
        </div>

        <div class="info-card">
          <div class="info-section-title">个人信息</div>
          <div class="info-form-grid">
            <label class="field-card">
              <span class="info-label">名字</span>
              <input
                v-model="info.name"
                class="info-input"
                type="text"
                placeholder="请输入名字"
                :disabled="!isEditing"
              />
            </label>
            <label class="field-card">
              <span class="info-label">学号</span>
              <input
                v-model="info.studentNo"
                class="info-input"
                type="text"
                placeholder="请输入学号"
                :disabled="!isEditing"
              />
            </label>
            <label class="field-card field-full">
              <span class="info-label">班别</span>
              <div class="class-inline">
                <select v-model="info.classYear" class="info-input" :disabled="!isEditing">
                  <option disabled value="">选择年份</option>
                  <option v-for="year in classYearOptions" :key="year" :value="year">
                    {{ year }}
                  </option>
                </select>
                <span class="class-text">级</span>
                <input
                  v-model="info.classMajor"
                  class="info-input"
                  type="text"
                  placeholder="专业"
                  :disabled="!isEditing"
                />
                <input
                  v-model="info.classNo"
                  class="info-input class-num"
                  type="text"
                  placeholder="数字"
                  :disabled="!isEditing"
                />
                <span class="class-text">班</span>
              </div>
            </label>
            <label class="field-card">
              <span class="info-label">学院</span>
              <input
                v-model="info.college"
                class="info-input"
                type="text"
                placeholder="请输入学院"
                :disabled="!isEditing"
              />
            </label>
          </div>
        </div>

        <div class="info-card">
          <div class="info-section-title">联系方式</div>
          <div class="info-form-grid">
            <label class="field-card">
              <span class="info-label">电话号码</span>
              <input
                v-model="info.phone"
                class="info-input"
                type="tel"
                placeholder="请输入电话号码"
                :disabled="!isEditing"
              />
            </label>
            <label class="field-card field-full">
              <span class="info-label">住址</span>
              <input
                v-model="info.address"
                class="info-input"
                type="text"
                placeholder="请输入住址"
                :disabled="!isEditing"
              />
            </label>
            <label class="field-card">
              <span class="info-label">身份证号</span>
              <input
                v-model="info.idNo"
                class="info-input"
                type="text"
                placeholder="请输入身份证号"
                :disabled="!isEditing"
              />
            </label>
            <label class="field-card">
              <span class="info-label">籍贯</span>
              <input
                v-model="info.nativePlace"
                class="info-input"
                type="text"
                placeholder="例：广东广州"
                :disabled="!isEditing"
              />
            </label>
          </div>
        </div>

        <div class="info-card">
          <div class="info-section-title">团组织与入党信息</div>
          <div class="info-form-grid">
            <label class="field-card">
              <span class="info-label">团号</span>
              <input
                v-model="info.leagueNo"
                class="info-input"
                type="text"
                placeholder="请输入团号"
                :disabled="!isEditing"
              />
            </label>
            <div class="field-card field-full">
              <span class="info-label">是否申请入党</span>
              <div class="info-inline">
                <label class="info-choice">
                  <input
                    v-model="info.partyApplied"
                    type="radio"
                    :value="true"
                    :disabled="!isEditing"
                  />
                  是
                </label>
                <label class="info-choice">
                  <input
                    v-model="info.partyApplied"
                    type="radio"
                    :value="false"
                    :disabled="!isEditing"
                  />
                  否
                </label>
              </div>
            </div>
            <label class="field-card">
              <span class="info-label">入党通知书申请日期</span>
              <input
                v-model="info.applicationDate"
                class="info-input"
                type="date"
                :disabled="!isEditing || !info.partyApplied || info.notDeveloped"
              />
            </label>
            <label class="field-card">
              <span class="info-label">成为入党积极分子日期</span>
              <div class="info-inline info-inline-date">
                <input
                  v-model="info.activistDate"
                  class="info-input"
                  type="date"
                  :disabled="!isEditing || !info.partyApplied || info.notDeveloped"
                />
                <label class="info-choice info-choice-muted">
                  <input
                    v-model="info.notDeveloped"
                    type="checkbox"
                    :disabled="!isEditing"
                  />
                  暂未发展
                </label>
              </div>
            </label>
          </div>
        </div>

        <div class="info-card">
          <div class="info-section-title">其他</div>
          <div class="info-form-grid">
            <label class="field-card">
              <span class="info-label">紧急联系人电话</span>
              <input
                v-model="info.emergencyPhone"
                class="info-input"
                type="tel"
                placeholder="请输入紧急联系人电话"
                :disabled="!isEditing"
              />
            </label>
            <label class="field-card">
              <span class="info-label">紧急联系人的关系</span>
              <input
                v-model="info.emergencyRelation"
                class="info-input"
                type="text"
                placeholder="如父母、亲属"
                :disabled="!isEditing"
              />
            </label>
          </div>
        </div>
      </section>
      <div class="mobile-capsule">
        <div class="capsule-left">
          <div
            class="capsule-action"
            role="button"
            tabindex="0"
            @click="openSidebar"
          >
            <span class="capsule-icon" aria-hidden="true">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path d="M4 6h16M4 12h16M4 18h16" />
              </svg>
            </span>
          </div>
        </div>
        <div class="capsule-right"></div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { reactive, computed, ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { filterMenuItemsByRole, isMenuEnabled } from "../constants/menu";
import { getStudentProfile, saveStudentProfile } from "../api/profile";
import { uploadMedia } from "../api/upload";
import { API_BASE } from "../api/request";

const router = useRouter();

const profile = reactive(loadUser());
const activeMenu = ref("my-info");
const isEditing = ref(false);
const avatarInput = ref(null);
const sidebarOpen = ref(false);

const info = reactive({
  name: profile.displayName || profile.username || "",
  avatarUrl: profile.avatarUrl || "",
  studentNo: profile.studentNo || "",
  classYear: "",
  classMajor: "",
  classNo: "",
  className: profile.className || "",
  college: profile.college || "",
  phone: "",
  address: "",
  idNo: "",
  nativePlace: "",
  leagueNo: "",
  partyApplied: false,
  notDeveloped: false,
  applicationDate: "",
  activistDate: "",
  emergencyPhone: "",
  emergencyRelation: "",
});

const classYearOptions = Array.from({ length: 19 }, (_, index) => 2022 + index);

const menuItems = computed(() => filterMenuItemsByRole(profile.role));

const avatarText = computed(() => {
  const name = profile.displayName || profile.username || "同学";
  return name.slice(0, 1).toUpperCase();
});

const roleLabel = computed(() => {
  if (profile.role === "ADMIN") {
    return "管理员";
  }
  if (profile.role === "TEACHER") {
    return "老师";
  }
  return "学生";
});

function handleMenuClick(key) {
  if (!isMenuEnabled(key)) {
    return;
  }
  sidebarOpen.value = false;
  if (key === "my-info") {
    return;
  }
  if (key === "achievements") {
    router.push("/achievements");
    return;
  }
  if (key === "student-info") {
    router.push("/student-info");
    return;
  }
  router.push("/myinfos");
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

function triggerAvatarUpload() {
  if (!isEditing.value) {
    return;
  }
  avatarInput.value && avatarInput.value.click();
}

async function onAvatarChange(event) {
  const [file] = Array.from(event.target.files || []);
  event.target.value = "";
  if (!file) {
    return;
  }
  try {
    const { data } = await uploadMedia(file);
    if (data?.mediaType !== "IMAGE") {
      return;
    }
    info.avatarUrl = data.url || "";
  } catch (err) {
    console.error(err);
  }
}

function enterEdit() {
  isEditing.value = true;
}

async function confirmEdit() {
  if (!isEditing.value) {
    return;
  }
  const className = buildClassName(
    info.classYear,
    info.classMajor,
    info.classNo,
    info.className,
  );
  const payload = {
    fullName: info.name,
    avatarUrl: info.avatarUrl,
    studentNo: info.studentNo,
    classYear: info.classYear || null,
    classMajor: info.classMajor,
    classNo: info.classNo,
    className,
    college: info.college,
    phone: info.phone,
    address: info.address,
    idNo: info.idNo,
    nativePlace: info.nativePlace,
    leagueNo: info.leagueNo,
    partyApplied: info.partyApplied,
    notDeveloped: info.notDeveloped,
    applicationDate: info.applicationDate || null,
    activistDate: info.activistDate || null,
    emergencyPhone: info.emergencyPhone,
    emergencyRelation: info.emergencyRelation,
  };
  try {
    const { data } = await saveStudentProfile(payload);
    applyProfileResponse(data);
    isEditing.value = false;
  } catch (err) {
    console.error(err);
  }
}

function buildClassName(year, major, no, fallback) {
  if (fallback) {
    return fallback;
  }
  const safeYear = year ? `${year}级` : "";
  const safeMajor = major || "";
  const safeNo = no ? `${no}班` : "";
  const result = `${safeYear}${safeMajor}${safeNo}`.trim();
  return result || "";
}

function applyProfileResponse(data) {
  if (!data) {
    return;
  }
  info.name = data.fullName || data.displayName || "";
  info.avatarUrl = data.avatarUrl || profile.avatarUrl || "";
  info.studentNo = data.studentNo || "";
  info.classYear = data.classYear || "";
  info.classMajor = data.classMajor || "";
  info.classNo = data.classNo || "";
  info.className = data.className || "";
  info.college = data.college || "";
  info.phone = data.phone || "";
  info.address = data.address || "";
  info.idNo = data.idNo || "";
  info.nativePlace = data.nativePlace || "";
  info.leagueNo = data.leagueNo || "";
  info.partyApplied = Boolean(data.partyApplied);
  info.notDeveloped = Boolean(data.notDeveloped);
  info.applicationDate = data.applicationDate || "";
  info.activistDate = data.activistDate || "";
  info.emergencyPhone = data.emergencyPhone || "";
  info.emergencyRelation = data.emergencyRelation || "";

  profile.displayName = data.displayName || profile.displayName;
  profile.username = data.username || profile.username;
  profile.avatarUrl = data.avatarUrl || profile.avatarUrl;
  profile.studentNo = data.studentNo || profile.studentNo;
  profile.className = data.className || profile.className;
  profile.college = data.college || profile.college;

  saveUser(profile);
}

function saveUser(data) {
  const user = {
    username: data.username,
    displayName: data.displayName,
    avatarUrl: data.avatarUrl || "",
    role: data.role || profile.role || "STUDENT",
    studentNo: data.studentNo || "",
    className: data.className || "",
    college: data.college || "",
  };
  localStorage.setItem("gcsc_user", JSON.stringify(user));
}

onMounted(async () => {
  try {
    const { data } = await getStudentProfile();
    applyProfileResponse(data);
  } catch (err) {
    console.error(err);
  }
});

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
