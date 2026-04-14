<template>
  <main class="dashboard-right">
    <header class="feed-header">
      <h1 class="feed-title">个人成就</h1>
    </header>

    <section
      v-if="activeCategory === 'all'"
      class="info-card achievement-intro-card"
    >
      <div class="info-section-title">全部</div>
      <p class="achievement-intro-text">这里存放了我所有的成就！</p>
    </section>

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
    <MobileCapsule :hidden="editorOpen" @open-sidebar="openDashboardSidebar">
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

    <transition name="publisher-backdrop">
      <div
        v-if="viewOpen"
        class="achievement-backdrop"
        @click="closeView"
      ></div>
    </transition>
    <section
      class="achievement-view"
      :class="{ open: viewOpen, closing: viewClosing, 'exit-up': viewExitUp }"
      :aria-hidden="!viewOpen"
    >
      <header class="publisher-header">
        <div class="publisher-title">成就查看</div>
        <button class="publisher-close" type="button" @click="closeView">
          关闭
        </button>
      </header>
      <div v-if="viewLoading" class="empty-tip">加载中...</div>
      <AchievementDetailRenderer
        v-else-if="viewItem"
        :item="viewItem"
        :attachment-icon="attachmentIcon"
        :is-video-file="isVideoFile"
        :is-document-file="isDocumentFile"
        :is-sheet-file="isSheetFile"
        :is-pdf-file="isPdfFile"
        :is-allowed-image="isAllowedImage"
        :is-pptx-file="isPptxFile"
        @preview="showPreview"
        @edit="editFromView"
        @delete="openDelete"
      />
    </section>

    <transition name="publisher-backdrop">
      <div
        v-if="editorOpen"
        class="achievement-backdrop"
        @click="closeEditor"
      ></div>
    </transition>
    <section
      class="achievement-sheet"
      :class="{ open: editorOpen }"
      :aria-hidden="!editorOpen"
    >
      <header class="publisher-header">
        <div class="publisher-title">
          {{ editId ? "编辑成就" : "新增成就" }}
        </div>
        <button class="publisher-close" type="button" @click="closeEditor">
          关闭
        </button>
      </header>
      <div class="achievement-grid" :class="{ 'has-media': form.category }">
        <transition name="panel-fade">
          <div v-show="form.category" class="achievement-media-panel">
            <div class="achievement-section-label">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                aria-hidden="true"
              >
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
                <circle cx="8.5" cy="8.5" r="1.5" />
                <polyline points="21 15 16 10 5 21" />
              </svg>
              图片（可选）
            </div>
            <div class="media-subtitle">
              最多 {{ imageMaxCount }} 张 · 单张不超过 {{ mediaLimitLabel }}
            </div>

            <div
              v-if="imagePreviews.length === 0"
              class="media-empty-state"
              @click="triggerImage"
            >
              <div class="media-empty-icon">+</div>
              <div class="media-empty-text">点击添加图片</div>
            </div>

            <div v-else-if="imagePreviews.length === 1" class="media-single">
              <button
                class="media-single-item"
                type="button"
                @click="selectEditorImage(0)"
              >
                <img :src="imagePreviews[0]" alt="成就图片" />
                <span class="media-remove" @click.stop="removeImage(0)"
                  >移除</span
                >
              </button>
              <button class="media-add-btn" type="button" @click="triggerImage">
                <span>+</span>
              </button>
            </div>

            <div v-else-if="imagePreviews.length === 2" class="media-two">
              <button
                v-for="(image, index) in imagePreviews"
                :key="`${image}-${index}`"
                class="media-thumb"
                type="button"
                @click="selectEditorImage(index)"
              >
                <img :src="image" alt="成就图片" />
                <span class="media-remove" @click.stop="removeImage(index)"
                  >移除</span
                >
              </button>
              <button
                class="media-thumb media-add"
                type="button"
                @click="triggerImage"
              >
                <span>+</span>
              </button>
            </div>

            <div v-else class="media-grid">
              <button
                v-for="(image, index) in imagePreviews"
                :key="`${image}-${index}`"
                class="media-thumb"
                type="button"
                @click="selectEditorImage(index)"
              >
                <img :src="image" alt="成就图片" />
                <span class="media-remove" @click.stop="removeImage(index)">
                  移除
                </span>
              </button>
              <button
                v-if="imagePreviews.length < imageMaxCount"
                class="media-thumb media-add"
                type="button"
                @click="triggerImage"
              >
                <span>+</span>
              </button>
            </div>
          </div>
        </transition>

        <div class="achievement-fields">
          <div v-if="!editId" class="achievement-category-row">
            <label class="field-label">成就分类</label>
            <select v-model="form.category">
              <option disabled value="">请选择分类</option>
              <option
                v-for="entry in categoryOptions"
                :key="entry.key"
                :value="entry.key"
              >
                {{ entry.label }}
              </option>
            </select>
          </div>
          <div v-if="!editId && activeCategoryHint" class="achievement-hint">
            <button
              class="achievement-hint-toggle"
              type="button"
              :aria-expanded="!hintCollapsed"
              @click="hintCollapsed = !hintCollapsed"
            >
              <span class="achievement-hint-toggle-label">
                <svg
                  width="13"
                  height="13"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  aria-hidden="true"
                >
                  <circle cx="12" cy="12" r="10" />
                  <line x1="12" y1="8" x2="12" y2="12" />
                  <line x1="12" y1="16" x2="12.01" y2="16" />
                </svg>
                填写说明
              </span>
              <svg
                class="achievement-hint-chevron"
                :class="{ collapsed: hintCollapsed }"
                width="12"
                height="12"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2.5"
                aria-hidden="true"
              >
                <polyline points="18 15 12 9 6 15" />
              </svg>
            </button>
            <Transition name="hint-expand">
              <div v-show="!hintCollapsed" class="achievement-hint-body">
                <ol class="achievement-hint-list">
                  <li v-for="item in activeCategoryHint" :key="item">
                    {{ item }}
                  </li>
                </ol>
              </div>
            </Transition>
          </div>
          <div v-if="!activeFormConfig" class="empty-tip">
            请选择成就分类后填写对应信息。
          </div>
          <div v-else class="achievement-dynamic-fields">
            <div
              v-for="field in activeFormConfig.fields"
              :key="field.key"
              class="field-row"
            >
              <label class="field-label">{{ field.label }}</label>
              <input
                v-if="field.kind === 'input'"
                v-model="form.fields[field.key]"
                class="form-input"
                :type="field.type || 'text'"
                :placeholder="field.placeholder || ''"
              />
              <textarea
                v-else
                v-model="form.fields[field.key]"
                class="publisher-input"
                :rows="field.rows || 2"
                :placeholder="field.placeholder || ''"
              ></textarea>
            </div>
          </div>
          <transition name="panel-fade">
            <div v-show="form.category" class="achievement-attachments-panel">
              <div class="achievement-section-label">
                <svg
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  aria-hidden="true"
                >
                  <path
                    d="M21.44 11.05l-9.19 9.19a6 6 0 01-8.49-8.49l9.19-9.19a4 4 0 015.66 5.66l-9.2 9.19a2 2 0 01-2.83-2.83l8.49-8.48"
                  />
                </svg>
                附件（可选）
              </div>
              <div class="media-subtitle">
                最多 {{ attachmentMaxCount }} 个 · 单个不超过
                {{ attachmentLimitLabel }}
              </div>
              <div v-if="!attachmentPreviews.length" class="media-empty">
                暂无附件
              </div>
              <div v-else class="attachment-list">
                <div
                  v-for="(file, index) in attachmentPreviews"
                  :key="`${file.url}-${index}`"
                  class="attachment-item"
                >
                  <img :src="attachmentIcon(file)" alt="" />
                  <div class="attachment-name">{{ file.name }}</div>
                  <button
                    class="attachment-remove"
                    type="button"
                    @click="removeAttachment(index)"
                  >
                    移除
                  </button>
                </div>
              </div>
              <div class="attachment-formats" @click="triggerAttachment">
                <div class="format-row">
                  <div
                    v-for="item in enabledAttachmentTypes.slice(0, 2)"
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
                    v-for="item in enabledAttachmentTypes.slice(2, 4)"
                    :key="item.key"
                    class="format-item"
                  >
                    <img class="format-icon" :src="item.icon" alt="" />
                    <span class="format-label">{{ item.label }}</span>
                    <span class="format-exts">{{ item.exts.join("/") }}</span>
                  </div>
                </div>
              </div>
            </div>
          </transition>

          <div class="achievement-actions">
            <button class="ghost-button" type="button" @click="closeEditor">
              取消
            </button>
            <button
              class="action-button"
              type="button"
              @click="saveAchievement"
            >
              提交审核
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- Media Preview -->
    <Teleport to="body">
      <Transition name="viewer" appear>
        <div v-if="previewVisible" class="viewer-backdrop" @click="hidePreview">
          <div class="viewer-header" @click.stop>
            <div class="viewer-counter">
              <span class="viewer-current">{{ previewIndex + 1 }}</span>
              <span class="viewer-sep">/</span>
              <span class="viewer-total">{{ previewImages.length }}</span>
            </div>
            <button class="viewer-close" @click="hidePreview">
              <svg
                width="20"
                height="20"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </button>
          </div>

          <div class="viewer-stage">
            <button
              v-if="previewImages.length > 1 && previewIndex > 0"
              class="viewer-arrow viewer-prev"
              @click.stop="previewPrev"
            >
              <svg
                width="24"
                height="24"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <polyline points="15 18 9 12 15 6"></polyline>
              </svg>
            </button>

            <Transition :name="'slide-' + slideDirection" mode="out-in">
              <div class="viewer-media" :key="previewIndex" @click.stop>
                <video
                  v-if="previewType === 'video'"
                  :src="previewImages[previewIndex]"
                  class="viewer-video"
                  controls
                  autoplay
                ></video>
                <div
                  v-else-if="
                    previewType === 'document' ||
                    previewType === 'sheet' ||
                    previewType === 'pdf'
                  "
                  class="viewer-document"
                  :class="{ 'viewer-doc-full': previewType === 'pdf' }"
                >
                  <div v-if="previewLoading" class="viewer-loading">
                    <div class="spinner spinner-lg"></div>
                    <span>加载中...</span>
                  </div>
                  <div v-else class="viewer-content-wrapper">
                    <div v-if="previewType === 'document'" class="viewer-tip">
                      <svg
                        width="14"
                        height="14"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        stroke-width="2"
                      >
                        <circle cx="12" cy="12" r="10"></circle>
                        <line x1="12" y1="16" x2="12" y2="12"></line>
                        <line x1="12" y1="8" x2="12.01" y2="8"></line>
                      </svg>
                      因渲染限制，预览与实际文件可能存在样式差异
                    </div>
                    <div v-if="previewType === 'sheet'" class="viewer-tip">
                      <svg
                        width="14"
                        height="14"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        stroke-width="2"
                      >
                        <circle cx="12" cy="12" r="10"></circle>
                        <line x1="12" y1="16" x2="12" y2="12"></line>
                        <line x1="12" y1="8" x2="12.01" y2="8"></line>
                      </svg>
                      点击工作表标签可切换Sheet |
                      因渲染限制，预览与实际可能存在差异
                    </div>
                    <div
                      v-html="previewContent"
                      class="viewer-content"
                      :class="{
                        'viewer-content-full':
                          previewType === 'pdf' || previewType === 'document',
                      }"
                    ></div>
                  </div>
                </div>
                <img
                  v-else
                  :src="previewImages[previewIndex]"
                  class="viewer-image"
                  alt=""
                  @click.stop
                />
              </div>
            </Transition>

            <button
              v-if="
                previewImages.length > 1 &&
                previewIndex < previewImages.length - 1
              "
              class="viewer-arrow viewer-next"
              @click.stop="previewNext"
            >
              <svg
                width="24"
                height="24"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <polyline points="9 18 15 12 9 6"></polyline>
              </svg>
            </button>
          </div>

          <div class="viewer-dots" v-if="previewImages.length > 1" @click.stop>
            <button
              v-for="(_, i) in previewImages"
              :key="i"
              class="viewer-dot"
              :class="{ active: i === previewIndex }"
              @click="
                previewIndex = i;
                slideDirection = i > previewIndex ? 'right' : 'left';
              "
            ></button>
          </div>
        </div>
      </Transition>
    </Teleport>

    <div
      v-if="deleteDialogOpen"
      class="dialog-backdrop"
      @click="closeDelete"
    ></div>
    <section v-if="deleteDialogOpen" class="dialog-card" @click.stop>
      <header class="dialog-header">确认删除</header>
      <div class="dialog-body">删除后无法恢复，确定要删除这条动态吗？</div>
      <div class="dialog-actions">
        <button class="ghost-button" type="button" @click="closeDelete">
          取消
        </button>
        <button
          class="action-button"
          type="button"
          :disabled="deleteBusy"
          @click="confirmDelete"
        >
          确定删除
        </button>
      </div>
    </section>

    <input
      ref="imageInput"
      type="file"
      accept=".jpeg,.jpg,.png,.heif,image/jpeg,image/png,image/heif"
      multiple
      hidden
      @change="onImageChange"
    />
    <input
      ref="attachmentInput"
      type="file"
      accept=".docx,.doc,.pdf,.xls,.xlsx,.zip,.rar,.7z,.pptx,.ppt,.mp4,.mov,.jpeg,.jpg,.png,.heif,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/pdf,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-powerpoint,application/vnd.openxmlformats-officedocument.presentationml.presentation,video/mp4,video/quicktime,image/jpeg,image/png,image/heif"
      multiple
      hidden
      @change="onAttachmentChange"
    />
  </main>
