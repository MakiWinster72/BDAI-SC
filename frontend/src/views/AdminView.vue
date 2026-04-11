<script setup>
import { computed, onMounted, reactive, shallowRef, watch } from "vue";
import { useAchievementUploadSettings } from "../composables/useAchievementUploadSettings";
import { useReviewSettings } from "../composables/useReviewSettings";
import { getUserList, updateUser, deleteUser, downloadBackupDb, restoreBackupDb } from "../api/admin";

const ATTACHMENT_TYPE_OPTIONS = [
  { key: "document", label: "文档", icon: "/assets/icons/doc.svg" },
  { key: "video", label: "视频", icon: "/assets/icons/video.svg" },
  { key: "image", label: "图片", icon: "/assets/icons/image.svg" },
  { key: "archive", label: "压缩包", icon: "/assets/icons/zip.svg" },
];

const profile = reactive(loadUser());
const activeSection = shallowRef("upload");

// Backup & Restore
const backupForm = reactive({
  backupDb: true,
  backupAttachments: false,
  sqlFile: null,
  zipFile: null,
  restoreDb: true,
  restoreAttachments: false,
});
const backupLoading = shallowRef(false);
const backupMessage = shallowRef({ type: "", text: "" });
const restoreLoading = shallowRef(false);
const restoreMessage = shallowRef({ type: "", text: "" });

async function handleBackupDownload() {
  backupLoading.value = true;
  backupMessage.value = { type: "", text: "" };
  try {
    const res = await downloadBackupDb();
    if (!res.ok) {
      const errData = await res.json().catch(() => ({}));
      backupMessage.value = { type: "error", text: errData.message || "备份失败" };
      return;
    }
    const blob = await res.blob();
    const filename = res.headers.get("Content-Disposition")?.match(/filename="(.+)"/)?.[1] || "gcsc_backup.sql";
    const serverPath = res.headers.get("X-Backup-Path") || "";
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = filename;
    a.click();
    URL.revokeObjectURL(url);
    backupMessage.value = {
      type: "success",
      text: serverPath
        ? "SQL 文件已下载，已保存至：" + serverPath
        : "SQL 文件已下载",
    };
  } catch (e) {
    backupMessage.value = { type: "error", text: "备份失败，请检查服务端 mysqldump 是否可用" };
  } finally {
    backupLoading.value = false;
  }
}

async function handleRestore() {
  if (!backupForm.sqlFile) {
    restoreMessage.value = { type: "error", text: "请先选择 SQL 备份文件" };
    return;
  }
  restoreLoading.value = true;
  restoreMessage.value = { type: "", text: "" };
  try {
    const res = await restoreBackupDb(backupForm.sqlFile);
    const data = await res.json().catch(() => ({}));
    if (!res.ok) {
      restoreMessage.value = { type: "error", text: data.message || "恢复失败" };
      return;
    }
    restoreMessage.value = { type: "success", text: "数据库恢复成功" };
    backupForm.sqlFile = null;
    // reset file input
    const input = document.getElementById("sql-file");
    if (input) input.value = "";
  } catch (e) {
    restoreMessage.value = { type: "error", text: "恢复失败，请检查服务端 mysql 是否可用" };
  } finally {
    restoreLoading.value = false;
  }
}

async function handleRestoreAttachments() {
  if (!backupForm.zipFile) {
    restoreMessage.value = { type: "error", text: "请先选择附件压缩包" };
    return;
  }
  restoreLoading.value = true;
  restoreMessage.value = { type: "", text: "" };
  try {
    restoreMessage.value = { type: "info", text: "附件恢复功能待后端实现" };
  } finally {
    restoreLoading.value = false;
  }
}

function onSqlFileChange(e) {
  const file = e.target.files?.[0];
  if (!file) return;
  if (!file.name.endsWith(".sql")) {
    restoreMessage.value = { type: "error", text: "请选择 .sql 格式的备份文件" };
    backupForm.sqlFile = null;
    return;
  }
  backupForm.sqlFile = file;
  restoreMessage.value = { type: "", text: "" };
}

function onZipFileChange(e) {
  const file = e.target.files?.[0];
  if (!file) return;
  const ext = file.name.split(".").pop()?.toLowerCase();
  if (!["zip", "rar", "7z"].includes(ext)) {
    restoreMessage.value = { type: "error", text: "请选择 zip 格式的压缩文件" };
    backupForm.zipFile = null;
    return;
  }
  backupForm.zipFile = file;
}
const saveMessage = shallowRef("");
const sectionKey = shallowRef(0); // force section re-render for transition
const users = shallowRef([]);
const usersLoading = shallowRef(false);
const usersError = shallowRef("");
const userSearch = shallowRef("");
const userRoleFilter = shallowRef("");
const userCurrentPage = shallowRef(1);
const userPageSize = shallowRef(20);
const userTotal = shallowRef(0);
const userPages = computed(() => Math.ceil(userTotal.value / userPageSize.value));
const {
  settings,
  loading: uploadLoading,
  saving: uploadSaving,
  errorMessage: uploadErrorMessage,
  fetchSettings: fetchUploadSettings,
  saveSettings: saveUploadSettings,
} = useAchievementUploadSettings();
const {
  settings: reviewSettings,
  loading: reviewLoading,
  saving: reviewSaving,
  errorMessage: reviewErrorMessage,
  fetchSettings: fetchReviewSettings,
  saveSettings: saveReviewSettings,
} = useReviewSettings();

const form = reactive({
  imageMaxCount: settings.imageMaxCount,
  imageMaxSizeMb: settings.imageMaxSizeMb,
  attachmentMaxCount: settings.attachmentMaxCount,
  attachmentMaxSizeMb: settings.attachmentMaxSizeMb,
  attachmentDocumentExts: settings.attachmentDocumentExts,
  attachmentVideoExts: settings.attachmentVideoExts,
  attachmentImageExts: settings.attachmentImageExts,
  attachmentArchiveExts: settings.attachmentArchiveExts,
});
const reviewForm = reactive({
  profileReviewEnabled: reviewSettings.profileReviewEnabled,
  profileReviewAutoApprove: reviewSettings.profileReviewAutoApprove,
  achievementReviewEnabled: reviewSettings.achievementReviewEnabled,
  achievementReviewAutoApprove: reviewSettings.achievementReviewAutoApprove,
});

const imageSubtitle = computed(
  () => `最多 ${form.imageMaxCount} 张 · 单张不超过 ${form.imageMaxSizeMb}MB`,
);
const attachmentSubtitle = computed(
  () => `最多 ${form.attachmentMaxCount} 个 · 单个不超过 ${form.attachmentMaxSizeMb}MB`,
);
const enabledPreviewTypes = computed(() =>
  ATTACHMENT_TYPE_OPTIONS.map((item) => ({
    ...item,
    exts: parseExts(form[extFieldKey(item.key)]),
  })).filter((item) => item.exts.length),
);
const attachmentTypeSummary = computed(() =>
  enabledPreviewTypes.value.length
    ? enabledPreviewTypes.value.map((item) => item.label).join(" / ")
    : "暂无可用附件类型",
);
const activeLoading = computed(() =>
  activeSection.value === "upload" ? uploadLoading.value : reviewLoading.value,
);
const activeSaving = computed(() =>
  activeSection.value === "upload" ? uploadSaving.value : reviewSaving.value,
);
const activeErrorMessage = computed(() =>
  activeSection.value === "upload"
    ? uploadErrorMessage.value
    : reviewErrorMessage.value,
);

function extFieldKey(typeKey) {
  if (typeKey === "document") return "attachmentDocumentExts";
  if (typeKey === "video") return "attachmentVideoExts";
  if (typeKey === "image") return "attachmentImageExts";
  return "attachmentArchiveExts";
}

function parseExts(value) {
  return (value || "")
    .split(",")
    .map((item) => item.trim().toLowerCase().replace(/^\./, ""))
    .filter(Boolean);
}

