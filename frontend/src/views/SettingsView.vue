<template>
  <main class="dashboard-right">
      <header class="feed-header">
        <h1 class="feed-title">设置</h1>
      </header>

      <section class="info-shell settings-shell">
        <div class="settings-card">
          <div class="settings-row">
            <div class="settings-text">
              <div class="settings-title">外观</div>
              <div class="settings-subtitle">TODO:明暗模式</div>
            </div>
            <label class="ios-switch" aria-label="切换明暗模式">
              <input type="checkbox" />
              <span class="ios-slider"></span>
            </label>
          </div>
        </div>
        <div class="settings-actions">
          <button class="settings-action" type="button" @click="handleLogout">
            退出登录
          </button>
          <button class="settings-action danger" type="button">
            TODO:注销账号
          </button>
        </div>
      </section>

      <div class="mobile-capsule">
        <div class="capsule-left">
          <div
            class="capsule-action"
            role="button"
            tabindex="0"
            @click="openDashboardSidebar"
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
</template>

<script setup>
import { computed, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import {
  getMenuLocation,
  isMenuEnabled,
} from "../constants/menu";
import { API_BASE } from "../api/request";
import { navigateWithViewTransition } from "../utils/viewTransition";
import { useDashboardShell } from "../composables/useDashboardShell";

const router = useRouter();
const { openSidebar: openDashboardSidebar } = useDashboardShell();
const profile = reactive(loadUser());
const activeMenu = ref("my-info");
const sidebarOpen = ref(false);

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

function resolveMediaUrl(url) {
  if (!url) {
    return "";
  }
  if (url.startsWith("http://") || url.startsWith("https://")) {
    return url;
  }
  return `${API_BASE}${url}`;
}

function handleMenuClick(key) {
  if (!isMenuEnabled(key)) {
    return;
  }
  sidebarOpen.value = false;
  navigateWithViewTransition(router, getMenuLocation(key));
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

function handleLogout() {
  localStorage.removeItem("gcsc_token");
  localStorage.removeItem("gcsc_user");
  router.push("/login");
}
</script>
