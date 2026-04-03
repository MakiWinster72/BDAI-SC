import { computed, reactive, shallowRef } from "vue";
import {
  getAchievementUploadSettings,
  updateAchievementUploadSettings,
} from "../api/achievementUploadSettings";

const STORAGE_KEY = "gcsc_achievement_upload_settings";

const DEFAULT_SETTINGS = Object.freeze({
  imageMaxCount: 3,
  imageMaxSizeMb: 10,
  attachmentMaxCount: 10,
  attachmentMaxSizeMb: 50,
  attachmentDocumentExts: "docx,doc,pdf,xls,xlsx,pptx,ppt",
  attachmentVideoExts: "mp4,mov",
  attachmentImageExts: "jpeg,jpg,png,heif",
  attachmentArchiveExts: "zip,rar,7z,tar",
});

function normalizeExtText(value, fallback) {
  if (typeof value !== "string") {
    return fallback;
  }
  return value
    .split(",")
    .map((item) => item.trim().toLowerCase().replace(/^\./, ""))
    .filter(Boolean)
    .filter((item, index, list) => list.indexOf(item) === index)
    .join(",");
}

function normalizeSettings(raw = {}) {
  const imageMaxCount = Number(raw.imageMaxCount);
  const imageMaxSizeMb = Number(raw.imageMaxSizeMb);
  const attachmentMaxCount = Number(raw.attachmentMaxCount);
  const attachmentMaxSizeMb = Number(raw.attachmentMaxSizeMb);

  return {
    imageMaxCount: Number.isFinite(imageMaxCount) && imageMaxCount > 0
      ? imageMaxCount
      : DEFAULT_SETTINGS.imageMaxCount,
    imageMaxSizeMb: Number.isFinite(imageMaxSizeMb) && imageMaxSizeMb > 0
      ? imageMaxSizeMb
      : DEFAULT_SETTINGS.imageMaxSizeMb,
    attachmentMaxCount:
      Number.isFinite(attachmentMaxCount) && attachmentMaxCount > 0
        ? attachmentMaxCount
        : DEFAULT_SETTINGS.attachmentMaxCount,
    attachmentMaxSizeMb:
      Number.isFinite(attachmentMaxSizeMb) && attachmentMaxSizeMb > 0
        ? attachmentMaxSizeMb
        : DEFAULT_SETTINGS.attachmentMaxSizeMb,
    attachmentDocumentExts: normalizeExtText(
      raw.attachmentDocumentExts,
      DEFAULT_SETTINGS.attachmentDocumentExts,
    ),
    attachmentVideoExts: normalizeExtText(
      raw.attachmentVideoExts,
      DEFAULT_SETTINGS.attachmentVideoExts,
    ),
    attachmentImageExts: normalizeExtText(
      raw.attachmentImageExts,
      DEFAULT_SETTINGS.attachmentImageExts,
    ),
    attachmentArchiveExts: normalizeExtText(
      raw.attachmentArchiveExts,
      DEFAULT_SETTINGS.attachmentArchiveExts,
    ),
  };
}

function readCachedSettings() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return { ...DEFAULT_SETTINGS };
    }
    return normalizeSettings(JSON.parse(raw));
  } catch {
    return { ...DEFAULT_SETTINGS };
  }
}

function persistSettings(settings) {
  const normalized = normalizeSettings(settings);
  localStorage.setItem(STORAGE_KEY, JSON.stringify(normalized));
  window.dispatchEvent(
    new CustomEvent("achievement-upload-settings-updated", {
      detail: normalized,
    }),
  );
  return normalized;
}

function splitExtText(value) {
  return value ? value.split(",").filter(Boolean) : [];
}

export function useAchievementUploadSettings() {
  const settings = reactive(readCachedSettings());
  const loading = shallowRef(false);
  const saving = shallowRef(false);
  const errorMessage = shallowRef("");

  const imageLimitText = computed(
    () => `最多 ${settings.imageMaxCount} 张 · 单张不超过 ${settings.imageMaxSizeMb}MB`,
  );
  const attachmentLimitText = computed(
    () => `最多 ${settings.attachmentMaxCount} 个 · 单个不超过 ${settings.attachmentMaxSizeMb}MB`,
  );
  const attachmentGroups = computed(() => [
    {
      key: "document",
      label: "文档",
      exts: splitExtText(settings.attachmentDocumentExts),
    },
    {
      key: "video",
      label: "视频",
      exts: splitExtText(settings.attachmentVideoExts),
    },
    {
      key: "image",
      label: "图片",
      exts: splitExtText(settings.attachmentImageExts),
    },
    {
      key: "archive",
      label: "压缩包",
      exts: splitExtText(settings.attachmentArchiveExts),
    },
  ]);

  function applySettings(nextSettings) {
    const normalized = persistSettings(nextSettings);
    settings.imageMaxCount = normalized.imageMaxCount;
    settings.imageMaxSizeMb = normalized.imageMaxSizeMb;
    settings.attachmentMaxCount = normalized.attachmentMaxCount;
    settings.attachmentMaxSizeMb = normalized.attachmentMaxSizeMb;
    settings.attachmentDocumentExts = normalized.attachmentDocumentExts;
    settings.attachmentVideoExts = normalized.attachmentVideoExts;
    settings.attachmentImageExts = normalized.attachmentImageExts;
    settings.attachmentArchiveExts = normalized.attachmentArchiveExts;
  }

  async function fetchSettings() {
    loading.value = true;
    errorMessage.value = "";
    try {
      const { data } = await getAchievementUploadSettings();
      applySettings(data || DEFAULT_SETTINGS);
      return settings;
    } catch (error) {
      errorMessage.value =
        error?.response?.data?.message || "加载上传限制失败";
      return settings;
    } finally {
      loading.value = false;
    }
  }

  async function saveSettings(payload) {
    saving.value = true;
    errorMessage.value = "";
    try {
      const { data } = await updateAchievementUploadSettings(payload);
      applySettings(data || payload);
      return { success: true, data: settings };
    } catch (error) {
      errorMessage.value =
        error?.response?.data?.message || "保存上传限制失败";
      return { success: false, error };
    } finally {
      saving.value = false;
    }
  }

  return {
    settings,
    loading,
    saving,
    errorMessage,
    imageLimitText,
    attachmentLimitText,
    attachmentGroups,
    applySettings,
    fetchSettings,
    saveSettings,
  };
}