async function loadPage() {
  await Promise.all([fetchUploadSettings(), fetchReviewSettings()]);
  syncFormFromSettings();
  syncReviewFormFromSettings();
}

function syncFormFromSettings() {
  form.imageMaxCount = settings.imageMaxCount;
  form.imageMaxSizeMb = settings.imageMaxSizeMb;
  form.attachmentMaxCount = settings.attachmentMaxCount;
  form.attachmentMaxSizeMb = settings.attachmentMaxSizeMb;
  form.attachmentDocumentExts = settings.attachmentDocumentExts;
  form.attachmentVideoExts = settings.attachmentVideoExts;
  form.attachmentImageExts = settings.attachmentImageExts;
  form.attachmentArchiveExts = settings.attachmentArchiveExts;
}

function syncReviewFormFromSettings() {
  reviewForm.profileReviewEnabled = reviewSettings.profileReviewEnabled;
  reviewForm.profileReviewAutoApprove = reviewSettings.profileReviewAutoApprove;
  reviewForm.achievementReviewEnabled = reviewSettings.achievementReviewEnabled;
  reviewForm.achievementReviewAutoApprove =
    reviewSettings.achievementReviewAutoApprove;
}

async function handleSubmit() {
  saveMessage.value = "";
  const result = await saveUploadSettings({
    imageMaxCount: Number(form.imageMaxCount),
    imageMaxSizeMb: Number(form.imageMaxSizeMb),
    attachmentMaxCount: Number(form.attachmentMaxCount),
    attachmentMaxSizeMb: Number(form.attachmentMaxSizeMb),
    attachmentDocumentExts: form.attachmentDocumentExts,
    attachmentVideoExts: form.attachmentVideoExts,
    attachmentImageExts: form.attachmentImageExts,
    attachmentArchiveExts: form.attachmentArchiveExts,
  });
  if (result.success) {
    saveMessage.value = "上传限制已更新，成就页面会同步显示。";
    syncFormFromSettings();
  }
}

async function handleReviewSubmit() {
  saveMessage.value = "";
  const result = await saveReviewSettings({
    profileReviewEnabled: Boolean(reviewForm.profileReviewEnabled),
    profileReviewAutoApprove: reviewForm.profileReviewEnabled
      ? Boolean(reviewForm.profileReviewAutoApprove)
      : false,
    achievementReviewEnabled: Boolean(reviewForm.achievementReviewEnabled),
    achievementReviewAutoApprove: reviewForm.achievementReviewEnabled
      ? Boolean(reviewForm.achievementReviewAutoApprove)
      : false,
  });
  if (result.success) {
    saveMessage.value = "审核设置已更新，前台提交行为会按新规则执行。";
    syncReviewFormFromSettings();
  }
}

function switchSection(sectionKey_) {
  activeSection.value = sectionKey_;
  saveMessage.value = "";
}

function loadUser() {
  try {
    const raw = JSON.parse(localStorage.getItem("gcsc_user") || "{}");
    return {
      username: raw.username || "",
      displayName: raw.displayName || "",
      role: raw.role || "STUDENT",
    };
  } catch {
    return {
      username: "",
      displayName: "",
      role: "STUDENT",
    };
  }
}

// User management
const ROLE_LABELS = {
  ADMIN: "管理员",
  TEACHER: "教师",
  STUDENT: "学生",
};
const ROLE_OPTIONS = [
  { value: "STUDENT", label: "学生" },
  { value: "TEACHER", label: "教师" },
  { value: "ADMIN", label: "管理员" },
];

function getRoleLabel(role) {
  return ROLE_LABELS[role] || role;
}

async function loadUsers(page = userCurrentPage.value) {
  usersLoading.value = true;
  usersError.value = "";
  try {
    const res = await getUserList({
      page,
      size: userPageSize.value,
      search: userSearch.value.trim() || undefined,
      role: userRoleFilter.value || undefined,
    });
    const payload = res.data;
    users.value = payload.data || [];
    userTotal.value = payload.total || 0;
    userCurrentPage.value = payload.page || page;
  } catch (e) {
    usersError.value = "加载用户列表失败";
  } finally {
    usersLoading.value = false;
  }
}

const editModal = reactive({
  visible: false,
  user: null,
  saving: false,
  error: "",
  form: {
    username: "",
    password: "",
    role: "",
  },
});

function openEditModal(user) {
  editModal.user = user;
  editModal.form.username = user.username;
  editModal.form.password = "";
  editModal.form.role = user.role;
  editModal.error = "";
  editModal.visible = true;
}

function closeEditModal() {
  editModal.visible = false;
  editModal.user = null;
}

async function handleUpdateUser() {
  editModal.saving = true;
  editModal.error = "";
  try {
    const data = {};
    if (editModal.form.username && editModal.form.username !== editModal.user.username) {
      data.username = editModal.form.username;
    }
    if (editModal.form.password) {
      data.password = editModal.form.password;
    }
    if (editModal.form.role !== editModal.user.role) {
      data.role = editModal.form.role;
    }
    if (Object.keys(data).length === 0) {
      closeEditModal();
      return;
    }
    const res = await updateUser(editModal.user.id, data);
    if (res.data.success === false) {
      editModal.error = res.data.message || "更新失败";
      return;
    }
    await loadUsers(userCurrentPage.value);
    closeEditModal();
  } catch (e) {
    editModal.error = e?.response?.data?.message || "更新失败";
  } finally {
    editModal.saving = false;
  }
}

async function handleDeleteUser(user) {
  if (!confirm(`确定要删除用户「${user.displayName}」吗？此操作不可恢复。`)) {
    return;
  }
  try {
    await deleteUser(user.id);
    await loadUsers();
  } catch (e) {
    alert(e?.response?.data?.message || "删除失败");
  }
}

onMounted(() => {
  loadPage();
  loadUsers();
});

// Reload from page 1 when filters change
watch([userSearch, userRoleFilter], () => {
  if (activeSection.value === "users") {
    userCurrentPage.value = 1;
    loadUsers(1);
  }
});
</script>

