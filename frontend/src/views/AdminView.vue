<script setup>
import { computed, onMounted, reactive, shallowRef } from "vue";
import { useAchievementUploadSettings } from "../composables/useAchievementUploadSettings";
import { useReviewSettings } from "../composables/useReviewSettings";

const ATTACHMENT_TYPE_OPTIONS = [
  { key: "document", label: "文档", icon: "/assets/icons/doc.svg" },
  { key: "video", label: "视频", icon: "/assets/icons/video.svg" },
  { key: "image", label: "图片", icon: "/assets/icons/image.svg" },
  { key: "archive", label: "压缩包", icon: "/assets/icons/zip.svg" },
];

const profile = reactive(loadUser());
const activeSection = shallowRef("upload");
const saveMessage = shallowRef("");
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

const statCards = computed(() => [
  ...(activeSection.value === "upload"
    ? [
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
      ]
    : [
        {
          label: "个人信息",
          value: reviewForm.profileReviewEnabled
            ? reviewForm.profileReviewAutoApprove
              ? "自动通过"
              : "人工审核"
            : "直接生效",
          note: "资料保存后的处理方式",
        },
        {
          label: "成就",
          value: reviewForm.achievementReviewEnabled
            ? reviewForm.achievementReviewAutoApprove
              ? "自动通过"
              : "人工审核"
            : "直接生效",
          note: "新增与修改后的处理方式",
        },
      ]),
  {
    label: "当前栏目",
    value: activeSection.value === "upload" ? "上传限制" : "审核设置",
    note: activeSection.value === "upload" ? "控制资源上传范围" : "控制审核流程开关",
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
const reviewCards = computed(() => [
  {
    key: "profile",
    title: "个人信息审核",
    enabled: reviewForm.profileReviewEnabled,
    autoApprove: reviewForm.profileReviewAutoApprove,
    disabledHint: "关闭后，学生提交个人信息会直接生效。",
    enabledHint: reviewForm.profileReviewAutoApprove
      ? "开启后仍保留审核轨迹，但系统会默认自动通过。"
      : "开启后，已保存过的个人信息修改需要管理员审核后才生效。",
  },
  {
    key: "achievement",
    title: "成就审核",
    enabled: reviewForm.achievementReviewEnabled,
    autoApprove: reviewForm.achievementReviewAutoApprove,
    disabledHint: "关闭后，新增与修改成就会直接生效。",
    enabledHint: reviewForm.achievementReviewAutoApprove
      ? "开启后仍保留审核记录，但新增与修改会默认自动通过。"
      : "开启后，新增与修改成就都会进入待审核状态。",
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
      <button
        class="admin-nav-item"
        :class="{ active: activeSection === 'upload' }"
        type="button"
        @click="switchSection('upload')"
      >
        <div class="admin-nav-title">上传限制</div>
        <div class="admin-nav-note">
          {{ activeSection === "upload" ? "当前栏目" : "切换查看" }}
        </div>
      </button>
      <button
        class="admin-nav-item"
        :class="{ active: activeSection === 'review' }"
        type="button"
        @click="switchSection('review')"
      >
        <div class="admin-nav-title">审核</div>
        <div class="admin-nav-note">
          {{ activeSection === "review" ? "当前栏目" : "切换查看" }}
        </div>
      </button>
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

    <section v-if="activeSection === 'upload'" class="admin-panel-grid">
      <article class="admin-panel admin-form-panel">
        <div class="admin-panel-head">
          <div>
            <div class="admin-panel-kicker">系统设置</div>
            <h3 class="admin-panel-title">成就页面上传限制</h3>
          </div>
          <div v-if="activeLoading" class="admin-panel-status">加载中...</div>
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

        <div v-if="activeErrorMessage" class="admin-feedback error">
          {{ activeErrorMessage }}
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
            :disabled="activeSaving"
            @click="handleSubmit"
          >
            {{ activeSaving ? "保存中..." : "保存设置" }}
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

    <section v-else class="admin-panel-grid">
      <article class="admin-panel admin-form-panel">
        <div class="admin-panel-head">
          <div>
            <div class="admin-panel-kicker">审核入口</div>
            <h3 class="admin-panel-title">审核策略设置</h3>
          </div>
          <div v-if="activeLoading" class="admin-panel-status">加载中...</div>
        </div>

        <div class="admin-setting-block">
          <div class="admin-setting-heading">
            <div class="admin-setting-index">01</div>
            <div>
              <div class="admin-setting-title">个人信息</div>
              <div class="admin-field-hint">
                控制学生资料修改后是直接生效，还是进入审核流程。
              </div>
            </div>
          </div>
          <div class="admin-toggle-list">
            <label class="admin-toggle-card">
              <div class="admin-toggle-copy">
                <span class="admin-toggle-title">开启个人信息审核</span>
                <span class="admin-toggle-hint">
                  关闭后，学生提交个人信息会直接更新。
                </span>
              </div>
              <input
                v-model="reviewForm.profileReviewEnabled"
                class="admin-toggle-input"
                type="checkbox"
              />
            </label>

            <label
              class="admin-toggle-card"
              :class="{ muted: !reviewForm.profileReviewEnabled }"
            >
              <div class="admin-toggle-copy">
                <span class="admin-toggle-title">默认通过</span>
                <span class="admin-toggle-hint">
                  开启审核但自动通过，保留审核流程入口与记录。
                </span>
              </div>
              <input
                v-model="reviewForm.profileReviewAutoApprove"
                class="admin-toggle-input"
                type="checkbox"
                :disabled="!reviewForm.profileReviewEnabled"
              />
            </label>
          </div>
        </div>

        <div class="admin-setting-block">
          <div class="admin-setting-heading">
            <div class="admin-setting-index">02</div>
            <div>
              <div class="admin-setting-title">成就</div>
              <div class="admin-field-hint">
                控制学生新增与修改成就后是否需要管理员审核。
              </div>
            </div>
          </div>
          <div class="admin-toggle-list">
            <label class="admin-toggle-card">
              <div class="admin-toggle-copy">
                <span class="admin-toggle-title">开启成就审核</span>
                <span class="admin-toggle-hint">
                  关闭后，新增和修改成就都会直接生效。
                </span>
              </div>
              <input
                v-model="reviewForm.achievementReviewEnabled"
                class="admin-toggle-input"
                type="checkbox"
              />
            </label>

            <label
              class="admin-toggle-card"
              :class="{ muted: !reviewForm.achievementReviewEnabled }"
            >
              <div class="admin-toggle-copy">
                <span class="admin-toggle-title">默认通过</span>
                <span class="admin-toggle-hint">
                  开启审核但自动通过，适合先保留入口再平滑切换。
                </span>
              </div>
              <input
                v-model="reviewForm.achievementReviewAutoApprove"
                class="admin-toggle-input"
                type="checkbox"
                :disabled="!reviewForm.achievementReviewEnabled"
              />
            </label>
          </div>
        </div>

        <div v-if="activeErrorMessage" class="admin-feedback error">
          {{ activeErrorMessage }}
        </div>
        <div v-if="saveMessage" class="admin-feedback success">
          {{ saveMessage }}
        </div>

        <div class="admin-actions">
          <button
            class="admin-secondary-btn"
            type="button"
            @click="syncReviewFormFromSettings"
          >
            重置
          </button>
          <button
            class="admin-primary-btn"
            type="button"
            :disabled="activeSaving"
            @click="handleReviewSubmit"
          >
            {{ activeSaving ? "保存中..." : "保存设置" }}
          </button>
        </div>
      </article>

      <article class="admin-panel admin-preview-panel">
        <div class="admin-panel-head">
          <div>
            <div class="admin-panel-kicker">流程预览</div>
            <h3 class="admin-panel-title">当前审核生效方式</h3>
          </div>
        </div>

        <div class="admin-review-preview">
          <article
            v-for="card in reviewCards"
            :key="card.key"
            class="admin-review-card"
          >
            <div class="admin-review-card-head">
              <div class="admin-review-card-title">{{ card.title }}</div>
              <div
                class="admin-review-badge"
                :class="{
                  enabled: card.enabled && !card.autoApprove,
                  auto: card.enabled && card.autoApprove,
                  disabled: !card.enabled,
                }"
              >
                {{
                  !card.enabled
                    ? "直接生效"
                    : card.autoApprove
                      ? "自动通过"
                      : "人工审核"
                }}
              </div>
            </div>
            <div class="admin-review-copy">
              {{ card.enabled ? card.enabledHint : card.disabledHint }}
            </div>
          </article>
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
  appearance: none;
  min-width: 9.5rem;
  padding: 0.9rem 1rem;
  border: 1px solid var(--admin-line);
  border-radius: 18px;
  background: rgba(255, 251, 242, 0.72);
  color: inherit;
  text-align: left;
  cursor: pointer;
}

.admin-nav-item.active {
  border-color: rgba(141, 95, 47, 0.24);
  background: rgba(255, 253, 249, 0.96);
  box-shadow: 0 14px 28px rgba(74, 51, 23, 0.08);
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

.admin-toggle-list {
  display: grid;
  gap: 0.9rem;
}

.admin-toggle-card,
.admin-review-card {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  padding: 1rem 1.05rem;
  border: 1px solid rgba(115, 88, 50, 0.14);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.74);
}

.admin-toggle-card.muted {
  opacity: 0.7;
}

.admin-toggle-copy {
  display: grid;
  gap: 0.3rem;
}

.admin-toggle-title,
.admin-review-card-title {
  font-weight: 700;
}

.admin-toggle-hint,
.admin-review-copy {
  color: var(--admin-muted);
  font-size: 0.92rem;
  line-height: 1.55;
}

.admin-toggle-input {
  width: 1.15rem;
  height: 1.15rem;
  margin-top: 0.2rem;
  accent-color: var(--admin-accent);
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

.admin-review-preview {
  display: grid;
  gap: 1rem;
}

.admin-review-card {
  display: grid;
  gap: 0.9rem;
}

.admin-review-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.8rem;
}

.admin-review-badge {
  padding: 0.38rem 0.8rem;
  border-radius: 999px;
  font-size: 0.82rem;
  font-weight: 700;
}

.admin-review-badge.enabled {
  background: rgba(173, 111, 45, 0.12);
  color: #8d5f2f;
}

.admin-review-badge.auto {
  background: rgba(61, 129, 82, 0.12);
  color: #25613a;
}

.admin-review-badge.disabled {
  background: rgba(109, 109, 109, 0.12);
  color: #5c5c5c;
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
