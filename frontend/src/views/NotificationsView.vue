<script setup>
import { computed, reactive, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import AchievementReviewSnapshotCard from "../components/AchievementReviewSnapshotCard.vue";
import StudentProfileEditor from "../components/StudentProfileEditor.vue";
import { API_BASE } from "../api/request";
import { useNotifications } from "../composables/useNotifications";
import { searchStudentProfiles, getStudentProfileById } from "../api/profile";

const route = useRoute();
const router = useRouter();
const profile = reactive(loadUser());
const { inboxEntries, updateReviewRequestStatus, cancelReviewRequest } = useNotifications(profile);

const rejectEditorOpen = ref(false);
const rejectReason = ref(localStorage.getItem("gcsc_reject_draft") || "");
watch(rejectReason, (val) => {
  if (val) localStorage.setItem("gcsc_reject_draft", val);
});
const actionError = ref("");
const selectedId = ref(null);

const CATEGORIES = [
  { key: "pending", label: "待处理", icon: "clock" },
  { key: "approved", label: "已通过", icon: "check" },
  { key: "rejected", label: "已驳回", icon: "x" },
  { key: "system", label: "系统通知", icon: "bell" },
];

const activeCategory = computed(() => {
  const raw = route.query.category;
  return typeof raw === "string" && raw ? raw : "pending";
});
const filteredEntries = computed(() =>
  inboxEntries.value.filter((entry) => entry.categoryKey === activeCategory.value),
);
const selectedEntry = computed(
  () =>
    filteredEntries.value.find((entry) => entry.id === selectedId.value) || null,
);
const canProcessSelected = computed(() => {
  if (!selectedEntry.value || selectedEntry.value.source !== "review-request") return false;
  if (selectedEntry.value.status !== "pending") return false;
  if (!["TEACHER", "ADMIN"].includes(profile.role)) return false;
  return selectedEntry.value.requester?.username !== profile.username;
});
const canCancelSelected = computed(() => {
  if (!selectedEntry.value || selectedEntry.value.source !== "review-request") return false;
  if (selectedEntry.value.status !== "pending") return false;
  return selectedEntry.value.requester?.username === profile.username;
});
const canViewStudentInfo = computed(() => {
  if (!selectedEntry.value) return false;
  if (!["TEACHER", "ADMIN"].includes(profile.role)) return false;
  return Boolean(selectedEntry.value.requester?.username);
});

const studentDetailOpen = ref(false);
const studentDetailLoading = ref(false);
const studentDetailItem = ref(null);
const cancelConfirmOpen = ref(false);
const achievementReviewSnapshot = computed(() => resolveAchievementReviewPayload(selectedEntry.value));

function setCategory(key) {
  router.replace({ path: "/notifications", query: { category: key, entry: "" } });
  selectedId.value = null;
}

watch(
  [filteredEntries, activeCategory],
  ([list]) => {
    if (!list?.length) return;
    if (!selectedId.value) {
      selectedId.value = list[0].id;
    } else if (!list.find((e) => e.id === selectedId.value)) {
      selectedId.value = list[0].id;
    }
  },
  { immediate: true },
);

watch(selectedId, () => {
  rejectEditorOpen.value = false;
  actionError.value = "";
  cancelConfirmOpen.value = false;
  closeStudentDetail();
});

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
    return { username: "", displayName: "", avatarUrl: "", role: "STUDENT", studentNo: "" };
  }
}

function formatChangeValue(value) {
  if (value === null || value === undefined || value === "") return "-";
  if (Array.isArray(value)) return value.length ? value.join("、") : "-";
  return String(value);
}

function isStructuredChangeValue(value) {
  const text = formatChangeValue(value);
  return text.includes("第1条\n") || /^第\d+条$/m.test(text);
}

function parseStructuredChangeValue(value) {
  const text = formatChangeValue(value);
  if (!isStructuredChangeValue(text)) return [];
  return text
    .split(/\n(?=第\d+条\n?)/)
    .map((b) => b.trim())
    .filter(Boolean)
    .map((block) => {
      const lines = block.split("\n").map((l) => l.trim()).filter(Boolean);
      const [title, ...details] = lines;
      return { title, details };
    });
}

function parseJsonArray(value) {
  if (!value) return [];
  try {
    const parsed = JSON.parse(value);
    return Array.isArray(parsed) ? parsed : [];
  } catch { return []; }
}

function resolveMediaUrl(url) {
  if (!url) return "";
  if (url.startsWith("http://") || url.startsWith("https://")) return url;
  return `${API_BASE}${url}`;
}