<template>
  <main class="admin-shell">
    <header class="admin-header">
      <h1 class="admin-title">后台管理</h1>
    </header>

    <!-- Category Tabs -->
    <nav class="admin-tabs" role="tablist" aria-label="管理功能分类">
      <button
        v-for="tab in [
          { key: 'upload', label: '上传限制', icon: 'M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z' },
          { key: 'review', label: '审核策略', icon: 'M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z' },
          { key: 'users', label: '用户管理', icon: 'M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z' },
          { key: 'backup', label: '备份与恢复', icon: 'M4 7v10c0 2.21 3.582 4 8 4s8-1.79 8-4V7M4 7c0 2.21 3.582 4 8 4s8-1.79 8-4M4 7c0-2.21 3.582-4 8-4s8 1.79 8 4m0 5c0 2.21-3.582 4-8 4s-8-1.79-8-4' },
        ]"
        :key="tab.key"
        class="admin-tab"
        :class="{ active: activeSection === tab.key }"
        role="tab"
        :aria-selected="activeSection === tab.key"
        type="button"
        @click="switchSection(tab.key)"
      >
        <svg class="tab-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" aria-hidden="true">
          <path stroke-linecap="round" stroke-linejoin="round" :d="tab.icon" />
        </svg>
        {{ tab.label }}
      </button>
    </nav>

    <!-- Section Content -->
    <Transition name="section-fade" mode="out-in">
      <div :key="activeSection" class="admin-content">

        <!-- Upload Section -->
        <div v-if="activeSection === 'upload'" class="admin-panel-grid">
          <!-- Form Card -->
          <div class="admin-card">
            <div class="card-header">
              <div class="card-header-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                  <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
              </div>
              <div>
                <div class="card-kicker">系统设置</div>
                <h2 class="card-title">成就页面上传限制</h2>
              </div>
            </div>
            <div class="card-body">

              <!-- Image Block -->
              <div class="setting-group">
                <div class="setting-group-label">
                  <span class="group-index">01</span>
                  <span class="group-title">图片设置</span>
                </div>
                <div class="field-row">
                  <div class="field-cell">
                    <label class="field-label" for="img-count">最多上传图片数</label>
                    <div class="input-wrap">
                      <input
                        id="img-count"
                        v-model.number="form.imageMaxCount"
                        class="text-input"
                        type="number"
                        min="1"
                        max="9"
                        aria-label="最多上传图片数"
                      />
                      <span class="input-unit">张</span>
                    </div>
                  </div>
                  <div class="field-cell">
                    <label class="field-label" for="img-size">单张图片最大大小</label>
                    <div class="input-wrap">
                      <input
                        id="img-size"
                        v-model.number="form.imageMaxSizeMb"
                        class="text-input"
                        type="number"
                        min="1"
                        max="200"
                        aria-label="单张图片最大大小 MB"
                      />
                      <span class="input-unit">MB</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Attachment Block -->
              <div class="setting-group">
                <div class="setting-group-label">
                  <span class="group-index">02</span>
                  <span class="group-title">附件设置</span>
                </div>
                <div class="field-row">
                  <div class="field-cell">
                    <label class="field-label" for="att-count">最多上传附件数</label>
                    <div class="input-wrap">
                      <input
                        id="att-count"
                        v-model.number="form.attachmentMaxCount"
                        class="text-input"
                        type="number"
                        min="1"
                        max="20"
                        aria-label="最多上传附件数"
                      />
                      <span class="input-unit">个</span>
                    </div>
                  </div>
                  <div class="field-cell">
                    <label class="field-label" for="att-size">单个附件最大大小</label>
                    <div class="input-wrap">
                      <input
                        id="att-size"
                        v-model.number="form.attachmentMaxSizeMb"
                        class="text-input"
                        type="number"
                        min="1"
                        max="200"
                        aria-label="单个附件最大大小 MB"
                      />
                      <span class="input-unit">MB</span>
                    </div>
                  </div>
                </div>

                <div class="ext-section">
                  <div class="ext-label-row">
                    <label class="field-label">支持的附件后缀</label>
                    <span class="field-hint">多个后缀用英文逗号隔开；留空则该类型不开放</span>
                  </div>
                  <div class="ext-grid">
                    <label
                      v-for="item in ATTACHMENT_TYPE_OPTIONS"
                      :key="item.key"
                      class="ext-card"
                    >
                      <div class="ext-card-head">
                        <img class="ext-icon" :src="item.icon" alt="" aria-hidden="true" />
                        <span class="ext-title">{{ item.label }}</span>
                      </div>
                      <input
                        v-model="form[extFieldKey(item.key)]"
                        class="ext-input"
                        type="text"
                        :aria-label="`${item.label}后缀`"
                        :placeholder="item.key === 'document'
                          ? 'docx, doc, pdf'
                          : item.key === 'video'
                            ? 'mp4, mov'
                            : item.key === 'image'
                              ? 'jpeg, jpg, png'
                              : 'zip, rar, 7z'"
                      />
                    </label>
                  </div>
                </div>
              </div>

              <!-- Feedback -->
              <Transition name="msg-fade">
                <div v-if="activeErrorMessage" class="msg-banner error" role="alert">
                  <svg class="msg-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-2.694-.833-3.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z" />
                  </svg>
                  {{ activeErrorMessage }}
                </div>
                <div v-else-if="saveMessage" class="msg-banner success" role="status">
                  <svg class="msg-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  {{ saveMessage }}
                </div>
              </Transition>

              <!-- Actions -->
              <div class="card-actions">
                <button
                  class="btn btn-ghost"
                  type="button"
                  @click="syncFormFromSettings"
                >
                  重置
                </button>
                <button
                  class="btn btn-primary"
                  type="button"
                  :disabled="activeSaving"
                  @click="handleSubmit"
                >
                  <svg v-if="activeSaving" class="btn-spinner" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                    <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83" stroke-linecap="round" />
                  </svg>
                  {{ activeSaving ? "保存中…" : "保存设置" }}
                </button>
              </div>
            </div>
          </div>

          <!-- Preview Card -->
          <div class="admin-card preview-card">
            <div class="card-header">
              <div class="card-header-icon preview-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  <path stroke-linecap="round" stroke-linejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                </svg>
              </div>
              <div>
                <div class="card-kicker">实时预览</div>
                <h2 class="card-title">用户侧展示效果</h2>
              </div>
            </div>
            <div class="card-body preview-shell">
              <!-- Image Preview -->
              <div class="preview-box" aria-hidden="true">
                <div class="preview-box-header">
                  <span class="preview-box-title">图片</span>
                  <span class="preview-box-sub">{{ imageSubtitle }}</span>
                </div>
                <div class="preview-empty">
                  <div class="preview-add-btn">+</div>
                  <span class="preview-empty-text">点击添加图片</span>
                </div>
              </div>
              <!-- Attachment Preview -->
              <div class="preview-box" aria-hidden="true">
                <div class="preview-box-header">
                  <span class="preview-box-title">附件</span>
                  <span class="preview-box-sub">{{ attachmentSubtitle }}</span>
                </div>
                <div class="format-list">
                  <div
                    v-for="item in enabledPreviewTypes"
                    :key="item.key"
                    class="format-chip"
                  >
                    <img class="format-chip-icon" :src="item.icon" alt="" aria-hidden="true" />
                    <span class="format-chip-label">{{ item.label }}</span>
                    <span class="format-chip-exts">{{ item.exts.slice(0, 3).join("/") }}</span>
                  </div>
                  <div v-if="!enabledPreviewTypes.length" class="preview-empty-text">
                    暂无可用附件类型
                  </div>
                </div>
                <div class="preview-tip">
                  {{ attachmentTypeSummary || "暂无可用附件类型" }} · 单个不超过 {{ form.attachmentMaxSizeMb }}MB
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Review Section -->
        <div v-else-if="activeSection === 'review'" class="admin-panel-single">
          <div class="admin-card">
            <div class="card-header">
              <div class="card-header-icon shield-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                </svg>
              </div>
              <div>
                <div class="card-kicker">审核入口</div>
                <h2 class="card-title">审核策略设置</h2>
              </div>
            </div>
            <div class="card-body toggle-list">

              <!-- Profile Review -->
              <div class="toggle-section">
                <div class="toggle-section-label">
                  <span class="group-index">01</span>
                  <span class="group-title">个人信息</span>
                </div>
                <div class="toggle-row" :class="{ muted: !reviewForm.profileReviewEnabled }">
                  <div class="toggle-copy">
                    <span class="toggle-title">开启个人信息审核</span>
                    <span class="toggle-hint">关闭后，学生提交个人信息会直接更新</span>
                  </div>
                  <label class="toggle-switch" :aria-label="`开启个人信息审核: ${reviewForm.profileReviewEnabled ? '已开启' : '已关闭'}`">
                    <input
                      v-model="reviewForm.profileReviewEnabled"
                      type="checkbox"
                      role="switch"
                      :aria-checked="reviewForm.profileReviewEnabled"
                    />
                    <span class="toggle-track">
                      <span class="toggle-thumb"></span>
                    </span>
                  </label>
                </div>
                <div class="toggle-row" :class="{ muted: !reviewForm.profileReviewEnabled }">
                  <div class="toggle-copy">
                    <span class="toggle-title">默认通过</span>
                    <span class="toggle-hint">开启审核但自动通过，保留审核流程入口与记录</span>
                  </div>
                  <label class="toggle-switch" :aria-label="`默认通过: ${reviewForm.profileReviewAutoApprove ? '已开启' : '已关闭'}`">
                    <input
                      v-model="reviewForm.profileReviewAutoApprove"
                      type="checkbox"
                      role="switch"
                      :aria-checked="reviewForm.profileReviewAutoApprove"
                      :disabled="!reviewForm.profileReviewEnabled"
                    />
                    <span class="toggle-track">
                      <span class="toggle-thumb"></span>
                    </span>
                  </label>
                </div>
              </div>

              <!-- Achievement Review -->
              <div class="toggle-section">
                <div class="toggle-section-label">
                  <span class="group-index">02</span>
                  <span class="group-title">成就</span>
                </div>
                <div class="toggle-row" :class="{ muted: !reviewForm.achievementReviewEnabled }">
                  <div class="toggle-copy">
                    <span class="toggle-title">开启成就审核</span>
                    <span class="toggle-hint">关闭后，新增和修改成就都会直接生效</span>
                  </div>
                  <label class="toggle-switch" :aria-label="`开启成就审核: ${reviewForm.achievementReviewEnabled ? '已开启' : '已关闭'}`">
                    <input
                      v-model="reviewForm.achievementReviewEnabled"
                      type="checkbox"
                      role="switch"
                      :aria-checked="reviewForm.achievementReviewEnabled"
                    />
                    <span class="toggle-track">
                      <span class="toggle-thumb"></span>
                    </span>
                  </label>
                </div>
                <div class="toggle-row" :class="{ muted: !reviewForm.achievementReviewEnabled }">
                  <div class="toggle-copy">
                    <span class="toggle-title">默认通过</span>
                    <span class="toggle-hint">开启审核但自动通过，适合先保留入口再平滑切换</span>
                  </div>
                  <label class="toggle-switch" :aria-label="`成就默认通过: ${reviewForm.achievementReviewAutoApprove ? '已开启' : '已关闭'}`">
                    <input
                      v-model="reviewForm.achievementReviewAutoApprove"
                      type="checkbox"
                      role="switch"
                      :aria-checked="reviewForm.achievementReviewAutoApprove"
                      :disabled="!reviewForm.achievementReviewEnabled"
                    />
                    <span class="toggle-track">
                      <span class="toggle-thumb"></span>
                    </span>
                  </label>
                </div>
              </div>

              <!-- Feedback -->
              <Transition name="msg-fade">
                <div v-if="activeErrorMessage" class="msg-banner error" role="alert">
                  <svg class="msg-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-2.694-.833-3.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z" />
                  </svg>
                  {{ activeErrorMessage }}
                </div>
                <div v-else-if="saveMessage" class="msg-banner success" role="status">
                  <svg class="msg-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  {{ saveMessage }}
                </div>
              </Transition>

              <!-- Actions -->
              <div class="card-actions">
                <button
                  class="btn btn-ghost"
                  type="button"
                  @click="syncReviewFormFromSettings"
                >
                  重置
                </button>
                <button
                  class="btn btn-primary"
                  type="button"
                  :disabled="activeSaving"
                  @click="handleReviewSubmit"
                >
                  <svg v-if="activeSaving" class="btn-spinner" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                    <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83" stroke-linecap="round" />
                  </svg>
                  {{ activeSaving ? "保存中…" : "保存设置" }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Users Section -->
        <div v-else-if="activeSection === 'users'" class="admin-panel-single">
          <div class="admin-card users-card">
            <!-- Card Header -->
            <div class="users-card-header">
              <div class="users-card-meta">
                <div class="users-card-icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" aria-hidden="true">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                  </svg>
                </div>
                <div class="users-card-titles">
                  <div class="card-kicker">用户管理</div>
                  <h2 class="card-title">系统用户列表</h2>
                </div>
              </div>
              <div class="users-count-badge">
                <span class="count-num">{{ userTotal }}</span>
                <span class="count-label">位用户</span>
              </div>
            </div>

            <!-- Toolbar -->
            <div class="users-toolbar">
              <div class="search-wrap">
                <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
                <input
                  v-model="userSearch"
                  class="search-input"
                  type="text"
                  placeholder="搜索用户名、姓名、学号…"
                  aria-label="搜索用户"
                />
              </div>
              <select v-model="userRoleFilter" class="filter-select" aria-label="按角色筛选">
                <option value="">全部角色</option>
                <option v-for="opt in ROLE_OPTIONS" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
              </select>
            </div>

            <!-- Table or States -->
            <div class="users-content">
              <div v-if="usersLoading" class="users-center-state">
                <svg class="loading-spinner" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                  <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83" stroke-linecap="round" />
                </svg>
                <span>加载中…</span>
              </div>
              <div v-else-if="usersError" class="users-center-state">
                <svg class="state-icon error" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-2.694-.833-3.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z" />
                </svg>
                <span>{{ usersError }}</span>
              </div>
              <div v-else-if="users.length === 0" class="users-center-state">
                <svg class="state-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" aria-hidden="true">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
                <span>暂无符合条件的用户</span>
              </div>
              <div v-else class="table-wrap">
                <table class="users-table" aria-label="用户列表">
                  <thead>
                    <tr>
                      <th scope="col">用户名</th>
                      <th scope="col">显示名称</th>
                      <th scope="col">角色</th>
                      <th scope="col">学号</th>
                      <th scope="col">班级</th>
                      <th scope="col" class="col-action"></th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="user in users" :key="user.id" class="user-row">
                      <td class="td-mono">{{ user.username }}</td>
                      <td>{{ user.displayName }}</td>
                      <td>
                        <span :class="['role-chip', 'role-' + user.role.toLowerCase()]">
                          {{ getRoleLabel(user.role) }}
                        </span>
                      </td>
                      <td class="td-mono">{{ user.studentNo || '—' }}</td>
                      <td>{{ user.className || '—' }}</td>
                      <td class="td-action">
                        <button class="icon-btn" @click.stop="openEditModal(user)" aria-label="编辑用户">
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                          </svg>
                        </button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <!-- Pagination -->
            <div v-if="userPages > 1" class="users-pagination">
              <button
                class="page-btn"
                :disabled="userCurrentPage <= 1"
                @click="loadUsers(userCurrentPage - 1)"
                aria-label="上一页"
              >
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7" />
                </svg>
              </button>
              <div class="page-info">
                <span class="page-current">{{ userCurrentPage }}</span>
                <span class="page-sep">/</span>
                <span class="page-total">{{ userPages }}</span>
              </div>
              <button
                class="page-btn"
                :disabled="userCurrentPage >= userPages"
                @click="loadUsers(userCurrentPage + 1)"
                aria-label="下一页"
              >
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7" />
                </svg>
              </button>
            </div>
          </div>
        </div>

        <!-- Backup & Restore Section -->
        <div v-else-if="activeSection === 'backup'" class="backup-panel">
          <!-- Backup Card -->
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
                  @click="() => { backupForm.backupDb = true; backupForm.backupAttachments = false; handleBackupDownload(); }"
                >
                  <svg v-if="backupLoading" class="btn-spinner" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                    <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83" stroke-linecap="round" />
                  </svg>
                  <svg v-else class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                  </svg>
                  {{ backupLoading && backupForm.backupDb ? "生成中…" : "导出 SQL 文件" }}
                </button>
                <button
                  class="btn btn-primary backup-btn"
                  :disabled="backupLoading"
                  type="button"
                  @click="() => { backupForm.backupDb = false; backupForm.backupAttachments = true; handleBackupDownload(); }"
                >
                  <svg v-if="backupLoading" class="btn-spinner" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                    <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83" stroke-linecap="round" />
                  </svg>
                  <svg v-else class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                  </svg>
                  {{ backupLoading && backupForm.backupAttachments ? "打包中…" : "导出 ZIP 文件" }}
                </button>
              </div>
              <Transition name="msg-fade">
                <div v-if="backupMessage.text" :class="['msg-banner', backupMessage.type || 'info']" role="status">
                  <svg class="msg-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  {{ backupMessage.text }}
                </div>
              </Transition>
            </div>
          </div>

          <!-- Restore Card -->
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
                <span>恢复操作会覆盖现有数据，建议提前备份。</span>
              </div>
              <div class="restore-two-items">
                <!-- Restore DB -->
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
                        @change="onSqlFileChange"
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
                      @click="handleRestore"
                    >
                      <svg v-if="restoreLoading" class="btn-spinner" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                        <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83" stroke-linecap="round" />
                      </svg>
                      {{ restoreLoading ? "恢复中…" : "恢复" }}
                    </button>
                  </div>
                </div>

                <!-- Restore Attachments -->
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
                        @change="onZipFileChange"
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
                      @click="handleRestoreAttachments"
                    >
                      <svg v-if="restoreLoading" class="btn-spinner" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                        <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83" stroke-linecap="round" />
                      </svg>
                      {{ restoreLoading ? "恢复中…" : "恢复" }}
                    </button>
                  </div>
                </div>
              </div>
              <Transition name="msg-fade">
                <div v-if="restoreMessage.text" :class="['msg-banner', restoreMessage.type || 'info']" role="alert">
                  <svg class="msg-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  {{ restoreMessage.text }}
                </div>
              </Transition>
            </div>
          </div>
        </div>

      </div>
    </Transition>

    <!-- Edit User Modal -->
    <Teleport to="body">
      <div :class="['sheet-overlay', { open: editModal.visible }]" @click.self="closeEditModal" role="dialog" aria-modal="true" aria-label="编辑用户">
        <div class="sheet-modal user-edit-modal" @click.stop>
          <div class="modal-top-bar">
            <div class="modal-title-group">
              <h3 class="modal-title">编辑用户</h3>
              <p class="modal-subtitle">正在编辑：{{ editModal.user?.displayName }}</p>
            </div>
            <button class="modal-close-btn" type="button" aria-label="关闭" @click="closeEditModal">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                <path d="M18 6L6 18M6 6l12 12"/>
              </svg>
            </button>
          </div>

          <div class="modal-body">
            <div class="modal-field">
              <label class="modal-label" for="edit-username">用户名</label>
              <input
                id="edit-username"
                v-model="editModal.form.username"
                class="modal-input"
                type="text"
                placeholder="留空则不修改"
              />
            </div>
            <div class="modal-field">
              <label class="modal-label" for="edit-password">密码</label>
              <input
                id="edit-password"
                v-model="editModal.form.password"
                class="modal-input"
                type="password"
                placeholder="留空则不修改"
              />
            </div>
            <div class="modal-field">
              <label class="modal-label" for="edit-role">角色</label>
              <select id="edit-role" v-model="editModal.form.role" class="modal-select">
                <option v-for="opt in ROLE_OPTIONS" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
              </select>
            </div>
            <Transition name="msg-fade">
              <div v-if="editModal.error" class="msg-banner error modal-error" role="alert">{{ editModal.error }}</div>
            </Transition>
          </div>

          <div class="modal-footer">
            <button class="btn btn-danger-ghost" type="button" @click="handleDeleteUser(editModal.user)">删除用户</button>
            <div class="modal-footer-btns">
              <button class="btn btn-ghost" type="button" @click="closeEditModal">取消</button>
              <button class="btn btn-primary" type="button" :disabled="editModal.saving" @click="handleUpdateUser">
                {{ editModal.saving ? "保存中…" : "保存" }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </main>
</template>

<style scoped>
/* ── Shell ─────────────────────────────────────────────── */
.admin-shell {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 0 0 4rem;
}

/* ── Header ────────────────────────────────────────────── */
.admin-header {
  padding: 8px 24px 0;
}

.admin-title {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  color: var(--text-main);
  letter-spacing: -0.3px;
}

/* ── Tab Bar ───────────────────────────────────────────── */
.admin-tabs {
  display: flex;
  gap: 4px;
  padding: 5px;
  margin: 0 20px;
  border-radius: 18px;
  background: rgba(100, 12, 114, 0.05);
  border: 1px solid var(--line);
  width: fit-content;
}

.admin-tab {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 9px 20px;
  border-radius: 13px;
  border: none;
  background: transparent;
  color: var(--text-sub);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition:
    background 200ms cubic-bezier(0.22, 1, 0.36, 1),
    color 200ms cubic-bezier(0.22, 1, 0.36, 1),
    box-shadow 200ms cubic-bezier(0.22, 1, 0.36, 1),
    transform 120ms ease;
  white-space: nowrap;
}

.tab-icon {
  width: 15px;
  height: 15px;
  opacity: 0.75;
  transition: opacity 200ms ease;
}

.admin-tab:hover:not(.active) {
  background: rgba(100, 12, 114, 0.08);
  color: var(--primary);
}

.admin-tab:hover:not(.active) .tab-icon {
  opacity: 1;
}

.admin-tab.active {
  background: var(--card);
  color: var(--primary-dark);
  font-weight: 700;
  box-shadow: 0 2px 12px rgba(100, 12, 114, 0.15), 0 1px 3px rgba(100, 12, 114, 0.1);
}

.admin-tab.active .tab-icon {
  opacity: 1;
  color: var(--primary);
}

.admin-tab:active {
  transform: scale(0.97);
}

@media (max-width: 600px) {
  .admin-tabs {
    width: calc(100% - 40px);
    margin: 0 20px;
    overflow-x: auto;
    scrollbar-width: none;
  }
  .admin-tabs::-webkit-scrollbar { display: none; }
  .admin-tab {
    flex: 1;
    justify-content: center;
    padding: 9px 14px;
    font-size: 13px;
  }
}

/* ── Section Transition ────────────────────────────────── */
.section-fade-enter-active {
  transition: opacity 300ms ease, transform 300ms cubic-bezier(0.22, 1, 0.36, 1);
}
.section-fade-leave-active {
  transition: opacity 200ms ease;
}
.section-fade-enter-from {
  opacity: 0;
  transform: translateY(14px);
}
.section-fade-leave-to {
  opacity: 0;
}

/* ── Content Layout ────────────────────────────────────── */
.admin-content {
  padding: 0 20px;
}

.admin-panel-grid {
  display: grid;
  grid-template-columns: 1fr 0.65fr;
  gap: 16px;
  align-items: start;
}

.admin-panel-single {
  max-width: 720px;
  width: 100%;
}

@media (max-width: 1100px) {
  .admin-panel-grid {
    grid-template-columns: 1fr;
  }
  .admin-panel-single {
    max-width: 100%;
  }
}

/* ── Cards ─────────────────────────────────────────────── */
.admin-card {
  border-radius: 24px;
  background: var(--card);
  border: 1px solid var(--line);
  box-shadow: var(--shadow);
  overflow: hidden;
  animation: cardEnter 420ms cubic-bezier(0.22, 1, 0.36, 1) both;
}

.admin-card:nth-child(2) {
  animation-delay: 60ms;
}

@keyframes cardEnter {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ── Card Header ───────────────────────────────────────── */
.card-header {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px 22px 0;
}

.card-header-icon {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  background: var(--primary-surface);
  color: var(--primary);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(100, 12, 114, 0.12);
}

.card-header-icon svg {
  width: 20px;
  height: 20px;
}

.preview-icon {
  background: rgba(39, 174, 96, 0.1);
  color: var(--success);
  box-shadow: 0 2px 8px rgba(39, 174, 96, 0.12);
}

.shield-icon {
  background: rgba(100, 12, 114, 0.1);
  color: var(--primary-dark);
}

.users-icon {
  background: rgba(100, 12, 114, 0.1);
  color: var(--primary);
}

.card-kicker {
  font-size: 10.5px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--primary);
  opacity: 0.7;
  margin-bottom: 2px;
}

.card-title {
  margin: 0;
  font-size: 18px;
  font-weight: 800;
  color: var(--text-main);
  letter-spacing: -0.2px;
}

/* ── Card Body ─────────────────────────────────────────── */
.card-body {
  padding: 18px 22px 22px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ── Setting Groups ─────────────────────────────────────── */
.setting-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.setting-group + .setting-group {
  padding-top: 14px;
  border-top: 1px solid var(--line);
}

.setting-group-label {
  display: flex;
  align-items: center;
  gap: 8px;
}

.group-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: 999px;
  background: var(--primary-surface);
  color: var(--primary);
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.05em;
  flex-shrink: 0;
}

.group-title {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-main);
}

/* ── Field Row ─────────────────────────────────────────── */
.field-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.field-cell {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.field-label {
  font-size: 12px;
  font-weight: 700;
  color: var(--text-sub);
  letter-spacing: 0.05em;
}

.field-hint {
  font-size: 11.5px;
  color: var(--text-sub);
  opacity: 0.7;
  line-height: 1.4;
}

/* ── Input ─────────────────────────────────────────────── */
.input-wrap {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 5px 6px 5px 14px;
  border: 1.5px solid var(--line-strong);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.88);
  transition: border-color 180ms ease, box-shadow 180ms ease;
}

.input-wrap:focus-within {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--primary-surface);
}

