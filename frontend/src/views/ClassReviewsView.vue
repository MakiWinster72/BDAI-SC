<template>
  <main class="dashboard-right notif-view">
    <header class="feed-header">
      <h1 class="feed-title">班级审核</h1>
    </header>

    <div v-if="!profile.className" class="notif-empty">
      <p class="notif-empty-title">您还未填写班级</p>
      <p class="notif-empty-sub">请先在「我的信息」中填写班级信息</p>
    </div>

    <div v-else-if="!selectedEntry" class="notif-empty">
      <p class="notif-empty-title">暂无选择</p>
      <p class="notif-empty-sub">从左侧列表选择一条申请查看详情</p>
    </div>

    <section v-else class="notif-detail">
      <div class="notif-detail-inner">
        <!-- Top Bar -->
        <div class="notif-detail-top">
          <div class="notif-detail-badges">
            <span class="notif-badge" :class="selectedEntry.resourceType === 'achievement' ? 'is-achievement' : 'is-profile'">
              {{ selectedEntry.resourceType === 'achievement' ? '成就' : '信息' }}审核
            </span>
            <span class="notif-badge is-pending">待处理</span>
            <time class="notif-time">{{ formatTime(selectedEntry.updatedAt) }}</time>
          </div>
          <div class="notif-detail-actions">
            <button class="notif-btn is-approve" type="button" @click="approveEntry">通过</button>
            <button class="notif-btn is-reject" type="button" @click="toggleRejectEditor">驳回</button>
          </div>
        </div>

        <!-- Title & Content -->
        <h2 class="notif-title">{{ selectedEntry.title }}</h2>
        <p class="notif-content">
          申请人：{{ selectedEntry.requester?.displayName || selectedEntry.requester?.username }}
          <br>
          学号：{{ selectedEntry.requester?.studentNo || '无' }}
        </p>
        <p v-if="selectedEntry.summary" class="notif-content">
          {{ selectedEntry.summary }}
        </p>

        <!-- Changes -->
        <template v-if="selectedEntry.changes?.length">
          <div class="notif-section-label">变更内容</div>
          <div class="notif-change-list">
            <article
              v-for="(change, idx) in selectedEntry.changes"
              :key="idx"
              class="notif-change-item"
            >
              <div class="notif-change-head">
                <span class="notif-change-label">{{ change.label }}</span>
                <span v-if="change.section" class="notif-change-section">{{ change.section }}</span>
              </div>
              <div class="notif-change-values">
                <div class="notif-change-col">
                  <div class="notif-change-cap">修改前</div>
                  <div class="notif-change-val">{{ formatChangeValue(change.before) }}</div>
                </div>
                <div class="notif-change-col is-next">
                  <div class="notif-change-cap">修改后</div>
                  <div class="notif-change-val is-next">{{ formatChangeValue(change.after) }}</div>
                </div>
              </div>
            </article>
          </div>
        </template>
      </div>
    </section>

    <!-- Reject Modal -->
    <Teleport to="body">
      <div :class="['sheet-overlay', { open: rejectOpen }]" @click.self="closeReject">
        <div class="sheet-modal reject-modal">
          <header class="reject-modal-header">
            <div class="reject-modal-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-2.694-.833-3.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z"/>
              </svg>
            </div>
            <div class="reject-modal-title-group">
              <h2 class="reject-modal-title">驳回申请</h2>
              <p class="reject-modal-subtitle">请填写驳回理由</p>
            </div>
            <button class="reject-modal-close" type="button" @click="closeReject">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
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
              placeholder="请输入驳回原因"
            />
          </div>
          <div class="reject-modal-actions">
            <button class="reject-btn-cancel" type="button" @click="closeReject">返回</button>
            <button class="reject-btn-confirm" type="button" @click="confirmReject" :disabled="!rejectReason.trim()">确认驳回</button>
          </div>
        </div>
      </div>
    </Teleport>
  </main>
</template>

<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { useToast } from "../composables/useToast";
import { listAchievementReviewRequests, approveAchievementReviewRequest, rejectAchievementReviewRequest } from "../api/achievementReviewRequests";
import { listProfileReviewRequests, approveProfileReviewRequest, rejectProfileReviewRequest } from "../api/profileReviewRequests";
import { loadUser } from "../utils/userStorage";

const { success: toastSuccess, error: toastError } = useToast();
const route = useRoute();
const profile = loadUser();
const allRequests = ref([]);
const selectedEntry = ref(null);
const rejectOpen = ref(false);
const rejectReason = ref("");

onMounted(async () => {
  await fetchRequests();
});

watch(() => route.query.entry, (entryId) => {
  if (entryId) {
    const [id, type] = String(entryId).split('-');
    selectedEntry.value = allRequests.value.find(r =>
      String(r.id) === String(id) && r.resourceType === type
    ) || null;
  } else {
    selectedEntry.value = null;
  }
}, { immediate: true });

async function fetchRequests() {
  try {
    const [achRes, profRes] = await Promise.all([
      listAchievementReviewRequests(),
      listProfileReviewRequests(),
    ]);
    const achList = (achRes.data || []).map(r => ({ ...r, resourceType: 'achievement' }));
    const profList = (profRes.data || []).map(r => ({ ...r, resourceType: 'profile' }));
    allRequests.value = [...achList, ...profList];

    // Re-select if entry query exists
    if (route.query.entry) {
      const [id, type] = String(route.query.entry).split('-');
      selectedEntry.value = allRequests.value.find(r =>
        String(r.id) === String(id) && r.resourceType === type
      ) || null;
    }
  } catch (e) {
    console.error(e);
  }
}

async function approveEntry() {
  if (!selectedEntry.value) return;
  try {
    if (selectedEntry.value.resourceType === 'achievement') {
      await approveAchievementReviewRequest(selectedEntry.value.id);
    } else {
      await approveProfileReviewRequest(selectedEntry.value.id);
    }
    toastSuccess("审核已通过");
    await fetchRequests();
  } catch (e) {
    toastError("操作失败，请刷新重试");
  }
}

function toggleRejectEditor() {
  rejectReason.value = "";
  rejectOpen.value = true;
}

function closeReject() {
  rejectOpen.value = false;
}

async function confirmReject() {
  if (!rejectReason.value.trim() || !selectedEntry.value) return;
  try {
    if (selectedEntry.value.resourceType === 'achievement') {
      await rejectAchievementReviewRequest(selectedEntry.value.id, { reason: rejectReason.value.trim() });
    } else {
      await rejectProfileReviewRequest(selectedEntry.value.id, { reason: rejectReason.value.trim() });
    }
    toastSuccess("已驳回");
    closeReject();
    await fetchRequests();
  } catch (e) {
    toastError("操作失败，请刷新重试");
  }
}

function formatTime(dateStr) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
}

function formatChangeValue(value) {
  if (value === null || value === undefined) return '-';
  if (typeof value === 'boolean') return value ? '是' : '否';
  if (typeof value === 'string' && value.trim() === '') return '-';
  return String(value);
}
</script>

<style scoped>
@import '../assets/styles/notifications-view.css';

.notif-badge.is-achievement {
  background: #e3f2fd;
  color: #1565c0;
}

.notif-badge.is-profile {
  background: #e8f5e9;
  color: #2e7d32;
}
</style>
