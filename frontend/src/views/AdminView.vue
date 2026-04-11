<script setup>
import { computed, onMounted, reactive, shallowRef } from "vue";
import { useAchievementUploadSettings } from "../composables/useAchievementUploadSettings";
import { useReviewSettings } from "../composables/useReviewSettings";
import { getUserList, updateUser, deleteUser } from "../api/admin";

const ATTACHMENT_TYPE_OPTIONS = [
  { key: "document", label: "文档", icon: "/assets/icons/doc.svg" },
  { key: "video", label: "视频", icon: "/assets/icons/video.svg" },
  { key: "image", label: "图片", icon: "/assets/icons/image.svg" },
  { key: "archive", label: "压缩包", icon: "/assets/icons/zip.svg" },
];

const profile = reactive(loadUser());
const activeSection = shallowRef("upload");
const saveMessage = shallowRef("");
const users = shallowRef([]);
const usersLoading = shallowRef(false);
const usersError = shallowRef("");
const userSearch = shallowRef("");
const userRoleFilter = shallowRef("");
const userClassFilter = shallowRef("");
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
const statCards = computed(() => [
  {
    label: "图片",
    value: `${form.imageMaxCount} 张 / ${form.imageMaxSizeMb}MB`,
    note: "数量与单张大小",
  },
  {
    label: "附件",
    value: `${form.attachmentMaxCount} 个 / ${form.attachmentMaxSizeMb}MB`,
    note: "数量与单个大小",
  },
]);
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

