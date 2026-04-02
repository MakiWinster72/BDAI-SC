<template>
  <section class="menu-card">
    <div class="menu-card-header">
      <button
        v-if="isAchievementPanelVisible"
        class="menu-card-back"
        type="button"
        @click="closeAchievementPanel"
      >
        &lt;返回
      </button>
      <div class="menu-card-title">
        {{ isAchievementPanelVisible ? "个人成就" : "导航" }}
      </div>
    </div>

    <Transition name="menu-panel-fade" mode="out-in">
      <div
        v-if="isAchievementPanelVisible"
        key="achievement-panel"
        class="menu-panel menu-sublist"
      >
        <button
          v-for="entry in achievementEntries"
          :key="entry.key"
          class="menu-subitem"
          :class="{ active: activeMenu === 'achievements' && activeAchievement === entry.key }"
          type="button"
          @click="$emit('achievement-entry-click', entry.key)"
        >
          {{ entry.label }}
        </button>
      </div>

      <div v-else key="menu-panel" class="menu-panel menu-list menu-grid">
        <button
          v-for="item in menuItems"
          :key="item.key"
          class="menu-item"
          :class="{ active: activeMenu === item.key, disabled: !isMenuEnabled(item.key) }"
          type="button"
          :disabled="!isMenuEnabled(item.key)"
          @click="handleMenuClick(item.key)"
        >
          <span class="menu-item-label">{{ item.label }}</span>
          <span class="menu-item-meta">{{ menuMeta[item.key] }}</span>
        </button>
      </div>
    </Transition>
  </section>
</template>

<script setup>
import { computed, ref, watch } from "vue";
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
});

const emit = defineEmits(["menu-click", "achievement-entry-click"]);

const menuItems = computed(() =>
  filterMenuItemsByRole(props.profile.role).filter((item) => item.key !== "admin"),
);

const menuMeta = {
  achievements: "查看与维护成果",
  "my-info": "编辑个人档案",
  "student-info": "检索学生资料",
};

const isAchievementPanelVisible = ref(props.activeMenu === "achievements");

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

watch(
  () => props.activeMenu,
  (activeMenu) => {
    isAchievementPanelVisible.value = activeMenu === "achievements";
  },
);

function handleMenuClick(key) {
  if (key === "achievements" && props.showAchievementsDrawer) {
    isAchievementPanelVisible.value = true;
  }
  emit("menu-click", key);
}

function closeAchievementPanel() {
  isAchievementPanelVisible.value = false;
}
</script>