.text-input {
  width: 100%;
  min-width: 0;
  border: none;
  background: transparent;
  color: var(--text-main);
  font-size: 15px;
  font-weight: 600;
  outline: none;
  font-variant-numeric: tabular-nums;
  -moz-appearance: textfield;
}

.text-input::-webkit-outer-spin-button,
.text-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.input-unit {
  flex-shrink: 0;
  padding: 5px 10px;
  border-radius: 10px;
  background: var(--primary-surface);
  color: var(--primary);
  font-size: 11.5px;
  font-weight: 700;
}

@media (max-width: 480px) {
  .field-row {
    grid-template-columns: 1fr;
  }
}

/* ── Extension Grid ─────────────────────────────────────── */
.ext-section {
  margin-top: 4px;
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.ext-label-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  flex-wrap: wrap;
}

.ext-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.ext-card {
  border-radius: 16px;
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.6);
  padding: 11px 12px;
  display: grid;
  gap: 7px;
  cursor: text;
  transition: border-color 180ms ease, background 180ms ease, box-shadow 180ms ease;
}

.ext-card:hover {
  border-color: rgba(100, 12, 114, 0.3);
  background: rgba(255, 255, 255, 0.85);
}

.ext-card:focus-within {
  border-color: var(--primary);
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 0 0 2px var(--primary-surface);
}