</template>

<script setup>
import {
  computed,
  nextTick,
  onBeforeUnmount,
  onMounted,
  reactive,
  ref,
  watch,
} from "vue";
import { useRouter, useRoute } from "vue-router";
import { getMenuLocation, isMenuEnabled } from "../constants/menu";
import {
  createAchievement,
  deleteAchievement,
  getAchievements,
  updateAchievement,
} from "../api/achievements";
import { useAchievementUploadSettings } from "../composables/useAchievementUploadSettings";
import { uploadMedia } from "../api/upload";
import { renderDocx } from "../utils/docxRenderer";
import { renderSheet } from "../utils/sheetRenderer";
import { renderPdf } from "../utils/pdfRenderer";
import { API_BASE } from "../api/request";
import MobileCapsule from "../components/MobileCapsule.vue";
import AchievementCardBody from "../components/AchievementCardBody.vue";
import AchievementDetailRenderer from "../components/achievement/AchievementDetailRenderer.vue";
import { navigateWithViewTransition } from "../utils/viewTransition";
import { useDashboardShell } from "../composables/useDashboardShell";
import { useNotifications } from "../composables/useNotifications";
import { useReviewSettings } from "../composables/useReviewSettings";
import { useToast } from "../composables/useToast";
import {
  resolveMediaUrl,
  stripMediaUrl,
  resolveMediaTypeByExtension,
  isMediaVideo,
  isMediaDocument,
  isMediaSheet,
  isMediaPdf,
  isVideoUrl,
  isVideoFile,
  isDocumentFile,
  isSheetFile,
  isPdfFile,
  isPptxFile,
  parseJsonArray,
} from "../utils/media";
import { loadUser } from "../utils/userStorage";
import { dedupeAchievements, attachmentIcon } from "../utils/achievement";
import {
  categoryFieldMap,
  categoryHints,
  achievementEntries,
  ATTACHMENT_TYPE_META,
  attachmentIconMap,
  IMAGE_URLS_FIELD,
  ATTACHMENTS_FIELD,
} from "../constants/achievementConstants";
import { useAchievementUpload } from "../composables/useAchievementUpload";