function switchSection(sectionKey) {
  activeSection.value = sectionKey;
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

async function loadUsers() {
  usersLoading.value = true;
  usersError.value = "";
  try {
    const res = await getUserList();
    users.value = res.data || [];
  } catch (e) {
    usersError.value = "加载用户列表失败";
  } finally {
    usersLoading.value = false;
  }
}

const filteredUsers = computed(() => {
  let result = users.value;
  const q = userSearch.value.trim().toLowerCase();
  if (q) {
    result = result.filter(u =>
      (u.username || "").toLowerCase().includes(q) ||
      (u.displayName || "").toLowerCase().includes(q) ||
      (u.studentNo || "").toLowerCase().includes(q) ||
      (u.className || "").toLowerCase().includes(q)
    );
  }
  if (userRoleFilter.value) {
    result = result.filter(u => u.role === userRoleFilter.value);
  }
  if (userClassFilter.value) {
    result = result.filter(u => u.className === userClassFilter.value);
  }
  return result;
});

const classOptions = computed(() => {
  const set = new Set(users.value.map(u => u.className).filter(Boolean));
  return Array.from(set).sort();
});

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
    await loadUsers();
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
</script>

<template>
  <main class="dashboard-right admin-shell">
    <header class="feed-header">
      <h1 class="feed-title">后台管理</h1>
    </header>

    <!-- Category Tabs -->
    <nav class="admin-tabs" role="tablist">
      <button
        class="admin-tab"
        :class="{ active: activeSection === 'upload' }"
        role="tab"
        :aria-selected="activeSection === 'upload'"
        type="button"
        @click="switchSection('upload')"
      >
        <svg class="admin-tab-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
          <path stroke-linecap="round" stroke-linejoin="round" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
        </svg>
        上传限制
      </button>
      <button
        class="admin-tab"
        :class="{ active: activeSection === 'review' }"
        role="tab"
        :aria-selected="activeSection === 'review'"
        type="button"
        @click="switchSection('review')"
      >
        <svg class="admin-tab-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
          <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
        </svg>
        审核策略
      </button>
      <button
        class="admin-tab"
        :class="{ active: activeSection === 'users' }"
        role="tab"
        :aria-selected="activeSection === 'users'"
        type="button"
        @click="switchSection('users')"
      >
        <svg class="admin-tab-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
        </svg>
        用户管理
      </button>
    </nav>

    <!-- Upload Section -->
    <div v-if="activeSection === 'upload'" class="admin-panel-grid">
      <div class="admin-card">
        <div class="admin-card-header">
          <div class="admin-card-kicker">系统设置</div>
          <h2 class="admin-card-title">成就页面上传限制</h2>
        </div>
        <div class="admin-card-body">

          <!-- Image Block -->
          <div class="admin-block">
            <div class="admin-block-heading">
              <span class="admin-block-index">01</span>
              <span class="admin-block-title">图片设置</span>
            </div>
            <div class="admin-form-row">
              <label class="admin-field">
                <span class="admin-field-label">最多上传图片数</span>
                <div class="admin-input-wrap">
                  <input
                    v-model.number="form.imageMaxCount"
                    class="admin-input"
                    type="number"
                    min="1"
                    max="9"
                    aria-label="最多上传图片数"
                  />
                  <span class="admin-input-unit">张</span>
                </div>
              </label>
              <label class="admin-field">
                <span class="admin-field-label">单张图片最大大小</span>
                <div class="admin-input-wrap">
                  <input
                    v-model.number="form.imageMaxSizeMb"
                    class="admin-input"
                    type="number"
                    min="1"
                    max="200"
                    aria-label="单张图片最大大小 MB"
                  />
                  <span class="admin-input-unit">MB</span>
                </div>
              </label>
            </div>
          </div>

          <!-- Attachment Block -->
          <div class="admin-block">
            <div class="admin-block-heading">
              <span class="admin-block-index">02</span>
              <span class="admin-block-title">附件设置</span>
            </div>
            <div class="admin-form-row">
              <label class="admin-field">
                <span class="admin-field-label">最多上传附件数</span>
                <div class="admin-input-wrap">
                  <input
                    v-model.number="form.attachmentMaxCount"
                    class="admin-input"
                    type="number"
                    min="1"
                    max="20"
                    aria-label="最多上传附件数"
                  />
                  <span class="admin-input-unit">个</span>
                </div>
              </label>
              <label class="admin-field">
                <span class="admin-field-label">单个附件最大大小</span>
                <div class="admin-input-wrap">
                  <input
                    v-model.number="form.attachmentMaxSizeMb"
                    class="admin-input"
                    type="number"
                    min="1"
                    max="200"
                    aria-label="单个附件最大大小 MB"
                  />
                  <span class="admin-input-unit">MB</span>
                </div>
              </label>
            </div>

            <div class="admin-field" style="margin-top: 12px;">
              <span class="admin-field-label">支持的附件后缀</span>
              <span class="admin-field-hint">按类型填写，多个后缀用英文逗号隔开；留空表示该类型暂不开放</span>
              <div class="admin-ext-grid" style="margin-top: 8px;">
                <label
                  v-for="item in ATTACHMENT_TYPE_OPTIONS"
                  :key="item.key"
                  class="admin-ext-card"
                >
                  <div class="admin-ext-head">
                    <img class="admin-ext-icon" :src="item.icon" alt="" aria-hidden="true" />
                    <span class="admin-ext-title">{{ item.label }}</span>
                  </div>
                  <input
                    v-model="form[extFieldKey(item.key)]"
                    class="admin-ext-input"
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
          <Transition name="feedback-slide">
            <div v-if="activeErrorMessage" class="admin-feedback error" role="alert">
              <svg class="admin-feedback-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-2.694-.833-3.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z" />
              </svg>
              {{ activeErrorMessage }}
            </div>
            <div v-else-if="saveMessage" class="admin-feedback success" role="status">
              <svg class="admin-feedback-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              {{ saveMessage }}
            </div>
          </Transition>

          <!-- Actions -->
          <div class="admin-actions">
            <button
              class="admin-btn secondary"
              type="button"
              @click="syncFormFromSettings"
            >
              重置
            </button>
            <button
              class="admin-btn primary"
              type="button"
              :disabled="activeSaving"
              @click="handleSubmit"
            >
              {{ activeSaving ? "保存中..." : "保存设置" }}
            </button>
          </div>
        </div>
      </div>

      <!-- Preview Card -->
      <div class="admin-preview-card">
        <div class="admin-card-header">
          <div class="admin-card-kicker">用户侧预览</div>
          <h2 class="admin-card-title">成就页面展示效果</h2>
        </div>
        <div class="preview-shell">
          <!-- Image Preview -->
          <div class="admin-preview-box" aria-hidden="true">
            <div class="media-header">
              <div>
                <div class="media-title">图片（可选）</div>
                <div class="media-subtitle">{{ imageSubtitle }}</div>
              </div>
            </div>
            <div class="media-empty-state">
              <div class="media-empty-icon">+</div>
              <div class="media-empty-text">点击添加图片</div>
            </div>
          </div>
          <!-- Attachment Preview -->
          <div class="admin-preview-box" aria-hidden="true">
            <div class="media-header">
              <div>
                <div class="media-title">附件（可选）</div>
                <div class="media-subtitle">{{ attachmentSubtitle }}</div>
              </div>
            </div>
            <div class="attachment-formats">
              <div class="format-row">
                <div
                  v-for="item in enabledPreviewTypes.slice(0, 2)"
                  :key="item.key"
                  class="format-item"
                >
                  <img class="format-icon" :src="item.icon" alt="" aria-hidden="true" />
                  <span class="format-label">{{ item.label }}</span>
                  <span class="format-exts">{{ item.exts.join("/") }}</span>
                </div>
              </div>
              <div class="format-row">
                <div
                  v-for="item in enabledPreviewTypes.slice(2, 4)"
                  :key="item.key"
                  class="format-item"
                >
                  <img class="format-icon" :src="item.icon" alt="" aria-hidden="true" />
                  <span class="format-label">{{ item.label }}</span>
                  <span class="format-exts">{{ item.exts.join("/") }}</span>
                </div>
              </div>
            </div>
            <div class="media-tip">
              {{ attachmentTypeSummary || "暂无可用附件类型" }} · 单个不超过 {{ form.attachmentMaxSizeMb }}MB
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Review Section -->
    <div v-else class="admin-panel-grid">
      <div class="admin-card" style="grid-column: 1 / -1;">
        <div class="admin-card-header">
          <div class="admin-card-kicker">审核入口</div>
          <h2 class="admin-card-title">审核策略设置</h2>
        </div>
        <div class="admin-card-body" style="display: grid; gap: 0;">

          <!-- Profile Review -->
          <div class="admin-block">
            <div class="admin-block-heading">
              <span class="admin-block-index">01</span>
              <span class="admin-block-title">个人信息</span>
            </div>
            <div class="admin-toggle-list">
              <div class="admin-toggle-card">
                <div class="admin-toggle-copy">
                  <span class="admin-toggle-title">开启个人信息审核</span>
                  <span class="admin-toggle-hint">关闭后，学生提交个人信息会直接更新</span>
                </div>
                <label class="ios-switch" :aria-label="`开启个人信息审核: ${reviewForm.profileReviewEnabled ? '已开启' : '已关闭'}`">
                  <input
                    v-model="reviewForm.profileReviewEnabled"
                    type="checkbox"
                    role="switch"
                    :aria-checked="reviewForm.profileReviewEnabled"
                  />
                  <span class="ios-slider"></span>
                </label>
              </div>
              <div class="admin-toggle-card" :class="{ muted: !reviewForm.profileReviewEnabled }">
                <div class="admin-toggle-copy">
                  <span class="admin-toggle-title">默认通过</span>
                  <span class="admin-toggle-hint">开启审核但自动通过，保留审核流程入口与记录</span>
                </div>
                <label class="ios-switch" :aria-label="`默认通过: ${reviewForm.profileReviewAutoApprove ? '已开启' : '已关闭'}`">
                  <input
                    v-model="reviewForm.profileReviewAutoApprove"
                    type="checkbox"
                    role="switch"
                    :aria-checked="reviewForm.profileReviewAutoApprove"
                    :disabled="!reviewForm.profileReviewEnabled"
                  />
                  <span class="ios-slider"></span>
                </label>
              </div>
            </div>
          </div>

          <!-- Achievement Review -->
          <div class="admin-block">
            <div class="admin-block-heading">
              <span class="admin-block-index">02</span>
              <span class="admin-block-title">成就</span>
            </div>
            <div class="admin-toggle-list">
              <div class="admin-toggle-card">
                <div class="admin-toggle-copy">
                  <span class="admin-toggle-title">开启成就审核</span>
                  <span class="admin-toggle-hint">关闭后，新增和修改成就都会直接生效</span>
                </div>
                <label class="ios-switch" :aria-label="`开启成就审核: ${reviewForm.achievementReviewEnabled ? '已开启' : '已关闭'}`">
                  <input
                    v-model="reviewForm.achievementReviewEnabled"
                    type="checkbox"
                    role="switch"
                    :aria-checked="reviewForm.achievementReviewEnabled"
                  />
                  <span class="ios-slider"></span>
                </label>
              </div>
              <div class="admin-toggle-card" :class="{ muted: !reviewForm.achievementReviewEnabled }">
                <div class="admin-toggle-copy">
                  <span class="admin-toggle-title">默认通过</span>
                  <span class="admin-toggle-hint">开启审核但自动通过，适合先保留入口再平滑切换</span>
                </div>
                <label class="ios-switch" :aria-label="`成就默认通过: ${reviewForm.achievementReviewAutoApprove ? '已开启' : '已关闭'}`">
                  <input
                    v-model="reviewForm.achievementReviewAutoApprove"
                    type="checkbox"
                    role="switch"
                    :aria-checked="reviewForm.achievementReviewAutoApprove"
                    :disabled="!reviewForm.achievementReviewEnabled"
                  />
                  <span class="ios-slider"></span>
                </label>
              </div>
            </div>
          </div>

          <!-- Feedback -->
          <Transition name="feedback-slide">
            <div v-if="activeErrorMessage" class="admin-feedback error" role="alert">
              <svg class="admin-feedback-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-2.694-.833-3.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z" />
              </svg>
              {{ activeErrorMessage }}
            </div>
            <div v-else-if="saveMessage" class="admin-feedback success" role="status">
              <svg class="admin-feedback-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              {{ saveMessage }}
            </div>
          </Transition>

          <!-- Actions -->
          <div class="admin-actions">
            <button
              class="admin-btn secondary"
              type="button"
              @click="syncReviewFormFromSettings"
            >
              重置
            </button>
            <button
              class="admin-btn primary"
              type="button"
              :disabled="activeSaving"
              @click="handleReviewSubmit"
            >
              {{ activeSaving ? "保存中..." : "保存设置" }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Users Section -->
    <div v-if="activeSection === 'users'" class="admin-user-section">
      <div class="admin-user-header">
        <div class="admin-panel-kicker">用户管理</div>
        <h2 class="admin-panel-title">系统用户列表</h2>
        <div v-if="usersLoading" class="admin-panel-status">加载中...</div>
        <div v-else class="admin-panel-status">共 {{ filteredUsers.length }} 位用户</div>
      </div>

      <div v-if="usersError" class="admin-feedback error">{{ usersError }}</div>

      <div class="user-filter-bar">
        <div class="user-search-wrap">
          <input
            v-model="userSearch"
            class="user-search-input"
            type="text"
            placeholder="搜索用户名/姓名/学号/班级"
          />
        </div>
        <select v-model="userRoleFilter" class="user-filter-select">
          <option value="">全部角色</option>
          <option v-for="opt in ROLE_OPTIONS" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
        </select>
        <select v-model="userClassFilter" class="user-filter-select">
          <option value="">全部班级</option>
          <option v-for="cls in classOptions" :key="cls" :value="cls">{{ cls }}</option>
        </select>
      </div>

      <div class="user-table-wrap">
        <table class="user-table">
          <thead>
            <tr>
              <th>用户名</th>
              <th>显示名称</th>
              <th>角色</th>
              <th>学号</th>
              <th>班级</th>
              <th style="width: 3rem;"></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in filteredUsers" :key="user.id">
              <td class="user-td-username">{{ user.username }}</td>
              <td>{{ user.displayName }}</td>
              <td><span :class="['role-badge', 'role-' + user.role.toLowerCase()]">{{ getRoleLabel(user.role) }}</span></td>
              <td>{{ user.studentNo || '—' }}</td>
              <td>{{ user.className || '—' }}</td>
              <td class="user-td-action">
                <button class="action-menu-btn" @click.stop="openEditModal(user)" aria-label="编辑用户">
                  <span class="dot"></span><span class="dot"></span><span class="dot"></span>
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Edit User Modal -->
    <Teleport to="body">
      <div :class="['sheet-overlay', { open: editModal.visible }]" @click.self="closeEditModal">
        <div class="sheet-modal user-edit-modal">
          <header class="sheet-modal-header">
            <div class="sheet-modal-title">编辑用户</div>
            <button class="sheet-modal-close" type="button" aria-label="关闭" @click="closeEditModal">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                <path d="M18 6L6 18M6 6l12 12"/>
              </svg>
            </button>
          </header>
          <div class="modal-body">
            <div class="modal-user-info">
              <span class="modal-user-label">当前编辑：</span>
              <span class="modal-user-name">{{ editModal.user?.displayName }}</span>
            </div>

            <label class="modal-field">
              <span class="modal-field-label">用户名</span>
              <input v-model="editModal.form.username" class="modal-input" type="text" placeholder="留空则不修改" />
            </label>

            <label class="modal-field">
              <span class="modal-field-label">密码</span>
              <input v-model="editModal.form.password" class="modal-input" type="password" placeholder="留空则不修改" />
            </label>

            <label class="modal-field">
              <span class="modal-field-label">角色</span>
              <select v-model="editModal.form.role" class="modal-select">
                <option v-for="opt in ROLE_OPTIONS" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
              </select>
            </label>

            <div v-if="editModal.error" class="modal-error">{{ editModal.error }}</div>
          </div>
          <div class="modal-foot">
            <button class="modal-btn danger" type="button" @click="handleDeleteUser(editModal.user)">删除用户</button>
            <div class="modal-foot-right">
              <button class="modal-btn secondary" type="button" @click="closeEditModal">取消</button>
              <button class="modal-btn primary" type="button" :disabled="editModal.saving" @click="handleUpdateUser">
                {{ editModal.saving ? "保存中..." : "保存" }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </main>
</template>

<style scoped>
/* ── View Shell ──────────────────────────────────────────── */
.admin-shell {
  display: grid;
  gap: 14px;
  padding: 0 0 3rem;
}

/* ── Section Tabs ──────────────────────────────────────── */
.admin-tabs {
  display: flex;
  gap: 6px;
  padding: 4px;
  margin: 0 18px;
  border-radius: 16px;
  background: var(--card);
  border: 1px solid var(--line);
  box-shadow: var(--shadow-sm);
  width: fit-content;
}

.admin-tab {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 18px;
  border-radius: 10px;
  border: none;
  background: transparent;
  color: var(--text-sub);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition:
    background 160ms ease,
    color 160ms ease,
    transform 120ms ease,
    box-shadow 160ms ease;
}

.admin-tab:hover:not(.active) {
  background: rgba(100, 12, 114, 0.06);
  color: var(--primary);
}

.admin-tab.active {
  background: var(--primary);
  color: #fff;
  box-shadow: 0 4px 14px rgba(100, 12, 114, 0.3);
}

.admin-tab:active {
  transform: scale(0.97);
}

.admin-tab-icon {
  width: 16px;
  height: 16px;
  opacity: 0.85;
}

/* ── Two-Column Grid ────────────────────────────────────── */
.admin-panel-grid {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 14px;
  margin: 0 18px;
  align-items: start;
}

/* ── Cards ──────────────────────────────────────────────── */
.admin-card {
  border-radius: 24px;
  background: var(--card);
  border: 1px solid var(--line);
  box-shadow: var(--shadow);
  overflow: hidden;
  animation: cardEnter 400ms var(--ease-out) both;
}

.admin-card-header {
  padding: 20px 22px 0;
}

.admin-card-kicker {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--primary);
  opacity: 0.7;
  margin-bottom: 4px;
}

.admin-card-title {
  margin: 0;
  font-size: 20px;
  font-weight: 800;
  color: var(--text-main);
  letter-spacing: -0.2px;
}

.admin-card-body {
  padding: 18px 22px 22px;
  display: grid;
  gap: 14px;
}

/* ── Setting Blocks ─────────────────────────────────────── */
.admin-block + .admin-block {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid var(--line);
}

.admin-block-heading {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.admin-block-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 999px;
  background: var(--primary-surface);
  color: var(--primary);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.05em;
  flex-shrink: 0;
}

.admin-block-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-main);
}

