<template>
  <div class="dashboard-layout">
    <aside class="dashboard-left" :class="{ open: sidebarOpen }">
      <ProfileCard
        :profile="profile"
        @settings-click="goToSettings"
      />
      <CardMenu
        :profile="profile"
        :active-menu="activeMenu"
        :show-achievements-drawer="true"
        :achievements-open="achievementsOpen"
        @menu-click="handleMenuClick"
        @toggle-achievements="achievementsOpen = !achievementsOpen"
      />
    </aside>

    <main class="dashboard-right">
      <header class="feed-header">
        <h1 class="feed-title">后台管理</h1>
      </header>
      <div class="admin-container">
        <p>管理功能开发中...</p>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import ProfileCard from "../components/ProfileCard.vue";
import CardMenu from "../components/CardMenu.vue";

const router = useRouter();
const activeMenu = ref("admin");
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
  if (key === "admin") {
    router.push("/admin");
    return;
  }
}

function goToSettings() {
  router.push("/settings");
}
</script>

<style scoped>
.admin-container {
  padding: 2rem;
}
</style>
