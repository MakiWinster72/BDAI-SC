/**
 * useAchievementEditor - 成就编辑器 composable
 *
 * 管理编辑/新增表单状态、图片/附件上传、审核提交流程。
 */
import { computed, reactive, ref } from "vue";
import { createAchievement, updateAchievement } from "../api/achievements";
import { uploadMedia } from "../api/upload";
import {
  resolveMediaUrl,
  stripMediaUrl,
  resolveMediaTypeByExtension,
} from "../utils/media";
import {
  categoryFieldMap,
  categoryHints,
  achievementEntries,
  IMAGE_URLS_FIELD,
  ATTACHMENTS_FIELD,
} from "../constants/achievementConstants";

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

const imagePreviews = computed(() =>
  form.imageUrls.map((url) => resolveMediaUrl(url)),
);
const attachmentPreviews = computed(() =>
  form.attachments.map((a) => ({
    ...a,
    url: resolveMediaUrl(a.url),
  })),
);

const categoryOptions = computed(() =>
  achievementEntries.filter((entry) => entry.key !== "all"),
);

const editorCategory = computed(() => {
  if (form.category) return form.category;
  return "";
});

const activeCategoryHint = computed(() => {
  if (!editorCategory.value) return null;
  return categoryHints[editorCategory.value] || null;
});

const activeFormConfig = computed(() => {
  if (!editorCategory.value) return null;
  return categoryFieldMap[editorCategory.value] || null;
});

function openEditorForCategory(activeCategory) {
  editId.value = null;
  resetForm();
  if (activeCategory && activeCategory !== "all") {
    form.category = activeCategory;
  }
  applyFieldDefaults();
  editorOpen.value = true;
}

function openEditorForEdit(item) {
  editId.value = item.id;
  form.category = item.category || "";
  form.fields = { ...(item.fields || {}) };
  form.imageUrls = (item.imageUrls || [])
    .map((url) => stripMediaUrl(url))
    .filter(Boolean);
  form.imageUrl = form.imageUrls[0] || "";
  form.attachments = (item.attachments || []).map((entry) => ({
    ...entry,
    url: stripMediaUrl(entry.url),
  }));
  applyFieldDefaults();
  editorOpen.value = true;
}

function closeEditor() {
  editorOpen.value = false;
  editId.value = null;
}

function resetForm() {
  form.imageUrl = "";
  form.imageUrls = [];
  form.attachments = [];
  form.category = "";
  form.fields = {};
}

function applyFieldDefaults() {
  const config = activeFormConfig.value;
  if (!config) return;
  const hasStudentName =
    config.fields.some((f) => f.key === "studentName") &&
    form.fields.studentName;
  const hasStudentNo =
    config.fields.some((f) => f.key === "studentNo") &&
    form.fields.studentNo;
  if (!hasStudentName) {
    const nameField = config.fields.find((f) => f.key === "studentName");
    if (nameField) {
      form.fields.studentName = "";
    }
  }
  if (!hasStudentNo) {
    const noField = config.fields.find((f) => f.key === "studentNo");
    if (noField) {
      form.fields.studentNo = "";
    }
  }
}

function triggerImage(imageInput) {
  imageInput?.value?.click();
}

function triggerAttachment(attachmentInput) {
  attachmentInput?.value?.click();
}

async function onImageChange(
  event,
  { uploadLimitConfig, mediaLimitLabel, imageMaxCount, isAllowedImage, isFileSizeAllowed, uploadMedia: doUpload },
  errorMessage,
) {
  const files = Array.from(event.target.files || []);
  event.target.value = "";
  if (!files.length) return;
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
      const { data } = await doUpload(file, { type: "image" });
      form.imageUrls.push(data.url);
      if (!form.imageUrl) {
        form.imageUrl = data.url;
      }
    } catch {
      errorMessage.value = "图片上传失败";
    }
  }
}

