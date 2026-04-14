<template>
  <aside class="dashboard-left">
    <div class="dashboard-menu-scroll">
      <CardMenu
        :profile="profile"
        :active-menu="activeMenu"
        :active-achievement="activeAchievement"
        :show-achievements-drawer="showAchievementsDrawer"
        :notification-active-category="notificationActiveCategory"
        :notification-active-entry="notificationActiveEntry"
        @menu-click="$emit('menu-click', $event)"
        @achievement-entry-click="$emit('achievement-entry-click', $event)"
        @notification-entry-click="$emit('notification-entry-click', $event)"
      />
    </div>
  </aside>
</template>

<script setup>
import CardMenu from "./CardMenu.vue";

const props = defineProps({
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
  notificationActiveCategory: {
    type: String,
    default: "pending",
  },
  notificationActiveEntry: {
    type: String,
    default: "",
  },
});

defineEmits([
  "menu-click",
  "achievement-entry-click",
  "notification-entry-click",
  "settings-click",
]);
</script>