function resolveAchievementReviewPayload(entry) {
  if (!entry || entry.resourceType !== "achievement") return null;
  const payload = entry.payloadSnapshot;
  if (payload?.type === "achievement-review") {
    return { before: payload.before || null, after: payload.after || null };
  }
  if (payload && typeof payload === "object" && payload.fields) {
    return { before: null, after: buildLegacyAchievementSnapshot(entry.category, payload) };
  }
  return null;
}

function buildLegacyAchievementSnapshot(category, payload) {
  const fields = payload?.fields || {};
  const hiddenKeys = new Set(["_imageUrls", "_attachments"]);
  const fieldEntries = Object.entries(fields)
    .filter(([k]) => !hiddenKeys.has(k))
    .map(([key, value]) => ({ key, label: key, value: formatChangeValue(value) }))
    .filter((f) => f.value !== "-");
  const imageUrls = [...(payload?.imageUrl ? [payload.imageUrl] : []), ...parseJsonArray(fields._imageUrls)]
    .filter(Boolean).map((url) => resolveMediaUrl(url));
  const attachments = parseJsonArray(fields._attachments)
    .map((item) => ({
      url: resolveMediaUrl(item?.url || ""),
      name: item?.name || item?.originalName || "附件",
      mediaType: item?.mediaType || "",
    })).filter((item) => item.url);
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
    contest: "学科竞赛、文体艺术", paper: "发表学术论文", journal: "发表期刊作品",
    patent: "专利(著作权)授权数(项)", certificate: "职业资格证书",
    research: "学生参与教师科研项目情况", works: "创作、表演的代表性作品",
    doubleHundred: "双百工程", ieerTraining: "大学生创新创业训练计划项目",
  };
  return labels[category] || "成就记录";
}

async function approveSelectedRequest() {
  if (!selectedEntry.value) return;
  actionError.value = "";
  try {
    await updateReviewRequestStatus({ requestId: selectedEntry.value.id, status: "approved", reviewer: profile });
  } catch (error) {
    actionError.value = error?.message || "处理失败，请稍后重试";
  }
}

async function rejectSelectedRequest() {
  if (!selectedEntry.value) return;
  actionError.value = "";
  try {
    await updateReviewRequestStatus({ requestId: selectedEntry.value.id, status: "rejected", reviewer: profile, reason: rejectReason.value });
    rejectEditorOpen.value = false;
    rejectReason.value = "";
    localStorage.removeItem("gcsc_reject_draft");
  } catch (error) {
    actionError.value = error?.message || "处理失败，请稍后重试";
  }
}

function toggleRejectEditor() {
  rejectEditorOpen.value = !rejectEditorOpen.value;
  actionError.value = "";
}

function openCancelConfirm() { cancelConfirmOpen.value = true; }
function closeCancelConfirm() { cancelConfirmOpen.value = false; }

async function confirmCancelRequest() {
  if (!selectedEntry.value) return;
  try {
    await cancelReviewRequest({ requestId: selectedEntry.value.id });
    cancelConfirmOpen.value = false;
  } catch (error) {
    actionError.value = error?.message || "取消失败，请稍后重试";
  }
}

function viewStudentInfo() {
  const requester = selectedEntry.value?.requester;
  if (!requester?.username) return;
  studentDetailOpen.value = true;
  studentDetailLoading.value = true;
  studentDetailItem.value = null;
  const keyword = requester.studentNo || requester.username;
  searchStudentProfiles({ keyword, size: 1 })
    .then(({ data }) => {
      const item = data?.items?.[0];
      if (item?.id) return getStudentProfileById(item.id);
      return Promise.reject(new Error("未找到该学生"));
    })
    .then(({ data }) => { studentDetailItem.value = data || null; })
    .catch(() => { studentDetailItem.value = null; })
    .finally(() => { studentDetailLoading.value = false; });
}

function closeStudentDetail() {
  studentDetailOpen.value = false;
  setTimeout(() => { studentDetailItem.value = null; }, 450);
}
</script>

