<script setup>
import { computed, reactive, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useNotifications } from "../composables/useNotifications";

const route = useRoute();
const router = useRouter();
const profile = reactive(loadUser());
const { inboxEntries, categoryCounts, updateReviewRequestStatus } = useNotifications(profile);

const rejectEditorOpen = ref(false);
const rejectReason = ref("");
const actionError = ref("");

const activeEntryKey = computed(() => {
  const raw = route.query.entry;
  return typeof raw === "string" ? raw : "";
});
const activeCategory = computed(() => {
  const raw = route.query.category;
  return typeof raw === "string" && raw ? raw : "pending";
});
const notificationTabs = computed(() => [
  { key: "pending", label: "待处理", count: categoryCounts.value.pending || 0 },
  { key: "delayed", label: "滞后", count: categoryCounts.value.delayed || 0 },
  { key: "processed", label: "已处理", count: categoryCounts.value.processed || 0 },
]);
const filteredEntries = computed(() =>
  inboxEntries.value.filter(
    (entry) => entry.categoryKey === activeCategory.value,
  ),
);
const selectedEntry = computed(
  () =>
    filteredEntries.value.find(
      (entry) => buildEntryKey(entry) === activeEntryKey.value,
    ) || null,
);
const canProcessSelected = computed(() => {
  if (!selectedEntry.value || selectedEntry.value.source !== "review-request") {
    return false;
  }
  if (selectedEntry.value.status !== "pending") {
    return false;
  }
  if (!["TEACHER", "ADMIN"].includes(profile.role)) {
    return false;
  }
  return selectedEntry.value.requester?.username !== profile.username;
});

watch(
  () => [filteredEntries.value, activeCategory.value],
  (entries) => {
    const list = Array.isArray(entries) ? entries[0] : [];
    if (!list.length) {
      return;
    }
    if (!activeEntryKey.value) {
      router.replace({
        path: "/notifications",
        query: {
          category: activeCategory.value,
          entry: buildEntryKey(list[0]),
        },
      });
      return;
    }
    const exists = list.some((entry) => buildEntryKey(entry) === activeEntryKey.value);
    if (!exists) {
      router.replace({
        path: "/notifications",
        query: {
          category: activeCategory.value,
          entry: buildEntryKey(list[0]),
        },
      });
    }
  },
  { immediate: true },
);

watch(
  () => activeEntryKey.value,
  () => {
    rejectEditorOpen.value = false;
    rejectReason.value = "";
    actionError.value = "";
  },
);

function buildEntryKey(entry) {
  return `${entry.source}:${entry.sourceId || entry.id}`;
}

function openCategory(category) {
  router.replace({
    path: "/notifications",
    query: { category },
  });
}

function loadUser() {
  try {
    const raw = JSON.parse(localStorage.getItem("gcsc_user") || "{}");
    return {
      username: raw.username || "",
      displayName: raw.displayName || "",
      avatarUrl: raw.avatarUrl || "",
      role: raw.role || "STUDENT",
      studentNo: raw.studentNo || "",
    };
  } catch {
    return {
      username: "",
      displayName: "",
      avatarUrl: "",
      role: "STUDENT",
      studentNo: "",
    };
  }
}

function formatChangeValue(value) {
  if (value === null || value === undefined || value === "") {
    return "-";
  }
  if (Array.isArray(value)) {
    return value.length ? value.join("、") : "-";
  }
  return String(value);
}

function toggleRejectEditor() {
  rejectEditorOpen.value = !rejectEditorOpen.value;
  actionError.value = "";
  if (!rejectEditorOpen.value) {
    rejectReason.value = "";
  }
}

function approveSelectedRequest() {
  if (!selectedEntry.value) {
    return;
  }
  actionError.value = "";
  try {
    updateReviewRequestStatus({
      requestId: selectedEntry.value.id,
      status: "approved",
      reviewer: profile,
    });
  } catch (error) {
    actionError.value = error?.message || "处理失败，请稍后重试";
  }
}

function rejectSelectedRequest() {
  if (!selectedEntry.value) {
    return;
  }
  actionError.value = "";
  try {
    updateReviewRequestStatus({
      requestId: selectedEntry.value.id,
      status: "rejected",
      reviewer: profile,
      reason: rejectReason.value,
    });
    rejectEditorOpen.value = false;
    rejectReason.value = "";
  } catch (error) {
    actionError.value = error?.message || "处理失败，请稍后重试";
  }
}
</script>

