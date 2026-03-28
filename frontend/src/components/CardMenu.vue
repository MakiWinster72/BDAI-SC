<template>
  <section class="menu-card">
    <template v-for="item in menuItems" :key="item.key">
      <!-- achievements drawer -->
      <div
        v-if="item.key === 'achievements'"
        class="menu-drawer"
        :class="{ open: achievementsOpen }"
      >
        <button
          class="menu-item menu-drawer-trigger"
          :class="{
            active: activeMenu === item.key,
            disabled: !isMenuEnabled(item.key),
          }"
          type="button"
          :disabled="!isMenuEnabled(item.key)"
          @click="handleAchievementsClick"
        >
          <span>{{ item.label }}</span>
          <span v-if="showAchievementsDrawer" class="menu-drawer-caret" aria-hidden="true"></span>
        </button>
        <transition name="menu-drawer-panel">
          <div
            v-show="showAchievementsDrawer && drawerVisible"
            class="menu-drawer-panel"
            :class="drawerAnimClass"
          >
            <div
              class="menu-drawer-indicator"
              :style="drawerIndicatorStyle"
              aria-hidden="true"
            ></div>
            <button
              v-for="entry in achievementEntries"
              :key="entry.key"
              class="menu-drawer-item"
              :class="{ active: activeAchievement === entry.key }"
              type="button"
              @click="$emit('achievement-entry-click', entry.key)"
            >
              {{ entry.label }}
            </button>
          </div>
        </transition>
      </div>
      <!-- simple menu item -->
      <button
        v-else
        class="menu-item"
        :class="{
          active: activeMenu === item.key,
          disabled: !isMenuEnabled(item.key),
        }"
        type="button"
        :disabled="!isMenuEnabled(item.key)"
        @click="$emit('menu-click', item.key)"
      >
        {{ item.label }}
      </button>
    </template>
  </section>
</template>

<script setup>
import { computed, onActivated, onDeactivated, onMounted, ref, watch } from "vue";
import { filterMenuItemsByRole, isMenuEnabled } from "../constants/menu";

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
    default: true,
  },
  achievementsOpen: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits([
  "menu-click",
  "achievement-entry-click",
  "toggle-achievements",
]);

const menuItems = computed(() => filterMenuItemsByRole(props.profile.role));

const achievementEntries = [
  { key: "all", label: "全部" },
  { key: "contest", label: "学科竞赛、文体艺术" },
  { key: "paper", label: "发表学术论文" },
  { key: "journal", label: "发表期刊作品" },
  { key: "patent", label: "专利(著作权)授权数(项)" },
  { key: "certificate", label: "职业资格证书" },
  { key: "research", label: "学生参与教师科研项目情况" },
  { key: "works", label: "创作、表演的代表性作品" },
  { key: "doubleHundred", label: "双百工程" },
  { key: "ieerTraining", label: "大学生创新创业训练计划项目" },
];

const activeAchievementIndex = computed(() => {
  const index = achievementEntries.findIndex(
    (entry) => entry.key === props.activeAchievement,
  );
  return index === -1 ? 0 : index;
});

const drawerIndicatorStyle = computed(() => ({
  transform: `translateY(calc(${activeAchievementIndex.value} * (var(--drawer-item-height) + var(--drawer-item-gap))))`,
}));

// 抽屉开闭动画状态
const drawerVisible = ref(false);
const drawerAnimClass = ref("");
let closeTimer = null;

function openDrawer() {
  drawerVisible.value = false;
  drawerAnimClass.value = "";
  queueMicrotask(() => {
    drawerVisible.value = true;
    drawerAnimClass.value = "is-open";
    setTimeout(() => {
      drawerAnimClass.value = "";
    }, 350);
  });
}

function closeDrawer() {
  drawerAnimClass.value = "is-closing";
  clearTimeout(closeTimer);
  closeTimer = setTimeout(() => {
    drawerAnimClass.value = "";
    drawerVisible.value = false;
  }, 350);
}

watch(
  () => props.achievementsOpen,
  (open) => {
    if (!props.showAchievementsDrawer) return;
    if (open) {
      openDrawer();
    } else {
      closeDrawer();
    }
  },
);

onMounted(() => {
  if (props.achievementsOpen && props.showAchievementsDrawer) {
    openDrawer();
  }
});

onActivated(() => {
  if (props.achievementsOpen && props.showAchievementsDrawer) {
    openDrawer();
  }
});

onDeactivated(() => {
  clearTimeout(closeTimer);
  drawerVisible.value = false;
  drawerAnimClass.value = "";
});

function handleAchievementsClick() {
  if (props.showAchievementsDrawer) {
    emit("toggle-achievements");
  } else {
    emit("menu-click", "achievements");
  }
}
</script>