<template>
  <main class="dashboard-right notif-view">
    <header class="feed-header">
      <h1 class="feed-title">通知详情</h1>
    </header>

    <!-- Category Tabs -->
    <nav class="notif-tabs" role="tablist">
      <button
        v-for="cat in CATEGORIES"
        :key="cat.key"
        class="notif-tab"
        :class="{ active: activeCategory === cat.key }"
        role="tab"
        :aria-selected="activeCategory === cat.key"
        type="button"
        @click="setCategory(cat.key)"
      >
        {{ cat.label }}
        <span
          v-if="inboxEntries.filter(e => e.categoryKey === cat.key).length"
          class="notif-tab-count"
        >
          {{ inboxEntries.filter(e => e.categoryKey === cat.key).length }}
        </span>
      </button>
    </nav>

    <!-- Empty State -->
    <div v-if="!filteredEntries.length" class="notif-empty">
      <div class="notif-empty-icon">
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" aria-hidden="true">
          <path stroke-linecap="round" stroke-linejoin="round" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6 6 0 10-10.999 0v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
        </svg>
      </div>
      <p class="notif-empty-title">暂无通知</p>
      <p class="notif-empty-sub">当前分类下暂时没有可查看的通知</p>
    </div>

    <!-- Master-Detail Layout -->
    <div v-else class="notif-master-detail">
      <!-- Notification List -->
      <aside class="notif-list" role="list">
        <button
          v-for="(entry, i) in filteredEntries"
          :key="entry.id"
          class="notif-item"
          :class="{ active: selectedId === entry.id }"
          role="listitem"
          type="button"
          :style="{ animationDelay: `${i * 35}ms` }"
          @click="selectedId = entry.id"
        >
          <div class="notif-item-top">
            <span class="notif-badge" :class="entry.badgeClass">{{ entry.badgeText }}</span>
            <time class="notif-item-time">{{ entry.timeText }}</time>
          </div>
          <p class="notif-item-title">{{ entry.title }}</p>
          <p class="notif-item-content">{{ entry.content }}</p>
        </button>
      </aside>

      <!-- Detail Panel -->
      <section class="notif-detail">
        <Transition name="detail-fade" mode="out-in">
          <div v-if="selectedEntry" :key="selectedEntry.id" class="notif-detail-inner">

            <!-- Top Bar -->
            <div class="notif-detail-top">
              <div class="notif-detail-badges">
                <span class="notif-badge" :class="selectedEntry.badgeClass">{{ selectedEntry.badgeText }}</span>
                <time class="notif-time">{{ selectedEntry.timeText }}</time>
              </div>
              <div class="notif-detail-actions">
                <button v-if="canCancelSelected" class="notif-btn is-cancel" type="button" @click="openCancelConfirm">取消申请</button>
                <button v-if="canViewStudentInfo" class="notif-btn is-info" type="button" @click="viewStudentInfo">学生信息</button>
                <template v-if="canProcessSelected">
                  <button class="notif-btn is-approve" type="button" @click="approveSelectedRequest">通过</button>
                  <button class="notif-btn is-reject" type="button" @click="toggleRejectEditor">驳回</button>
                </template>
              </div>
            </div>

            <!-- Title & Content -->
            <h2 class="notif-title">{{ selectedEntry.title }}</h2>
            <p class="notif-content">{{ selectedEntry.content }}</p>

            <!-- Achievement Review Snapshot -->
            <Transition name="section-fade" mode="out-in">
              <section v-if="achievementReviewSnapshot" key="achievement" class="notif-changes notif-achievement">
                <div class="notif-section-label">成就审核</div>
                <div class="notif-compare">
                  <AchievementReviewSnapshotCard
                    :snapshot="achievementReviewSnapshot.before"
                    :empty-text="selectedEntry.action === 'create' ? '新增前暂无记录' : '暂无原记录'"
                  />
                  <div class="notif-compare-arrow" aria-hidden="true">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                      <path stroke-linecap="round" stroke-linejoin="round" d="M13 7l5 5m0 0l-5 5m5-5H6" />
                    </svg>
                  </div>
                  <AchievementReviewSnapshotCard
                    :snapshot="achievementReviewSnapshot.after"
                    empty-text="暂无提交内容"
                  />
                </div>
              </section>

              <!-- Structured Changes -->
              <section v-else-if="selectedEntry.changes?.length" key="changes" class="notif-changes">
                <div class="notif-section-label">变更内容</div>
                <div class="notif-change-list">
                  <article
                    v-for="change in selectedEntry.changes"
                    :key="`${change.section || ''}-${change.label}-${change.after}`"
                    class="notif-change-item"
                  >
                    <div class="notif-change-head">
                      <span class="notif-change-label">{{ change.label }}</span>
                      <span v-if="change.section" class="notif-change-section">{{ change.section }}</span>
                    </div>
                    <div class="notif-change-values">
                      <div class="notif-change-col">
                        <div class="notif-change-cap">修改前</div>
                        <template v-if="isStructuredChangeValue(change.before)">
                          <article
                            v-for="entry in parseStructuredChangeValue(change.before)"
                            :key="entry.title"
                            class="notif-change-entry"
                          >
                            <div class="notif-change-entry-title">{{ entry.title }}</div>
                            <div
                              v-for="detail in entry.details"
                              :key="detail"
                              class="notif-change-entry-line"
                            >{{ detail }}</div>
                          </article>
                        </template>
                        <div v-else class="notif-change-val">{{ formatChangeValue(change.before) }}</div>
                      </div>
                      <div class="notif-change-col is-next">
                        <div class="notif-change-cap">修改后</div>
                        <template v-if="isStructuredChangeValue(change.after)">
                          <article
                            v-for="entry in parseStructuredChangeValue(change.after)"
                            :key="entry.title"
                            class="notif-change-entry is-next"
                          >
                            <div class="notif-change-entry-title">{{ entry.title }}</div>
                            <div
                              v-for="detail in entry.details"
                              :key="detail"
                              class="notif-change-entry-line"
                            >{{ detail }}</div>
                          </article>
                        </template>
                        <div v-else class="notif-change-val is-next">{{ formatChangeValue(change.after) }}</div>
                      </div>
                    </div>
                  </article>
                </div>
              </section>
            </Transition>

            <!-- Rejection Reason -->
            <div v-if="selectedEntry.reason" class="notif-reason">
              <div class="notif-section-label">驳回理由</div>
              <p class="notif-reason-text">{{ selectedEntry.reason }}</p>
            </div>

          </div>
        </Transition>
      </section>
    </div>

    <!-- Reject Editor Modal -->
    <Teleport to="body">
      <div :class="['sheet-overlay', { open: rejectEditorOpen }]" @click.self="toggleRejectEditor">
        <div class="sheet-modal reject-modal" role="dialog" aria-modal="true" aria-label="驳回申请" @click.stop>
          <header class="reject-modal-header">
            <div class="reject-modal-icon" aria-hidden="true">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-2.694-.833-3.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z"/>
              </svg>
            </div>
            <div class="reject-modal-title-group">
              <h2 class="reject-modal-title">驳回申请</h2>
              <p class="reject-modal-subtitle">请填写驳回理由，学生将收到通知</p>
            </div>
            <button class="reject-modal-close" type="button" aria-label="关闭" @click="toggleRejectEditor">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                <path d="M18 6L6 18M6 6l12 12"/>
              </svg>
            </button>
          </header>

          <div class="reject-modal-body">
            <label class="reject-label" for="reject-reason">驳回理由</label>
            <textarea
              id="reject-reason"
              v-model="rejectReason"
              class="reject-textarea"
              rows="5"
              placeholder="请输入驳回原因，学生会在通知详情里看到这条理由。"
              maxlength="500"
            />
            <div class="reject-footer">
              <span v-if="actionError" class="reject-error">{{ actionError }}</span>
              <span class="reject-char-count">{{ rejectReason.length }} / 500</span>
            </div>
          </div>

          <div class="reject-modal-actions">
            <button class="reject-btn-cancel" type="button" @click="toggleRejectEditor">返回</button>
            <button class="reject-btn-confirm" type="button" @click="rejectSelectedRequest">确认驳回</button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Cancel Confirm Modal -->
    <Teleport to="body">
      <div :class="['sheet-overlay', { open: cancelConfirmOpen }]" @click.self="closeCancelConfirm">
        <div class="sheet-modal cancel-confirm-modal">
          <div class="cancel-confirm-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-2.694-.833-3.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z" />
            </svg>
          </div>
          <h3 class="cancel-confirm-title">取消审核申请</h3>
          <p class="cancel-confirm-text">取消申请后，修改的内容将不会被保存。<br>请在取消前确认已保存好需要保留的信息。</p>
          <div v-if="actionError" class="cancel-confirm-error">{{ actionError }}</div>
          <div class="cancel-confirm-actions">
            <button class="cancel-confirm-btn is-cancel" type="button" @click="closeCancelConfirm">返回</button>
            <button class="cancel-confirm-btn is-confirm" type="button" @click="confirmCancelRequest">确认取消</button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Student Detail Modal -->
    <Teleport to="body">
      <div :class="['sheet-overlay', { open: studentDetailOpen }]" @click.self="closeStudentDetail">
        <div class="sheet-modal student-detail-modal">
          <header class="sheet-modal-header">
            <div class="sheet-modal-title">学生详情</div>
            <button class="sheet-modal-close" type="button" @click="closeStudentDetail">关闭</button>
          </header>
          <div v-if="studentDetailLoading" class="sheet-modal-loading">加载中...</div>
          <StudentProfileEditor
            v-else-if="studentDetailItem"
            :student="studentDetailItem"
            :resolve-media-url="resolveMediaUrl"
            :save-profile="() => Promise.resolve()"
            :can-edit="false"
            :show-achievements="false"
          />
          <div v-else class="sheet-modal-empty">未找到该学生信息</div>
        </div>
      </div>
    </Teleport>
  </main>
