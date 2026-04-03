<template>
  <section class="menu-card">
    <div class="menu-card-header">
      <Transition name="menu-back-fade">
        <button
          v-if="isAchievementPanelVisible"
          class="menu-card-back"
          type="button"
          @click="closeAchievementPanel"
        >
          &lt;返回
        </button>
      </Transition>
      <Transition :name="titleTransitionName" mode="out-in">
        <div :key="isAchievementPanelVisible ? 'achievement-title' : 'menu-title'" class="menu-card-title">
          {{ isAchievementPanelVisible ? "个人成就" : "导航" }}
        </div>
      </Transition>
    </div>

    <div
      ref="menuBodyRef"
      class="menu-card-body"
      :class="{
        'menu-card-body-fade-top': true,
        'menu-card-body-fade-bottom': showBottomFade,
      }"
      @scroll="handleBodyScroll"
    >
      <Transition :name="panelTransitionName" mode="out-in">
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
    </div>
  </section>
</template>

<script setup>
import { computed, nextTick, onMounted, onUpdated, ref, watch } from "vue";
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
const panelDirection = ref(props.activeMenu === "achievements" ? "forward" : "back");
const menuBodyRef = ref(null);
const showBottomFade = ref(false);
const panelTransitionName = computed(() =>
  panelDirection.value === "forward" ? "menu-panel-forward" : "menu-panel-back",
);
const titleTransitionName = computed(() =>
  panelDirection.value === "forward" ? "menu-title-forward" : "menu-title-back",
);

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
  (activeMenu, previousMenu) => {
    if (activeMenu === "achievements" && previousMenu !== "achievements") {
      panelDirection.value = "forward";
    } else if (activeMenu !== "achievements" && previousMenu === "achievements") {
      panelDirection.value = "back";
    }
    isAchievementPanelVisible.value = activeMenu === "achievements";
    nextTick(updateBodyFadeState);
  },
);

onMounted(() => {
  updateBodyFadeState();
});

onUpdated(() => {
  updateBodyFadeState();
});

function handleMenuClick(key) {
  if (key === "achievements" && props.showAchievementsDrawer) {
    panelDirection.value = "forward";
    isAchievementPanelVisible.value = true;
  }
  emit("menu-click", key);
  nextTick(updateBodyFadeState);
}

function closeAchievementPanel() {
  panelDirection.value = "back";
  isAchievementPanelVisible.value = false;
  nextTick(updateBodyFadeState);
}

function handleBodyScroll() {
  updateBodyFadeState();
}

function updateBodyFadeState() {
  const el = menuBodyRef.value;
  if (!el) {
    showBottomFade.value = false;
    return;
  }

  const maxScrollTop = el.scrollHeight - el.clientHeight;
  showBottomFade.value = maxScrollTop - el.scrollTop > 4;
}
</script>