/* ── Number Inputs ───────────────────────────────────────── */
.admin-form-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.admin-field {
  display: grid;
  gap: 5px;
}

.admin-field-label {
  font-size: 12.5px;
  font-weight: 700;
  color: var(--text-sub);
  letter-spacing: 0.1px;
}

.admin-field-hint {
  font-size: 11.5px;
  color: var(--text-sub);
  opacity: 0.75;
  line-height: 1.4;
}

.admin-input-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 6px 4px 14px;
  border: 1.5px solid var(--line-strong);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.9);
  transition: border-color 180ms ease, box-shadow 180ms ease;
}

.admin-input-wrap:focus-within {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--primary-surface);
}

.admin-input {
  width: 100%;
  min-width: 0;
  border: none;
  background: transparent;
  color: var(--text-main);
  font-size: 15px;
  font-weight: 600;
  outline: none;
  -moz-appearance: textfield;
}

.admin-input::-webkit-outer-spin-button,
.admin-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.admin-input-unit {
  flex-shrink: 0;
  padding: 6px 10px;
  border-radius: 10px;
  background: var(--primary-surface);
  color: var(--primary);
  font-size: 12px;
  font-weight: 700;
}

/* ── Extension Grid ─────────────────────────────────────── */
.admin-ext-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.admin-ext-card {
  border-radius: 16px;
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.7);
  padding: 12px;
  display: grid;
  gap: 8px;
  transition: border-color 180ms ease, background 180ms ease;
}

