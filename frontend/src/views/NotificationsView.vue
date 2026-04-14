<script setup>
import { computed, reactive, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import AchievementReviewSnapshotCard from "../components/AchievementReviewSnapshotCard.vue";
import StudentProfileEditor from "../components/StudentProfileEditor.vue";
import { API_BASE } from "../api/request";
import { useNotifications } from "../composables/useNotifications";
import { searchStudentProfiles, getStudentProfileById } from "../api/profile";
import { resolveMediaUrl } from "../utils/media";
import { loadUser } from "../utils/userStorage";

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

function formatChangeValue(value) {
  if (value === null || value === undefined || value === "") return "-";
  if (Array.isArray(value)) return value.length ? value.join("、") : "-";
  return String(value);
}

function isAvatarChange(change) {
  return change?.label === "头像";
}

function resolveAvatarUrlForChange(value) {
  const url = formatChangeValue(value);
  if (!url || url === "-") return null;
  return resolveMediaUrl(url);
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

            <!-- Rejection Reason -->
            <div v-if="selectedEntry.reason" class="notif-reason">
              <div class="notif-section-label">驳回理由</div>
              <p class="notif-reason-text">{{ selectedEntry.reason }}</p>
            </div>

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
                        <template v-if="isAvatarChange(change)">
                          <img
                            v-if="resolveAvatarUrlForChange(change.before)"
                            class="notif-change-avatar"
                            :src="resolveAvatarUrlForChange(change.before)"
                            alt="修改前头像"
                          />
                          <div v-else class="notif-change-val">-</div>
                        </template>
                        <template v-else-if="isStructuredChangeValue(change.before)">
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
                        <template v-if="isAvatarChange(change)">
                          <img
                            v-if="resolveAvatarUrlForChange(change.after)"
                            class="notif-change-avatar"
                            :src="resolveAvatarUrlForChange(change.after)"
                            alt="修改后头像"
                          />
                          <div v-else class="notif-change-val">-</div>
                        </template>
                        <template v-else-if="isStructuredChangeValue(change.after)">
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
@import '../assets/styles/notifications-view.css';
</style>