const router = useRouter();
const route = useRoute();
const { openSidebar: openDashboardSidebar } = useDashboardShell();
const profile = reactive(loadUser());
const { submitAchievementReviewRequest } = useNotifications(profile);
const { settings: reviewSettings, fetchSettings: fetchReviewSettings } =
  useReviewSettings();
const { info: toastInfo } = useToast();
const activeMenu = ref("achievements");
const editorOpen = ref(false);
const hintCollapsed = ref(false);
const errorMessage = ref("");
const viewOpen = ref(false);
const viewLoading = ref(false);
const viewItem = ref(null);
const viewClosing = ref(false);
const viewExitUp = ref(false);
const editId = ref(null);
const deleteDialogOpen = ref(false);
const deleteBusy = ref(false);
const sidebarOpen = ref(false);
const achievementsOpen = ref(true);
const activeCategory = ref("all");
const previewImages = ref([]);
const previewIndex = ref(0);
const previewVisible = ref(false);
const previewType = ref("image");
const previewContent = ref("");
const previewLoading = ref(false);
const previewWorkbook = ref(null);
const slideDirection = ref("right");
const {
  settings: achievementUploadSettings,
  fetchSettings: fetchAchievementUploadSettings,
} = useAchievementUploadSettings();

const {
  uploadLimitConfig,
  setUploadLimits,
  isAllowedImage,
  isAllowedAttachment,
  isFileSizeAllowed,
  attachmentExtsByType,
  resolveAttachmentTypeByExtension,
  imageMaxCount,
  attachmentMaxCount,
  mediaLimitLabel,
  attachmentLimitLabel,
  allowedAttachmentExtensions,
  imageInput,
  attachmentInput,
  enabledAttachmentTypes,
} = useAchievementUpload();