</template>

<style scoped>
/* ── View Shell ──────────────────────────────────────────── */
.notif-view {
  display: grid;
  gap: 14px;
}

/* ── Category Tabs ──────────────────────────────────────── */
.notif-tabs {
  display: flex;
  gap: 6px;
  padding: 4px;
  border-radius: 16px;
  background: var(--card);
  border: 1px solid var(--line);
  box-shadow: var(--shadow-sm);
  width: fit-content;
  flex-wrap: wrap;
}

.notif-tab {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 16px;
  border-radius: 10px;
  border: none;
  background: transparent;
  color: var(--text-sub);
  font-size: 13.5px;
  font-weight: 600;
  cursor: pointer;
  transition:
    background 160ms ease,
    color 160ms ease,
    transform 120ms ease;
}

.notif-tab:hover:not(.active) {
  background: rgba(100, 12, 114, 0.06);
  color: var(--primary);
}

.notif-tab.active {
  background: var(--primary);
  color: #fff;
  box-shadow: 0 4px 14px rgba(100, 12, 114, 0.3);
}

.notif-tab:active {
  transform: scale(0.97);
}

.notif-tab-count {
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 999px;
  background: rgba(100, 12, 114, 0.14);
  color: inherit;
  font-size: 11px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.notif-tab.active .notif-tab-count {
  background: rgba(255, 255, 255, 0.25);
}

/* ── Empty State ─────────────────────────────────────────── */
.notif-empty {
  display: grid;
  place-items: center;
  padding: 56px 24px;
  border-radius: 24px;
  background: var(--card);
  border: 1px solid var(--line);
  box-shadow: var(--shadow-sm);
  animation: cardEnter 400ms var(--ease-out) both;
}

.notif-empty-icon {
  width: 72px;
  height: 72px;
  border-radius: 20px;
  background: var(--primary-surface);
  color: var(--primary);
  display: grid;
  place-items: center;
  margin-bottom: 16px;
}

.notif-empty-title {
  margin: 0;
  color: var(--text-main);
  font-size: 17px;
  font-weight: 700;
}

.notif-empty-sub {
  margin: 6px 0 0;
  color: var(--text-sub);
  font-size: 13.5px;
}

/* ── Master-Detail Layout ─────────────────────────────────── */
.notif-master-detail {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 14px;
  align-items: start;
}

/* ── Notification List ───────────────────────────────────── */
.notif-list {
  display: grid;
  gap: 8px;
}

.notif-item {
  display: grid;
  gap: 6px;
  padding: 14px 16px;
  border-radius: 16px;
  border: 1px solid var(--line);
  background: var(--card);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  text-align: left;
  width: 100%;
  transition:
    background 180ms ease,
    border-color 180ms ease,
    transform 160ms ease,
    box-shadow 180ms ease;
  animation: slideInLeft 300ms var(--ease-out) both;
}

.notif-item:hover {
  background: var(--card-hover);
  border-color: var(--line-strong);
  transform: translateX(3px);
  box-shadow: var(--shadow);
}

.notif-item.active {
  background: rgba(100, 12, 114, 0.06);
  border-color: var(--primary);
  box-shadow: 0 0 0 2px rgba(100, 12, 114, 0.15), var(--shadow-sm);
}

.notif-item-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.notif-item-time {
  color: var(--text-sub);
  font-size: 11.5px;
  white-space: nowrap;
}

.notif-item-title {
  margin: 0;
  color: var(--text-main);
  font-size: 14px;
  font-weight: 700;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notif-item-content {
  margin: 0;
  color: var(--text-sub);
  font-size: 12.5px;
  line-height: 1.5;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* ── Status Badges ───────────────────────────────────────── */
.notif-badge {
  display: inline-flex;
  align-items: center;
  height: 22px;
  padding: 0 9px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.2px;
}

.notif-badge.is-pending {
  background: rgba(234, 179, 8, 0.14);
  color: #926300;
}

.notif-badge.is-approved {
  background: rgba(34, 197, 94, 0.14);
  color: #166534;
}

.notif-badge.is-rejected {
  background: rgba(239, 68, 68, 0.13);
  color: #991b1b;
}

.notif-badge.is-system {
  background: var(--primary-surface);
  color: var(--primary);
}

/* ── Detail Panel ────────────────────────────────────────── */
.notif-detail {
  border-radius: 24px;
  background: var(--card);
  border: 1px solid var(--line);
  box-shadow: var(--shadow);
  overflow: hidden;
  min-height: 400px;
}

.notif-detail-inner {
  display: grid;
  gap: 16px;
  padding: 22px;
}

.notif-detail-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.notif-detail-badges {
  display: flex;
  align-items: center;
  gap: 8px;
}

.notif-time {
  color: var(--text-sub);
  font-size: 12px;
}

.notif-detail-actions {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* ── Action Buttons ──────────────────────────────────────── */
.notif-btn {
  display: inline-flex;
  align-items: center;
  height: 34px;
  padding: 0 14px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 700;
  border: none;
  cursor: pointer;
  transition:
    background 160ms ease,
    border-color 160ms ease,
    color 160ms ease,
    transform 120ms ease,
    box-shadow 160ms ease;
}

.notif-btn:active {
  transform: scale(0.96);
}

.notif-btn.is-cancel {
  background: rgba(239, 68, 68, 0.1);
  color: var(--danger);
  border: 1px solid rgba(239, 68, 68, 0.2);
}

.notif-btn.is-cancel:hover {
  background: rgba(239, 68, 68, 0.18);
  border-color: rgba(239, 68, 68, 0.4);
}

.notif-btn.is-approve {
  background: rgba(34, 197, 94, 0.12);
  color: #166534;
  border: 1px solid rgba(34, 197, 94, 0.25);
}

.notif-btn.is-approve:hover {
  background: rgba(34, 197, 94, 0.22);
  box-shadow: 0 4px 12px rgba(34, 197, 94, 0.2);
}

.notif-btn.is-reject {
  background: rgba(239, 68, 68, 0.1);
  color: var(--danger);
  border: 1px solid rgba(239, 68, 68, 0.2);
}

.notif-btn.is-reject:hover {
  background: rgba(239, 68, 68, 0.18);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.18);
}

.notif-btn.is-info {
  background: var(--primary-surface);
  color: var(--primary);
  border: 1px solid rgba(100, 12, 114, 0.18);
}

.notif-btn.is-info:hover {
  background: rgba(100, 12, 114, 0.14);
  border-color: var(--primary);
}

.notif-btn.is-submit {
  margin-top: 10px;
  height: 38px;
  padding: 0 20px;
}

/* ── Title & Content ─────────────────────────────────────── */
.notif-title {
  margin: 0;
  color: var(--text-main);
  font-size: 22px;
  font-weight: 800;
  letter-spacing: -0.2px;
  line-height: 1.35;
}

.notif-content {
  margin: 0;
  color: var(--text-sub);
  font-size: 14.5px;
  line-height: 1.75;
}

/* ── Section Label ───────────────────────────────────────── */
.notif-section-label {
  display: block;
  margin-bottom: 10px;
  color: var(--primary);
  font-size: 11.5px;
  font-weight: 700;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  opacity: 0.75;
}

/* ── Changes Section ─────────────────────────────────────── */
.notif-changes {
  border-radius: 16px;
  padding: 16px;
  background: rgba(100, 12, 114, 0.03);
  border: 1px solid var(--line);
}

.notif-achievement {
  background: rgba(100, 12, 114, 0.03);
}

.notif-compare {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 48px minmax(0, 1fr);
  gap: 12px;
  align-items: stretch;
}

.notif-compare-arrow {
  display: grid;
  place-items: center;
  color: var(--primary);
  opacity: 0.5;
}

.notif-change-list {
  display: grid;
  gap: 10px;
}

.notif-change-item {
  border-radius: 14px;
  padding: 14px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid var(--line);
}

.notif-change-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 10px;
}

.notif-change-label {
  color: var(--text-main);
  font-size: 13.5px;
  font-weight: 700;
}

.notif-change-section {
  color: var(--text-sub);
  font-size: 11.5px;
}

.notif-change-values {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.notif-change-col {
  border-radius: 10px;
  padding: 10px 12px;
  background: rgba(100, 12, 114, 0.04);
}

.notif-change-col.is-next {
  background: var(--primary-surface);
}

.notif-change-cap {
  margin-bottom: 6px;
  color: var(--text-sub);
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.4px;
}

.notif-change-val {
  color: var(--text-main);
  font-size: 13px;
  line-height: 1.55;
  word-break: break-word;
  white-space: pre-wrap;
}

.notif-change-val.is-next {
  color: var(--primary);
  font-weight: 600;
}

.notif-change-entry {
  border-radius: 8px;
  padding: 8px 10px;
  background: rgba(255, 255, 255, 0.7);
  margin-bottom: 6px;
}

.notif-change-entry:last-child {
  margin-bottom: 0;
}

.notif-change-entry.is-next {
  background: rgba(100, 12, 114, 0.08);
}

.notif-change-entry-title {
  color: var(--text-main);
  font-size: 12.5px;
  font-weight: 700;
  margin-bottom: 4px;
}

.notif-change-entry-line {
  color: var(--text-sub);
  font-size: 12px;
  line-height: 1.5;
}

/* ── Reason ─────────────────────────────────────────────── */
.notif-reason {
  border-radius: 14px;
  padding: 14px;
  background: rgba(239, 68, 68, 0.06);
  border: 1px solid rgba(239, 68, 68, 0.15);
}

.notif-reason-text {
  margin: 0;
  color: var(--danger);
  font-size: 13.5px;
  line-height: 1.7;
}

/* ── Transitions ─────────────────────────────────────────── */
.detail-fade-enter-active {
  transition: opacity 280ms ease, transform 280ms cubic-bezier(0.22, 1, 0.36, 1);
}

.detail-fade-leave-active {
  transition: opacity 200ms ease, transform 200ms ease;
}

.detail-fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.detail-fade-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

.section-fade-enter-active,
.section-fade-leave-active {
  transition: opacity 220ms ease, transform 220ms ease;
}

.section-fade-enter-from,
.section-fade-leave-to {
  opacity: 0;
  transform: translateY(6px);
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-12px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* ── Reject Editor Modal ──────────────────────────────── */
.reject-modal {
  max-width: 400px;
  padding: 0;
  overflow: hidden;
}

.reject-modal-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 20px 20px 0;
}

.reject-modal-icon {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  border-radius: 12px;
  background: rgba(192, 57, 43, 0.1);
  color: var(--danger);
  display: grid;
  place-items: center;
  margin-top: 2px;
}

.reject-modal-title-group {
  flex: 1;
  min-width: 0;
}

.reject-modal-title {
  margin: 0;
  font-size: 16px;
  font-weight: 800;
  color: var(--primary-dark);
  line-height: 1.3;
}

.reject-modal-subtitle {
  margin: 3px 0 0;
  font-size: 12.5px;
  color: var(--text-sub);
  line-height: 1.4;
}

.reject-modal-close {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  border-radius: 8px;
  border: 1px solid var(--line);
  background: transparent;
  color: var(--text-sub);
  display: grid;
  place-items: center;
  cursor: pointer;
  transition: background 160ms ease, border-color 160ms ease, color 160ms ease;
  margin-top: 2px;
}

.reject-modal-close:hover {
  background: rgba(100, 12, 114, 0.08);
  border-color: var(--primary);
  color: var(--primary);
}

.reject-modal-close:active {
  transform: scale(0.95);
}

.reject-modal-close:focus-visible {
  outline: 2px solid var(--primary);
  outline-offset: 2px;
}

.reject-modal-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 16px 20px 0;
}

.reject-label {
  font-size: 13px;
  font-weight: 700;
  color: var(--primary-dark);
}

.reject-textarea {
  width: 100%;
  min-height: 120px;
  border: 1.5px solid var(--line-strong);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.9);
  padding: 12px 14px;
  resize: vertical;
  color: var(--text-main);
  font-size: 14px;
  line-height: 1.65;
  outline: none;
  transition: border-color 200ms ease, box-shadow 200ms ease;
}

.reject-textarea:focus {
  border-color: var(--danger);
  box-shadow: 0 0 0 3px rgba(192, 57, 43, 0.1);
}

.reject-textarea::placeholder {
  color: var(--text-sub);
  opacity: 0.7;
}

.reject-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  min-height: 20px;
}

