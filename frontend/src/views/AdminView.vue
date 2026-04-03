<script setup>
import { computed, onMounted, reactive, shallowRef } from "vue";
import { useAchievementUploadSettings } from "../composables/useAchievementUploadSettings";

const ATTACHMENT_TYPE_OPTIONS = [
  { key: "document", label: "文档", icon: "/assets/icons/doc.svg" },
  { key: "video", label: "视频", icon: "/assets/icons/video.svg" },
  { key: "image", label: "图片", icon: "/assets/icons/image.svg" },
  { key: "archive", label: "压缩包", icon: "/assets/icons/zip.svg" },
];

const profile = reactive(loadUser());
const saveMessage = shallowRef("");
const {
  settings,
  loading,
  saving,
  errorMessage,
  fetchSettings,
  saveSettings,
} = useAchievementUploadSettings();

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
  await fetchSettings();
  syncFormFromSettings();
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

async function handleSubmit() {
  saveMessage.value = "";
  const result = await saveSettings({
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

onMounted(() => {
  loadPage();
});
</script>

<template>
  <main class="dashboard-right admin-shell">
    <header class="feed-header">
      <h1 class="feed-title">后台管理</h1>
    </header>

    <section class="admin-nav">
      <div class="admin-nav-item active">
        <div class="admin-nav-title">上传限制</div>
        <div class="admin-nav-note">当前栏目</div>
      </div>
      <div class="admin-nav-item disabled" aria-disabled="true">
        <div class="admin-nav-title">审核</div>
        <div class="admin-nav-note">入口预留</div>
      </div>
    </section>

    <section class="admin-hero">
      <div class="admin-stat-grid">
        <article
          v-for="card in statCards"
          :key="card.label"
          class="admin-stat-card"
        >
          <div class="admin-stat-label">{{ card.label }}</div>
          <div class="admin-stat-value">{{ card.value }}</div>
          <div class="admin-stat-note">{{ card.note }}</div>
        </article>
      </div>
    </section>

    <section class="admin-panel-grid">
      <article class="admin-panel admin-form-panel">
        <div class="admin-panel-head">
          <div>
            <div class="admin-panel-kicker">系统设置</div>
            <h3 class="admin-panel-title">成就页面上传限制</h3>
          </div>
          <div v-if="loading" class="admin-panel-status">加载中...</div>
        </div>

        <div class="admin-setting-block">
          <div class="admin-setting-heading">
            <div class="admin-setting-index">01</div>
            <div>
              <div class="admin-setting-title">图片设置</div>
            </div>
          </div>
          <div class="admin-form-list two-cols">
            <label class="admin-field">
              <span class="admin-field-label">最多上传图片数</span>
              <span class="admin-field-hint">决定用户一次最多可添加多少张图片</span>
              <div class="admin-input-wrap">
                <input
                  v-model.number="form.imageMaxCount"
                  class="admin-input"
                  type="number"
                  min="1"
                  max="9"
                />
                <span class="admin-input-unit">张</span>
              </div>
            </label>

            <label class="admin-field">
              <span class="admin-field-label">单张图片最大大小</span>
              <span class="admin-field-hint">超出限制时会直接提示并阻止上传</span>
              <div class="admin-input-wrap">
                <input
                  v-model.number="form.imageMaxSizeMb"
                  class="admin-input"
                  type="number"
                  min="1"
                  max="200"
                />
                <span class="admin-input-unit">MB</span>
              </div>
            </label>
          </div>
        </div>

        <div class="admin-setting-block">
          <div class="admin-setting-heading">
            <div class="admin-setting-index">02</div>
            <div>
              <div class="admin-setting-title">附件设置</div>
            </div>
          </div>
          <div class="admin-form-list two-cols">
            <label class="admin-field">
              <span class="admin-field-label">最多上传附件数</span>
              <span class="admin-field-hint">决定附件区一次最多能保留多少个文件</span>
              <div class="admin-input-wrap">
                <input
                  v-model.number="form.attachmentMaxCount"
                  class="admin-input"
                  type="number"
                  min="1"
                  max="20"
                />
                <span class="admin-input-unit">个</span>
              </div>
            </label>

            <label class="admin-field">
              <span class="admin-field-label">单个附件最大大小</span>
              <span class="admin-field-hint">所有附件共用这一项单文件大小限制</span>
              <div class="admin-input-wrap">
                <input
                  v-model.number="form.attachmentMaxSizeMb"
                  class="admin-input"
                  type="number"
                  min="1"
                  max="200"
                />
                <span class="admin-input-unit">MB</span>
              </div>
            </label>
          </div>

          <div class="admin-field">
            <span class="admin-field-label">支持的附件后缀</span>
            <span class="admin-field-hint">按类型填写，多个后缀用英文逗号隔开；留空表示这一类暂不开放</span>
            <div class="admin-ext-grid">
              <label
                v-for="item in ATTACHMENT_TYPE_OPTIONS"
                :key="item.key"
                class="admin-ext-card"
              >
                <div class="admin-ext-head">
                  <img class="admin-ext-icon" :src="item.icon" alt="" />
                  <span class="admin-ext-title">{{ item.label }}</span>
                </div>
                <input
                  v-model="form[extFieldKey(item.key)]"
                  class="admin-ext-input"
                  type="text"
                  :placeholder="item.key === 'document'
                    ? 'docx,doc,pdf'
                    : item.key === 'video'
                      ? 'mp4,mov'
                      : item.key === 'image'
                        ? 'jpeg,jpg,png'
                        : 'zip,rar,7z'"
                />
              </label>
            </div>
          </div>
        </div>

        <div v-if="errorMessage" class="admin-feedback error">
          {{ errorMessage }}
        </div>
        <div v-if="saveMessage" class="admin-feedback success">
          {{ saveMessage }}
        </div>

        <div class="admin-actions">
          <button
            class="admin-secondary-btn"
            type="button"
            @click="syncFormFromSettings"
          >
            重置
          </button>
          <button
            class="admin-primary-btn"
            type="button"
            :disabled="saving"
            @click="handleSubmit"
          >
            {{ saving ? "保存中..." : "保存设置" }}
          </button>
        </div>
      </article>

      <article class="admin-panel admin-preview-panel">
        <div class="admin-panel-head">
          <div>
            <div class="admin-panel-kicker">用户侧预览</div>
            <h3 class="admin-panel-title">成就页面展示效果</h3>
          </div>
        </div>

        <div class="preview-shell">
          <div class="achievement-media-panel admin-preview-box">
            <div class="media-header">
              <div>
                <div class="media-title">图片(可选)</div>
                <div class="media-subtitle">{{ imageSubtitle }}</div>
              </div>
            </div>
            <div class="media-empty-state">
              <div class="media-empty-icon">+</div>
              <div class="media-empty-text">点击添加图片</div>
            </div>
          </div>

          <div class="achievement-attachments-panel admin-preview-box">
            <div class="media-header">
              <div>
                <div class="media-title">附件(可选)</div>
                <div class="media-subtitle">{{ attachmentSubtitle }}</div>
              </div>
            </div>
            <div class="media-empty">暂无附件</div>
            <div class="attachment-formats admin-format-mock">
              <div class="format-row">
                <div
                  v-for="item in enabledPreviewTypes.slice(0, 2)"
                  :key="item.key"
                  class="format-item"
                >
                  <img class="format-icon" :src="item.icon" alt="" />
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
                  <img class="format-icon" :src="item.icon" alt="" />
                  <span class="format-label">{{ item.label }}</span>
                  <span class="format-exts">{{ item.exts.join("/") }}</span>
                </div>
              </div>
            </div>
            <div class="media-tip">
              {{ attachmentTypeSummary }} · 单个不超过 {{ form.attachmentMaxSizeMb }}MB
            </div>
          </div>
        </div>
      </article>
    </section>
  </main>
</template>

<style scoped>
.admin-shell {
  --admin-bg: linear-gradient(135deg, #fffdf4 0%, #f4efe3 42%, #ece3d1 100%);
  --admin-panel: rgba(255, 251, 242, 0.9);
  --admin-line: rgba(115, 88, 50, 0.12);
  --admin-text: #342516;
  --admin-muted: #7e6547;
  --admin-accent: #8d5f2f;
  padding: 0 0 3rem;
  color: var(--admin-text);
}

.admin-nav {
  display: flex;
  gap: 0.8rem;
  margin: 0 1.5rem 1rem;
}

.admin-nav-item {
  min-width: 9.5rem;
  padding: 0.9rem 1rem;
  border: 1px solid var(--admin-line);
  border-radius: 18px;
  background: rgba(255, 251, 242, 0.72);
}

.admin-nav-item.active {
  border-color: rgba(141, 95, 47, 0.24);
  background: rgba(255, 253, 249, 0.96);
  box-shadow: 0 14px 28px rgba(74, 51, 23, 0.08);
}

.admin-nav-item.disabled {
  opacity: 0.72;
  cursor: default;
}

.admin-nav-title {
  font-weight: 700;
}

.admin-nav-note {
  margin-top: 0.22rem;
  color: var(--admin-muted);
  font-size: 0.88rem;
}

.admin-hero {
  margin: 0 1.5rem 1.5rem;
  padding: 0;
}

.admin-stat-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0.9rem;
}

.admin-stat-card,
.admin-panel,
.admin-ext-card {
  border: 1px solid var(--admin-line);
  border-radius: 26px;
  background: var(--admin-panel);
  backdrop-filter: blur(18px);
}

.admin-stat-card {
  padding: 1rem 1.05rem;
}

.admin-stat-label,
.admin-panel-kicker {
  font-size: 0.78rem;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--admin-muted);
}

.admin-stat-value {
  margin-top: 0.45rem;
  font-size: 1.8rem;
  font-weight: 700;
}

.admin-stat-note {
  margin-top: 0.32rem;
  color: var(--admin-muted);
  font-size: 0.92rem;
}

.admin-panel-grid {
  display: grid;
  grid-template-columns: 1.15fr 0.85fr;
  gap: 1.25rem;
  margin: 0 1.5rem;
}

.admin-panel {
  padding: 1.4rem;
}

.admin-panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1.2rem;
}

.admin-panel-title {
  margin: 0.25rem 0 0;
  font-size: 1.35rem;
}

.admin-panel-status {
  color: var(--admin-muted);
  font-size: 0.92rem;
}

.admin-setting-block + .admin-setting-block {
  margin-top: 1.3rem;
  padding-top: 1.3rem;
  border-top: 1px solid rgba(115, 88, 50, 0.12);
}

.admin-setting-title {
  font-size: 1rem;
  font-weight: 700;
}

.admin-setting-heading {
  display: flex;
  align-items: center;
  gap: 0.9rem;
  margin-bottom: 0.95rem;
}

.admin-setting-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 2rem;
  height: 2rem;
  border-radius: 999px;
  background: rgba(141, 95, 47, 0.1);
  color: var(--admin-accent);
  font-size: 0.8rem;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.admin-form-list {
  display: grid;
  gap: 1rem;
}

.admin-form-list.two-cols {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.admin-field {
  display: grid;
  gap: 0.4rem;
}

.admin-field-label {
  font-weight: 700;
}

.admin-field-hint {
  color: var(--admin-muted);
  font-size: 0.92rem;
}

.admin-input-wrap {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.25rem 0.25rem 0.25rem 1rem;
  border: 1px solid rgba(115, 88, 50, 0.18);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.82);
}

.admin-input {
  width: 100%;
  border: none;
  background: transparent;
  color: var(--admin-text);
  font-size: 1rem;
  outline: none;
}

.admin-input-unit {
  flex-shrink: 0;
  padding: 0.65rem 0.9rem;
  border-radius: 14px;
  background: rgba(141, 95, 47, 0.1);
  color: var(--admin-accent);
  font-weight: 700;
}

.admin-ext-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.85rem;
}

