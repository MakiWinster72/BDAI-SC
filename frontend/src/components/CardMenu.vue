<template>
  <section class="menu-card">
    <!-- Header: title row + sticky notification tabs -->
    <div class="menu-card-header" :class="{ 'has-notif-tabs': currentPanel === 'notifications' || currentPanel === 'class-reviews' }">
      <div class="menu-card-header-top">
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

      <!-- Notification Tabs (sticky at top, only when in notifications panel) -->
      <Transition name="tabs-slide">
        <nav v-if="currentPanel === 'notifications'" class="admin-tabs" role="tablist">
          <button
            v-for="cat in notificationCategories"
            :key="cat.key"
            class="admin-tab"
            :class="{ active: notificationActiveCategory === cat.key }"
            role="tab"
            type="button"
            @click="selectNotificationCategory(cat.key)"
          >
            {{ cat.label }}
            <span v-if="inboxEntries.filter(e => e.categoryKey === cat.key).length" class="tab-count">
              {{ inboxEntries.filter(e => e.categoryKey === cat.key).length }}
            </span>
          </button>
        </nav>
      </Transition>

      <!-- Class Reviews Tabs -->
      <Transition name="tabs-slide">
        <nav v-if="currentPanel === 'class-reviews'" class="admin-tabs" role="tablist">
          <button
            v-for="cat in classReviewCategories"
            :key="cat.key"
            class="admin-tab"
            :class="{ active: classReviewActiveCategory === cat.key }"
            role="tab"
            type="button"
            @click="classReviewActiveCategory = cat.key"
          >
            {{ cat.label }}
            <span v-if="classReviewCounts[cat.key]" class="tab-count">{{ classReviewCounts[cat.key] }}</span>
          </button>
        </nav>
      </Transition>
    </div>

    <!-- Scrollable body -->
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
        <!-- Achievements Sub-Panel -->
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

        <!-- Notifications Sub-Panel -->
        <div
          v-else-if="currentPanel === 'notifications'"
          key="notifications-panel"
          class="menu-panel menu-notification-list"
        >
          <button
            v-for="entry in filteredNotificationEntries"
            :key="entry.id"
            class="menu-notification-item"
            :class="{ active: String(notificationActiveEntry) === String(entry.id) }"
            type="button"
            @click="selectNotificationEntry(entry.id)"
          >
            <div class="menu-notification-head">
              <span class="menu-notification-badge" :class="entry.badgeClass">{{ entry.badgeText }}</span>
              <time class="menu-notification-time">{{ entry.timeText }}</time>
              <span
                v-if="(entry.categoryKey === 'approved' || entry.categoryKey === 'rejected') && !processedReadIds.has(String(entry.id))"
                class="menu-notification-dot"
                aria-label="未读"
              />
            </div>
            <p class="menu-notification-title">{{ entry.title }}</p>
            <p class="menu-notification-content">{{ entry.content }}</p>
          </button>
          <div v-if="!filteredNotificationEntries.length" class="menu-notification-empty">
            暂无通知
          </div>
        </div>

        <!-- Class Reviews Sub-Panel -->
        <div
          v-else-if="currentPanel === 'class-reviews'"
          key="class-reviews-panel"
          class="menu-panel menu-notification-list"
        >
          <button
            v-for="item in filteredClassReviewEntries"
            :key="item.id + '-' + item.resourceType"
            class="menu-notification-item"
            :class="{ active: String(classReviewsActiveEntry) === String(item.id) + '-' + item.resourceType }"
            type="button"
            @click="selectClassReviewEntry(item)"
          >
            <div class="menu-notification-head">
              <span class="menu-notification-badge" :class="item.badgeClass">{{ item.badgeText }}</span>
              <span class="menu-notification-badge is-type" :class="item.resourceType === 'achievement' ? 'is-achievement' : 'is-profile'">
                {{ item.resourceType === 'achievement' ? '成就' : '信息' }}
              </span>
              <time class="menu-notification-time">{{ item.timeText }}</time>
            </div>
            <p class="menu-notification-title">{{ item.title }}</p>
            <p class="menu-notification-content">{{ item.content }}</p>
          </button>
          <div v-if="!filteredClassReviewEntries.length" class="menu-notification-empty">
            暂无{{ classReviewCategories.find(c => c.key === classReviewActiveCategory)?.label || '' }}申请
          </div>
        </div>

        <!-- Main Menu -->
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
            </span>
            <span class="menu-item-meta">{{ menuMeta[item.key] }}</span>
          </button>
        </div>
      </Transition>
    </div>
  </section>
</template>

