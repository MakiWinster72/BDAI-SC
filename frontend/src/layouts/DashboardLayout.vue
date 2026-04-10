<template>
  <div class="dashboard-shell" :class="{ 'dashboard-shell-embedded': isEmbedded }">
    <!-- Full-width brand header -->
    <BrandHeader
      v-if="!isEmbedded"
      :profile="profile"
      @menu-click="handleMenuClick"
      @settings-click="goToSettings"
    />

    <div class="dashboard-layout" :class="{ 'dashboard-layout-embedded': isEmbedded }">
      <DashboardSidebar
        v-if="!isEmbedded"
        :profile="profile"
        :active-menu="activeMenu"
        :active-achievement="activeAchievement"
        :show-achievements-drawer="showAchievementsDrawer"
        @menu-click="handleMenuClick"
        @achievement-entry-click="handleAchievementEntry"
        @settings-click="goToSettings"
      />

      <RouterView />
    </div>

    <div v-if="!isEmbedded" style="margin-top: auto">
      <AppFooter />
    </div>
  </div>
</template>

<script setup>
import { computed, provide, reactive, ref, watch } from "vue";
import { RouterView, useRoute, useRouter } from "vue-router";
import BrandHeader from "../components/BrandHeader.vue";
import DashboardSidebar from "../components/DashboardSidebar.vue";
import AppFooter from "../components/AppFooter.vue";
import {
  getActiveMenuFromRoute,
  getMenuLocation,
} from "../constants/menu";
import { dashboardShellKey } from "../composables/useDashboardShell";
import { navigateWithViewTransition } from "../utils/viewTransition";

const router = useRouter();
const route = useRoute();
const profile = reactive(loadUser());

const activeMenu = computed(() => getActiveMenuFromRoute(route));
const activeAchievement = computed(() => {
  if (route.name !== "achievements") {
    return "all";
  }
  const raw = route.query.category;
  return typeof raw === "string" && raw ? raw : "all";
});
const showAchievementsDrawer = computed(() => route.name !== "settings");
const isEmbedded = computed(() => {
  if (route.name !== "achievements") {
    return false;
  }
  const raw = route.query.embed;
  if (typeof raw !== "string") {
    return false;
  }
  const value = raw.trim().toLowerCase();
  return value === "1" || value === "true";
});

provide(dashboardShellKey, {
  openSidebar,
  closeSidebar,
});

watch(
  () => route.name,
  () => {
    closeSidebar();
  },
  { immediate: true },
);

function openSidebar() {
}

function closeSidebar() {
}

function handleMenuClick(key) {
  closeSidebar();
  navigateWithViewTransition(router, getMenuLocation(key));
}

function handleAchievementEntry(key) {
  closeSidebar();
  navigateWithViewTransition(router, {
    path: "/achievements",
    query: { category: key || "all" },
  });
}

function goToSettings() {
  closeSidebar();
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
      studentCategory: raw.studentCategory || "",
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
      studentCategory: "",
    };
  }
}
</script>