const achievements = ref([]);

const activeStudentQuery = computed(() => {
  const rawName = route.query.studentName;
  const rawNo = route.query.studentNo;
  const rawEmbed = route.query.embed;
  const studentName = typeof rawName === "string" ? rawName.trim() : "";
  const studentNo = typeof rawNo === "string" ? rawNo.trim() : "";
  const embedValue = typeof rawEmbed === "string" ? rawEmbed.trim() : "";
  return {
    studentName,
    studentNo,
    embedValue,
  };
});

const isEmbedded = computed(() => {
  const value = activeStudentQuery.value.embedValue;
  return value === "1" || value.toLowerCase() === "true";
});

const activeCategoryIndex = computed(() => {
  const index = achievementEntries.findIndex(
    (entry) => entry.key === activeCategory.value,
  );
  return index === -1 ? 0 : index;
});

const drawerIndicatorStyle = computed(() => ({
  transform: `translateY(calc(${activeCategoryIndex.value} * (var(--drawer-item-height) + var(--drawer-item-gap))))`,
}));

const form = reactive({
  imageUrl: "",
  imageUrls: [],
  attachments: [],
  category: "",
  fields: {},
});

const imagePreviews = computed(() =>
  (form.imageUrls || []).map((url) => resolveMediaUrl(url)),
);
const attachmentPreviews = computed(() =>
  (form.attachments || []).map((file) => ({
    ...file,
    url: resolveMediaUrl(file.url),
  })),
);

const categoryOptions = computed(() =>
  achievementEntries.filter((entry) => entry.key !== "all"),
);
const roleLabelMap = {
  STUDENT: "学生",
  TEACHER: "教师",
  ADMIN: "管理员",
};