<script setup>
import { computed, nextTick, onMounted, onUpdated, ref, toRefs, watch } from "vue";
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
  showAchievementsDrawer: {
    type: Boolean,
    default: true,
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

// Destructure with toRefs to maintain reactivity
const { notificationActiveCategory, notificationActiveEntry } = toRefs(props);

const emit = defineEmits([
  "menu-click",
  "achievement-entry-click",
  "notification-entry-click",
  "class-reviews-entry-click",
  "class-reviews-category-change",
]);

const { inboxEntries, pendingCount, processedUnreadCount, processedReadIds, markProcessedEntryRead, classReviewEntries } = useNotifications(props.profile);

const menuItems = computed(() => filterMenuItemsByRole(props.profile.role));

const menuMeta = computed(() => {
  const totalPending = classReviewEntries.value.filter(e => e.categoryKey === 'pending' || e.categoryKey === 'delayed').length;
  return {
    notifications: processedUnreadCount.value > 0
      ? `待处理 ${pendingCount.value} | 已处理 ${processedUnreadCount.value}`
      : `待处理 ${pendingCount.value}`,
    achievements: "查看与维护成果",
    "my-info": "编辑个人档案",
    "student-info": "检索学生资料",
    admin: "开关与系统设置",
    "class-reviews": totalPending > 0
      ? `${totalPending} 条待审`
      : "无待审申请",
  };
});

const classReviewActiveCategory = ref("pending");

const filteredClassReviewEntries = computed(() => {
  const cat = classReviewActiveCategory.value;
  return classReviewEntries.value.filter(entry => entry.categoryKey === cat);
});

const classReviewCategories = [
  { key: "pending", label: "待处理" },
  { key: "delayed", label: "已滞后" },
  { key: "approved", label: "已通过" },
  { key: "rejected", label: "已驳回" },
];

const classReviewCounts = computed(() => {
  const counts = { pending: 0, delayed: 0, approved: 0, rejected: 0 };
  classReviewEntries.value.forEach(entry => {
    const key = entry.categoryKey || "pending";
    if (counts[key] !== undefined) counts[key]++;
  });
  return counts;
});

const notificationCategories = [
  { key: "pending", label: "待处理" },
  { key: "delayed", label: "已滞后" },
  { key: "approved", label: "已通过" },
  { key: "rejected", label: "已驳回" },
];

const filteredNotificationEntries = computed(() =>
  inboxEntries.value.filter((e) => e.categoryKey === notificationActiveCategory.value),
);

const currentPanel = ref("menu");
const panelDirection = ref("back");
const menuBodyRef = ref(null);
const showBottomFade = ref(false);

const isSubPanelVisible = computed(() =>
  currentPanel.value === "achievements" || currentPanel.value === "notifications" || currentPanel.value === "class-reviews",
);
const panelTransitionName = computed(() =>
  panelDirection.value === "forward" ? "menu-panel-forward" : "menu-panel-back",
);
const titleTransitionName = computed(() =>
  panelDirection.value === "forward" ? "menu-title-forward" : "menu-title-back",
);
const panelTitle = computed(() => {
  if (currentPanel.value === "achievements") return "个人成就";
  if (currentPanel.value === "notifications") return "通知详情";
  if (currentPanel.value === "class-reviews") return "班级审核";
  return "导航";
});

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
    if (activeMenu === "achievements" && previousMenu !== activeMenu) {
      panelDirection.value = "forward";
      currentPanel.value = "achievements";
    } else if (activeMenu === "notifications" && previousMenu !== activeMenu) {
      panelDirection.value = "forward";
      currentPanel.value = "notifications";
    } else if (activeMenu === "class-reviews" && previousMenu !== activeMenu) {
      panelDirection.value = "forward";
      currentPanel.value = "class-reviews";
    } else if (previousMenu === "achievements" || previousMenu === "notifications" || previousMenu === "class-reviews") {
      panelDirection.value = "back";
      currentPanel.value = "menu";
    }
    nextTick(updateBodyFadeState);
  },
);

onMounted(() => {
  updateBodyFadeState();
  // Initialize panel from props on mount
  if (props.activeMenu === "achievements") {
    currentPanel.value = "achievements";
    panelDirection.value = "forward";
  } else if (props.activeMenu === "notifications") {
    currentPanel.value = "notifications";
    panelDirection.value = "forward";
  } else if (props.activeMenu === "class-reviews") {
    currentPanel.value = "class-reviews";
    panelDirection.value = "forward";
  }
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
    nextTick(updateBodyFadeState);
    emit("menu-click", key);
    return;
  }
  if (key === "class-reviews") {
    panelDirection.value = "forward";
    currentPanel.value = "class-reviews";
    nextTick(updateBodyFadeState);
    emit("menu-click", key);
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

function selectNotificationCategory(category) {
  emit("notification-entry-click", { category, entryId: "" });
}

function selectNotificationEntry(entryId) {
  const entry = filteredNotificationEntries.value.find((e) => String(e.id) === String(entryId));
  if (entry && (entry.categoryKey === "approved" || entry.categoryKey === "rejected")) {
    markProcessedEntryRead(String(entryId));
  }
  emit("notification-entry-click", { category: notificationActiveCategory.value, entryId: String(entryId) });
}

function selectClassReviewEntry(item) {
  emit("class-reviews-entry-click", { entry: item });
}

function formatClassReviewTime(dateStr) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
}

function handleBodyScroll() {
  updateBodyFadeState();
}

function isItemActive(key) {
  if (key === "achievements") {
    return currentPanel.value === "achievements" || props.activeMenu === key;
  }
  if (key === "notifications") {
    return currentPanel.value === "notifications" || props.activeMenu === key;
  }
  return currentPanel.value === "menu" && props.activeMenu === key;
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
