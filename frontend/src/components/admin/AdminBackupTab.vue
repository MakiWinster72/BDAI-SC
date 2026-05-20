<script setup>
defineProps({
  backupForm: {
    type: Object,
    required: true,
  },
  backupLoading: {
    type: Boolean,
    default: false,
  },
  restoreLoading: {
    type: Boolean,
    default: false,
  },
  storageData: {
    type: Object,
    default: null,
  },
  storageLoading: {
    type: Boolean,
    default: false,
  },
  storageError: {
    type: String,
    default: "",
  },
  storageDeleting: {
    type: Object,
    required: true,
  },
  storageLabel: {
    type: Function,
    required: true,
  },
  barWidth: {
    type: Function,
    required: true,
  },
});

const emit = defineEmits([
  "backup-db",
  "backup-zip",
  "sql-file-change",
  "zip-file-change",
  "restore-db",
  "restore-attachments",
  "refresh-storage",
  "delete-storage",
]);
</script>

<template>
  <div class="backup-panel">
    <div class="admin-card backup-card">
      <div class="card-header">
        <div class="card-header-icon backup-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" d="M4 7v10c0 2.21 3.582 4 8 4s8-1.79 8-4V7M4 7c0 2.21 3.582 4 8 4s8-1.79 8-4M4 7c0-2.21 3.582-4 8-4s8 1.79 8 4m0 5c0 2.21-3.582 4-8 4s-8-1.79-8-4" />
          </svg>
        </div>
        <div>
          <div class="card-kicker">数据安全</div>
          <h2 class="card-title">备份</h2>
        </div>
      </div>
      <div class="card-body backup-card-body">
        <p class="backup-desc">导出 SQL 文件，包含用户、成就、审核策略等全部业务数据。</p>
        <p class="backup-desc">导出 ZIP 压缩包，包含学生荣誉中上传的所有附件文件。</p>
        <div class="backup-two-btns">
          <button
            class="btn btn-primary backup-btn"
            :disabled="backupLoading"
            type="button"
            @click="emit('backup-db')"
          >
            <svg v-if="backupLoading" class="btn-spinner" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
              <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83" stroke-linecap="round" />
            </svg>
            <svg v-else class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
              <path stroke-linecap="round" stroke-linejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
            </svg>
            {{ backupLoading ? "生成中…" : "导出 SQL 文件" }}
          </button>
          <button
            class="btn btn-primary backup-btn"
            :disabled="backupLoading"
            type="button"
            @click="emit('backup-zip')"
          >
            <svg v-if="backupLoading" class="btn-spinner" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
              <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83" stroke-linecap="round" />
            </svg>
            <svg v-else class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
              <path stroke-linecap="round" stroke-linejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
            </svg>
            {{ backupLoading ? "打包中…" : "导出 ZIP 文件" }}
          </button>
        </div>
      </div>
    </div>

    <div class="admin-card restore-card">
      <div class="card-header">
        <div class="card-header-icon restore-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
          </svg>
        </div>
        <div>
          <div class="card-kicker">数据安全</div>
          <h2 class="card-title">恢复</h2>
        </div>
      </div>
      <div class="card-body restore-card-body">
        <div class="restore-warning">
          <svg class="warning-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-2.694-.833-3.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z" />
          </svg>
          <span>数据库恢复为全覆盖，会覆盖现有数据；附件恢复为增量补充，缺失文件补回，多余文件保留。</span>
        </div>
        <div class="restore-two-items">
          <div class="restore-item">
            <div class="restore-item-label">恢复数据库</div>
            <div class="restore-item-row">
              <div class="file-input-wrap">
                <input
                  id="sql-file"
                  type="file"
                  accept=".sql"
                  class="file-input"
                  aria-label="选择 SQL 备份文件"
                  @change="emit('sql-file-change', $event)"
                />
                <label for="sql-file" class="file-label">
                  <svg class="file-label-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                  </svg>
                  <span class="file-label-text">{{ backupForm.sqlFile ? backupForm.sqlFile.name : "选择 .sql 文件" }}</span>
                </label>
              </div>
              <button
                class="btn btn-danger restore-btn"
                :disabled="restoreLoading || !backupForm.sqlFile"
                type="button"
                @click="emit('restore-db')"
              >
                <svg v-if="restoreLoading" class="btn-spinner" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                  <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83" stroke-linecap="round" />
                </svg>
                {{ restoreLoading ? "恢复中…" : "恢复" }}
              </button>
            </div>
          </div>

          <div class="restore-item">
            <div class="restore-item-label">恢复附件</div>
            <div class="restore-item-row">
              <div class="file-input-wrap">
                <input
                  id="zip-file"
                  type="file"
                  accept=".zip"
                  class="file-input"
                  aria-label="选择附件压缩包"
                  @change="emit('zip-file-change', $event)"
                />
                <label for="zip-file" class="file-label">
                  <svg class="file-label-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M5 8h14M5 8a2 2 0 110-4h14a2 2 0 110 4M5 8v10a2 2 0 002 2h10a2 2 0 002-2V8m-9 4h4" />
                  </svg>
                  <span class="file-label-text">{{ backupForm.zipFile ? backupForm.zipFile.name : "选择 zip 文件" }}</span>
                </label>
              </div>
              <button
                class="btn btn-danger restore-btn"
                :disabled="restoreLoading || !backupForm.zipFile"
                type="button"
                @click="emit('restore-attachments')"
              >
                <svg v-if="restoreLoading" class="btn-spinner" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                  <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83" stroke-linecap="round" />
                </svg>
                {{ restoreLoading ? "恢复中…" : "恢复" }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="admin-card storage-card">
      <div class="card-header">
        <div class="card-header-icon storage-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" d="M4 7v10c0 2.21 3.582 4 8 4s8-1.79 8-4V7M4 7c0 2.21 3.582 4 8 4s8-1.79 8-4M4 7c0-2.21 3.582-4 8-4s8 1.79 8 4" />
            <path stroke-linecap="round" stroke-linejoin="round" d="M9 17v1m3-2v2m3-3v3" />
          </svg>
        </div>
        <div>
          <div class="card-kicker">存储分析</div>
          <h2 class="card-title">用户附件占用</h2>
        </div>
        <button
          class="btn btn-ghost storage-refresh-btn"
          type="button"
          :disabled="storageLoading"
          @click="emit('refresh-storage')"
        >
          <svg v-if="storageLoading" class="btn-spinner" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
            <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83" stroke-linecap="round" />
          </svg>
          <svg v-else class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
          </svg>
          刷新
        </button>
      </div>
      <div class="card-body storage-card-body">
        <div v-if="storageData" class="storage-summary">
          <div class="storage-summary-item">
            <span class="storage-summary-label">总占用</span>
            <span class="storage-summary-value">{{ storageData.totalFormatted }}</span>
          </div>
          <div class="storage-summary-item">
            <span class="storage-summary-label">用户数</span>
            <span class="storage-summary-value">{{ storageData.totalUsers }} 个</span>
          </div>
        </div>

        <div v-if="storageLoading" class="storage-center-state">
          <svg class="loading-spinner" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
            <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83" stroke-linecap="round" />
          </svg>
          <span>扫描中…</span>
        </div>
        <div v-else-if="storageError" class="storage-center-state">
          <span>{{ storageError }}</span>
        </div>
        <div v-else-if="!storageData || !storageData.entries.length" class="storage-center-state">
          <span>暂无数据，点击“刷新”开始扫描</span>
        </div>
        <div v-else class="storage-chart">
          <div
            v-for="(item, idx) in storageData.entries"
            :key="item.userId"
            class="storage-bar-row"
            :class="{ 'storage-bar-row--odd': idx % 2 === 1 }"
          >
            <div class="storage-bar-label">
              <span class="storage-bar-displayname">{{ storageLabel(item) }}</span>
              <span v-if="item.displayName" class="storage-bar-username">{{ item.username }}</span>
            </div>
            <div class="storage-bar-track-wrap">
              <div
                class="storage-bar-fill"
                :style="{ width: barWidth(item.sizeBytes, storageData.entries[0].sizeBytes) }"
              ></div>
            </div>
            <span class="storage-bar-size">{{ item.sizeFormatted }}</span>
            <button
              class="storage-delete-btn"
              type="button"
              :disabled="storageDeleting.has(item.userId)"
              :aria-label="'删除' + storageLabel(item) + '的附件'"
              @click="emit('delete-storage', item)"
            >
              <svg v-if="storageDeleting.has(item.userId)" class="btn-spinner" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83" stroke-linecap="round" />
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                <path stroke-linecap="round" stroke-linejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