.admin-ext-card {
  display: grid;
  gap: 0.7rem;
  padding: 1rem;
  align-content: start;
}

.admin-ext-head {
  display: flex;
  align-items: center;
  gap: 0.65rem;
}

.admin-ext-icon {
  width: 1.25rem;
  height: 1.25rem;
}

.admin-ext-title {
  font-weight: 700;
}

.admin-ext-input {
  width: 100%;
  padding: 0.78rem 0.9rem;
  border: 1px solid rgba(115, 88, 50, 0.16);
  border-radius: 14px;
  background: #fff;
  color: var(--admin-text);
  outline: none;
  font-size: 0.95rem;
}

.admin-feedback {
  margin-top: 1rem;
  padding: 0.85rem 1rem;
  border-radius: 16px;
  font-size: 0.94rem;
}

.admin-feedback.error {
  background: rgba(196, 68, 68, 0.1);
  color: #a33a3a;
}

.admin-feedback.success {
  background: rgba(61, 129, 82, 0.1);
  color: #25613a;
}

.admin-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.8rem;
  margin-top: 1.2rem;
}

.admin-primary-btn,
.admin-secondary-btn {
  min-width: 8.5rem;
  padding: 0.85rem 1.2rem;
  border-radius: 999px;
  border: none;
  cursor: pointer;
  font-weight: 700;
}

