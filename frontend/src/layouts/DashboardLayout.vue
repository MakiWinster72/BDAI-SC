<template>
  <div
    class="dashboard-layout"
    :class="{ 'dashboard-layout-embedded': isEmbedded }"
  >
    <transition name="publisher-backdrop">
      <div
        v-if="sidebarOpen && !isEmbedded"
        class="mobile-sidebar-backdrop"
        @click="closeSidebar"
      ></div>
    </transition>

    <DashboardSidebar
      v-if="!isEmbedded"
      :profile="profile"
      :active-menu="activeMenu"
      :active-achievement="activeAchievement"
      :show-achievements-drawer="showAchievementsDrawer"
      :achievements-open="achievementsOpen"
      :sidebar-open="sidebarOpen"
      @menu-click="handleMenuClick"
      @achievement-entry-click="handleAchievementEntry"
      @toggle-achievements="toggleAchievements"
      @settings-click="goToSettings"
    />

    <RouterView />
  </div>
</template>

<script setup>
import { computed, provide, reactive, ref, watch } from "vue";
import { RouterView, useRoute, useRouter } from "vue-router";
import DashboardSidebar from "../components/DashboardSidebar.vue";
import {
  getActiveMenuFromRoute,
  getMenuLocation,
} from "../constants/menu";
import { dashboardShellKey } from "../composables/useDashboardShell";
import { navigateWithViewTransition } from "../utils/viewTransition";

const router = useRouter();
const route = useRoute();
const profile = reactive(loadUser());
const sidebarOpen = ref(false);
const achievementsOpen = ref(route.name === "achievements");

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
  (name) => {
    achievementsOpen.value = name === "achievements";
    if (isEmbedded.value) {
      sidebarOpen.value = false;
      return;
    }
    closeSidebar();
  },
  { immediate: true },
);

function openSidebar() {
  if (isEmbedded.value) {
    return;
  }
  sidebarOpen.value = true;
}

function closeSidebar() {
  sidebarOpen.value = false;
}

function handleMenuClick(key) {
  closeSidebar();
  if (key === "achievements") {
    if (route.name === "achievements") {
      achievementsOpen.value = !achievementsOpen.value;
      return;
    }
    achievementsOpen.value = true;
  } else {
    achievementsOpen.value = false;
  }
  navigateWithViewTransition(router, getMenuLocation(key));
}

function handleAchievementEntry(key) {
  closeSidebar();
  achievementsOpen.value = true;
  navigateWithViewTransition(router, {
    path: "/achievements",
    query: { category: key || "all" },
  });
}

function toggleAchievements() {
  if (route.name === "achievements") {
    achievementsOpen.value = !achievementsOpen.value;
    return;
  }
  achievementsOpen.value = true;
  navigateWithViewTransition(router, getMenuLocation("achievements"));
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