const roleLabel = computed(() => roleLabelMap[profile.role] || "学生");
const avatarText = computed(() => {
  const name = profile.displayName || profile.username || "同学";
  return name.slice(0, 1).toUpperCase();
});
const activeCategoryLabel = computed(() => {
  const match = achievementEntries.find(
    (entry) => entry.key === activeCategory.value,
  );
  return match ? match.label : "全部";
});
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
const filteredAchievements = computed(() => {
  const baseList =
    activeCategory.value === "all"
      ? achievements.value
      : achievements.value.filter(
          (item) => item.category === activeCategory.value,
        );
  const { studentName, studentNo } = activeStudentQuery.value;
  if (!studentName && !studentNo) {
    return baseList;
  }
  const normalizeText = (value) =>
    String(value || "")
      .trim()
      .toLowerCase();
  const targetName = normalizeText(studentName);
  const targetNo = normalizeText(studentNo);
  return baseList.filter((item) => {
    const fields = item.fields || {};
    const itemNo = normalizeText(fields.studentNo);
    const itemName = normalizeText(fields.studentName);
    if (targetNo) {
      return itemNo ? itemNo === targetNo : true;
    }
    return itemName ? itemName === targetName : true;
  });
});
const emptyMessage = computed(() => {
  const { studentName, studentNo } = activeStudentQuery.value;
  if (studentName || studentNo) {
    return "该学生暂无成就。";
  }
  if (activeCategory.value === "all") {
    return "还没有成就，点击右下角添加。";
  }
  return "该分类暂无成就，点击右下角添加。";
});
const addButtonLabel = computed(() => {
  if (activeCategory.value === "all") {
    return "添加成就";
  }
  return `添加${activeCategoryLabel.value}`;
});

function getCurrentStudentNo() {
  const { studentNo } = activeStudentQuery.value;
  if (studentNo) {
    return studentNo;
  }
  try {
    const raw = JSON.parse(localStorage.getItem("gcsc_user") || "{}");
    return raw.studentNo || profile.studentNo || "";
  } catch {
    return profile.studentNo || "";
  }
}

function getCurrentStudentName() {
  const { studentName } = activeStudentQuery.value;
  if (studentName) {
    return studentName;
  }
  return profile.displayName || profile.username || "";
}

function handleMenuClick(key) {
  if (!isMenuEnabled(key)) {
    return;
  }
  sidebarOpen.value = false;
  if (key === "achievements") {
    handleAchievementEntry("all");
    return;
  }
  navigateWithViewTransition(router, getMenuLocation(key));
}

function toggleAchievements() {
  if (!isMenuEnabled("achievements")) {
    return;
  }
  achievementsOpen.value = !achievementsOpen.value;
  activeMenu.value = "achievements";
  if (achievementsOpen.value) {
    handleAchievementEntry("all");
  }
}

function handleAchievementEntry(key) {
  if (!isMenuEnabled("achievements")) {
    return;
  }
  const safeKey = achievementEntries.some((entry) => entry.key === key)
    ? key
    : "all";
  activeCategory.value = safeKey;
  achievementsOpen.value = true;
  activeMenu.value = "achievements";
  sidebarOpen.value = false;
  const { studentName, studentNo, embedValue } = activeStudentQuery.value;
  const query = { category: safeKey };
  if (studentName) {
    query.studentName = studentName;
  }
  if (studentNo) {
    query.studentNo = studentNo;
  }
  if (embedValue) {
    query.embed = embedValue;
  }
  navigateWithViewTransition(router, { path: "/achievements", query });
}

function openSidebar() {
  sidebarOpen.value = true;
}

function closeSidebar() {
  sidebarOpen.value = false;
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

function goToSettings() {
  navigateWithViewTransition(router, "/settings");
}

function resetForm() {
  form.imageUrl = "";
  form.imageUrls = [];
  form.attachments = [];
  form.category = "";
  form.fields = {};
}

async function saveAchievement() {
  const config = activeFormConfig.value;
  if (!config) {
    errorMessage.value = "请先选择成就分类";
    return;
  }
  const category =
    form.category ||
    (activeCategory.value === "all" ? "" : activeCategory.value);
  if (!category) {
    errorMessage.value = "请先选择成就分类";
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
    // ADMIN bypasses review; only STUDENT role follows the review flow
    if (profile.role !== "ADMIN" && reviewSettings.achievementReviewEnabled) {
      toastInfo("已提交审核，请等待审核成功后显示");
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
      }
      resetForm();
      closeEditor();
      errorMessage.value = "";
      return;
    }

    if (editId.value) {
      const { data } = await updateAchievement(category, editId.value, payload);
      const normalizedData = normalizeAchievement(data);
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
      const normalizedData = normalizeAchievement(data);
      achievements.value = dedupeAchievements([
        normalizedData,
        ...achievements.value,
      ]);
    }
    toastInfo("保存成功");
    resetForm();
    closeEditor();
    errorMessage.value = "";
  } catch (err) {
    errorMessage.value = err?.response?.data?.message || "保存失败，请重新登录";
  }
}

