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
        :class-reviews-active-category="classReviewsActiveCategory"
        :class-reviews-active-entry="classReviewsActiveEntry"
        :class-review-entries="classReviewEntries"
        @menu-click="$emit('menu-click', $event)"
        @achievement-entry-click="$emit('achievement-entry-click', $event)"
        @notification-entry-click="$emit('notification-entry-click', $event)"
        @class-reviews-entry-click="$emit('class-reviews-entry-click', $event)"
        @class-reviews-category-change="$emit('class-reviews-category-change', $event)"
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
  classReviewsActiveCategory: {
    type: String,
    default: "all",
  },
  classReviewsActiveEntry: {
    type: String,
    default: "",
  },
  classReviewEntries: {
    type: Array,
    default: () => [],
  },
});

defineEmits([
  "menu-click",
  "achievement-entry-click",
  "notification-entry-click",
  "class-reviews-entry-click",
  "class-reviews-category-change",
  "settings-click",
]);
</script>