.ext-card-head {
  display: flex;
  align-items: center;
  gap: 7px;
}

.ext-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
  opacity: 0.8;
}

.ext-title {
  font-size: 12.5px;
  font-weight: 700;
  color: var(--text-main);
}

.ext-input {
  width: 100%;
  padding: 6px 9px;
  border: 1px solid var(--line);
  border-radius: 9px;
  background: rgba(255, 255, 255, 0.9);
  color: var(--text-main);
  font-size: 12.5px;
  outline: none;
  transition: border-color 160ms ease;
}

.ext-input:focus {
  border-color: var(--primary);
}

.ext-input::placeholder {
  color: var(--text-sub);
  opacity: 0.45;
}

@media (max-width: 480px) {
  .ext-grid {
    grid-template-columns: 1fr;
  }
}

/* ── Toggle ─────────────────────────────────────────────── */
.toggle-list {
  gap: 12px;
}

.toggle-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.toggle-section + .toggle-section {
  padding-top: 14px;
  border-top: 1px solid var(--line);
}

.toggle-section-label {
  display: flex;
  align-items: center;
  gap: 8px;
}

.toggle-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 13px 16px;
  border-radius: 16px;
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.75);
  transition: background 200ms ease, border-color 200ms ease, opacity 200ms ease;
}