async function onAttachmentChange(
  event,
  { uploadLimitConfig, attachmentLimitLabel, attachmentMaxCount, isAllowedAttachment, isFileSizeAllowed, uploadMedia: doUpload, resolveAttachmentTypeByExtension },
  errorMessage,
) {
  const files = Array.from(event.target.files || []);
  event.target.value = "";
  if (!files.length) return;
  const remaining = attachmentMaxCount.value - form.attachments.length;
  if (remaining <= 0) {
    errorMessage.value = `最多上传${attachmentMaxCount.value}个附件`;
    return;
  }
  const uploadList = files.slice(0, remaining);
  for (const file of uploadList) {
    if (!isAllowedAttachment(file)) {
      errorMessage.value = "不支持此文件格式";
      continue;
    }
    if (!isFileSizeAllowed(file, uploadLimitConfig.attachmentMaxMB)) {
      errorMessage.value = `附件大小不可超过 ${attachmentLimitLabel.value}`;
      continue;
    }
    try {
      const { data } = await doUpload(file, { type: "attachment" });
      form.attachments.push({
        url: data.url,
        name: data.originalName || file.name,
        mediaType: data.mediaType || resolveAttachmentTypeByExtension(file.name),
      });
    } catch {
      errorMessage.value = "附件上传失败";
    }
  }
}

function selectEditorImage(index) {
  if (index > 0 && index < form.imageUrls.length) {
    const [selected] = form.imageUrls.splice(index, 1);
    form.imageUrls.unshift(selected);
    form.imageUrl = selected;
  }
}

function removeImage(index) {
  form.imageUrls.splice(index, 1);
  form.imageUrl = form.imageUrls[0] || "";
}

function removeAttachment(index) {
  form.attachments.splice(index, 1);
}

async function saveAchievement({
  profile,
  reviewSettings,
  achievements,
  submitAchievementReviewRequest,
  fetchAchievements,
  getCurrentStudentNo,
  errorMessage,
}) {
  const config = activeFormConfig.value;
  if (!config) {
    errorMessage.value = "请先选择成就分类";
    return;
  }
  const titleKey = config.titleKey;
  const title = form.fields[titleKey];
  if (!title || !title.trim()) {
    errorMessage.value = "请填写标题";
    return;
  }

  const category = form.category;
  const isNew = !editId.value;
  const fields = { ...form.fields };
  if (form.imageUrls.length > 0) {
    fields[IMAGE_URLS_FIELD] = JSON.stringify(form.imageUrls);
  }
  if (form.attachments.length > 0) {
    fields[ATTACHMENTS_FIELD] = JSON.stringify(
      form.attachments.map((a) => ({
        url: a.url,
        name: a.name,
        mediaType: a.mediaType,
      })),
    );
  }

  const payload = { category, fields };
  let savedItem;
  try {
    if (isNew) {
      const data = await createAchievement(payload);
      savedItem = data;
    } else {
      const data = await updateAchievement(editId.value, payload);
      savedItem = data;
    }
    const shouldSubmitReview =
      reviewSettings?.enabled &&
      (isNew || !reviewSettings.autoApproveEdits);
    if (shouldSubmitReview) {
      const existingItem = isNew
        ? null
        : achievements.value.find((a) => a.id === editId.value);
      await submitAchievementReviewRequest({
        type: isNew ? "create" : "update",
        category,
        title,
        payload,
        existingItem: existingItem || null,
      });
      errorMessage.value = "";
    }
    achievements.value = [...achievements.value, savedItem].filter(Boolean);
    resetForm();
    closeEditor();
    if (fetchAchievements) {
      fetchAchievements();
    }
  } catch (err) {
    errorMessage.value = isNew ? "添加失败" : "保存失败";
    console.error("saveAchievement error", err);
  }
}

export function useAchievementEditor() {
  return {
    // state
    editorOpen,
    editId,
    hintCollapsed,
    form,
    // computed
    imagePreviews,
    attachmentPreviews,
    categoryOptions,
    editorCategory,
    activeCategoryHint,
    activeFormConfig,
    // methods
    openEditorForCategory,
    openEditorForEdit,
    closeEditor,
    resetForm,
    applyFieldDefaults,
    triggerImage,
    triggerAttachment,
    onImageChange,
    onAttachmentChange,
    selectEditorImage,
    removeImage,
    removeAttachment,
    saveAchievement,
  };
}
