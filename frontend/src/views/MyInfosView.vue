<template>
  <div class="dashboard-layout">
    <aside class="dashboard-left">
      <section class="profile-card">
        <div class="profile-row profile-main">
          <div class="profile-avatar">{{ avatarText }}</div>
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

      <section class="info-card">
        <div class="info-header">
          <button class="avatar-square" type="button">
            点击设置头像
          </button>
          <div class="info-grid">
            <div v-for="item in primaryInfos" :key="item.key" class="info-row">
              <span class="info-label">{{ item.label }}</span>
              <span class="info-value">{{ info[item.key] || "未填写" }}</span>
            </div>
          </div>
        </div>

        <div class="info-grid info-grid-secondary">
          <div v-for="item in secondaryInfos" :key="item.key" class="info-row">
            <span class="info-label">{{ item.label }}</span>
            <span class="info-value">{{ info[item.key] || "未填写" }}</span>
          </div>
        </div>

        <div class="info-divider"></div>

        <div class="info-grid info-grid-tertiary">
          <div v-for="item in tertiaryInfos" :key="item.key" class="info-row">
            <span class="info-label">{{ item.label }}</span>
            <span class="info-value">{{ info[item.key] || "未填写" }}</span>
          </div>
        </div>

        <div class="info-actions">
          <button class="action-button" type="button">
            确认（初次填写）
          </button>
          <button class="ghost-button" type="button">
            请求变更
          </button>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { reactive, computed, ref } from "vue";
import { useRouter } from "vue-router";
import { MENU_ITEMS, isMenuEnabled } from "../constants/menu";

const router = useRouter();

const profile = reactive(loadUser());
const activeMenu = ref("my-info");

const info = reactive({
  name: "",
  studentNo: "",
  className: "",
  college: "",
  phone: "",
  address: "",
  idNo: "",
  nativePlace: "",
  leagueNo: "",
  partyApplied: "",
  applicationDate: "",
  activistDate: "",
});

const primaryInfos = [
  { key: "name", label: "名字" },
  { key: "studentNo", label: "学号" },
  { key: "className", label: "班别" },
  { key: "college", label: "学院" },
];

const secondaryInfos = [
  { key: "phone", label: "电话号码" },
  { key: "address", label: "住址" },
  { key: "idNo", label: "身份证号" },
  { key: "nativePlace", label: "籍贯" },
];

const tertiaryInfos = [
  { key: "leagueNo", label: "团号" },
  { key: "partyApplied", label: "是否申请入党" },
  { key: "applicationDate", label: "入党通知书申请日期" },
  { key: "activistDate", label: "成为入党积极分子日期" },
];

const menuItems = computed(() => MENU_ITEMS);

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
  if (key === "my-info") {
    return;
  }
  if (key === "achievements") {
    router.push("/achievements");
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

function loadUser() {
  try {
    const raw = JSON.parse(localStorage.getItem("gcsc_user") || "{}");
    return {
      username: raw.username || "",
      displayName: raw.displayName || "",
      role: raw.role || "STUDENT",
      studentNo: raw.studentNo || "",
      className: raw.className || "",
      college: raw.college || "",
    };
  } catch {
    return {
      username: "",
      displayName: "",
      role: "STUDENT",
      studentNo: "",
      className: "",
      college: "",
    };
  }
}
</script>
