import { computed, reactive, shallowRef } from "vue";
import {
  getAchievementUploadSettings,
  updateAchievementUploadSettings,
} from "../api/achievementUploadSettings";

const STORAGE_KEY = "gcsc_achievement_upload_settings";

const DEFAULT_SETTINGS = Object.freeze({
  imageMaxCount: 3,
  imageMaxSizeMb: 10,
  attachmentMaxSizeMb: 50,
});

function normalizeSettings(raw = {}) {
  const imageMaxCount = Number(raw.imageMaxCount);
  const imageMaxSizeMb = Number(raw.imageMaxSizeMb);
  const attachmentMaxSizeMb = Number(raw.attachmentMaxSizeMb);

  return {
    imageMaxCount: Number.isFinite(imageMaxCount) && imageMaxCount > 0
      ? imageMaxCount
      : DEFAULT_SETTINGS.imageMaxCount,
    imageMaxSizeMb: Number.isFinite(imageMaxSizeMb) && imageMaxSizeMb > 0
      ? imageMaxSizeMb
      : DEFAULT_SETTINGS.imageMaxSizeMb,
    attachmentMaxSizeMb:
      Number.isFinite(attachmentMaxSizeMb) && attachmentMaxSizeMb > 0
        ? attachmentMaxSizeMb
        : DEFAULT_SETTINGS.attachmentMaxSizeMb,
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

export function useAchievementUploadSettings() {
  const settings = reactive(readCachedSettings());
  const loading = shallowRef(false);
  const saving = shallowRef(false);
  const errorMessage = shallowRef("");

  const imageLimitText = computed(
    () => `最多 ${settings.imageMaxCount} 张 · 单张不超过 ${settings.imageMaxSizeMb}MB`,
  );
  const attachmentLimitText = computed(
    () => `单个不超过 ${settings.attachmentMaxSizeMb}MB`,
  );

  function applySettings(nextSettings) {
    const normalized = persistSettings(nextSettings);
    settings.imageMaxCount = normalized.imageMaxCount;
    settings.imageMaxSizeMb = normalized.imageMaxSizeMb;
    settings.attachmentMaxSizeMb = normalized.attachmentMaxSizeMb;
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
    applySettings,
    fetchSettings,
    saveSettings,
  };
}