.reject-error {
  color: var(--danger);
  font-size: 12.5px;
  font-weight: 600;
}

.reject-char-count {
  color: var(--text-sub);
  font-size: 11.5px;
  font-variant-numeric: tabular-nums;
  margin-left: auto;
}

.reject-modal-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  padding: 16px 20px 20px;
}

.reject-btn-cancel,
.reject-btn-confirm {
  height: 42px;
  font-size: 14px;
  font-weight: 700;
  border-radius: 10px;
  border: none;
  cursor: pointer;
  transition: background 160ms ease, filter 160ms ease, transform 120ms ease;
}

.reject-btn-cancel {
  background: rgba(100, 12, 114, 0.08);
  color: var(--primary);
}

.reject-btn-cancel:hover {
  background: rgba(100, 12, 114, 0.14);
}

.reject-btn-cancel:active {
  transform: scale(0.97);
}

.reject-btn-cancel:focus-visible {
  outline: 2px solid var(--primary);
  outline-offset: 2px;
}

.reject-btn-confirm {
  background: var(--danger);
  color: #fff;
}

.reject-btn-confirm:hover {
  filter: brightness(1.08);
}

.reject-btn-confirm:active {
  transform: scale(0.97);
}

.reject-btn-confirm:focus-visible {
  outline: 2px solid var(--danger);
  outline-offset: 2px;
}

