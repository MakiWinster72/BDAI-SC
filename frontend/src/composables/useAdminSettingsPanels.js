import { computed, reactive, shallowRef } from "vue";
import { useAchievementUploadSettings } from "@/composables/useAchievementUploadSettings";
import { useReviewSettings } from "@/composables/useReviewSettings";
import { useToast } from "@/composables/useToast";

export function useAdminSettingsPanels(activeSection) {
  const { success } = useToast();
  const saveMessage = shallowRef("");

  const {
    settings,
    saving: uploadSaving,
    errorMessage: uploadErrorMessage,
    fetchSettings: fetchUploadSettings,
    saveSettings: saveUploadSettings,
  } = useAchievementUploadSettings();

  const {
    settings: reviewSettings,
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
    supportingDocMaxCount: settings.supportingDocMaxCount,
    supportingDocMaxSizeMb: settings.supportingDocMaxSizeMb,
  });

  const reviewForm = reactive({
    profileReviewEnabled: reviewSettings.profileReviewEnabled,
    profileReviewAutoApprove: reviewSettings.profileReviewAutoApprove,
    achievementReviewEnabled: reviewSettings.achievementReviewEnabled,
    achievementReviewAutoApprove: reviewSettings.achievementReviewAutoApprove,
  });

  const activeSaving = computed(() =>
    activeSection.value === "upload" ? uploadSaving.value : reviewSaving.value,
  );

  const activeErrorMessage = computed(() =>
    activeSection.value === "upload"
      ? uploadErrorMessage.value
      : reviewErrorMessage.value,
  );

  function syncFormFromSettings() {
    form.imageMaxCount = settings.imageMaxCount;
    form.imageMaxSizeMb = settings.imageMaxSizeMb;
    form.attachmentMaxCount = settings.attachmentMaxCount;
    form.attachmentMaxSizeMb = settings.attachmentMaxSizeMb;
    form.attachmentDocumentExts = settings.attachmentDocumentExts;
    form.attachmentVideoExts = settings.attachmentVideoExts;
    form.attachmentImageExts = settings.attachmentImageExts;
    form.attachmentArchiveExts = settings.attachmentArchiveExts;
    form.supportingDocMaxCount = settings.supportingDocMaxCount;
    form.supportingDocMaxSizeMb = settings.supportingDocMaxSizeMb;
  }

  function syncReviewFormFromSettings() {
    reviewForm.profileReviewEnabled = reviewSettings.profileReviewEnabled;
    reviewForm.profileReviewAutoApprove = reviewSettings.profileReviewAutoApprove;
    reviewForm.achievementReviewEnabled = reviewSettings.achievementReviewEnabled;
    reviewForm.achievementReviewAutoApprove =
      reviewSettings.achievementReviewAutoApprove;
  }

  function updateUploadFormField({ key, value }) {
    form[key] = value;
  }

  function updateReviewFormField({ key, value }) {
    reviewForm[key] = value;
  }

  async function loadSettingsPanels() {
    await Promise.all([fetchUploadSettings(), fetchReviewSettings()]);
    syncFormFromSettings();
    syncReviewFormFromSettings();
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
      supportingDocMaxCount: Number(form.supportingDocMaxCount),
      supportingDocMaxSizeMb: Number(form.supportingDocMaxSizeMb),
    });
    if (result.success) {
      saveMessage.value = "上传限制已更新，成就页面会同步显示。";
      syncFormFromSettings();
      success("上传限制设置已保存");
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
      success("审核策略设置已保存");
    }
  }

  return {
    saveMessage,
    form,
    reviewForm,
    uploadSaving,
    uploadErrorMessage,
    activeSaving,
    activeErrorMessage,
    syncFormFromSettings,
    syncReviewFormFromSettings,
    updateUploadFormField,
    updateReviewFormField,
    loadSettingsPanels,
    handleSubmit,
    handleReviewSubmit,
  };
}
