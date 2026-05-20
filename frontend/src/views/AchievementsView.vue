<template>
  <main class="dashboard-right">
    <header class="feed-header">
      <h1 class="feed-title">个人成果</h1>
    </header>

    <div v-if="!filteredAchievements.length" class="empty-tip">
      {{ emptyMessage }}
    </div>
    <div v-if="errorMessage" class="form-tip">{{ errorMessage }}</div>

    <section class="achievement-list">
      <article
        v-for="item in filteredAchievements"
        :key="item.id"
        class="achievement-card"
        :class="{
          'achievement-card-paper': item.category === 'paper',
          'achievement-card-journal': item.category === 'journal',
          'achievement-card-patent': item.category === 'patent',
          'achievement-card-certificate': item.category === 'certificate',
          'achievement-card-research': item.category === 'research',
          'achievement-card-works': item.category === 'works',
        }"
        @click="openDetail(item)"
      >
        <div class="achievement-card-image">
          <img v-if="item.image" :src="item.image" alt="成就图片" />
          <div v-else class="achievement-card-placeholder">未上传图片</div>
        </div>
        <AchievementCardBody :item="item" />
      </article>
    </section>

    <button
      class="achievement-add"
      type="button"
      :aria-label="addButtonLabel"
      @click="openEditorForCategory"
    >
      <span aria-hidden="true">+</span>
    </button>

    <MobileCapsule
      :hidden="editorOpen || viewOpen"
      @open-sidebar="openDashboardSidebar"
    >
      <template #right>
        <div
          class="capsule-action capsule-primary"
          role="button"
          tabindex="0"
          :aria-label="addButtonLabel"
          @click="openEditorForCategory"
        >
          +
        </div>
      </template>
    </MobileCapsule>

    <AchievementViewPanel
      v-if="viewOpen || viewClosing"
      :view-closing="viewClosing"
      :view-exit-up="viewExitUp"
      :view-loading="viewLoading"
      :view-item="viewItem"
      :attachment-icon="attachmentIcon"
      :is-video-file="isVideoFile"
      :is-document-file="isDocumentFile"
      :is-sheet-file="isSheetFile"
      :is-pdf-file="isPdfFile"
      :is-allowed-image="isAllowedImage"
      :is-pptx-file="isPptxFile"
      @close="closeView"
      @preview="(urls, index, hint) => showPreview(urls, index, hint)"
      @download="downloadAttachment"
      @edit="editFromView"
      @delete="openDelete"
    />

    <AchievementEditorSheet
      v-if="editorOpen"
      :edit-id="editId"
      :form="form"
      :image-previews="imagePreviews"
      :hint-collapsed="hintCollapsed"
      :active-category-hint="activeCategoryHint"
      :active-form-config="activeFormConfig"
      :category-options="categoryOptions"
      :attachment-previews="attachmentPreviews"
      :attachment-icon="attachmentIcon"
      :image-max-count="imageMaxCount"
      :attachment-max-count="attachmentMaxCount"
      :media-limit-label="mediaLimitLabel"
      :attachment-limit-label="attachmentLimitLabel"
      :enabled-attachment-types="enabledAttachmentTypes"
      @close="closeEditor"
      @save="saveAchievement"
      @toggle-hint="toggleEditorHint"
      @trigger-image="triggerImage"
      @trigger-attachment="triggerAttachment"
      @select-image="selectEditorImage"
      @remove-image="removeImage"
      @remove-attachment="removeAttachment"
    />

    <AchievementPreviewViewer
      v-if="previewVisible"
      :preview-images="previewImages"
      :preview-index="previewIndex"
      :preview-type="previewType"
      :preview-content="previewContent"
      :preview-loading="previewLoading"
      :slide-direction="slideDirection"
      @hide="hidePreview"
      @prev="previewPrev"
      @next="previewNext"
      @dot="goToPreviewDot"
    />

    <AchievementDeleteDialog
      :delete-dialog-open="deleteDialogOpen"
      :delete-busy="deleteBusy"
      @close="closeDelete"
      @confirm="confirmDelete"
    />

    <input
      ref="imageInputEl"
      type="file"
      accept=".jpeg,.jpg,.png,.heif,image/jpeg,image/png,image/heif"
      multiple
      hidden
      @change="onImageChange"
    />
    <input
      ref="attachmentInputEl"
      type="file"
      accept=".docx,.doc,.pdf,.xls,.xlsx,.zip,.rar,.7z,.pptx,.ppt,.mp4,.mov,.jpeg,.jpg,.png,.heif,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/pdf,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-powerpoint,application/vnd.openxmlformats-officedocument.presentationml.presentation,video/mp4,video/quicktime,image/jpeg,image/png,image/heif"
      multiple
      hidden
      @change="onAttachmentChange"
    />
  </main>