/* ── Cancel Confirm Modal ───────────────────────────────── */
.cancel-confirm-modal {
  max-width: 320px;
  padding: 28px 24px;
  text-align: center;
}

.cancel-confirm-icon {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  background: rgba(239, 68, 68, 0.1);
  color: var(--danger);
  display: grid;
  place-items: center;
  margin: 0 auto 16px;
}

.cancel-confirm-title {
  margin: 0 0 10px;
  color: var(--text-main);
  font-size: 18px;
  font-weight: 800;
}

.cancel-confirm-text {
  margin: 0 0 20px;
  color: var(--text-sub);
  font-size: 14px;
  line-height: 1.65;
}

.cancel-confirm-error {
  color: var(--danger);
  font-size: 13px;
  margin-bottom: 12px;
}

.cancel-confirm-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.cancel-confirm-btn {
  flex: 1;
  padding: 11px 16px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 700;
  border: none;
  cursor: pointer;
  transition:
    opacity 160ms ease,
    transform 120ms ease,
    background 160ms ease;
}

.cancel-confirm-btn:active {
  transform: scale(0.97);
}

.cancel-confirm-btn.is-cancel {
  background: rgba(100, 12, 114, 0.08);
  color: var(--primary);
}

.cancel-confirm-btn.is-cancel:hover {
  background: rgba(100, 12, 114, 0.14);
}