.admin-ext-card:focus-within {
  border-color: var(--primary);
  background: rgba(255, 255, 255, 0.95);
}

.admin-ext-head {
  display: flex;
  align-items: center;
  gap: 8px;
}

.admin-ext-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}

.admin-ext-title {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-main);
}

.admin-ext-input {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid var(--line);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.9);
  color: var(--text-main);
  font-size: 13px;
  outline: none;
  transition: border-color 160ms ease, box-shadow 160ms ease;
}

.admin-ext-input:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 2px var(--primary-surface);
}

.admin-ext-input::placeholder {
  color: var(--text-sub);
  opacity: 0.5;
}

/* ── iOS Toggles ───────────────────────────────────────── */
.admin-toggle-list {
  display: grid;
  gap: 10px;
}

.admin-toggle-card {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  padding: 14px 16px;
  border-radius: 18px;
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.75);
  transition: background 180ms ease, border-color 180ms ease;
}

.admin-toggle-card.muted {
  opacity: 0.55;
}

.admin-toggle-card:not(.muted):hover {
  background: rgba(255, 255, 255, 0.92);
  border-color: var(--line-strong);
}

.admin-toggle-copy {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.admin-toggle-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-main);
}

.admin-toggle-hint {
  color: var(--text-sub);
  font-size: 12.5px;
  line-height: 1.55;
}

