<template>
  <main class="dashboard-right">
    <header class="feed-header">
      <h1 class="feed-title">个人成果</h1>
    </header>

    <div v-if="!list.filteredAchievements.length" class="empty-tip">
      {{ list.emptyMessage }}
    </div>
    <div v-if="errorMessage" class="form-tip">{{ errorMessage }}</div>

    <section class="achievement-list">
      <article
        v-for="item in list.filteredAchievements"
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
        @click="view.openDetail(item)"
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
      :aria-label="list.addButtonLabel"
      @click="editor.openEditorForCategory"
    >
      <span aria-hidden="true">+</span>
    </button>

    <MobileCapsule
      :hidden="editor.editorOpen || view.viewOpen"
      @open-sidebar="openDashboardSidebar"
    >
      <template #right>
        <div
          class="capsule-action capsule-primary"
          role="button"
          tabindex="0"
          :aria-label="list.addButtonLabel"
          @click="editor.openEditorForCategory"
        >
          +
        </div>
      </template>
    </MobileCapsule>

    <AchievementViewPanel
      v-if="view.viewOpen || view.viewClosing"
      :view-closing="view.viewClosing"
      :view-exit-up="view.viewExitUp"
      :view-loading="view.viewLoading"
      :view-item="viewItem"
      :attachment-icon="attachmentIcon"
      :is-video-file="isVideoFile"
      :is-document-file="isDocumentFile"
      :is-sheet-file="isSheetFile"
      :is-pdf-file="isPdfFile"
      :is-allowed-image="isAllowedImage"
      :is-pptx-file="isPptxFile"
      @close="view.closeView"
      @preview="(urls, index, hint) => preview.showPreview(urls, index, hint)"
      @download="downloadAttachment"
      @edit="view.editFromView"
      @delete="view.openDelete"
    />

    <AchievementEditorSheet
      v-if="editor.editorOpen"
      :edit-id="editor.editId"
      :form="editor.form"
      :image-previews="editor.imagePreviews"
      :hint-collapsed="editor.hintCollapsed"
      :active-category-hint="editor.activeCategoryHint"
      :active-form-config="editor.activeFormConfig"
      :category-options="list.categoryOptions"
      :attachment-previews="editor.attachmentPreviews"
      :attachment-icon="attachmentIcon"
      :image-max-count="editor.imageMaxCount"
      :attachment-max-count="editor.attachmentMaxCount"
      :media-limit-label="editor.mediaLimitLabel"
      :attachment-limit-label="editor.attachmentLimitLabel"
      :enabled-attachment-types="editor.enabledAttachmentTypes"
      :save-action-label="achievementSaveActionLabel"
      @close="editor.closeEditor"
      @save="editor.saveAchievement"
      @toggle-hint="toggleEditorHint"
      @trigger-image="editor.triggerImage"
      @trigger-attachment="editor.triggerAttachment"
      @select-image="editor.selectEditorImage"
      @remove-image="editor.removeImage"
      @remove-attachment="editor.removeAttachment"
    />

    <AchievementPreviewViewer
      v-if="preview.previewVisible"
      :preview-images="preview.previewImages"
      :preview-index="preview.previewIndex"
      :preview-type="preview.previewType"
      :preview-content="preview.previewContent"
      :preview-loading="preview.previewLoading"
      :slide-direction="preview.slideDirection"
      @hide="preview.hidePreview"
      @prev="preview.previewPrev"
      @next="preview.previewNext"
      @dot="preview.goToPreviewDot"
    />

    <AchievementDeleteDialog
      :delete-dialog-open="view.deleteDialogOpen"
      :delete-busy="view.deleteBusy"
      @close="view.closeDelete"
      @confirm="view.confirmDelete"
    />

    <input
      ref="imageInputEl"
      type="file"
      accept=".jpeg,.jpg,.png,.heif,image/jpeg,image/png,image/heif"
      multiple
      hidden
      @change="editor.onImageChange"
    />
    <input
      ref="attachmentInputEl"
      type="file"
      accept=".docx,.doc,.pdf,.xls,.xlsx,.zip,.rar,.7z,.pptx,.ppt,.mp4,.mov,.jpeg,.jpg,.png,.heif,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/pdf,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-powerpoint,application/vnd.openxmlformats-officedocument.presentationml.presentation,video/mp4,video/quicktime,image/jpeg,image/png,image/heif"
      multiple
      hidden
      @change="editor.onAttachmentChange"
    />
  </main>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from "vue";
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
import { getAchievementSaveActionLabel } from "@/config/achievementReviewConfig";

const router = useRouter();
const route = useRoute();
const { openSidebar: openDashboardSidebar } = useDashboardShell();
const profile = reactive(loadUser());
const { submitAchievementReviewRequest, findPendingAchievementReview } =
  useNotifications(profile);
const { settings: reviewSettings, fetchSettings: fetchReviewSettings } =
  useReviewSettings();
const achievementSaveActionLabel = computed(() =>
  getAchievementSaveActionLabel(
    reviewSettings.achievementReviewEnabled,
    profile.role || "STUDENT",
  ),
);
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

const listResult = useAchievementList({
  route,
  router,
  errorMessage,
  uploadLimitConfig,
});
const list = reactive(listResult);

const viewItem = ref(null);

const preview = reactive(useAchievementPreview());

const editor = reactive(
  useAchievementEditor({
    profile,
    reviewSettings,
    fetchReviewSettings,
    submitAchievementReviewRequest,
    achievements: listResult.achievements,
    viewItem,
    activeCategory: listResult.activeCategory,
    activeStudentQuery: listResult.activeStudentQuery,
    errorMessage,
    fetchAchievements: () => list.fetchAchievements(),
    uploadLimitConfig,
    uploadHelpers,
  }),
);

const view = reactive(
  useAchievementView({
    achievements: listResult.achievements,
    errorMessage,
    findPendingAchievementReview,
    onEditFromView: (item) => editor.openEditorFromItem(item),
    viewItem,
  }),
);

function toggleEditorHint() {
  editor.hintCollapsed = !editor.hintCollapsed;
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
  list.syncCategoryFromRoute();
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
  list.fetchAchievements();
  preview.bindSheetSwitcher();
  window.addEventListener(
    "achievement-upload-settings-updated",
    handleUploadSettingsUpdated,
  );
});

onBeforeUnmount(() => {
  preview.hidePreview();
  revokePrivateMediaObjectUrls();
  window.removeEventListener(
    "achievement-upload-settings-updated",
    handleUploadSettingsUpdated,
  );
});

watch(
  () => [route.query.category, route.query.studentNo, route.query.studentName],
  () => {
    list.syncCategoryFromRoute();
    list.fetchAchievements();
  },
);
</script>
