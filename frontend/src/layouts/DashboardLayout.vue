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
        :notification-active-category="notificationActiveCategory"
        :notification-active-entry="notificationActiveEntry"
        :class-reviews-active-category="classReviewsActiveCategory"
        :class-reviews-active-entry="classReviewsActiveEntry"
        :class-review-entries="classReviewEntries"
        @menu-click="handleMenuClick"
        @achievement-entry-click="handleAchievementEntry"
        @notification-entry-click="handleNotificationEntry"
        @class-reviews-entry-click="handleClassReviewsEntry"
        @class-reviews-category-change="handleClassReviewsCategory"
        @settings-click="goToSettings"
      />

      <RouterView />
    </div>

    <!-- Mobile sidebar overlay -->
    <Transition name="sidebar">
      <div v-if="sidebarOpen" class="mobile-sidebar-backdrop" @click="closeSidebar" />
    </Transition>
    <Transition name="sidebar-panel">
      <div v-if="sidebarOpen" class="mobile-sidebar-panel">
        <DashboardSidebar
          :profile="profile"
          :active-menu="activeMenu"
          :active-achievement="activeAchievement"
          :show-achievements-drawer="showAchievementsDrawer"
          :notification-active-category="notificationActiveCategory"
          :notification-active-entry="notificationActiveEntry"
          :class-reviews-active-category="classReviewsActiveCategory"
          :class-reviews-active-entry="classReviewsActiveEntry"
          :class-review-entries="classReviewEntries"
          @menu-click="handleMenuClick"
          @achievement-entry-click="handleAchievementEntry"
          @notification-entry-click="handleNotificationEntry"
          @class-reviews-entry-click="handleClassReviewsEntry"
          @class-reviews-category-change="handleClassReviewsCategory"
          @settings-click="goToSettings"
        />
      </div>
    </Transition>

    <ToastContainer />
    <div v-if="!isEmbedded" class="dashboard-footer-wrap">
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
const sidebarOpen = ref(false);

const activeMenu = computed(() => getActiveMenuFromRoute(route));
const activeAchievement = computed(() => {
  if (route.name !== "achievements") {
    return "all";
  }
  const raw = route.query.category;
  return typeof raw === "string" && raw ? raw : "all";
});
const notificationActiveCategory = computed(() => {
  if (route.name !== "notifications") return "pending";
  const raw = route.query.category;
  return typeof raw === "string" && raw ? raw : "pending";
});
const notificationActiveEntry = computed(() => {
  if (route.name !== "notifications") return "";
  return typeof route.query.entry === "string" ? route.query.entry : "";
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

const classReviewsActiveCategory = computed(() => {
  if (route.name !== "notifications" || route.query.panel !== "class-reviews") return "pending";
  const raw = route.query.category;
  return typeof raw === "string" && raw ? raw : "pending";
});
const classReviewsActiveEntry = computed(() => {
  if (route.name !== "notifications" || route.query.panel !== "class-reviews") return "";
  return typeof route.query.entry === "string" ? route.query.entry : "";
});
const classReviewEntries = computed(() => []); // Not used in layout, CardMenu gets it directly from useNotifications

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
  sidebarOpen.value = true;
}

function closeSidebar() {
  sidebarOpen.value = false;
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

function handleNotificationEntry({ category, entryId }) {
  if (entryId) closeSidebar();
  navigateWithViewTransition(router, {
    path: "/notifications",
    query: { category, entry: entryId || "" },
  });
}

function handleClassReviewsEntry({ entry }) {
  closeSidebar();
  navigateWithViewTransition(router, {
    path: "/notifications",
    query: { panel: "class-reviews", category: "pending", entry: String(entry.id) },
  });
}

function handleClassReviewsCategory(category) {
  navigateWithViewTransition(router, {
    path: "/notifications",
    query: { panel: "class-reviews", category },
  });
}

function goToSettings() {
  closeSidebar();
  navigateWithViewTransition(router, "/settings");
}
</script>
