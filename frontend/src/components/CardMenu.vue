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
          <div
            v-if="selectedNotification"
            class="menu-panel menu-notification-detail"
          >
            <div class="menu-notification-detail-head">
              <span
                class="menu-notification-badge"
                :class="selectedNotification.badgeClass"
              >
                {{ selectedNotification.badgeText }}
              </span>
              <span class="menu-notification-time">
                {{ selectedNotification.timeText }}
              </span>
            </div>
            <div class="menu-notification-detail-title">
              {{ selectedNotification.title }}
            </div>
            <div class="menu-notification-detail-content">
              {{ selectedNotification.content }}
            </div>
            <div
              v-if="selectedNotification.summary"
              class="menu-notification-detail-summary"
            >
              {{ selectedNotification.summary }}
            </div>
            <div class="menu-notification-detail-meta">
              {{ selectedNotification.meta }}
            </div>
            <div
              v-if="selectedNotification.requester"
              class="menu-notification-detail-block"
            >
              <div class="menu-notification-detail-label">提交人</div>
              <div class="menu-notification-detail-value">
                {{ selectedNotification.requester.displayName || selectedNotification.requester.username || "-" }}
              </div>
            </div>
            <div
              v-if="selectedNotification.reviewer"
              class="menu-notification-detail-block"
            >
              <div class="menu-notification-detail-label">处理人</div>
              <div class="menu-notification-detail-value">
                {{ selectedNotification.reviewer.displayName || selectedNotification.reviewer.username || "-" }}
              </div>
            </div>
            <div
              v-if="selectedNotification.reason"
              class="menu-notification-reason"
            >
              驳回理由：{{ selectedNotification.reason }}
            </div>
            <div v-if="canProcessSelected" class="menu-notification-actions">
              <button
                class="menu-notification-action is-approve"
                type="button"
                @click="approveSelectedRequest"
              >
                通过
              </button>
              <button
                class="menu-notification-action is-reject"
                type="button"
                @click="toggleRejectEditor"
              >
                {{ rejectEditorOpen ? "取消驳回" : "驳回" }}
              </button>
            </div>
            <div
              v-if="canProcessSelected && rejectEditorOpen"
              class="menu-notification-reject-panel"
            >
              <label class="menu-notification-detail-label" for="reject-reason">
                驳回理由
              </label>
              <textarea
                id="reject-reason"
                v-model="rejectReason"
                class="menu-notification-textarea"
                rows="4"
                placeholder="请输入驳回原因，学生会在通知里看到这条理由。"
              />
              <div v-if="actionError" class="menu-notification-action-error">
                {{ actionError }}
              </div>
              <button
                class="menu-notification-action is-reject is-submit"
                type="button"
                @click="rejectSelectedRequest"
              >
                确认驳回
              </button>
            </div>
          </div>
          <div v-else-if="!inboxEntries.length" class="menu-notification-empty">
            暂无通知
          </div>
          <article
            v-for="entry in inboxEntries"
            :key="entry.source + entry.id"
            class="menu-notification-item"
            :class="{ active: selectedNotificationKey === buildNotificationKey(entry) }"
            @click="openNotificationDetail(entry)"
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
            <div class="menu-notification-content">{{ entry.content }}</div>
            <div class="menu-notification-meta">{{ entry.meta }}</div>
            <div v-if="entry.reason" class="menu-notification-reason">
              驳回理由：{{ entry.reason }}
            </div>
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
  showAchievementsDrawer: {
    type: Boolean,
    default: true,
  },
});

const emit = defineEmits(["menu-click", "achievement-entry-click"]);

const {
  inboxEntries,
  pendingCount,
  updateReviewRequestStatus,
} = useNotifications(props.profile);

const menuItems = computed(() => filterMenuItemsByRole(props.profile.role));

const menuMeta = {
  notifications: "待处理请求与结果通知",
  achievements: "查看与维护成果",
  "my-info": "编辑个人档案",
  "student-info": "检索学生资料",
  admin: "开关与系统设置",
};

const currentPanel = ref(props.activeMenu === "achievements" ? "achievements" : "menu");
const panelDirection = ref(props.activeMenu === "achievements" ? "forward" : "back");
const menuBodyRef = ref(null);
const showBottomFade = ref(false);
const selectedNotificationKey = ref("");
const rejectEditorOpen = ref(false);
const rejectReason = ref("");
const actionError = ref("");
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
    return selectedNotification.value ? "通知详情" : "通知";
  }
  return "导航";
});
const selectedNotification = computed(() =>
  inboxEntries.value.find(
    (entry) => buildNotificationKey(entry) === selectedNotificationKey.value,
  ) || null,
);
const canProcessSelected = computed(() => {
  if (!selectedNotification.value || selectedNotification.value.source !== "review-request") {
    return false;
  }
  if (selectedNotification.value.status !== "pending") {
    return false;
  }
  if (!["TEACHER", "ADMIN"].includes(props.profile.role)) {
    return false;
  }
  return selectedNotification.value.requester?.username !== props.profile.username;
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
    if (activeMenu === "achievements" && previousMenu !== "achievements") {
      panelDirection.value = "forward";
      currentPanel.value = "achievements";
    } else if (activeMenu !== "achievements" && previousMenu === "achievements") {
      panelDirection.value = "back";
      if (currentPanel.value === "achievements") {
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
    resetNotificationDetail();
    nextTick(updateBodyFadeState);
    return;
  }
  currentPanel.value = "menu";
  emit("menu-click", key);
  nextTick(updateBodyFadeState);
}

function closeSubPanel() {
  if (currentPanel.value === "notifications" && selectedNotification.value) {
    resetNotificationDetail();
    nextTick(updateBodyFadeState);
    return;
  }
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

function openNotificationDetail(entry) {
  selectedNotificationKey.value = buildNotificationKey(entry);
  rejectEditorOpen.value = false;
  rejectReason.value = "";
  actionError.value = "";
  nextTick(updateBodyFadeState);
}

function resetNotificationDetail() {
  selectedNotificationKey.value = "";
  rejectEditorOpen.value = false;
  rejectReason.value = "";
  actionError.value = "";
}

function toggleRejectEditor() {
  rejectEditorOpen.value = !rejectEditorOpen.value;
  actionError.value = "";
  if (!rejectEditorOpen.value) {
    rejectReason.value = "";
  }
  nextTick(updateBodyFadeState);
}

function approveSelectedRequest() {
  if (!selectedNotification.value) {
    return;
  }
  actionError.value = "";
  try {
    updateReviewRequestStatus({
      requestId: selectedNotification.value.id,
      status: "approved",
      reviewer: props.profile,
    });
    rejectEditorOpen.value = false;
    rejectReason.value = "";
  } catch (error) {
    actionError.value = error?.message || "处理失败，请稍后重试";
  }
}

function rejectSelectedRequest() {
  if (!selectedNotification.value) {
    return;
  }
  actionError.value = "";
  try {
    updateReviewRequestStatus({
      requestId: selectedNotification.value.id,
      status: "rejected",
      reviewer: props.profile,
      reason: rejectReason.value,
    });
    rejectEditorOpen.value = false;
    rejectReason.value = "";
  } catch (error) {
    actionError.value = error?.message || "处理失败，请稍后重试";
  }
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