/* ── Feedback Banners ───────────────────────────────────── */
.admin-feedback {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: 14px;
  font-size: 13.5px;
  font-weight: 600;
  animation: slideDown 280ms cubic-bezier(0.22, 1, 0.36, 1) both;
}

.admin-feedback.error {
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.2);
  color: var(--danger);
}

.admin-feedback.success {
  background: rgba(34, 197, 94, 0.1);
  border: 1px solid rgba(34, 197, 94, 0.2);
  color: #166534;
}

.admin-feedback-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

/* ── Actions ────────────────────────────────────────────── */
.admin-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 4px;
}

.admin-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 100px;
  height: 40px;
  padding: 0 20px;
  border-radius: 12px;
  border: none;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition:
    transform 160ms ease,
    box-shadow 180ms ease,
    filter 180ms ease,
    background 180ms ease;
}

.admin-btn:active {
  transform: scale(0.97);
}

.admin-btn.primary {
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
  color: #fff;
  box-shadow: 0 6px 20px rgba(100, 12, 114, 0.3);
}

.admin-btn.primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 28px rgba(100, 12, 114, 0.38);
  filter: brightness(1.04);
}

.admin-btn.primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
  filter: none;
}

.admin-btn.secondary {
  background: var(--primary-surface);
  color: var(--primary);
  border: 1px solid rgba(100, 12, 114, 0.2);
}