<template>
  <main class="dashboard-right notification-view">
    <header class="feed-header">
      <h1 class="feed-title">通知详情</h1>
    </header>

    <section class="notification-categories">
      <button
        v-for="tab in notificationTabs"
        :key="tab.key"
        class="notification-category-tab"
        :class="{ active: activeCategory === tab.key }"
        type="button"
        @click="openCategory(tab.key)"
      >
        <span>{{ tab.label }}</span>
        <span class="notification-category-count">{{ tab.count }}</span>
      </button>
    </section>

    <section v-if="!filteredEntries.length" class="notification-empty-card">
      <div class="notification-empty-title">暂无通知</div>
      <div class="notification-empty-text">当前分类下暂时没有可查看的通知。</div>
    </section>

    <section v-else-if="selectedEntry" class="notification-detail-card">
      <div class="notification-detail-top">
        <span class="notification-badge" :class="selectedEntry.badgeClass">
          {{ selectedEntry.badgeText }}
        </span>
        <span class="notification-time">{{ selectedEntry.timeText }}</span>
      </div>

      <h2 class="notification-detail-title">{{ selectedEntry.title }}</h2>
      <p class="notification-detail-content">{{ selectedEntry.content }}</p>

      <section
        v-if="selectedEntry.summary"
        class="notification-section"
      >
        <div class="notification-section-label">概要</div>
        <div class="notification-section-value">{{ selectedEntry.summary }}</div>
      </section>

      <section
        v-if="selectedEntry.requester"
        class="notification-grid"
      >
        <div class="notification-section">
          <div class="notification-section-label">提交人</div>
          <div class="notification-section-value">
            {{ selectedEntry.requester.displayName || selectedEntry.requester.username || "-" }}
          </div>
        </div>
        <div class="notification-section">
          <div class="notification-section-label">类型</div>
          <div class="notification-section-value">{{ selectedEntry.meta }}</div>
        </div>
      </section>

      <section
        v-if="selectedEntry.changes?.length"
        class="notification-section"
      >
        <div class="notification-section-label">变更内容</div>
        <div class="notification-change-list">
          <article
            v-for="change in selectedEntry.changes"
            :key="`${change.section || ''}-${change.label}-${change.after}`"
            class="notification-change-item"
          >
            <div class="notification-change-head">
              <span class="notification-change-label">{{ change.label }}</span>
              <span v-if="change.section" class="notification-change-section">
                {{ change.section }}
              </span>
            </div>
            <div class="notification-change-values">
              <div class="notification-change-column">
                <div class="notification-change-caption">修改前</div>
                <div class="notification-change-value">
                  {{ formatChangeValue(change.before) }}
                </div>
              </div>
              <div class="notification-change-column">
                <div class="notification-change-caption">修改后</div>
                <div class="notification-change-value is-next">
                  {{ formatChangeValue(change.after) }}
                </div>
              </div>
            </div>
          </article>
        </div>
      </section>

      <section
        v-if="selectedEntry.reason"
        class="notification-section is-reason"
      >
        <div class="notification-section-label">驳回理由</div>
        <div class="notification-section-value">{{ selectedEntry.reason }}</div>
      </section>

      <section
        v-if="selectedEntry.reviewer"
        class="notification-section"
      >
        <div class="notification-section-label">处理人</div>
        <div class="notification-section-value">
          {{ selectedEntry.reviewer.displayName || selectedEntry.reviewer.username || "-" }}
        </div>
      </section>

      <section v-if="canProcessSelected" class="notification-actions-card">
        <div class="notification-actions">
          <button
            class="notification-action is-approve"
            type="button"
            @click="approveSelectedRequest"
          >
            通过审核
          </button>
          <button
            class="notification-action is-reject"
            type="button"
            @click="toggleRejectEditor"
          >
            {{ rejectEditorOpen ? "取消驳回" : "驳回申请" }}
          </button>
        </div>
        <div v-if="rejectEditorOpen" class="notification-reject-box">
          <label class="notification-section-label" for="notification-reject-reason">
            驳回理由
          </label>
          <textarea
            id="notification-reject-reason"
            v-model="rejectReason"
            class="notification-textarea"
            rows="5"
            placeholder="请输入驳回原因，学生会在通知详情里看到这条理由。"
          />
          <div v-if="actionError" class="notification-error">{{ actionError }}</div>
          <button
            class="notification-action is-reject is-submit"
            type="button"
            @click="rejectSelectedRequest"
          >
            确认驳回
          </button>
        </div>
      </section>
    </section>
  </main>
