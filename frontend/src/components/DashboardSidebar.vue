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
        :show-achievements-drawer="showAchievementsDrawer"
        @menu-click="$emit('menu-click', $event)"
        @achievement-entry-click="$emit('achievement-entry-click', $event)"
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
  showAchievementsDrawer: {
    type: Boolean,
    default: false,
  },
});

defineEmits(["menu-click", "achievement-entry-click", "settings-click"]);
</script>