.admin-btn.secondary:hover {
  background: rgba(100, 12, 114, 0.14);
  border-color: var(--primary);
}

/* ── Preview Panel ──────────────────────────────────────── */
.admin-preview-card {
  border-radius: 24px;
  background: var(--card);
  border: 1px solid var(--line);
  box-shadow: var(--shadow);
  overflow: hidden;
  animation: cardEnter 500ms var(--ease-out) 80ms both;
}

.admin-preview-header {
  padding: 20px 22px 0;
}

.preview-shell {
  display: grid;
  gap: 12px;
  padding: 0 22px 22px;
}

.admin-preview-box {
  border-radius: 18px;
  border: 1.5px dashed var(--line-strong);
  background: rgba(255, 255, 255, 0.5);
  overflow: hidden;
  transition: background 180ms ease;
}

.media-header {
  padding: 12px 14px 8px;
  border-bottom: 1px solid var(--line);
}

.media-title {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-main);
}

.media-subtitle {
  font-size: 11.5px;
  color: var(--text-sub);
  margin-top: 2px;
}

.media-empty-state {
  display: grid;
  place-items: center;
  padding: 24px;
  gap: 6px;
}

.media-empty-icon {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background: var(--primary-surface);
  color: var(--primary);
  font-size: 22px;
  font-weight: 300;
  display: grid;
  place-items: center;
  line-height: 1;
}

