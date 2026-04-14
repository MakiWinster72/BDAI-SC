/**
 * useAchievementUpload - 成就上传配置 composable
 *
 * 管理上传限制配置、文件输入引用、文件类型/大小校验。
 * 事件监听在 onMounted/onBeforeUnmount 中注册/注销。
 */
import { computed, onBeforeUnmount, onMounted, reactive, ref } from "vue";
import { ATTACHMENT_TYPE_META } from "../constants/achievementConstants";
import { resolveMediaTypeByExtension } from "../utils/media";

const imageInput = ref(null);
const attachmentInput = ref(null);

const uploadLimitConfig = reactive({
  imageMaxCount: 9,
  mediaMaxMB: 10,
  attachmentMaxCount: 9,
  attachmentMaxMB: 200,
  attachmentDocumentExts: [],
  attachmentVideoExts: [],
  attachmentImageExts: [],
  attachmentArchiveExts: [],
});

function splitExtText(value = "") {
  return value
    .split(",")
    .map((s) => s.trim().toLowerCase().replace(/^\./, ""))
    .filter(Boolean);
}

function setUploadLimits({
  imageMaxCount,
  mediaMaxMB,
  attachmentMaxCount,
  attachmentMaxMB,
  attachmentDocumentExts,
  attachmentVideoExts,
  attachmentImageExts,
  attachmentArchiveExts,
} = {}) {
  if (Number.isFinite(imageMaxCount)) {
    uploadLimitConfig.imageMaxCount = imageMaxCount;
  }
  if (Number.isFinite(mediaMaxMB)) {
    uploadLimitConfig.mediaMaxMB = mediaMaxMB;
  }
  if (Number.isFinite(attachmentMaxCount)) {
    uploadLimitConfig.attachmentMaxCount = attachmentMaxCount;
  }
  if (Number.isFinite(attachmentMaxMB)) {
    uploadLimitConfig.attachmentMaxMB = attachmentMaxMB;
  }
  if (attachmentDocumentExts) {
    uploadLimitConfig.attachmentDocumentExts = splitExtText(attachmentDocumentExts);
  }
  if (attachmentVideoExts) {
    uploadLimitConfig.attachmentVideoExts = splitExtText(attachmentVideoExts);
  }
  if (attachmentImageExts) {
    uploadLimitConfig.attachmentImageExts = splitExtText(attachmentImageExts);
  }
  if (attachmentArchiveExts) {
    uploadLimitConfig.attachmentArchiveExts = splitExtText(attachmentArchiveExts);
  }
}

function handleUploadSettingsUpdated(event) {
  const next = event.detail || {};
  setUploadLimits({
    imageMaxCount: next.imageMaxCount,
    mediaMaxMB: next.imageMaxSizeMb,
    attachmentMaxCount: next.attachmentMaxCount,
    attachmentMaxMB: next.attachmentMaxSizeMb,
    attachmentDocumentExts: next.attachmentDocumentExts,
    attachmentVideoExts: next.attachmentVideoExts,
    attachmentImageExts: next.attachmentImageExts,
    attachmentArchiveExts: next.attachmentArchiveExts,
  });
}

function attachmentExtsByType(typeKey) {
  if (typeKey === "document") return uploadLimitConfig.attachmentDocumentExts;
  if (typeKey === "video") return uploadLimitConfig.attachmentVideoExts;
  if (typeKey === "image") return uploadLimitConfig.attachmentImageExts;
  return uploadLimitConfig.attachmentArchiveExts;
}

function isAllowedImage(file) {
  const ext = resolveMediaTypeByExtension(file.name || "");
  return ["jpeg", "jpg", "png", "heif"].includes(ext);
}

function isAllowedAttachment(file) {
  const ext = resolveMediaTypeByExtension(file.name || "");
  return allowedAttachmentExtensions.value.includes(ext);
}

function isFileSizeAllowed(file, limitMb) {
  if (!Number.isFinite(limitMb)) {
    return true;
  }
  return file.size / (1024 * 1024) <= limitMb;
}

function formatFileSize(value) {
  return `${value}MB`;
}

function resolveAttachmentTypeByExtension(ext = "") {
  if (uploadLimitConfig.attachmentDocumentExts.includes(ext)) return "document";
  if (uploadLimitConfig.attachmentVideoExts.includes(ext)) return "video";
  if (uploadLimitConfig.attachmentImageExts.includes(ext)) return "image";
  if (uploadLimitConfig.attachmentArchiveExts.includes(ext)) return "archive";
  return "";
}

// ── computed ────────────────────────────────────────────

const imageMaxCount = computed(() => uploadLimitConfig.imageMaxCount);
const attachmentMaxCount = computed(() => uploadLimitConfig.attachmentMaxCount);
const mediaLimitLabel = computed(() => formatFileSize(uploadLimitConfig.mediaMaxMB));
const attachmentLimitLabel = computed(() => formatFileSize(uploadLimitConfig.attachmentMaxMB));

const allowedAttachmentExtensions = computed(() => {
  const exts = [
    ...uploadLimitConfig.attachmentDocumentExts,
    ...uploadLimitConfig.attachmentVideoExts,
    ...uploadLimitConfig.attachmentImageExts,
    ...uploadLimitConfig.attachmentArchiveExts,
  ];
  return [...new Set(exts)];
});

const enabledAttachmentTypes = computed(() =>
  ATTACHMENT_TYPE_META.map((type) => ({
    ...type,
    exts: attachmentExtsByType(type.key),
  })).filter((item) => item.exts.length),
);

// ── lifecycle ─────────────────────────────────────────

onMounted(() => {
  window.addEventListener("achievement-upload-settings-updated", handleUploadSettingsUpdated);
});

onBeforeUnmount(() => {
  window.removeEventListener("achievement-upload-settings-updated", handleUploadSettingsUpdated);
});

export function useAchievementUpload() {
  return {
    // refs
    imageInput,
    attachmentInput,
    uploadLimitConfig,
    // methods
    setUploadLimits,
    attachmentExtsByType,
    isAllowedImage,
    isAllowedAttachment,
    isFileSizeAllowed,
    formatFileSize,
    resolveAttachmentTypeByExtension,
    // computed
    imageMaxCount,
    attachmentMaxCount,
    mediaLimitLabel,
    attachmentLimitLabel,
    allowedAttachmentExtensions,
    enabledAttachmentTypes,
  };
}