</template>

<script setup>
import { onBeforeUnmount, onMounted, reactive, ref, toRefs, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import MobileCapsule from "@/components/MobileCapsule.vue";
import AchievementCardBody from "@/components/AchievementCardBody.vue";
import AchievementViewPanel from "@/components/achievement/AchievementViewPanel.vue";
import AchievementEditorSheet from "@/components/achievement/AchievementEditorSheet.vue";
import AchievementPreviewViewer from "@/components/achievement/AchievementPreviewViewer.vue";
import AchievementDeleteDialog from "@/components/achievement/AchievementDeleteDialog.vue";
import { useDashboardShell } from "@/composables/useDashboardShell";
import { useNotifications } from "@/composables/useNotifications";
import { useReviewSettings } from "@/composables/useReviewSettings";
import { useAchievementUploadSettings } from "@/composables/useAchievementUploadSettings";
import { useAchievementUpload } from "@/composables/useAchievementUpload";
import { useAchievementList } from "@/composables/useAchievementList";
import { useAchievementPreview } from "@/composables/useAchievementPreview";
import { useAchievementView } from "@/composables/useAchievementView";
import { useAchievementEditor } from "@/composables/useAchievementEditor";
import { attachmentIcon } from "@/utils/achievement";
import {
  downloadMedia,
  isVideoFile,
  isDocumentFile,
  isSheetFile,
  isPdfFile,
  isPptxFile,
  revokePrivateMediaObjectUrls,
} from "@/utils/media";
import { loadUser } from "@/utils/userStorage";

const router = useRouter();
const route = useRoute();
const { openSidebar: openDashboardSidebar } = useDashboardShell();
const profile = reactive(loadUser());
const { submitAchievementReviewRequest, findPendingAchievementReview } =
  useNotifications(profile);
const { settings: reviewSettings, fetchSettings: fetchReviewSettings } =
  useReviewSettings();
const errorMessage = ref("");

const {
  settings: achievementUploadSettings,
  fetchSettings: fetchAchievementUploadSettings,
} = useAchievementUploadSettings();

const uploadHelpers = useAchievementUpload();
const { uploadLimitConfig, setUploadLimits, isAllowedImage, imageInput, attachmentInput } =
  uploadHelpers;

const imageInputEl = ref(null);
const attachmentInputEl = ref(null);

const listApi = useAchievementList({
  route,
  router,
  errorMessage,
  uploadLimitConfig,
});
const {
  filteredAchievements,
  emptyMessage,
  addButtonLabel,
  categoryOptions,
} = toRefs(listApi);
const { fetchAchievements, syncCategoryFromRoute } = listApi;

const viewItem = ref(null);

const previewApi = useAchievementPreview();
const {
  previewImages,
  previewIndex,
  previewVisible,
  previewType,
  previewContent,
  previewLoading,
  slideDirection,
} = toRefs(previewApi);
const {
  showPreview,
  hidePreview,
  previewPrev,
  previewNext,
  goToPreviewDot,
  bindSheetSwitcher,
} = previewApi;

const editorApi = useAchievementEditor({
  profile,
  reviewSettings,
  submitAchievementReviewRequest,
  achievements: listApi.achievements,
  viewItem,
  activeCategory: listApi.activeCategory,
  activeStudentQuery: listApi.activeStudentQuery,
  errorMessage,
  fetchAchievements,
  uploadLimitConfig,
  uploadHelpers,
});
const {
  editorOpen,
  editId,
  hintCollapsed,
  imagePreviews,
  imageMaxCount,
  attachmentMaxCount,
  mediaLimitLabel,
  attachmentLimitLabel,
  attachmentPreviews,
  activeCategoryHint,
  activeFormConfig,
  enabledAttachmentTypes,
} = toRefs(editorApi);
const {
  form,
  openEditorForCategory,
  closeEditor,
  openEditorFromItem,
  saveAchievement,
  triggerImage,
  triggerAttachment,
  onImageChange,
  onAttachmentChange,
  selectEditorImage,
  removeImage,
  removeAttachment,
} = editorApi;

const viewApi = useAchievementView({
  achievements: listApi.achievements,
  errorMessage,
  findPendingAchievementReview,
  onEditFromView: openEditorFromItem,
  viewItem,
});
const {
  viewOpen,
  viewClosing,
  viewExitUp,
  viewLoading,
  deleteDialogOpen,
  deleteBusy,
} = toRefs(viewApi);
const {
  openDetail,
  closeView,
  editFromView,
  openDelete,
  closeDelete,
  confirmDelete,
} = viewApi;

function toggleEditorHint() {
  hintCollapsed.value = !hintCollapsed.value;
}

watch(imageInputEl, (el) => {
  imageInput.value = el;
});
watch(attachmentInputEl, (el) => {
  attachmentInput.value = el;
});

function downloadAttachment(file) {
  if (!file?.url) {
    return;
  }
  downloadMedia(file.url, file.name || "附件").catch(() => {
    errorMessage.value = "附件下载失败";
  });
}

function handleUploadSettingsUpdated(event) {
  const nextSettings = event.detail || {};
  setUploadLimits({
    imageMaxCount: nextSettings.imageMaxCount,
    mediaMaxMB: nextSettings.imageMaxSizeMb,
    attachmentMaxCount: nextSettings.attachmentMaxCount,
    attachmentMaxMB: nextSettings.attachmentMaxSizeMb,
    attachmentDocumentExts: nextSettings.attachmentDocumentExts,
    attachmentVideoExts: nextSettings.attachmentVideoExts,
    attachmentImageExts: nextSettings.attachmentImageExts,
    attachmentArchiveExts: nextSettings.attachmentArchiveExts,
  });
}

onMounted(() => {
  syncCategoryFromRoute();
  fetchReviewSettings().catch(() => {});
  fetchAchievementUploadSettings().then(() => {
    setUploadLimits({
      imageMaxCount: achievementUploadSettings.imageMaxCount,
      mediaMaxMB: achievementUploadSettings.imageMaxSizeMb,
      attachmentMaxCount: achievementUploadSettings.attachmentMaxCount,
      attachmentMaxMB: achievementUploadSettings.attachmentMaxSizeMb,
      attachmentDocumentExts: achievementUploadSettings.attachmentDocumentExts,
      attachmentVideoExts: achievementUploadSettings.attachmentVideoExts,
      attachmentImageExts: achievementUploadSettings.attachmentImageExts,
      attachmentArchiveExts: achievementUploadSettings.attachmentArchiveExts,
    });
  });
  fetchAchievements();
  bindSheetSwitcher();
  window.addEventListener(
    "achievement-upload-settings-updated",
    handleUploadSettingsUpdated,
  );
});

onBeforeUnmount(() => {
  revokePrivateMediaObjectUrls();
  window.removeEventListener(
    "achievement-upload-settings-updated",
    handleUploadSettingsUpdated,
  );
});

watch(
  () => [route.query.category, route.query.studentNo, route.query.studentName],
  () => {
    syncCategoryFromRoute();
    fetchAchievements();
  },
);
</script>