.media-empty-text {
  font-size: 12.5px;
  color: var(--text-sub);
}

.media-empty {
  padding: 12px 14px;
  font-size: 12.5px;
  color: var(--text-sub);
  text-align: center;
}

.attachment-formats {
  padding: 12px 14px;
  display: grid;
  gap: 8px;
}

.format-row {
  display: flex;
  gap: 8px;
}

.format-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 10px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid var(--line);
  min-width: 0;
}

.format-icon {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
  opacity: 0.7;
}

.format-label {
  font-size: 11.5px;
  font-weight: 700;
  color: var(--text-main);
  white-space: nowrap;
}

.format-exts {
  font-size: 10px;
  color: var(--text-sub);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.media-tip {
  padding: 8px 14px;
  font-size: 11.5px;
  color: var(--text-sub);
  border-top: 1px solid var(--line);
}

/* ── Animations ─────────────────────────────────────────── */
@keyframes cardEnter {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ── Responsive ─────────────────────────────────────────── */
@media (max-width: 1100px) {
  .admin-panel-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .admin-tabs {
    width: 100%;
    margin: 0 12px;
  }

  .admin-panel-grid {
    margin: 0 12px;
  }

  .admin-tab {
    flex: 1;
    justify-content: center;
    padding: 8px 10px;
    font-size: 13px;
  }

  .admin-form-row {
    grid-template-columns: 1fr;
  }

  .admin-ext-grid {
    grid-template-columns: 1fr;
  }

  .admin-actions {
    flex-direction: column;
  }

  .admin-btn {
    width: 100%;
  }
}

@media (prefers-reduced-motion: reduce) {
  .admin-card,
  .admin-preview-card,
  .admin-feedback,
  .admin-tab,
  .admin-btn,
  .admin-toggle-card,
  .admin-ext-card {
    animation: none !important;
    transition: none !important;
  }
}

/* ── Feedback Transition ────────────────────────────────── */
.feedback-slide-enter-active {
  transition: opacity 240ms ease, transform 240ms cubic-bezier(0.22, 1, 0.36, 1);
}

.feedback-slide-leave-active {
  transition: opacity 160ms ease;
}

.feedback-slide-enter-from {
  opacity: 0;
  transform: translateY(-6px);
}

.feedback-slide-leave-to {
  opacity: 0;
}


/* ── User Management ─────────────────────────────────────── */
.admin-user-section {
  border-radius: 24px;
  background: var(--card);
  border: 1px solid var(--line);
  box-shadow: var(--shadow);
  padding: 20px;
  display: grid;
  gap: 14px;
}

.admin-user-header {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.admin-user-header .admin-panel-kicker {
  font-size: 11px;
  font-weight: 700;
  color: var(--primary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.admin-user-header .admin-panel-title {
  font-size: 18px;
  font-weight: 800;
  color: var(--primary-dark);
  margin: 0;
}

.admin-panel-status {
  margin-left: auto;
  font-size: 12.5px;
  color: var(--text-sub);
}

.user-filter-bar {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  align-items: center;
}

.user-search-wrap {
  flex: 1;
  min-width: 180px;
}

.user-search-input {
  width: 100%;
  padding: 9px 14px;
  border: 1.5px solid var(--line-strong);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.9);
  color: var(--text-main);
  font-size: 14px;
  outline: none;
  transition: border-color 180ms ease, box-shadow 180ms ease;
}

.user-search-input:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--primary-surface);
}

.user-search-input::placeholder {
  color: var(--text-sub);
}

.user-filter-select {
  padding: 9px 12px;
  border: 1.5px solid var(--line-strong);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.9);
  color: var(--text-main);
  font-size: 13.5px;
  outline: none;
  cursor: pointer;
  transition: border-color 180ms ease;
}

.user-filter-select:focus {
  border-color: var(--primary);
}

.user-table-wrap {
  border-radius: 16px;
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.7);
  overflow: auto;
}