</template>

<style scoped>
.notification-view {
  display: grid;
  gap: 14px;
}

.notification-categories {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.notification-category-tab {
  min-height: 40px;
  padding: 0 14px;
  border: 1px solid rgba(3, 107, 114, 0.16);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.68);
  color: #0f555d;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}

.notification-category-tab.active {
  background: linear-gradient(135deg, #0a7a82, #075961);
  color: #fff;
  box-shadow: 0 14px 28px rgba(3, 107, 114, 0.16);
}

.notification-category-count {
  min-width: 22px;
  height: 22px;
  padding: 0 7px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: rgba(3, 107, 114, 0.1);
}

.notification-category-tab.active .notification-category-count {
  background: rgba(255, 255, 255, 0.18);
}

.notification-empty-card,
.notification-detail-card {
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.38);
  background: linear-gradient(
    145deg,
    rgba(255, 255, 255, 0.8),
    rgba(205, 255, 249, 0.48)
  );
  box-shadow: 0 24px 60px rgba(3, 107, 114, 0.14);
}

.notification-empty-card {
  padding: 28px;
  text-align: center;
}

.notification-empty-title {
  color: #0d4047;
  font-size: 18px;
  font-weight: 700;
}

.notification-empty-text {
  margin-top: 8px;
  color: #5a747c;
  font-size: 14px;
}

.notification-detail-card {
  padding: 22px;
  display: grid;
  gap: 16px;
}

.notification-detail-top,
.notification-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.notification-badge {
  min-height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
}

.notification-badge.is-pending {
  background: rgba(250, 172, 44, 0.18);
  color: #925000;
}

.notification-badge.is-approved {
  background: rgba(40, 150, 88, 0.16);
  color: #236248;
}

.notification-badge.is-rejected {
  background: rgba(216, 69, 54, 0.14);
  color: #a43a2e;
}

.notification-badge.is-system {
  background: rgba(3, 107, 114, 0.12);
  color: #0f555d;
}

.notification-time {
  color: #698188;
  font-size: 12px;
}

.notification-detail-title {
  margin: 0;
  color: #0d4047;
  font-size: 24px;
  line-height: 1.35;
}

.notification-detail-content {
  margin: 0;
  color: #496871;
  font-size: 15px;
  line-height: 1.8;
}

.notification-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.notification-section,
.notification-actions-card {
  border-radius: 18px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.44);
}

.notification-section.is-reason {
  background: rgba(216, 69, 54, 0.08);
}

.notification-section-label {
  display: block;
  margin-bottom: 8px;
  color: #154f57;
  font-size: 12px;
  font-weight: 700;
}

.notification-section-value {
  color: #4e6d75;
  font-size: 14px;
  line-height: 1.7;
}

.notification-change-list {
  display: grid;
  gap: 12px;
}

.notification-change-item {
  border-radius: 16px;
  padding: 14px;
  background: rgba(255, 255, 255, 0.52);
}

.notification-change-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 12px;
}

.notification-change-label {
  color: #103f46;
  font-size: 14px;
  font-weight: 700;
}

.notification-change-section {
  color: #6b838a;
  font-size: 12px;
}

.notification-change-values {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.notification-change-column {
  border-radius: 12px;
  padding: 12px;
  background: rgba(205, 255, 249, 0.26);
}

.notification-change-caption {
  margin-bottom: 6px;
  color: #6b838a;
  font-size: 12px;
}

.notification-change-value {
  color: #54727a;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-word;
}

.notification-change-value.is-next {
  color: #0f555d;
  font-weight: 600;
}

.notification-action {
  min-width: 140px;
  min-height: 42px;
  border: 0;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}

.notification-action.is-approve {
  background: linear-gradient(135deg, #0a7a82, #075961);
  color: #fff;
}

.notification-action.is-reject {
  background: rgba(216, 69, 54, 0.12);
  color: #a43a2e;
}

.notification-action.is-submit {
  margin-top: 12px;
}

.notification-reject-box {
  margin-top: 14px;
}

.notification-textarea {
  width: 100%;
  min-height: 120px;
  border: 1px solid rgba(3, 107, 114, 0.16);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.82);
  padding: 12px 14px;
  resize: vertical;
  color: #164d55;
}

.notification-error {
  margin-top: 10px;
  color: #a43a2e;
  font-size: 12px;
}

@media (max-width: 900px) {
  .notification-grid,
  .notification-change-values {
    grid-template-columns: 1fr;
  }
}
</style>