.toggle-row:not(.muted):hover {
  background: rgba(255, 255, 255, 0.92);
  border-color: var(--line-strong);
}

.toggle-row.muted {
  opacity: 0.5;
}

.toggle-copy {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
}

.toggle-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-main);
}

.toggle-hint {
  font-size: 12px;
  color: var(--text-sub);
  line-height: 1.5;
}

/* ── iOS Toggle Switch ─────────────────────────────────── */
.toggle-switch {
  flex-shrink: 0;
  cursor: pointer;
  display: inline-block;
}

.toggle-switch input {
  position: absolute;
  width: 1px;
  height: 1px;
  opacity: 0;
  pointer-events: none;
}

.toggle-track {
  display: block;
  width: 46px;
  height: 26px;
  border-radius: 999px;
  background: var(--line-strong);
  position: relative;
  transition: background 250ms cubic-bezier(0.22, 1, 0.36, 1);
  box-shadow: inset 0 1px 3px rgba(0,0,0,0.1);
}

.toggle-thumb {
  position: absolute;
  top: 3px;
  left: 3px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 2px 6px rgba(0,0,0,0.2);
  transition: transform 250ms cubic-bezier(0.22, 1, 0.36, 1);
}

.toggle-switch input:checked ~ .toggle-track {
  background: var(--primary);
}

.toggle-switch input:checked ~ .toggle-track .toggle-thumb {
  transform: translateX(20px);
}

.toggle-switch input:disabled ~ .toggle-track {
  opacity: 0.45;
  cursor: not-allowed;
}

.toggle-switch:has(input:focus-visible) .toggle-track {
  box-shadow: 0 0 0 3px var(--primary-surface), inset 0 1px 3px rgba(0,0,0,0.1);
}

/* ── Message Banners ───────────────────────────────────── */
.msg-banner {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 11px 15px;
  border-radius: 14px;
  font-size: 13.5px;
  font-weight: 600;
  line-height: 1.4;
}

.msg-banner.error {
  background: rgba(192, 57, 43, 0.08);
  border: 1px solid rgba(192, 57, 43, 0.18);
  color: var(--danger);
}

.msg-banner.success {
  background: rgba(39, 174, 96, 0.08);
  border: 1px solid rgba(39, 174, 96, 0.18);
  color: #166534;
}

.msg-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.msg-fade-enter-active {
  transition: opacity 260ms ease, transform 260ms cubic-bezier(0.22, 1, 0.36, 1);
}
.msg-fade-leave-active {
  transition: opacity 180ms ease;
}
.msg-fade-enter-from {
  opacity: 0;
  transform: translateY(-8px);
}
.msg-fade-leave-to {
  opacity: 0;
}

/* ── Buttons ───────────────────────────────────────────── */
.card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-width: 96px;
  height: 40px;
  padding: 0 18px;
  border-radius: 12px;
  border: none;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition:
    transform 140ms ease,
    box-shadow 180ms ease,
    filter 180ms ease,
    background 180ms ease,
    opacity 180ms ease;
}

.btn:active {
  transform: scale(0.97);
}

.btn-primary {
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
  color: #fff;
  box-shadow: 0 4px 16px rgba(100, 12, 114, 0.3);
}

.btn-primary:hover {
  filter: brightness(1.06);
  box-shadow: 0 8px 24px rgba(100, 12, 114, 0.38);
  transform: translateY(-1px);
}

.btn-primary:hover:active {
  transform: scale(0.97) translateY(0);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
  filter: none;
}

.btn-ghost {
  background: var(--primary-surface);
  color: var(--primary);
  border: 1px solid rgba(100, 12, 114, 0.18);
}

.btn-ghost:hover {
  background: rgba(100, 12, 114, 0.14);
  border-color: rgba(100, 12, 114, 0.3);
}

.btn-danger-ghost {
  background: rgba(192, 57, 43, 0.07);
  color: var(--danger);
  border: 1px solid rgba(192, 57, 43, 0.18);
}

.btn-danger-ghost:hover {
  background: rgba(192, 57, 43, 0.14);
}