.user-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13.5px;
}

.user-table th {
  padding: 12px 14px;
  text-align: left;
  font-size: 11px;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  color: var(--text-sub);
  border-bottom: 1px solid var(--line);
  white-space: nowrap;
  position: sticky;
  top: 0;
  background: var(--primary-surface);
}

.user-table td {
  padding: 12px 14px;
  border-bottom: 1px solid var(--line);
  color: var(--text-main);
}

.user-table tr:last-child td {
  border-bottom: none;
}

.user-table tr:hover td {
  background: rgba(100, 12, 114, 0.03);
}

.user-td-username {
  font-family: monospace;
  color: var(--primary);
  font-weight: 600;
}

.user-td-action {
  text-align: center;
}

.role-badge {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.role-admin {
  background: rgba(192, 57, 43, 0.1);
  color: var(--danger);
}

.role-teacher {
  background: rgba(100, 12, 114, 0.1);
  color: var(--primary);
}

.role-student {
  background: rgba(39, 174, 96, 0.1);
  color: var(--success);
}

.action-menu-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 3px;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 8px;
  background: var(--primary-surface);
  cursor: pointer;
  transition: background 160ms ease;
}

.action-menu-btn:hover {
  background: rgba(100, 12, 114, 0.15);
}

.dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: var(--primary);
}

/* ── User Edit Modal ─────────────────────────────────────── */
.user-edit-modal {
  max-width: 400px;
  padding: 0;
  overflow: hidden;
}

.modal-body {
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.modal-user-info {
  padding: 10px 14px;
  background: var(--primary-surface);
  border-radius: 12px;
  font-size: 13.5px;
}

.modal-user-label {
  color: var(--text-sub);
}

.modal-user-name {
  font-weight: 700;
  color: var(--primary);
  margin-left: 4px;
}

.modal-field {
  display: grid;
  gap: 6px;
}

.modal-field-label {
  font-size: 13px;
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

.modal-error {
  padding: 10px 14px;
  background: rgba(192, 57, 43, 0.08);
  color: var(--danger);
  border-radius: 12px;
  font-size: 13px;
  font-weight: 600;
}

.modal-foot {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 20px;
  border-top: 1px solid var(--line);
}

.modal-foot-right {
  display: flex;
  gap: 8px;
  margin-left: auto;
}

.modal-btn {
  padding: 9px 18px;
  border-radius: 10px;
  border: none;
  font-size: 13.5px;
  font-weight: 700;
  cursor: pointer;
  transition: background 160ms ease, filter 160ms ease, transform 120ms ease;
}

.modal-btn:active {
  transform: scale(0.97);
}

.modal-btn.primary {
  background: var(--primary);
  color: #fff;
  box-shadow: 0 4px 12px rgba(100, 12, 114, 0.3);
}

.modal-btn.primary:hover {
  filter: brightness(1.1);
}

.modal-btn.primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.modal-btn.secondary {
  background: var(--primary-surface);
  color: var(--primary);
}

.modal-btn.secondary:hover {
  background: rgba(100, 12, 114, 0.14);
}

.modal-btn.danger {
  background: rgba(192, 57, 43, 0.08);
  color: var(--danger);
}

.modal-btn.danger:hover {
  background: rgba(192, 57, 43, 0.14);
}
</style>
