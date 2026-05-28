import { computed, reactive, ref, watch } from "vue";
import {
  createAchievement,
  updateAchievement,
} from "@/api/achievements";
import { uploadMedia } from "@/api/upload";
import { useUploadProgress } from "@/composables/useUploadProgress";
import { useToast } from "@/composables/useToast";
import {
  categoryFieldMap,
  categoryHints,
  IMAGE_URLS_FIELD,
  ATTACHMENTS_FIELD,
} from "@/constants/achievementConstants";
import { checkAchievementSaveRequiresReview } from "@/config/achievementReviewConfig";
import {
  buildAchievementChanges,
  buildAchievementDraftSourceFromPayload,
  buildAchievementReviewPayloadSnapshot,
} from "@/composables/useAchievementReviewPayload";
import {
  resolveMediaObjectUrl,
  resolveMediaUrl,
  resolveMediaTypeByExtension,
  stripMediaUrl,
} from "@/utils/media";
import { dedupeAchievements } from "@/utils/achievement";
import { normalizeAchievement } from "@/utils/achievementListModel";
import {
  buildAchievementChanges,
  buildAchievementDraftSourceFromPayload,
  buildAchievementReviewPayloadSnapshot,
} from "@/composables/useAchievementReviewPayload";

export function useAchievementEditor({
  profile,
  reviewSettings,
  fetchReviewSettings,
  submitAchievementReviewRequest,
  achievements,
  viewItem,
  activeCategory,
  activeStudentQuery,
  errorMessage,
  fetchAchievements,
  uploadLimitConfig,
  uploadHelpers,
}) {
  const { info: toastInfo, success: toastSuccess, warn: toastWarn } = useToast();
  const { uploadWithProgress } = useUploadProgress();

  const editorOpen = ref(false);
  const editId = ref(null);
  const hintCollapsed = ref(false);

  const form = reactive({
    imageUrl: "",
    imageUrls: [],
    attachments: [],
    category: "",
    fields: {},
  });

  const imagePreviews = ref([]);

  const {
    isAllowedImage,
    isAllowedAttachment,
    isFileSizeAllowed,
    imageMaxCount,
    attachmentMaxCount,
    mediaLimitLabel,
    attachmentLimitLabel,
    allowedAttachmentExtensions,
    imageInput,
    attachmentInput,
    enabledAttachmentTypes,
  } = uploadHelpers;

  const attachmentPreviews = computed(() =>
    (form.attachments || []).map((file) => ({
      ...file,
      url: resolveMediaUrl(file.url),
    })),
  );

  const editorCategory = computed(() => {
    if (form.category) {
      return form.category;
    }
    return activeCategory.value === "all" ? "" : activeCategory.value;
  });

  const activeCategoryHint = computed(() => {
    if (!editorCategory.value) {
      return null;
    }
    return categoryHints[editorCategory.value] || null;
  });

  const activeFormConfig = computed(() => {
    if (!editorCategory.value) {
      return null;
    }
    return categoryFieldMap[editorCategory.value] || null;
  });

  watch(
    () => [...(form.imageUrls || [])],
    async (urls) => {
      imagePreviews.value = await Promise.all(
        urls.map((url) =>
          resolveMediaObjectUrl(url).catch(() => ""),
        ),
      );
    },
    { immediate: true },
  );

  watch(
    () => form.category,
    () => {
      applyFieldDefaults();
    },
  );

  function getCurrentStudentNo() {
    const { studentNo } = activeStudentQuery.value;
    if (studentNo) {
      return studentNo;
    }
    return profile.studentNo || "";
  }

  function getCurrentStudentName() {
    const { studentName } = activeStudentQuery.value;
    if (studentName) {
      return studentName;
    }
    return profile.displayName || profile.username || "";
  }

  function applyFieldDefaults() {
    const config = activeFormConfig.value;
    if (!config) {
      return;
    }
    const hasStudentNo = config.fields.some((field) => field.key === "studentNo");
    const hasStudentName = config.fields.some(
      (field) => field.key === "studentName",
    );
    if (hasStudentNo && !form.fields.studentNo) {
      form.fields.studentNo = getCurrentStudentNo();
    }
    if (hasStudentName && !form.fields.studentName) {
      form.fields.studentName = getCurrentStudentName();
    }
  }

  function resetForm() {
    form.imageUrl = "";
    form.imageUrls = [];
    form.attachments = [];
    form.category = "";
    form.fields = {};
  }

  function openEditorForCategory() {
    editId.value = null;
    resetForm();
    form.category = activeCategory.value === "all" ? "" : activeCategory.value;
    applyFieldDefaults();
    editorOpen.value = true;
  }

  function closeEditor() {
    editorOpen.value = false;
    editId.value = null;
  }

  function openEditorFromItem(item) {
    editId.value = item.id;
    form.category = item.category || "";
    form.fields = { ...(item.fields || {}) };
    form.imageUrls = (item.rawImageUrls || item.imageUrls || [])
      .map((url) => stripMediaUrl(url))
      .filter(Boolean);
    form.imageUrl = form.imageUrls[0] || "";
    form.attachments = (item.rawAttachments || item.attachments || []).map(
      (entry) => ({
        ...entry,
        url: stripMediaUrl(entry.url),
      }),
    );
    applyFieldDefaults();
    editorOpen.value = true;
  }

  function triggerImage() {
    imageInput.value?.click();
  }

  function triggerAttachment() {
    attachmentInput.value?.click();
  }

  function selectEditorImage(index) {
    if (index < 0 || index >= form.imageUrls.length) {
      return;
    }
    const [selected] = form.imageUrls.splice(index, 1);
    form.imageUrls.unshift(selected);
  }

  function removeImage(index) {
    form.imageUrls.splice(index, 1);
  }

  function removeAttachment(index) {
    form.attachments.splice(index, 1);
  }

  async function onImageChange(event) {
    const files = Array.from(event.target.files || []);
    event.target.value = "";
    if (!files.length) {
      return;
    }
    const remaining = imageMaxCount.value - form.imageUrls.length;
    if (remaining <= 0) {
      errorMessage.value = `最多上传${imageMaxCount.value}张图片`;
      return;
    }
    const uploadList = files.slice(0, remaining);
    for (const file of uploadList) {
      if (!isAllowedImage(file)) {
        errorMessage.value = "仅支持 jpeg/jpg/png/heif 图片格式";
        continue;
      }
      if (!isFileSizeAllowed(file, uploadLimitConfig.mediaMaxMB)) {
        errorMessage.value = `图片大小不可超过 ${mediaLimitLabel.value}`;
        continue;
      }
      try {
        const { data } = await uploadWithProgress(file, uploadMedia, {
          context: "achievement-image",
        });
        if (data?.url) {
          form.imageUrls.push(data.url);
        }
      } catch (err) {
        errorMessage.value =
          err?.response?.data?.message || "图片上传失败";
      }
    }
  }

  async function onAttachmentChange(event) {
    const files = Array.from(event.target.files || []);
    event.target.value = "";
    if (!files.length) {
      return;
    }
    const remaining = attachmentMaxCount.value - form.attachments.length;
    if (remaining <= 0) {
      errorMessage.value = `最多上传${attachmentMaxCount.value}个附件`;
      return;
    }
    const uploadList = files.slice(0, remaining);
    for (const file of uploadList) {
      if (!isAllowedAttachment(file)) {
        errorMessage.value = allowedAttachmentExtensions.value.length
          ? `当前仅支持 ${allowedAttachmentExtensions.value.join(" / ")} 格式附件`
          : "当前未开放附件上传";
        continue;
      }
      if (!isFileSizeAllowed(file, uploadLimitConfig.attachmentMaxMB)) {
        errorMessage.value = `附件大小不可超过 ${attachmentLimitLabel.value}`;
        continue;
      }
      try {
        const { data } = await uploadWithProgress(file, uploadMedia, {
          context: "achievement-attachment",
        });
        if (data?.url) {
          form.attachments.push({
            url: data.url,
            name: data.originalName || file.name,
            mediaType:
              data.mediaType || resolveMediaTypeByExtension(file.name),
          });
        }
      } catch (err) {
        errorMessage.value =
          err?.response?.data?.message || "附件上传失败";
      }
    }
  }

  async function saveAchievement() {
    const config = activeFormConfig.value;
    if (!config) {
      toastWarn("请先选择成果分类");
      return;
    }
    const category =
      form.category ||
      (activeCategory.value === "all" ? "" : activeCategory.value);
    if (!category) {
      toastWarn("请先选择成果分类");
      return;
    }
    const titleKey = config.titleKey;
    const titleValue = (form.fields[titleKey] || "").trim();
    if (!titleValue) {
      errorMessage.value = "请填写必填项";
      return;
    }
    const payload = {
      imageUrl: form.imageUrls[0] || form.imageUrl || null,
      fields: {
        ...form.fields,
        [IMAGE_URLS_FIELD]: JSON.stringify(form.imageUrls || []),
        [ATTACHMENTS_FIELD]: JSON.stringify(form.attachments || []),
      },
    };
    const existingItem = editId.value
      ? achievements.value.find((item) => item.id === editId.value) || null
      : null;
    const changes = buildAchievementChanges({
      category,
      payload,
      existingItem,
    });
    try {
      if (fetchReviewSettings) {
        await fetchReviewSettings().catch(() => {});
      }
      const requiresReview = checkAchievementSaveRequiresReview(
        reviewSettings.achievementReviewEnabled,
        profile.role || "STUDENT",
      );
      if (requiresReview) {
        const reviewRequest = await submitAchievementReviewRequest({
          actor: profile,
          action: editId.value ? "update" : "create",
          category,
          title: titleValue,
          payload,
          payloadSnapshot: buildAchievementReviewPayloadSnapshot({
            category,
            beforeItem: existingItem,
            afterItem: buildAchievementDraftSourceFromPayload(payload),
          }),
          recordId: editId.value,
          changes,
        });
        if (
          reviewSettings.achievementReviewAutoApprove &&
          reviewRequest?.status === "approved"
        ) {
          await fetchAchievements();
          toastSuccess("个人成果已更新");
        } else {
          toastInfo("已提交审核，请等待审核成功后显示");
        }
        resetForm();
        closeEditor();
        errorMessage.value = "";
        return;
      }

      const imageMaxCount = uploadLimitConfig?.imageMaxCount ?? 9;
      if (editId.value) {
        const { data } = await updateAchievement(category, editId.value, payload);
        const normalizedData = normalizeAchievement(data, imageMaxCount);
        achievements.value = dedupeAchievements(
          achievements.value.map((item) =>
            item.id === data.id ? normalizedData : item,
          ),
        );
        if (viewItem.value && viewItem.value.id === data.id) {
          viewItem.value = normalizedData;
        }
      } else {
        const { data } = await createAchievement(category, payload);
        const normalizedData = normalizeAchievement(data, imageMaxCount);
        achievements.value = dedupeAchievements([
          normalizedData,
          ...achievements.value,
        ]);
      }
      toastSuccess("保存成功");
      resetForm();
      closeEditor();
      errorMessage.value = "";
    } catch (err) {
      errorMessage.value =
        err?.response?.data?.message || "保存失败，请重新登录";
    }
  }

  return {
    editorOpen,
    editId,
    hintCollapsed,
    form,
    imagePreviews,
    attachmentPreviews,
    editorCategory,
    activeCategoryHint,
    activeFormConfig,
    imageMaxCount,
    attachmentMaxCount,
    mediaLimitLabel,
    attachmentLimitLabel,
    enabledAttachmentTypes,
    imageInput,
    attachmentInput,
    openEditorForCategory,
    closeEditor,
    openEditorFromItem,
    resetForm,
    saveAchievement,
    triggerImage,
    triggerAttachment,
    onImageChange,
    onAttachmentChange,
    selectEditorImage,
    removeImage,
    removeAttachment,
  };
}
