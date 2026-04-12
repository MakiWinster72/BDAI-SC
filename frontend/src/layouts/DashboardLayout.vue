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

    <ToastContainer />
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
import ToastContainer from "../components/ToastContainer.vue";
import {
  getActiveMenuFromRoute,
  getMenuLocation,
} from "../constants/menu";
import { dashboardShellKey } from "../composables/useDashboardShell";
import { navigateWithViewTransition } from "../utils/viewTransition";
import { loadUser } from "../utils/userStorage";

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
</script>