.cancel-confirm-btn.is-confirm {
  background: var(--danger);
  color: #fff;
}

.cancel-confirm-btn.is-confirm:hover {
  filter: brightness(1.08);
}

/* ── Student Detail Modal ───────────────────────────────── */
.student-detail-modal {
  width: 90%;
  max-width: 720px;
  max-height: 85vh;
  border-radius: 22px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.sheet-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--line);
  flex-shrink: 0;
}

.sheet-modal-title {
  color: var(--text-main);
  font-size: 17px;
  font-weight: 800;
}

.sheet-modal-close {
  display: inline-flex;
  align-items: center;
  height: 32px;
  padding: 0 14px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 700;
  background: var(--primary-surface);
  color: var(--primary);
  border: 1px solid rgba(100, 12, 114, 0.18);
  cursor: pointer;
  transition:
    background 160ms ease,
    border-color 160ms ease,
    transform 120ms ease;
}

.sheet-modal-close:hover {
  background: rgba(100, 12, 114, 0.14);
  border-color: var(--primary);
}

.sheet-modal-close:active {
  transform: scale(0.96);
}

.sheet-modal-loading,
.sheet-modal-empty {
  padding: 48px 24px;
  text-align: center;
  color: var(--text-sub);
  font-size: 14px;
  flex: 1;
}

.sheet-modal :deep(.student-profile-editor) {
  flex: 1;
  overflow-y: auto;
}

/* ── Responsive ─────────────────────────────────────────── */
@media (max-width: 900px) {
  .notif-master-detail {
    grid-template-columns: 1fr;
  }

  .notif-list {
    max-height: 320px;
    overflow-y: auto;
    padding-right: 4px;
  }

  .notif-item {
    animation-delay: 0ms !important;
  }

  .notif-compare {
    grid-template-columns: 1fr;
  }

  .notif-compare-arrow {
    transform: rotate(90deg);
    min-height: 32px;
  }

  .notif-change-values {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 600px) {
  .notif-tabs {
    width: 100%;
  }

  .notif-tab {
    flex: 1;
    justify-content: center;
    padding: 7px 8px;
    font-size: 13px;
  }

  .notif-detail-top {
    flex-direction: column;
    align-items: flex-start;
  }

  .notif-detail-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .notif-btn {
    flex: 1;
    justify-content: center;
  }
}
</style>
