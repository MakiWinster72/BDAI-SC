<template>
  <section class="menu-card">
    <div class="menu-card-header">
      <Transition name="menu-back-fade">
        <button
          v-if="isSubPanelVisible"
          class="menu-card-back"
          type="button"
          @click="closeSubPanel"
        >
          &lt;返回
        </button>
      </Transition>
      <Transition :name="titleTransitionName" mode="out-in">
        <div :key="currentPanel" class="menu-card-title">
          {{ panelTitle }}
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
          v-if="currentPanel === 'achievements'"
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

        <div
          v-else-if="currentPanel === 'notifications'"
          key="notification-panel"
          class="menu-panel menu-notification-list"
        >
          <div class="menu-notification-tabs">
            <button
              v-for="tab in notificationTabs"
              :key="tab.key"
              class="menu-notification-tab"
              :class="{ active: activeNotificationCategory === tab.key }"
              type="button"
              @click="openNotificationCategory(tab.key)"
            >
              <span>{{ tab.label }}</span>
              <span class="menu-notification-tab-count">{{ tab.count }}</span>
            </button>
          </div>
          <div v-if="!filteredInboxEntries.length" class="menu-notification-empty">
            暂无通知
          </div>
          <article
            v-for="entry in filteredInboxEntries"
            :key="entry.source + entry.id"
            class="menu-notification-item"
            :class="{ active: activeNotificationKey === buildNotificationKey(entry) }"
            @click="$emit('notification-entry-click', buildNotificationKey(entry))"
          >
            <div class="menu-notification-head">
              <span
                class="menu-notification-badge"
                :class="entry.badgeClass"
              >
                {{ entry.badgeText }}
              </span>
              <span class="menu-notification-time">{{ entry.timeText }}</span>
            </div>
            <div class="menu-notification-title">{{ entry.title }}</div>
          </article>
        </div>

        <div v-else key="menu-panel" class="menu-panel menu-list menu-grid">
          <button
            v-for="item in menuItems"
            :key="item.key"
            class="menu-item"
            :class="{ active: isItemActive(item.key), disabled: !isMenuEnabled(item.key) }"
            type="button"
            :disabled="!isMenuEnabled(item.key)"
            @click="handleMenuClick(item.key)"
          >
            <span class="menu-item-header">
              <span class="menu-item-label">{{ item.label }}</span>
              <span
                v-if="item.key === 'notifications' && pendingCount > 0"
                class="menu-item-count"
              >
                {{ pendingCount }}
              </span>
            </span>
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
import { useNotifications } from "../composables/useNotifications";

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
    default: true,
  },
});

const emit = defineEmits([
  "menu-click",
  "achievement-entry-click",
  "notification-entry-click",
  "notification-category-click",
]);

const { inboxEntries, pendingCount, categoryCounts } = useNotifications(props.profile);

const menuItems = computed(() => filterMenuItemsByRole(props.profile.role));

const menuMeta = {
  notifications: "待处理请求与结果通知",
  achievements: "查看与维护成果",
  "my-info": "编辑个人档案",
  "student-info": "检索学生资料",
  admin: "开关与系统设置",
};

const currentPanel = ref(
  props.activeMenu === "achievements" || props.activeMenu === "notifications"
    ? props.activeMenu
    : "menu",
);
const panelDirection = ref(
  props.activeMenu === "achievements" || props.activeMenu === "notifications"
    ? "forward"
    : "back",
);
const menuBodyRef = ref(null);
const showBottomFade = ref(false);
const isSubPanelVisible = computed(() => currentPanel.value !== "menu");
const panelTransitionName = computed(() =>
  panelDirection.value === "forward" ? "menu-panel-forward" : "menu-panel-back",
);
const titleTransitionName = computed(() =>
  panelDirection.value === "forward" ? "menu-title-forward" : "menu-title-back",
);
const panelTitle = computed(() => {
  if (currentPanel.value === "achievements") {
    return "个人成就";
  }
  if (currentPanel.value === "notifications") {
    return "通知";
  }
  return "导航";
});
const notificationTabs = computed(() => [
  { key: "pending", label: "待处理", count: categoryCounts.value.pending || 0 },
  { key: "delayed", label: "滞后", count: categoryCounts.value.delayed || 0 },
  { key: "processed", label: "已处理", count: categoryCounts.value.processed || 0 },
]);
const filteredInboxEntries = computed(() =>
  inboxEntries.value.filter(
    (entry) => entry.categoryKey === (props.activeNotificationCategory || "pending"),
  ),
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
    if (
      (activeMenu === "achievements" || activeMenu === "notifications") &&
      previousMenu !== activeMenu
    ) {
      panelDirection.value = "forward";
      currentPanel.value = activeMenu;
    } else if (
      activeMenu !== "achievements" &&
      activeMenu !== "notifications" &&
      (previousMenu === "achievements" || previousMenu === "notifications")
    ) {
      panelDirection.value = "back";
      if (
        currentPanel.value === "achievements" ||
        currentPanel.value === "notifications"
      ) {
        currentPanel.value = "menu";
      }
    }
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
    currentPanel.value = "achievements";
    nextTick(updateBodyFadeState);
    emit("menu-click", key);
    return;
  }
  if (key === "notifications") {
    panelDirection.value = "forward";
    currentPanel.value = "notifications";
    emit("menu-click", key);
    nextTick(updateBodyFadeState);
    return;
  }
  currentPanel.value = "menu";
  emit("menu-click", key);
  nextTick(updateBodyFadeState);
}

function closeSubPanel() {
  panelDirection.value = "back";
  currentPanel.value = "menu";
  nextTick(updateBodyFadeState);
}

function handleBodyScroll() {
  updateBodyFadeState();
}

function isItemActive(key) {
  if (key === "notifications") {
    return currentPanel.value === "notifications";
  }
  if (key === "achievements") {
    return currentPanel.value === "achievements" || props.activeMenu === key;
  }
  return currentPanel.value === "menu" && props.activeMenu === key;
}

function buildNotificationKey(entry) {
  return `${entry.source}:${entry.sourceId || entry.id}`;
}

function openNotificationCategory(category) {
  emit("notification-category-click", category);
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
