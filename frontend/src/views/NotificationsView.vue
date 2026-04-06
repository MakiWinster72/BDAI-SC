<script setup>
import { computed, reactive, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import AchievementReviewSnapshotCard from "../components/AchievementReviewSnapshotCard.vue";
import { API_BASE } from "../api/request";
import { useNotifications } from "../composables/useNotifications";

const route = useRoute();
const router = useRouter();
const profile = reactive(loadUser());
const { inboxEntries, updateReviewRequestStatus, cancelReviewRequest } = useNotifications(profile);

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

const canCancelSelected = computed(() => {
  if (!selectedEntry.value || selectedEntry.value.source !== "review-request") {
    return false;
  }
  if (selectedEntry.value.status !== "pending") {
    return false;
  }
  return selectedEntry.value.requester?.username === profile.username;
});

const cancelConfirmOpen = ref(false);
const achievementReviewSnapshot = computed(() =>
  resolveAchievementReviewPayload(selectedEntry.value),
);

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
    cancelConfirmOpen.value = false;
  },
);

function buildEntryKey(entry) {
  return `${entry.source}:${entry.sourceId || entry.id}`;
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

function isStructuredChangeValue(value) {
  const text = formatChangeValue(value);
  return text.includes("第1条\n") || /^第\d+条$/m.test(text);
}

function parseStructuredChangeValue(value) {
  const text = formatChangeValue(value);
  if (!isStructuredChangeValue(text)) {
    return [];
  }
  return text
    .split(/\n(?=第\d+条\n?)/)
    .map((block) => block.trim())
    .filter(Boolean)
    .map((block) => {
      const lines = block.split("\n").map((line) => line.trim()).filter(Boolean);
      const [title, ...details] = lines;
      return { title, details };
    });
}

function parseJsonArray(value) {
  if (!value) {
    return [];
  }
  try {
    const parsed = JSON.parse(value);
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

function resolveMediaUrl(url) {
  if (!url) {
    return "";
  }
  if (url.startsWith("http://") || url.startsWith("https://")) {
    return url;
  }
  return `${API_BASE}${url}`;
}

function resolveAchievementReviewPayload(entry) {
  if (!entry || entry.resourceType !== "achievement") {
    return null;
  }
  const payload = entry.payloadSnapshot;
  if (payload?.type === "achievement-review") {
    return {
      before: payload.before || null,
      after: payload.after || null,
    };
  }
  if (payload && typeof payload === "object" && payload.fields) {
    return {
      before: null,
      after: buildLegacyAchievementSnapshot(entry.category, payload),
    };
  }
  return null;
}

function buildLegacyAchievementSnapshot(category, payload) {
  const fields = payload?.fields || {};
  const hiddenKeys = new Set(["_imageUrls", "_attachments"]);
  const fieldEntries = Object.entries(fields)
    .filter(([key]) => !hiddenKeys.has(key))
    .map(([key, value]) => ({
      key,
      label: key,
      value: formatChangeValue(value),
    }))
    .filter((field) => field.value !== "-");

  const imageUrls = [
    ...(payload?.imageUrl ? [payload.imageUrl] : []),
    ...parseJsonArray(fields._imageUrls),
  ]
    .filter(Boolean)
    .map((url) => resolveMediaUrl(url));
  const attachments = parseJsonArray(fields._attachments)
    .map((item) => ({
      url: resolveMediaUrl(item?.url || ""),
      name: item?.name || item?.originalName || "附件",
      mediaType: item?.mediaType || "",
    }))
    .filter((item) => item.url);

  return {
    category,
    categoryLabel: entryCategoryLabel(category),
    title: fieldEntries[0]?.value || payload?.title || "成就记录",
    dateLabel: "",
    dateValue: "",
    fieldEntries,
    imageUrls,
    attachments,
  };
}

function entryCategoryLabel(category) {
  const labels = {
    contest: "学科竞赛、文体艺术",
    paper: "发表学术论文",
    journal: "发表期刊作品",
    patent: "专利(著作权)授权数(项)",
    certificate: "职业资格证书",
    research: "学生参与教师科研项目情况",
    works: "创作、表演的代表性作品",
    doubleHundred: "双百工程",
    ieerTraining: "大学生创新创业训练计划项目",
  };
  return labels[category] || "成就记录";
}

function toggleRejectEditor() {
  rejectEditorOpen.value = !rejectEditorOpen.value;
  actionError.value = "";
  if (!rejectEditorOpen.value) {
    rejectReason.value = "";
  }
}

async function approveSelectedRequest() {
  if (!selectedEntry.value) {
    return;
  }
  actionError.value = "";
  try {
    await updateReviewRequestStatus({
      requestId: selectedEntry.value.id,
      status: "approved",
      reviewer: profile,
    });
  } catch (error) {
    actionError.value = error?.message || "处理失败，请稍后重试";
  }
}

async function rejectSelectedRequest() {
  if (!selectedEntry.value) {
    return;
  }
  actionError.value = "";
  try {
    await updateReviewRequestStatus({
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

function openCancelConfirm() {
  cancelConfirmOpen.value = true;
}

function closeCancelConfirm() {
  cancelConfirmOpen.value = false;
}

async function confirmCancelRequest() {
  if (!selectedEntry.value) {
    return;
  }
  try {
    await cancelReviewRequest({
      requestId: selectedEntry.value.id,
    });
    cancelConfirmOpen.value = false;
  } catch (error) {
    actionError.value = error?.message || "取消失败，请稍后重试";
  }
}
</script>

<template>
  <main class="dashboard-right notification-view">
    <header class="feed-header">
      <h1 class="feed-title">通知详情</h1>
    </header>

    <section v-if="!filteredEntries.length" class="notification-empty-card">
      <div class="notification-empty-title">暂无通知</div>
      <div class="notification-empty-text">当前分类下暂时没有可查看的通知。</div>
    </section>

    <section v-else-if="selectedEntry" class="notification-detail-card">
      <div class="notification-detail-top">
        <div class="notification-detail-badges">
          <span class="notification-badge" :class="selectedEntry.badgeClass">
            {{ selectedEntry.badgeText }}
          </span>
          <span class="notification-time">{{ selectedEntry.timeText }}</span>
        </div>
        <div class="notification-detail-actions">
          <button
            v-if="canCancelSelected"
            class="notification-action-btn is-cancel"
            type="button"
            @click="openCancelConfirm"
          >
            取消申请
          </button>
          <template v-if="canProcessSelected">
            <button
              class="notification-action-btn is-approve"
              type="button"
              @click="approveSelectedRequest"
            >
              通过
            </button>
            <button
              class="notification-action-btn is-reject"
              type="button"
              @click="toggleRejectEditor"
            >
              驳回
            </button>
          </template>
        </div>
      </div>

      <h2 class="notification-detail-title">{{ selectedEntry.title }}</h2>
      <p class="notification-detail-content">{{ selectedEntry.content }}</p>

      <section
        v-if="achievementReviewSnapshot"
        class="notification-changes notification-achievement-review"
      >
        <div class="notification-changes-label">成就审核</div>
        <div class="notification-achievement-compare">
          <AchievementReviewSnapshotCard
            :snapshot="achievementReviewSnapshot.before"
            :empty-text="
              selectedEntry.action === 'create' ? '新增前暂无记录' : '暂无原记录'
            "
          />
          <div class="notification-achievement-arrow" aria-hidden="true">
            →
          </div>
          <AchievementReviewSnapshotCard
            :snapshot="achievementReviewSnapshot.after"
            empty-text="暂无提交内容"
          />
        </div>
      </section>

      <section
        v-else-if="selectedEntry.changes?.length"
        class="notification-changes"
      >
        <div class="notification-changes-label">变更内容</div>
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
                <template v-if="isStructuredChangeValue(change.before)">
                  <div class="notification-change-entries">
                    <article
                      v-for="entry in parseStructuredChangeValue(change.before)"
                      :key="entry.title"
                      class="notification-change-entry"
                    >
                      <div class="notification-change-entry-title">{{ entry.title }}</div>
                      <div class="notification-change-entry-lines">
                        <div
                          v-for="detail in entry.details"
                          :key="detail"
                          class="notification-change-entry-line"
                        >
                          {{ detail }}
                        </div>
                      </div>
                    </article>
                  </div>
                </template>
                <div v-else class="notification-change-value">
                  {{ formatChangeValue(change.before) }}
                </div>
              </div>
              <div class="notification-change-column">
                <div class="notification-change-caption">修改后</div>
                <template v-if="isStructuredChangeValue(change.after)">
                  <div class="notification-change-entries">
                    <article
                      v-for="entry in parseStructuredChangeValue(change.after)"
                      :key="entry.title"
                      class="notification-change-entry is-next"
                    >
                      <div class="notification-change-entry-title">{{ entry.title }}</div>
                      <div class="notification-change-entry-lines">
                        <div
                          v-for="detail in entry.details"
                          :key="detail"
                          class="notification-change-entry-line"
                        >
                          {{ detail }}
                        </div>
                      </div>
                    </article>
                  </div>
                </template>
                <div v-else class="notification-change-value is-next">
                  {{ formatChangeValue(change.after) }}
                </div>
              </div>
            </div>
          </article>
        </div>
      </section>

      <section
        v-if="selectedEntry.reason"
        class="notification-reason"
      >
        <div class="notification-reason-label">驳回理由</div>
        <div class="notification-reason-text">{{ selectedEntry.reason }}</div>
      </section>

      <div v-if="canProcessSelected && rejectEditorOpen" class="notification-reject-box">
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

    <!-- Cancel Confirmation Modal -->
    <Teleport to="body">
      <div :class="['sheet-overlay', { open: cancelConfirmOpen }]" @click.self="closeCancelConfirm">
        <div class="sheet-modal cancel-confirm-modal">
          <div class="cancel-confirm-icon">!</div>
          <h3 class="cancel-confirm-title">取消审核申请</h3>
          <p class="cancel-confirm-text">
            取消申请后，修改的内容将不会被保存。<br>
            请在取消前确认已保存好需要保留的信息。
          </p>
          <div v-if="actionError" class="cancel-confirm-error">{{ actionError }}</div>
          <div class="cancel-confirm-actions">
            <button
              class="cancel-confirm-btn is-cancel"
              type="button"
              @click="closeCancelConfirm"
            >
              返回
            </button>
            <button
              class="cancel-confirm-btn is-confirm"
              type="button"
              @click="confirmCancelRequest"
            >
              确认取消
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </main>
</template>

<style scoped>
.notification-view {
  display: grid;
  gap: 14px;
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

.notification-detail-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.notification-detail-badges {
  display: flex;
  align-items: center;
  gap: 8px;
}

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

.notification-detail-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.notification-action-btn {
  padding: 6px 14px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
}

.notification-action-btn.is-cancel {
  background: rgba(216, 69, 54, 0.1);
  color: #a43a2e;
  border: 1px solid rgba(216, 69, 54, 0.2);
}

.notification-action-btn.is-cancel:hover {
  background: rgba(216, 69, 54, 0.18);
}

.notification-action-btn.is-approve {
  background: rgba(40, 150, 88, 0.12);
  color: #236248;
}

.notification-action-btn.is-approve:hover {
  background: rgba(40, 150, 88, 0.22);
}

.notification-action-btn.is-reject {
  background: rgba(216, 69, 54, 0.1);
  color: #a43a2e;
}

.notification-action-btn.is-reject:hover {
  background: rgba(216, 69, 54, 0.18);
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

.notification-changes,
.notification-actions-card {
  border-radius: 18px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.44);
}

.notification-changes-label {
  display: block;
  margin-bottom: 12px;
  color: #154f57;
  font-size: 12px;
  font-weight: 700;
}

.notification-reason {
  border-radius: 18px;
  padding: 16px;
  background: rgba(216, 69, 54, 0.08);
}

.notification-reason-label {
  display: block;
  margin-bottom: 8px;
  color: #a43a2e;
  font-size: 12px;
  font-weight: 700;
}

.notification-reason-text {
  color: #a43a2e;
  font-size: 14px;
  line-height: 1.7;
}

.notification-change-list {
  display: grid;
  gap: 12px;
}

.notification-achievement-compare {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 56px minmax(0, 1fr);
  gap: 14px;
  align-items: stretch;
}

.notification-achievement-arrow {
  display: grid;
  place-items: center;
  color: #0f555d;
  font-size: 28px;
  font-weight: 700;
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
  white-space: pre-wrap;
}

.notification-change-value.is-next {
  color: #0f555d;
  font-weight: 600;
}

.notification-change-entries {
  display: grid;
  gap: 10px;
}

.notification-change-entry {
  border-radius: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.58);
  border: 1px solid rgba(3, 107, 114, 0.08);
}

.notification-change-entry.is-next {
  background: rgba(205, 255, 249, 0.4);
  border-color: rgba(3, 107, 114, 0.12);
}

.notification-change-entry-title {
  margin-bottom: 8px;
  color: #103f46;
  font-size: 13px;
  font-weight: 700;
}

.notification-change-entry-lines {
  display: grid;
  gap: 6px;
}

.notification-change-entry-line {
  color: #54727a;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-word;
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
  .notification-achievement-compare {
    grid-template-columns: 1fr;
  }

  .notification-achievement-arrow {
    min-height: 28px;
    transform: rotate(90deg);
  }

  .notification-change-values {
    grid-template-columns: 1fr;
  }
}

.cancel-confirm-modal {
  max-width: 320px;
  padding: 28px 24px;
  text-align: center;
}

.cancel-confirm-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: rgba(216, 69, 54, 0.12);
  color: #a43a2e;
  font-size: 24px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.cancel-confirm-title {
  color: #0d4047;
  font-size: 18px;
  font-weight: 700;
  margin: 0 0 12px;
}

.cancel-confirm-text {
  color: #5a747c;
  font-size: 14px;
  line-height: 1.6;
  margin: 0 0 20px;
}

.cancel-confirm-error {
  color: #a43a2e;
  font-size: 13px;
  margin-bottom: 12px;
}

.cancel-confirm-actions {
  display: flex;
  gap: 12px;
}

.cancel-confirm-btn {
  flex: 1;
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: opacity 0.2s;
}

.cancel-confirm-btn:hover {
  opacity: 0.85;
}

.cancel-confirm-btn.is-cancel {
  background: rgba(0, 0, 0, 0.08);
  color: #5a747c;
}

.cancel-confirm-btn.is-confirm {
  background: #d84536;
  color: #fff;
}
</style>
