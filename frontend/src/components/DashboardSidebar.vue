<template>
  <aside class="dashboard-left">
    <ProfileCard
      :profile="profile"
      :compact="false"
      @menu-click="$emit('menu-click', $event)"
      @settings-click="$emit('settings-click')"
    />

    <div class="dashboard-menu-scroll">
      <CardMenu
        :profile="profile"
        :active-menu="activeMenu"
        :active-achievement="activeAchievement"
        :active-notification-key="activeNotificationKey"
        :active-notification-category="activeNotificationCategory"
        :show-achievements-drawer="showAchievementsDrawer"
        @menu-click="$emit('menu-click', $event)"
        @achievement-entry-click="$emit('achievement-entry-click', $event)"
        @notification-entry-click="$emit('notification-entry-click', $event)"
        @notification-category-click="$emit('notification-category-click', $event)"
      />
    </div>
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
  activeNotificationKey: {
    type: String,
    default: "",
  },
  activeNotificationCategory: {
    type: String,
    default: "pending",
  },
  showAchievementsDrawer: {
    type: Boolean,
    default: false,
  },
});

defineEmits([
  "menu-click",
  "achievement-entry-click",
  "notification-entry-click",
  "notification-category-click",
  "settings-click",
]);
</script>