.admin-primary-btn {
  background: linear-gradient(135deg, #8d5f2f, #a97945);
  color: #fffaf2;
  box-shadow: 0 16px 28px rgba(141, 95, 47, 0.22);
}

.admin-primary-btn:disabled {
  opacity: 0.7;
  cursor: wait;
}

.admin-secondary-btn {
  background: rgba(141, 95, 47, 0.1);
  color: var(--admin-accent);
}

.preview-shell {
  display: grid;
  gap: 1rem;
}

.admin-preview-box {
  pointer-events: none;
}

.admin-format-mock {
  cursor: default;
}

@media (max-width: 1300px) {
  .admin-panel-grid {
    grid-template-columns: 1fr;
  }

  .admin-preview-panel {
    order: 2;
  }
}

@media (max-width: 960px) {
  .admin-stat-grid,
  .admin-form-list.two-cols,
  .admin-ext-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .admin-nav,
  .admin-hero,
  .admin-panel-grid {
    margin-left: 1rem;
    margin-right: 1rem;
  }

  .admin-nav {
    overflow-x: auto;
  }

  .admin-panel {
    padding: 1rem;
  }

  .admin-actions {
    flex-direction: column;
  }

  .admin-primary-btn,
  .admin-secondary-btn {
    width: 100%;
  }
}
</style>
