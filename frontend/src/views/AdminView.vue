<template>
  <main class="dashboard-right">
      <header class="feed-header">
        <h1 class="feed-title">后台管理</h1>
      </header>
      <div class="admin-container">
        <p>管理功能开发中...</p>
      </div>
  </main>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { getMenuLocation } from "../constants/menu";
import { navigateWithViewTransition } from "../utils/viewTransition";

const router = useRouter();
const activeMenu = ref("admin");
const activeAchievement = ref("all");
const sidebarOpen = ref(false);
const achievementsOpen = ref(false);

const profile = reactive(loadUser());

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

function handleMenuClick(key) {
  sidebarOpen.value = false;
  if (key === "achievements") {
    toggleAchievements();
    return;
  }
  navigateWithViewTransition(router, getMenuLocation(key));
}

function handleAchievementEntry(key) {
  activeMenu.value = "achievements";
  activeAchievement.value = key || "all";
  achievementsOpen.value = true;
  navigateWithViewTransition(router, {
    path: "/achievements",
    query: { category: activeAchievement.value },
  });
}

function toggleAchievements() {
  achievementsOpen.value = !achievementsOpen.value;
  if (achievementsOpen.value) {
    handleAchievementEntry("all");
  }
}

function goToSettings() {
  navigateWithViewTransition(router, "/settings");
}
</script>

<style scoped>
.admin-container {
  padding: 2rem;
}
</style>