function buildAchievementChanges({ category, payload, existingItem }) {
  const config = categoryFieldMap[category] || null;
  if (!config) {
    return [];
  }
  const nextFields = payload?.fields || {};
  const previousFields = existingItem?.fields || {};
  const changes = [];

  config.fields.forEach((field) => {
    const before = stringifyChangeValue(previousFields[field.key]);
    const after = stringifyChangeValue(nextFields[field.key]);
    if (!existingItem && after === "-") {
      return;
    }
    if (existingItem && before === after) {
      return;
    }
    changes.push({
      section: "成就信息",
      label: field.label,
      before,
      after,
    });
  });

  const previousImages = stringifyChangeValue(existingItem?.imageUrls || []);
  const nextImages = stringifyChangeValue(resolveImageUrlsFromPayload(payload));
  if (
    (!existingItem && nextImages !== "-") ||
    (existingItem && previousImages !== nextImages)
  ) {
    changes.push({
      section: "多媒体",
      label: "图片",
      before: previousImages,
      after: nextImages,
    });
  }

  const previousAttachments = stringifyChangeValue(
    (existingItem?.attachments || []).map(
      (item) => item.name || item.url || "",
    ),
  );
  const nextAttachments = stringifyChangeValue(
    (payload?.fields?.[ATTACHMENTS_FIELD]
      ? JSON.parse(payload.fields[ATTACHMENTS_FIELD])
      : []
    ).map((item) => item.name || item.url || ""),
  );
  if (
    (!existingItem && nextAttachments !== "-") ||
    (existingItem && previousAttachments !== nextAttachments)
  ) {
    changes.push({
      section: "多媒体",
      label: "附件",
      before: previousAttachments,
      after: nextAttachments,
    });
  }

  return changes;
}

function resolveImageUrlsFromPayload(payload) {
  const raw = payload?.fields?.[IMAGE_URLS_FIELD];
  if (!raw) {
    return payload?.imageUrl ? [payload.imageUrl] : [];
  }
  try {
    return Array.isArray(JSON.parse(raw)) ? JSON.parse(raw) : [];
  } catch {
    return payload?.imageUrl ? [payload.imageUrl] : [];
  }
}

function resolveAttachmentsFromPayload(payload) {
  return parseJsonArray(payload?.fields?.[ATTACHMENTS_FIELD])
    .map((item) => ({
      url: resolveMediaUrl(item?.url || ""),
      name: item?.name || item?.originalName || "附件",
      mediaType: item?.mediaType || "",
    }))
    .filter((item) => item.url);
}

function buildAchievementDraftSourceFromPayload(payload) {
  return {
    fields: payload?.fields || {},
    imageUrl: payload?.imageUrl || "",
    imageUrls: resolveImageUrlsFromPayload(payload).map((url) =>
      resolveMediaUrl(url),
    ),
    attachments: resolveAttachmentsFromPayload(payload),
  };
}

function buildAchievementReviewPayloadSnapshot({
  category,
  beforeItem = null,
  afterItem = null,
}) {
  return {
    type: "achievement-review",
    category,
    before: beforeItem
      ? buildAchievementReviewSnapshot({ category, source: beforeItem })
      : null,
    after: afterItem
      ? buildAchievementReviewSnapshot({ category, source: afterItem })
      : null,
  };
}

function buildAchievementReviewSnapshot({ category, source }) {
  const config = categoryFieldMap[category] || null;
  const fields = source?.fields || {};
  const fieldConfigList = Array.isArray(config?.fields) ? config.fields : [];
  const titleKey = config?.titleKey || "";
  const dateKey = config?.dateKey || "";
  const dateField =
    fieldConfigList.find((field) => field.key === dateKey) || null;
  const imageUrls = Array.isArray(source?.imageUrls)
    ? source.imageUrls.filter(Boolean).map((url) => resolveMediaUrl(url))
    : resolveImageUrlsFromPayload({
        imageUrl: source?.imageUrl || source?.image || "",
        fields,
      });
  const attachments = Array.isArray(source?.attachments)
    ? source.attachments
        .map((item) => ({
          url: resolveMediaUrl(item?.url || ""),
          name: item?.name || "附件",
          mediaType: item?.mediaType || "",
        }))
        .filter((item) => item.url)
    : resolveAttachmentsFromPayload({ fields });

  return {
    category,
    categoryLabel:
      achievementEntries.find((entry) => entry.key === category)?.label ||
      "成就记录",
    title: stringifyChangeValue(fields[titleKey]),
    dateLabel: dateField?.label || "",
    dateValue: dateKey ? stringifyChangeValue(fields[dateKey]) : "",
    fieldEntries: fieldConfigList.map((field) => ({
      key: field.key,
      label: field.label,
      value: stringifyChangeValue(fields[field.key]),
    })),
    imageUrls,
    attachments,
  };
}

function stringifyChangeValue(value) {
  if (Array.isArray(value)) {
    const filtered = value
      .filter(Boolean)
      .map((item) => String(item).trim())
      .filter(Boolean);
    return filtered.length ? filtered.join("、") : "-";
  }
  const text = String(value ?? "").trim();
  return text || "-";
}

function triggerImage() {
  imageInput.value && imageInput.value.click();
}