.btn-spinner {
  width: 15px;
  height: 15px;
  animation: spin 0.9s linear infinite;
  flex-shrink: 0;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@media (max-width: 480px) {
  .card-actions {
    flex-direction: column;
  }
  .btn {
    width: 100%;
  }
}

/* ── Preview Panel ──────────────────────────────────────── */
.preview-card .card-header-icon {
  box-shadow: 0 2px 8px rgba(39, 174, 96, 0.15);
}

.preview-shell {
  gap: 12px;
}

.preview-box {
  border-radius: 18px;
  border: 1.5px dashed var(--line-strong);
  background: rgba(255, 255, 255, 0.4);
  overflow: hidden;
}

.preview-box-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px 8px;
  border-bottom: 1px solid var(--line);
}

.preview-box-title {
  font-size: 12.5px;
  font-weight: 700;
  color: var(--text-main);
}

.preview-box-sub {
  font-size: 11px;
  color: var(--text-sub);
}

.preview-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 20px;
}

.preview-add-btn {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: var(--primary-surface);
  color: var(--primary);
  font-size: 20px;
  font-weight: 400;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  transition: background 180ms ease, transform 140ms ease;
}

.preview-add-btn:hover {
  background: rgba(100, 12, 114, 0.15);
  transform: scale(1.06);
}

.preview-empty-text {
  font-size: 12px;
  color: var(--text-sub);
}

.format-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 10px 14px;
}

.format-chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 10px 5px 7px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.75);
  border: 1px solid var(--line);
}

.format-chip-icon {
  width: 13px;
  height: 13px;
  opacity: 0.7;
  flex-shrink: 0;
}

.format-chip-label {
  font-size: 11.5px;
  font-weight: 700;
  color: var(--text-main);
}

.format-chip-exts {
  font-size: 10px;
  color: var(--text-sub);
  font-variant-numeric: tabular-nums;
}

.preview-tip {
  padding: 7px 14px;
  font-size: 11px;
  color: var(--text-sub);
  border-top: 1px solid var(--line);
}

/* ── Users Card ────────────────────────────────────────── */
.users-card {
  overflow: visible;
}

/* Card Header */
.users-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 18px 22px 16px;
  border-bottom: 1px solid var(--line);
}

.users-card-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.users-card-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: rgba(100, 12, 114, 0.08);
  color: var(--primary);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(100, 12, 114, 0.1);
}

.users-card-icon svg {
  width: 18px;
  height: 18px;
}

.users-card-titles {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.users-count-badge {
  display: flex;
  align-items: baseline;
  gap: 3px;
  padding: 6px 14px;
  border-radius: 999px;
  background: rgba(100, 12, 114, 0.06);
  border: 1px solid rgba(100, 12, 114, 0.12);
  flex-shrink: 0;
}

.count-num {
  font-size: 18px;
  font-weight: 800;
  color: var(--primary-dark);
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.3px;
}

.count-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-sub);
}

/* Toolbar */
.users-toolbar {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
  padding: 14px 22px;
  border-bottom: 1px solid var(--line);
}

.search-wrap {
  flex: 1;
  min-width: 200px;
  position: relative;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 15px;
  height: 15px;
  color: var(--text-sub);
  pointer-events: none;
}

.search-input {
  width: 100%;
  padding: 9px 14px 9px 36px;
  border: 1.5px solid var(--line-strong);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.92);
  color: var(--text-main);
  font-size: 14px;
  outline: none;
  transition: border-color 180ms ease, box-shadow 180ms ease;
}

.search-input:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--primary-surface);
}

.search-input::placeholder {
  color: var(--text-sub);
}

.filter-select {
  padding: 9px 32px 9px 12px;
  border: 1.5px solid var(--line-strong);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.92);
  color: var(--text-main);
  font-size: 13.5px;
  font-weight: 600;
  outline: none;
  cursor: pointer;
  transition: border-color 180ms ease, box-shadow 180ms ease;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%237b5a94' stroke-width='2.5'%3E%3Cpath d='M6 9l6 6 6-6'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 10px center;
}

.filter-select:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--primary-surface);
}

/* Content */
.users-content {
  padding: 0;
}

/* Center state */
.users-center-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 56px 20px;
  color: var(--text-sub);
  font-size: 14px;
  font-weight: 500;
}

.loading-spinner {
  width: 22px;
  height: 22px;
  animation: spin 0.9s linear infinite;
  flex-shrink: 0;
  color: var(--primary);
}

.state-icon {
  width: 44px;
  height: 44px;
  opacity: 0.35;
}

.state-icon.error {
  opacity: 1;
  color: var(--danger);
}

/* Table */
.table-wrap {
  overflow: auto;
  -webkit-overflow-scrolling: touch;
}

.users-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13.5px;
}

.users-table th {
  padding: 11px 16px;
  text-align: left;
  font-size: 10.5px;
  font-weight: 800;
  letter-spacing: 0.07em;
  text-transform: uppercase;
  color: var(--text-sub);
  border-bottom: 1px solid var(--line);
  background: rgba(100, 12, 114, 0.04);
  white-space: nowrap;
  position: sticky;
  top: 0;
  z-index: 1;
}

.users-table td {
  padding: 13px 16px;
  color: var(--text-main);
  vertical-align: middle;
  background: transparent;
  transition: background 120ms ease;
}

.users-table tr + tr td {
  border-top: 1px solid rgba(100, 12, 114, 0.06);
}

.user-row:hover td {
  background: rgba(100, 12, 114, 0.035);
}

.td-mono {
  font-family: "SF Mono", "Fira Code", monospace;
  font-size: 12.5px;
  color: var(--primary);
  font-weight: 600;
}

.td-action {
  text-align: center;
  width: 3rem;
}

/* Role Chip */
.role-chip {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 11.5px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.role-admin {
  background: rgba(192, 57, 43, 0.09);
  color: var(--danger);
}

.role-teacher {
  background: rgba(100, 12, 114, 0.09);
  color: var(--primary);
}

.role-student {
  background: rgba(39, 174, 96, 0.09);
  color: var(--success);
}

/* Icon Button */
.icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: none;
  border-radius: 9px;
  background: transparent;
  color: var(--text-sub);
  cursor: pointer;
  transition: background 160ms ease, color 160ms ease, transform 120ms ease;
}

.icon-btn svg {
  width: 15px;
  height: 15px;
}

.icon-btn:hover {
  background: rgba(100, 12, 114, 0.1);
  color: var(--primary);
}

.icon-btn:active {
  transform: scale(0.93);
}

.icon-btn:focus-visible {
  outline: 2px solid var(--primary);
  outline-offset: 2px;
}

/* Pagination */
.users-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 14px 22px;
  border-top: 1px solid var(--line);
}

.page-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: 1.5px solid var(--line-strong);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.9);
  color: var(--primary);
  cursor: pointer;
  transition: background 160ms ease, border-color 160ms ease, transform 120ms ease, opacity 160ms ease;
}

.page-btn svg {
  width: 14px;
  height: 14px;
}

.page-btn:hover:not(:disabled) {
  background: var(--primary-surface);
  border-color: var(--primary);
}

.page-btn:active:not(:disabled) {
  transform: scale(0.93);
}

.page-btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.page-btn:focus-visible {
  outline: 2px solid var(--primary);
  outline-offset: 2px;
}

.page-info {
  display: flex;
  align-items: baseline;
  gap: 3px;
  font-size: 14px;
  font-variant-numeric: tabular-nums;
}

.page-current {
  font-weight: 800;
  color: var(--primary-dark);
  font-size: 16px;
}

.page-sep {
  color: var(--text-sub);
  font-size: 12px;
}

.page-total {
  color: var(--text-sub);
  font-size: 13px;
}

/* ── Edit Modal ────────────────────────────────────────── */
.user-edit-modal {
  max-width: 400px;
  padding: 0;
  overflow: hidden;
}

