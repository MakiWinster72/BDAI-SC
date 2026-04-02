<template>
  <aside class="dashboard-left" :class="{ open: sidebarOpen }">
    <ProfileCard
      :profile="profile"
      :profile-extra-label="profileExtraLabel"
      @settings-click="$emit('settings-click')"
    />

    <CardMenu
      :profile="profile"
      :active-menu="activeMenu"
      :active-achievement="activeAchievement"
      :show-achievements-drawer="showAchievementsDrawer"
      :achievements-open="achievementsOpen"
      @menu-click="$emit('menu-click', $event)"
      @achievement-entry-click="$emit('achievement-entry-click', $event)"
      @toggle-achievements="$emit('toggle-achievements')"
    />
  </aside>
</template>

<script setup>
import CardMenu from "./CardMenu.vue";
import ProfileCard from "./ProfileCard.vue";

defineProps({
  profile: {
    type: Object,
    required: true,
  },
  activeMenu: {
    type: String,
    default: "",
  },
  activeAchievement: {
    type: String,
    default: "all",
  },
  showAchievementsDrawer: {
    type: Boolean,
    default: false,
  },
  achievementsOpen: {
    type: Boolean,
    default: false,
  },
  sidebarOpen: {
    type: Boolean,
    default: false,
  },
  profileExtraLabel: {
    type: String,
    default: "学院",
  },
});

defineEmits([
  "menu-click",
  "achievement-entry-click",
  "settings-click",
  "toggle-achievements",
]);
</script>