function triggerAttachment() {
  attachmentInput.value && attachmentInput.value.click();
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
      const { data } = await uploadMedia(file, {
        context: "achievement-image",
      });
      if (data?.url) {
        form.imageUrls.push(data.url);
      }
    } catch (err) {
      errorMessage.value = err?.response?.data?.message || "图片上传失败";
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
      const { data } = await uploadMedia(file, {
        context: "achievement-attachment",
      });
      if (data?.url) {
        form.attachments.push({
          url: data.url,
          name: data.originalName || file.name,
          mediaType: data.mediaType || resolveMediaTypeByExtension(file.name),
        });
      }
    } catch (err) {
      errorMessage.value = err?.response?.data?.message || "附件上传失败";
    }
  }
}

function normalizeAchievement(item) {
  const config = categoryFieldMap[item.category] || null;
  const fields = item.fields || {};
  const imageUrls = resolveImageUrls(item);
  const attachments = resolveAttachments(fields);
  const titleKey = config?.titleKey;
  const dateKey = config?.dateKey;
  const dateLabel =
    config?.fields.find((field) => field.key === dateKey)?.label || "";
  const fieldLines = config
    ? config.fields.map(
        (field) => `${field.label}：${fields[field.key] || "-"}`,
      )
    : [];
  return {
    id: item.id,
    title: titleKey ? fields[titleKey] : "",
    dateLabel,
    dateValue: dateKey ? fields[dateKey] : "",
    fields,
    fieldLines,
    previewFields: fieldLines.slice(0, 2),
    image: imageUrls[0] || "",
    imageUrls,
    attachments,
    category: item.category || "",
  };
}

function openDetail(item) {
  viewItem.value = item;
  viewOpen.value = true;
  viewClosing.value = false;
}

function closeView() {
  viewOpen.value = false;
  viewClosing.value = true;
  setTimeout(() => {
    viewItem.value = null;
    viewClosing.value = false;
  }, 260);
}

function editFromView() {
  if (!viewItem.value) {
    return;
  }
  const item = viewItem.value;
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
  viewOpen.value = false;
  viewClosing.value = true;
  editorOpen.value = true;
  setTimeout(() => {
    viewItem.value = null;
    viewClosing.value = false;
  }, 260);
}

function openDelete() {
  if (deleteDialogOpen.value) {
    return;
  }
  deleteDialogOpen.value = true;
}

function showPreview(urls, index = 0) {
  previewImages.value = urls;
  previewIndex.value = index;
  previewVisible.value = true;
  document.body.style.overflow = "hidden";
  previewContent.value = "";
  previewLoading.value = false;
  // Detect media type
  if (urls.length > 0) {
    const url = urls[index];
    if (isMediaVideo(url)) {
      previewType.value = "video";
    } else if (isMediaDocument(url)) {
      previewType.value = "document";
      loadDocumentPreview(url);
    } else if (isMediaSheet(url)) {
      previewType.value = "sheet";
      loadSheetPreview(url);
    } else if (isMediaPdf(url)) {
      previewType.value = "pdf";
      loadPdfPreview(url);
    } else {
      previewType.value = "image";
    }
  }
}

async function loadDocumentPreview(url) {
  previewLoading.value = true;
  try {
    const html = await renderDocx(url);
    previewContent.value = html;
  } catch (e) {
    previewContent.value = `<div class="docx-error"><div>加载失败</div></div>`;
  } finally {
    previewLoading.value = false;
  }
}

async function loadSheetPreview(url) {
  previewLoading.value = true;
  try {
    const result = await renderSheet(url);
    previewContent.value = result.html;
    // Store workbook for sheet switching
    if (result.workbook) {
      previewWorkbook.value = result.workbook;
    }
  } catch (e) {
    previewContent.value = `<div class="sheet-error"><div>加载失败</div></div>`;
  } finally {
    previewLoading.value = false;
  }
}

async function loadPdfPreview(url) {
  previewLoading.value = true;
  try {
    const html = await renderPdf(url);
    previewContent.value = html;
  } catch (e) {
    previewContent.value = `<div class="pdf-error"><div>加载失败</div></div>`;
  } finally {
    previewLoading.value = false;
  }
}

async function switchSheet(sheetIndex) {
  if (!previewWorkbook.value) return;
  previewLoading.value = true;
  try {
    const XLSX = await import("xlsx");
    const sheetNames = previewWorkbook.value.SheetNames;
    const sheet = previewWorkbook.value.Sheets[sheetNames[sheetIndex]];
    const html = XLSX.utils.sheet_to_html(sheet, {
      header: true,
      footer: false,
      editable: false,
    });
    // Build new tabs
    const tabs = sheetNames
      .map(
        (name, i) => `
      <div class="sheet-tab ${i === sheetIndex ? "active" : ""}" onclick="window.__switchSheet(${i})">
        ${name}
      </div>
    `,
      )
      .join("");
    previewContent.value = `
      <div class="sheet-container">
        <div class="sheet-tabs">${tabs}</div>
        <div class="sheet-content">
          <div class="sheet-table-wrapper">${html}</div>
        </div>
        <div class="sheet-footer">
          <span>${sheetNames.length} 个工作表</span>
        </div>
      </div>
    `;
  } catch (e) {
    previewContent.value = `<div class="sheet-error"><div>切换失败</div></div>`;
  } finally {
    previewLoading.value = false;
  }
}