.modal-top-bar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 18px 20px 14px;
  border-bottom: 1px solid var(--line);
}

.modal-title-group {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.modal-title {
  margin: 0;
  font-size: 16px;
  font-weight: 800;
  color: var(--primary-dark);
}

.modal-subtitle {
  margin: 0;
  font-size: 12.5px;
  color: var(--text-sub);
  font-weight: 500;
}

.modal-close-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border: none;
  border-radius: 9px;
  background: var(--primary-surface);
  color: var(--primary);
  cursor: pointer;
  flex-shrink: 0;
  transition: background 160ms ease, transform 120ms ease;
}

.modal-close-btn svg {
  width: 15px;
  height: 15px;
}

.modal-close-btn:hover {
  background: rgba(100, 12, 114, 0.16);
}

.modal-close-btn:active {
  transform: scale(0.93);
}

.modal-close-btn:focus-visible {
  outline: 2px solid var(--primary);
  outline-offset: 2px;
}

.modal-body {
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.modal-field {
  display: grid;
  gap: 6px;
}

.modal-label {
  font-size: 12.5px;
  font-weight: 700;
  color: var(--primary-dark);
}

.modal-input,
.modal-select {
  width: 100%;
  padding: 10px 14px;
  border: 1.5px solid var(--line-strong);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.9);
  color: var(--text-main);
  font-size: 14px;
  outline: none;
  transition: border-color 180ms ease, box-shadow 180ms ease;
  box-sizing: border-box;
}

.modal-input:focus,
.modal-select:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--primary-surface);
}

.modal-select {
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%237b5a94' stroke-width='2.5'%3E%3Cpath d='M6 9l6 6 6-6'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  padding-right: 32px;
}

.modal-error {
  margin: 0;
}

/* ── Modal Footer ──────────────────────────────────────── */
.modal-footer {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 20px;
  border-top: 1px solid var(--line);
}

.modal-footer-btns {
  display: flex;
  gap: 8px;
  margin-left: auto;
}

.modal-footer .btn {
  height: 38px;
  min-width: 80px;
}

/* ── Responsive ─────────────────────────────────────────── */
@media (max-width: 640px) {
  .admin-shell {
    gap: 14px;
  }
  .admin-header {
    padding: 4px 16px 0;
  }
  .admin-content {
    padding: 0 14px;
  }
  .admin-tabs {
    margin: 0 14px;
  }
  .card-header {
    padding: 16px 16px 0;
  }
  .card-body {
    padding: 14px 16px 18px;
  }
  .users-card-header {
    padding: 14px 16px;
    flex-direction: column;
    align-items: flex-start;
  }
  .users-count-badge {
    align-self: flex-start;
  }
  .users-toolbar {
    flex-direction: column;
    padding: 12px 16px;
    gap: 8px;
  }
  .search-wrap,
  .filter-select {
    width: 100%;
    min-width: 0;
    flex: none;
  }
  .filter-select {
    flex: 1;
  }
  .users-pagination {
    padding: 12px 16px;
  }
  .modal-footer {
    flex-direction: column;
  }
  .modal-footer .btn {
    width: 100%;
  }
  .modal-footer-btns {
    flex-direction: column;
    margin-left: 0;
    width: 100%;
  }
  .modal-footer-btns .btn {
    width: 100%;
  }
}

/* ── Reduced Motion ────────────────────────────────────── */
@media (prefers-reduced-motion: reduce) {
  .admin-card,
  .admin-tab,
  .btn,
  .input-wrap,
  .ext-card,
  .toggle-row,
  .toggle-track,
  .toggle-thumb,
  .preview-add-btn,
  .icon-btn,
  .modal-close-btn {
    animation: none !important;
    transition: none !important;
  }
  .section-fade-enter-active,
  .section-fade-leave-active,
  .msg-fade-enter-active,
  .msg-fade-leave-active {
    transition: opacity 100ms ease !important;
  }
}

/* ── Backup Panel ──────────────────────────────────────── */
.backup-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-width: 720px;
}

.backup-icon {
  background: rgba(39, 174, 96, 0.1);
  color: var(--success);
  box-shadow: 0 2px 8px rgba(39, 174, 96, 0.12);
}

.restore-icon {
  background: rgba(192, 57, 43, 0.09);
  color: var(--danger);
  box-shadow: 0 2px 8px rgba(192, 57, 43, 0.1);
}

.backup-desc {
  margin: 0;
  font-size: 13.5px;
  color: var(--text-sub);
  line-height: 1.6;
}

.backup-card-body,
.restore-card-body {
  gap: 14px;
}

.backup-two-btns {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.backup-btn {
  gap: 7px;
  flex: 1;
  min-width: 160px;
}

.btn-icon {
  width: 15px;
  height: 15px;
  flex-shrink: 0;
}

/* Restore card */
.restore-warning {
  display: flex;
  align-items: flex-start;
  gap: 9px;
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(192, 57, 43, 0.07);
  border: 1px solid rgba(192, 57, 43, 0.15);
  font-size: 12.5px;
  color: var(--danger);
  line-height: 1.5;
  font-weight: 500;
}

.warning-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
  margin-top: 1px;
}

.restore-two-items {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.restore-item {
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.restore-item + .restore-item {
  padding-top: 10px;
  border-top: 1px solid var(--line);
}

.restore-item-label {
  font-size: 12px;
  font-weight: 700;
  color: var(--text-sub);
  letter-spacing: 0.05em;
}

.restore-item-row {
  display: flex;
  gap: 8px;
  align-items: center;
}

.restore-btn {
  flex-shrink: 0;
  min-width: 80px;
}

.restore-item-row .file-input-wrap {
  flex: 1;
}

.field-label {
  font-size: 12px;
  font-weight: 700;
  color: var(--text-sub);
  letter-spacing: 0.05em;
}

/* File Input */
.file-input {
  position: absolute;
  width: 1px;
  height: 1px;
  opacity: 0;
  pointer-events: none;
}

.file-input-wrap {
  position: relative;
}

.file-label {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 14px;
  border: 1.5px dashed var(--line-strong);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  transition: border-color 180ms ease, background 180ms ease;
}

.file-label:hover {
  border-color: var(--primary);
  background: rgba(255, 255, 255, 0.85);
}

.file-input:focus + .file-label {
  border-color: var(--primary);
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 0 0 3px var(--primary-surface);
}

.file-label-icon {
  width: 17px;
  height: 17px;
  color: var(--primary);
  flex-shrink: 0;
}

.file-label-text {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-sub);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Field slide transition */
.field-slide-enter-active {
  transition: opacity 260ms ease, transform 260ms cubic-bezier(0.22, 1, 0.36, 1);
}
.field-slide-leave-active {
  transition: opacity 180ms ease;
}
.field-slide-enter-from {
  opacity: 0;
  transform: translateY(-8px);
}
.field-slide-leave-to {
  opacity: 0;
}

/* btn-danger */
.btn-danger {
  background: linear-gradient(135deg, var(--danger) 0%, #b03020 100%);
  color: #fff;
  box-shadow: 0 4px 16px rgba(192, 57, 43, 0.3);
}

.btn-danger:hover {
  filter: brightness(1.06);
  box-shadow: 0 8px 24px rgba(192, 57, 43, 0.38);
  transform: translateY(-1px);
}

.btn-danger:hover:active {
  transform: scale(0.97) translateY(0);
}

.btn-danger:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
  filter: none;
}

.code-inline {
  font-family: "SF Mono", "Fira Code", monospace;
  font-size: 11.5px;
  padding: 1px 5px;
  border-radius: 5px;
  background: rgba(100, 12, 114, 0.07);
  color: var(--primary);
  font-weight: 600;
}

.msg-banner.info {
  background: rgba(100, 12, 114, 0.07);
  border: 1px solid rgba(100, 12, 114, 0.15);
  color: var(--primary-dark);
}
</style>
