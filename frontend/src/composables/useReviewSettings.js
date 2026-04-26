import { reactive, shallowRef } from "vue";
import { getReviewSettings, updateReviewSettings } from "../api/reviewSettings";

const STORAGE_KEY = "bdai_sc_review_settings";

const DEFAULT_SETTINGS = Object.freeze({
  profileReviewEnabled: true,
  profileReviewAutoApprove: false,
  achievementReviewEnabled: true,
  achievementReviewAutoApprove: false,
});

function normalizeSettings(raw = {}) {
  return {
    profileReviewEnabled: raw.profileReviewEnabled !== false,
    profileReviewAutoApprove: Boolean(raw.profileReviewAutoApprove),
    achievementReviewEnabled: raw.achievementReviewEnabled !== false,
    achievementReviewAutoApprove: Boolean(raw.achievementReviewAutoApprove),
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
  return normalized;
}

export function useReviewSettings() {
  const settings = reactive(readCachedSettings());
  const loading = shallowRef(false);
  const saving = shallowRef(false);
  const errorMessage = shallowRef("");

  function applySettings(nextSettings) {
    const normalized = persistSettings(nextSettings);
    settings.profileReviewEnabled = normalized.profileReviewEnabled;
    settings.profileReviewAutoApprove = normalized.profileReviewAutoApprove;
    settings.achievementReviewEnabled = normalized.achievementReviewEnabled;
    settings.achievementReviewAutoApprove = normalized.achievementReviewAutoApprove;
  }

  async function fetchSettings() {
    loading.value = true;
    errorMessage.value = "";
    try {
      const { data } = await getReviewSettings();
      applySettings(data || DEFAULT_SETTINGS);
      return settings;
    } catch (error) {
      errorMessage.value = error?.response?.data?.message || "加载审核设置失败";
      return settings;
    } finally {
      loading.value = false;
    }
  }

  async function saveSettings(payload) {
    saving.value = true;
    errorMessage.value = "";
    try {
      const { data } = await updateReviewSettings(payload);
      applySettings(data || payload);
      return { success: true, data: settings };
    } catch (error) {
      errorMessage.value = error?.response?.data?.message || "保存审核设置失败";
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
    applySettings,
    fetchSettings,
    saveSettings,
  };
}