function hidePreview() {
  previewVisible.value = false;
  document.body.style.overflow = "";
}

function previewPrev() {
  if (previewIndex.value > 0) {
    slideDirection.value = "left";
    previewIndex.value--;
  }
}

function previewNext() {
  if (previewIndex.value < previewImages.value.length - 1) {
    slideDirection.value = "right";
    previewIndex.value++;
  }
}

function closeDelete() {
  if (deleteBusy.value || !deleteDialogOpen.value) {
    return;
  }
  deleteDialogOpen.value = false;
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

async function confirmDelete() {
  if (!viewItem.value) {
    closeDelete();
    return;
  }
  deleteBusy.value = true;
  try {
    await deleteAchievement(viewItem.value.category, viewItem.value.id);
    achievements.value = achievements.value.filter(
      (item) => item.id !== viewItem.value.id,
    );
    closeView();
  } catch (err) {
    errorMessage.value = err?.response?.data?.message || "删除失败";
  } finally {
    deleteBusy.value = false;
    closeDelete();
  }
}

async function fetchAchievements() {
  try {
    const params = {};
    if (activeCategory.value && activeCategory.value !== "all") {
      params.category = activeCategory.value;
    }
    const { studentName, studentNo } = activeStudentQuery.value;
    if (studentName) {
      params.studentName = studentName;
    }
    if (studentNo) {
      params.studentNo = studentNo;
    }
    const { data } = await getAchievements(params);
    achievements.value = Array.isArray(data)
      ? dedupeAchievements(data.map(normalizeAchievement))
      : [];
    errorMessage.value = "";
  } catch (err) {
    achievements.value = [];
    errorMessage.value = err?.response?.data?.message || "无法获取成就列表";
  }
}

function syncCategoryFromRoute() {
  const rawCategory = route.query.category;
  const safeCategory =
    typeof rawCategory === "string" &&
    achievementEntries.some((entry) => entry.key === rawCategory)
      ? rawCategory
      : "all";
  activeCategory.value = safeCategory;
  achievementsOpen.value = true;
  if (rawCategory !== safeCategory) {
    const { studentName, studentNo, embedValue } = activeStudentQuery.value;
    const query = { category: safeCategory };
    if (studentName) {
      query.studentName = studentName;
    }
    if (studentNo) {
      query.studentNo = studentNo;
    }
    if (embedValue) {
      query.embed = embedValue;
    }
    router.replace({
      path: "/achievements",
      query,
    });
  }
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

function resolveImageUrls(item) {
  const urls = [];
  if (item?.imageUrl) {
    urls.push(resolveMediaUrl(item.imageUrl));
  }
  const rawField = item?.fields?.[IMAGE_URLS_FIELD];
  const parsed = parseJsonArray(rawField);
  parsed.forEach((url) => {
    const resolved = resolveMediaUrl(url);
    if (resolved && !urls.includes(resolved)) {
      urls.push(resolved);
    }
  });
  return urls.slice(0, uploadLimitConfig.imageMaxCount);
}

function resolveAttachments(fields = {}) {
  const raw = fields[ATTACHMENTS_FIELD];
  const parsed = parseJsonArray(raw);
  return parsed
    .map((item) => ({
      url: resolveMediaUrl(item.url),
      name: item.name || item.originalName || "附件",
      mediaType: item.mediaType || "",
    }))
    .filter((item) => item.url);
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
  window.addEventListener(
    "achievement-upload-settings-updated",
    handleUploadSettingsUpdated,
  );
  // Expose sheet switcher globally for v-html content
  window.__switchSheet = (index) => {
    switchSheet(index);
  };
});

onBeforeUnmount(() => {
  window.removeEventListener(
    "achievement-upload-settings-updated",
    handleUploadSettingsUpdated,
  );
  if (window.__switchSheet) {
    delete window.__switchSheet;
  }
});

watch(
  () => [route.query.category, route.query.studentNo, route.query.studentName],
  () => {
    syncCategoryFromRoute();
    fetchAchievements();
  },
);

watch(
  () => form.category,
  () => {
    applyFieldDefaults();
  },
);

// Delegate sheet tab clicks after content renders
watch(previewContent, () => {
  if (previewType.value === "sheet") {
    nextTick(() => {
      const container = document.querySelector(".viewer-document");
      if (container) {
        container.onclick = (e) => {
          const tab = e.target.closest(".sheet-tab");
          if (tab) {
            const idx = parseInt(tab.dataset.sheet, 10);
            if (!isNaN(idx)) {
              switchSheet(idx);
            }
          }
        };
      }
    });
  }
});
</script>
