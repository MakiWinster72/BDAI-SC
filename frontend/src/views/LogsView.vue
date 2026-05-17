<script setup>
import { computed, onMounted, shallowRef, watch } from "vue";
import PaginationBar from "../components/PaginationBar.vue";
import MobileCapsule from "../components/MobileCapsule.vue";
import { getAuditLogs } from "../api/auditLog";
import { useDashboardShell } from "../composables/useDashboardShell";

const { openSidebar: openDashboardSidebar } = useDashboardShell();

const ACTION_LABELS = {
  BACKUP_DB: "导出数据库",
  RESTORE_DB: "恢复数据库",
  BACKUP_ATTACHMENTS: "导出附件",
  RESTORE_ATTACHMENTS: "恢复附件",
  DELETE_STORAGE: "删除附件",
  EXPORT_STUDENTS: "导出学生信息",
  APPROVE_ACHIEVEMENT: "通过成就审核",
  REJECT_ACHIEVEMENT: "驳回成就审核",
  APPROVE_PROFILE: "通过信息审核",
  REJECT_PROFILE: "驳回信息审核",
};

const auditLogs = shallowRef([]);
const auditLogsLoading = shallowRef(false);
const auditLogsError = shallowRef("");
const auditLogSearch = shallowRef("");
const auditLogCurrentPage = shallowRef(1);
const auditLogPageSize = shallowRef(20);
const auditLogTotal = shallowRef(0);
const auditLogPages = computed(() => Math.ceil(auditLogTotal.value / auditLogPageSize.value));

function formatLogTime(isoString) {
  if (!isoString) return "—";
  return isoString.replace("T", " ").substring(0, 19);
}

async function loadAuditLogs(page = auditLogCurrentPage.value) {
  auditLogsLoading.value = true;
  auditLogsError.value = "";
  try {
    const res = await getAuditLogs({
      page,
      size: auditLogPageSize.value,
      search: auditLogSearch.value.trim() || undefined,
    });
    const payload = res.data;
    auditLogs.value = payload.data || [];
    auditLogTotal.value = payload.total || 0;
    auditLogCurrentPage.value = payload.page || page;
  } catch (e) {
    auditLogsError.value = "加载操作日志失败";
  } finally {
    auditLogsLoading.value = false;
  }
}

onMounted(() => {
  loadAuditLogs();
});

watch(auditLogSearch, () => {
  auditLogCurrentPage.value = 1;
  loadAuditLogs(1);
});
</script>

<template>
  <main class="dashboard-right">
    <MobileCapsule @open-sidebar="openDashboardSidebar" />

    <header class="feed-header">
      <h1 class="feed-title">操作日志</h1>
    </header>

    <div class="admin-panel-single">
      <div class="card admin-card users-card">
        <div class="users-card-header">
          <div class="users-card-meta">
            <div class="users-card-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" aria-hidden="true">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <div class="users-card-titles">
              <div class="card-kicker">安全审计</div>
              <h2 class="card-title">操作日志</h2>
            </div>
          </div>
          <div class="users-count-badge">
            <span class="count-num">{{ auditLogTotal }}</span>
            <span class="count-label">条记录</span>
          </div>
        </div>

        <div class="users-toolbar">
          <div class="search-wrap">
            <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
              <path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
            <input
              v-model="auditLogSearch"
              class="search-input"
              type="text"
              placeholder="搜索用户名、操作、详情…"
              aria-label="搜索操作日志"
            />
          </div>
        </div>

        <div class="users-content">
          <div v-if="auditLogsLoading" class="users-center-state">
            <svg class="loading-spinner" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
              <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83" stroke-linecap="round" />
            </svg>
            <span>加载中…</span>
          </div>
          <div v-else-if="auditLogsError" class="users-center-state">
            <svg class="state-icon error" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-2.694-.833-3.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z" />
            </svg>
            <span>{{ auditLogsError }}</span>
          </div>
          <div v-else-if="auditLogs.length === 0" class="users-center-state">
            <svg class="state-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" aria-hidden="true">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <span>暂无操作记录</span>
          </div>
          <div v-else class="table-wrap logs-table-wrap">
            <table class="users-table" aria-label="操作日志列表">
              <thead>
                <tr>
                  <th scope="col">时间</th>
                  <th scope="col">用户</th>
                  <th scope="col">操作</th>
                  <th scope="col">详情</th>
                  <th scope="col">IP</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="entry in auditLogs" :key="entry.id" class="user-row">
                  <td class="td-mono">{{ formatLogTime(entry.createdAt) }}</td>
                  <td>{{ entry.displayName || entry.username }}</td>
                  <td>
                    <span class="role-chip role-admin">
                      {{ ACTION_LABELS[entry.action] || entry.action }}
                    </span>
                  </td>
                  <td>{{ entry.detail }}</td>
                  <td class="td-mono">{{ entry.ipAddress || '—' }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <PaginationBar
          v-if="auditLogPages > 1"
          v-model:current-page="auditLogCurrentPage"
          :total-pages="auditLogPages"
          mode="simple"
          @update:current-page="loadAuditLogs"
        />
      </div>
    </div>
  </main>
</template>

<style scoped>
@import '../assets/styles/admin-view.css';

/* Keep user column visible on small screens */
@media (max-width: 700px) {
  .logs-table-wrap .users-table th:nth-child(2),
  .logs-table-wrap .user-row td:nth-child(2) {
    display: table-cell;
  }
}
</style>
