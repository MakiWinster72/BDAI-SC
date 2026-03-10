<template>
  <div class="dashboard-layout">
    <aside class="dashboard-left">
      <section class="profile-card">
        <div class="profile-row profile-main">
          <div class="profile-avatar">{{ avatarText }}</div>
          <div class="profile-name-wrap">
            <p class="profile-name">
              {{ profile.displayName || profile.username || "同学" }}
            </p>
            <p class="profile-role">{{ roleLabel }}</p>
          </div>
        </div>
        <div class="profile-row">学号：{{ profile.studentNo || "未填写" }}</div>
        <div class="profile-row">班级：{{ profile.className || "未填写" }}</div>
        <div class="profile-row">学院：{{ profile.college || "未填写" }}</div>
      </section>

      <section class="menu-card">
        <button
          v-for="item in menuItems"
          :key="item.key"
          class="menu-item"
          :class="{
            active: activeMenu === item.key,
            disabled: !isMenuEnabled(item.key),
          }"
          type="button"
          :disabled="!isMenuEnabled(item.key)"
          @click="handleMenuClick(item.key)"
        >
          {{ item.label }}
        </button>
      </section>
    </aside>

    <main class="dashboard-right">
      <header class="feed-header">
        <h1 class="feed-title">{{ currentMenuLabel }}</h1>
        <button class="ghost-button" type="button" @click="logout">
          退出登录
        </button>
      </header>

      <section class="feed-waterfall">
        <article v-for="post in visiblePosts" :key="post.id" class="post-card">
          <div class="post-header">
            <div class="post-author">
              <div class="post-avatar">{{ postAvatarText(post) }}</div>
              <div class="post-author-info">
                <div class="post-author-name">
                  {{ post.authorName || post.authorUsername || "同学" }}
                </div>
              </div>
            </div>
          </div>
          <div class="post-content" v-html="renderMarkdown(post.content)"></div>
          <div
            v-if="post.media && post.media.some((m) => m.mediaType === 'FILE')"
            class="post-attachments"
          >
            <a
              v-for="item in post.media.filter((m) => m.mediaType === 'FILE')"
              :key="item.id || item.url"
              class="attach-link"
              :href="resolveMediaUrl(item.url)"
              target="_blank"
              rel="noreferrer"
            >
              {{ item.originalName || "附件" }}
            </a>
          </div>
          <div class="post-footer">
            <span class="post-time">{{ formatTime(post.createdAt) }}</span>
            <div class="post-actions">
              <button
                v-if="canEditPost(post)"
                class="post-action"
                type="button"
                @click="showToast('编辑功能待接入')"
              >
                编辑
              </button>
              <button
                v-if="canDeletePost(post)"
                class="post-action danger"
                type="button"
                @click="openDeleteDialog(post)"
              >
                删除
              </button>
            </div>
          </div>
        </article>

        <div
          v-if="visiblePosts.length === 0 && !loadingPosts"
          class="empty-tip"
        >
          当前分区还没有内容。
        </div>
        <div v-if="loadingPosts" class="empty-tip">加载中...</div>
      </section>

      <button class="footer-action" type="button" @click="openPublisher">
        {{ footerActionText }}
      </button>
      <div v-if="toastMessage" class="toast">{{ toastMessage }}</div>

      <transition name="publisher-backdrop">
        <div
          v-if="publisherOpen"
          class="publisher-backdrop"
          @click="closePublisher"
        ></div>
      </transition>
      <section
        class="publisher-sheet"
        :class="{ open: publisherOpen, expanded: hasPreview }"
        :aria-hidden="!publisherOpen"
      >
        <header class="publisher-header">
          <div class="publisher-title">
            {{ isGoodNewsMode ? "发布喜报" : "发布动态" }}
          </div>
          <button class="publisher-close" type="button" @click="closePublisher">
            关闭
          </button>
        </header>

        <div class="publisher-body">
          <div class="publisher-section-title">内容</div>
          <div class="publisher-section-divider"></div>
          <div class="editor-toolbar">
            <div class="toolbar-item-wrap">
              <div
                class="toolbar-dropdown"
                ref="headingDropdown"
                :class="{ open: headingMenuOpen }"
              >
                <button
                  class="toolbar-button"
                  type="button"
                  title="标题"
                  @click.stop="toggleHeadingMenu"
                >
                  <span class="icon" aria-hidden="true">
                    <svg
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="1.8"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    >
                      <path d="M6 5v14M18 5v14" />
                      <path d="M6 12h12" />
                    </svg>
                  </span>
                </button>
                <div
                  class="toolbar-menu"
                  :class="{ open: headingMenuOpen }"
                  :aria-hidden="!headingMenuOpen"
                  @click.stop
                >
                  <button
                    type="button"
                    class="toolbar-menu-item"
                    @click="applyHeading(2)"
                  >
                    二级标题
                  </button>
                  <button
                    type="button"
                    class="toolbar-menu-item"
                    @click="applyHeading(3)"
                  >
                    三级标题
                  </button>
                  <button
                    type="button"
                    class="toolbar-menu-item"
                    @click="applyHeading(4)"
                  >
                    四级标题
                  </button>
                  <button
                    type="button"
                    class="toolbar-menu-item"
                    @click="applyHeading(5)"
                  >
                    五级标题
                  </button>
                </div>
              </div>
            </div>

            <div class="toolbar-item-wrap">
              <button
                class="toolbar-button"
                type="button"
                title="加粗"
                @click="wrapInline('**', '粗体')"
              >
                <span class="icon" aria-hidden="true">
                  <svg
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.8"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <path d="M7 5h6a3.5 3.5 0 0 1 0 7H7V5z" />
                    <path d="M7 12h7a3.5 3.5 0 0 1 0 7H7v-7z" />
                  </svg>
                </span>
              </button>
            </div>

            <div class="toolbar-item-wrap">
              <button
                class="toolbar-button"
                type="button"
                title="斜体"
                @click="wrapInline('*', '斜体')"
              >
                <span class="icon" aria-hidden="true">
                  <svg
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.8"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <path d="M19 5h-8" />
                    <path d="M13 19H5" />
                    <path d="M14 5 10 19" />
                  </svg>
                </span>
              </button>
            </div>

            <div class="toolbar-item-wrap">
              <button
                class="toolbar-button"
                type="button"
                title="引用"
                @click="applyQuote"
              >
                <span class="icon quote-icon" aria-hidden="true">&gt;</span>
              </button>
            </div>

            <div class="toolbar-item-wrap">
              <button
                class="toolbar-button"
                type="button"
                title="表格"
                @click="insertTable"
              >
                <span class="icon" aria-hidden="true">
                  <svg
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.8"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <path d="M4 7h16" />
                    <path d="M4 12h16" />
                    <path d="M4 17h16" />
                    <path d="M8 5v14" />
                    <path d="M16 5v14" />
                    <rect x="4" y="5" width="16" height="14" rx="2" />
                  </svg>
                </span>
              </button>
            </div>

            <div class="toolbar-divider"></div>

            <div class="toolbar-item-wrap">
              <button
                class="toolbar-button"
                type="button"
                title="图片/视频"
                @click="openMediaDialog"
              >
                <span class="icon" aria-hidden="true">
                  <svg
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.8"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <rect x="4" y="6" width="16" height="12" rx="2" />
                    <path d="M8 14l2-2 3 3 3-3 2 2" />
                    <circle cx="9" cy="10" r="1" />
                  </svg>
                </span>
              </button>
              <input
                ref="mediaInput"
                type="file"
                accept="image/*,video/*"
                multiple
                hidden
                @change="onMediaChange"
              />
            </div>

            <div class="toolbar-item-wrap">
              <button
                class="toolbar-button"
                type="button"
                title="文件"
                @click="triggerFileUpload"
              >
                <span class="icon" aria-hidden="true">
                  <svg
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.8"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <path d="M8 7h6" />
                    <path d="M8 11h8" />
                    <path d="M8 15h6" />
                    <path
                      d="M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V9l-5-6z"
                    />
                    <path d="M14 3v6h6" />
                  </svg>
                </span>
              </button>
              <input
                ref="fileInput"
                type="file"
                accept=".doc,.ppt,.xls,.docx,.pptx,.xlsx,.pdf,.zip,.rar,.7z,.tar"
                multiple
                hidden
                @change="onMediaChange"
              />
            </div>

            <div class="toolbar-divider"></div>

            <div class="toolbar-item-wrap">
              <button
                class="toolbar-button"
                type="button"
                title="有序列表"
                @click="applyList('ordered')"
              >
                <span class="icon" aria-hidden="true">
                  <svg
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.8"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <path d="M8 6h12" />
                    <path d="M8 12h12" />
                    <path d="M8 18h12" />
                    <path d="M4 6h1" />
                    <path d="M4 12h1" />
                    <path d="M4 18h1" />
                  </svg>
                </span>
              </button>
            </div>

            <div class="toolbar-item-wrap">
              <button
                class="toolbar-button"
                type="button"
                title="无序列表"
                @click="applyList('unordered')"
              >
                <span class="icon" aria-hidden="true">
                  <svg
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.8"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <path d="M8 6h12" />
                    <path d="M8 12h12" />
                    <path d="M8 18h12" />
                    <circle cx="5" cy="6" r="1" />
                    <circle cx="5" cy="12" r="1" />
                    <circle cx="5" cy="18" r="1" />
                  </svg>
                </span>
              </button>
            </div>

            <div class="toolbar-item-wrap">
              <button
                class="toolbar-button"
                type="button"
                title="缩进"
                @click="indentSelection(true)"
              >
                <span class="icon" aria-hidden="true">
                  <svg
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.8"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <path d="M4 6h16" />
                    <path d="M4 12h10" />
                    <path d="M4 18h16" />
                    <path d="M14 9l3 3-3 3" />
                  </svg>
                </span>
              </button>
            </div>

            <div class="toolbar-item-wrap">
              <button
                class="toolbar-button"
                type="button"
                title="减少缩进"
                @click="indentSelection(false)"
              >
                <span class="icon" aria-hidden="true">
                  <svg
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.8"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <path d="M4 6h16" />
                    <path d="M4 12h10" />
                    <path d="M4 18h16" />
                    <path d="M10 9l-3 3 3 3" />
                  </svg>
                </span>
              </button>
            </div>
          </div>
          <textarea
            ref="composerInput"
            v-model="composer.content"
            class="publisher-input"
            maxlength="2000"
            placeholder="支持 Markdown：标题、引用、列表、表格、加粗、斜体..."
            @keydown="handleComposerKeydown"
          />

          <div v-if="hasPreview" class="publisher-preview">
            <div class="publisher-section-title">预览</div>
            <div class="publisher-section-divider"></div>
            <div class="preview-body post-content" v-html="previewHtml"></div>
          </div>
        </div>

        <div v-if="composer.media.length" class="publisher-media">
          <div
            v-for="(item, index) in composer.media"
            :key="item.url"
            class="media-chip"
          >
            <span class="chip-type">{{ item.mediaType }}</span>
            <span class="chip-name">{{ item.originalName || "附件" }}</span>
            <button type="button" @click="removeMedia(index)">移除</button>
          </div>
        </div>

        <div class="publisher-footer">
          <label class="publisher-check" v-if="!isGoodNewsMode && !isRecordsMode"
            ><input v-model="composer.isPrivate" type="checkbox" />
            仅自己可见</label
          >
          <label class="publisher-check"
            ><input v-model="composer.isAchievement" type="checkbox" />
            标记为个人成就</label
          >
          <span v-if="isUploading" class="publisher-hint">上传中...</span>
          <button
            class="action-button publish-btn"
            type="button"
            :disabled="composerBusy"
            @click="publishPost"
          >
            {{ composerBusy ? "发布中..." : "发布" }}
          </button>
        </div>
        <p v-if="composerError" class="form-tip">{{ composerError }}</p>
      </section>

      <transition name="dialog-fade">
        <div
          v-if="mediaDialogOpen"
          class="media-dialog-backdrop"
          @click="closeMediaDialog"
        ></div>
      </transition>
      <transition name="dialog-pop">
        <section v-if="mediaDialogOpen" class="media-dialog" @click.stop>
          <header class="media-dialog-header">
            <div class="media-dialog-title">上传图片/视频</div>
            <button
              class="media-dialog-close"
              type="button"
              @click="closeMediaDialog"
            >
              关闭
            </button>
          </header>
          <div class="media-dialog-body">
            <button
              class="action-button"
              type="button"
              @click="triggerMediaUpload"
            >
              选择文件
            </button>
            <div class="media-dialog-hint">
              支持多选，图片/视频会上传到服务器。
            </div>
            <div v-if="uploadQueue.length" class="upload-queue">
              <div v-for="item in uploadQueue" :key="item.id" class="upload-row">
                <div class="upload-row-header">
                  <span class="upload-name">{{ item.name }}</span>
                  <span class="upload-percent">{{ item.progress }}%</span>
                </div>
                <div class="upload-bar">
                  <div
                    class="upload-bar-fill"
                    :style="{ width: `${item.progress}%` }"
                  ></div>
                </div>
              </div>
            </div>
          </div>
        </section>
      </transition>

      <transition name="dialog-fade">
        <div
          v-if="deleteDialogOpen"
          class="dialog-backdrop"
          @click="closeDeleteDialog"
        ></div>
      </transition>
      <transition name="dialog-pop">
        <section v-if="deleteDialogOpen" class="dialog-card" @click.stop>
          <header class="dialog-header">确认删除</header>
          <div class="dialog-body">删除后无法恢复，确定要删除这条动态吗？</div>
          <div class="dialog-actions">
            <button class="ghost-button" type="button" @click="closeDeleteDialog">
              取消
            </button>
            <button
              class="action-button"
              type="button"
              :disabled="deleteBusy"
              @click="confirmDelete"
            >
              {{ deleteBusy ? "删除中..." : "确定删除" }}
            </button>
          </div>
        </section>
      </transition>
    </main>
  </div>
</template>

<script setup>
import {
  computed,
  nextTick,
  onMounted,
  onBeforeUnmount,
  reactive,
  ref,
  watch,
} from "vue";
import { useRoute, useRouter } from "vue-router";
import { getMe } from "../api/auth";
import { createPost, deletePost, getPosts, uploadMedia } from "../api/posts";

const router = useRouter();
const route = useRoute();
const API_BASE = "http://localhost:8080";

const menuItems = computed(() => [
  { key: "campus", label: "校园生活" },
  { key: "good-news", label: "喜报🎉" },
  { key: "records", label: "记录" },
  { key: "achievements", label: "个人成就" },
  { key: "my-info", label: "我的信息" },
  { key: "contacts", label: "教师/部门联系方式" },
  { key: "student-info", label: "学生信息" },
  { key: "admin", label: "后台管理" },
]);

const menuLabelMap = {
  campus: "校园生活",
  "good-news": "喜报🎉",
  records: "记录",
  achievements: "个人成就",
  "my-info": "我的信息",
  contacts: "教师/部门联系方式",
  "student-info": "学生信息",
  admin: "后台管理",
};

const roleLabelMap = {
  STUDENT: "学生",
  TEACHER: "教师",
  ADMIN: "管理员",
};

const activeMenu = ref("campus");
const profile = reactive(loadUser());
const posts = ref([]);
const loadingPosts = ref(false);

const composerBusy = ref(false);
const composerError = ref("");
const uploadingCount = ref(0);
const composerInput = ref(null);
const mediaInput = ref(null);
const fileInput = ref(null);

const publisherOpen = ref(false);
const headingMenuOpen = ref(false);
const mediaDialogOpen = ref(false);
const toastMessage = ref("");
let toastTimer = null;
const headingDropdown = ref(null);
const deleteDialogOpen = ref(false);
const deleteBusy = ref(false);
const deleteTarget = ref(null);
const uploadQueue = ref([]);

const composer = reactive({
  content: "",
  isPrivate: false,
  isAchievement: false,
  media: [],
});

const roleLabel = computed(() => roleLabelMap[profile.role] || "学生");
const avatarText = computed(() => {
  const name = profile.displayName || profile.username || "同学";
  return name.slice(0, 1).toUpperCase();
});
const currentMenuLabel = computed(
  () => menuLabelMap[activeMenu.value] || "校园生活",
);
const canPost = computed(() => Boolean(profile.username));
const isFeedMenu = computed(() =>
  ["campus", "good-news", "records"].includes(activeMenu.value),
);
const showComposer = computed(() =>
  ["campus", "good-news", "records"].includes(activeMenu.value),
);
const isRecordsMode = computed(() => activeMenu.value === "records");
const isGoodNewsMode = computed(() => activeMenu.value === "good-news");
const isUploading = computed(() => uploadingCount.value > 0);
const canOpenPublisher = computed(() => canPost.value && showComposer.value);
const previewHtml = computed(() => renderMarkdown(composer.content));
const hasPreview = computed(() => (composer.content || "").trim().length > 0);
const footerActionText = computed(() => {
  if (activeMenu.value === "good-news") {
    return "发布喜报";
  }
  if (activeMenu.value === "campus") {
    return "发布动态";
  }
  return "发布";
});

const visiblePosts = computed(() => posts.value);

function syncMenuFromRoute() {
  if (route.path === "/congra") {
    activeMenu.value = "good-news";
    return;
  }
  if (route.path === "/memory") {
    activeMenu.value = "records";
    return;
  }
  activeMenu.value = "campus";
}

watch(
  () => route.path,
  () => {
    syncMenuFromRoute();
  },
  { immediate: true },
);

watch(activeMenu, () => {
  if (isFeedMenu.value) {
    fetchPosts();
  }
  publisherOpen.value = false;
  headingMenuOpen.value = false;
  resetComposerState();
  if (isRecordsMode.value) {
    composer.isPrivate = true;
  }
});

onMounted(async () => {
  try {
    const { data } = await getMe();
    saveUser(data);
    await fetchPosts();
  } catch {
    localStorage.removeItem("gcsc_token");
    localStorage.removeItem("gcsc_user");
    router.push("/login");
  }
  document.addEventListener("click", handleDocumentClick);
});

onBeforeUnmount(() => {
  document.removeEventListener("click", handleDocumentClick);
});

async function fetchPosts() {
  loadingPosts.value = true;
  try {
    const { data } = await getPosts(activeMenu.value);
    posts.value = Array.isArray(data) ? data : [];
    applyPostImageLayout();
  } catch {
    posts.value = [];
  } finally {
    loadingPosts.value = false;
  }
}

function openPublisher() {
  if (!canPost.value) {
    showToast("请先登录");
    return;
  }

  if (!showComposer.value) {
    activeMenu.value = "campus";
    nextTick(() => {
      publisherOpen.value = true;
      headingMenuOpen.value = false;
      composerInput.value && composerInput.value.focus();
    });
    return;
  }

  publisherOpen.value = true;
  headingMenuOpen.value = false;
  nextTick(() => composerInput.value && composerInput.value.focus());
}

function closePublisher() {
  publisherOpen.value = false;
  headingMenuOpen.value = false;
  mediaDialogOpen.value = false;
}

function toggleHeadingMenu() {
  headingMenuOpen.value = !headingMenuOpen.value;
}

function handleDocumentClick(event) {
  if (
    headingMenuOpen.value &&
    headingDropdown.value &&
    !headingDropdown.value.contains(event.target)
  ) {
    headingMenuOpen.value = false;
  }
}

function showToast(message) {
  toastMessage.value = message;
  if (toastTimer) {
    clearTimeout(toastTimer);
  }
  toastTimer = setTimeout(() => {
    toastMessage.value = "";
    toastTimer = null;
  }, 1600);
}

function isMenuEnabled(key) {
  return key === "campus" || key === "good-news" || key === "records" || key === "achievements";
}

function handleMenuClick(key) {
  if (!isMenuEnabled(key)) {
    return;
  }
  if (key === "achievements") {
    router.push("/achievements");
    return;
  }
  if (key === "good-news") {
    router.push("/congra");
    return;
  }
  if (key === "records") {
    router.push("/memory");
    return;
  }
  router.push("/home");
}

function resetComposerState() {
  composer.content = "";
  composer.isPrivate = false;
  composer.isAchievement = false;
  composer.media = [];
  composerError.value = "";
}

async function publishPost() {
  if (composerBusy.value) {
    return;
  }
  composerError.value = "";

  const payload = {
    content: composer.content,
    goodNews: isGoodNewsMode.value,
    privatePost: isGoodNewsMode.value
      ? false
      : isRecordsMode.value
        ? true
        : composer.isPrivate,
    achievement: composer.isAchievement,
    media: composer.media,
  };

  if (!payload.content && payload.media.length === 0) {
    composerError.value = "内容或媒体至少填写一项";
    return;
  }

  composerBusy.value = true;
  try {
    const { data } = await createPost(payload);
    posts.value = [data, ...posts.value];
    applyPostImageLayout();
    resetComposerState();
    closePublisher();
  } catch (err) {
    composerError.value =
      err?.response?.data?.message || "发布失败，请稍后再试";
  } finally {
    composerBusy.value = false;
  }
}

function getInputSelection() {
  const input = composerInput.value;
  if (!input) {
    return null;
  }
  return {
    input,
    start: input.selectionStart || 0,
    end: input.selectionEnd || 0,
  };
}

function replaceRange(text, start, end, inserted) {
  return text.slice(0, start) + inserted + text.slice(end);
}

function wrapInline(marker, placeholder) {
  const sel = getInputSelection();
  if (!sel) {
    return;
  }
  headingMenuOpen.value = false;

  const { input, start, end } = sel;
  const value = composer.content || "";
  const selected = value.slice(start, end);
  const inner = selected || placeholder;
  const wrapped = `${marker}${inner}${marker}`;
  composer.content = replaceRange(value, start, end, wrapped);
  nextTick(() => {
    const innerStart = start + marker.length;
    const innerEnd = innerStart + inner.length;
    input.setSelectionRange(innerStart, innerEnd);
    input.focus();
  });
}

function applyHeading(level) {
  const sel = getInputSelection();
  if (!sel) {
    return;
  }
  const { input, start } = sel;
  const value = composer.content || "";
  const prefix = "#".repeat(level) + " ";
  headingMenuOpen.value = false;

  const lineStart = value.lastIndexOf("\n", Math.max(0, start - 1)) + 1;
  const lineEnd = value.indexOf("\n", start);
  const endIndex = lineEnd === -1 ? value.length : lineEnd;
  const currentLine = value.slice(lineStart, endIndex);
  const cleanedLine = currentLine.replace(/^#{1,6}\s+/, "");
  const replacedLine = prefix + cleanedLine;
  composer.content = replaceRange(value, lineStart, endIndex, replacedLine);
  nextTick(() => {
    const cursor = lineStart + prefix.length;
    input.setSelectionRange(cursor, cursor);
    input.focus();
  });
}

function applyQuote() {
  const sel = getInputSelection();
  if (!sel) {
    return;
  }
  headingMenuOpen.value = false;

  const { input, start, end } = sel;
  const value = composer.content || "";
  if (start === end) {
    const insert =
      start === 0 || value[start - 1] === "\n" ? "> 引用\n" : "\n> 引用\n";
    composer.content = replaceRange(value, start, end, insert);
    nextTick(() => {
      const quoteStart = start + (insert.startsWith("\n") ? 3 : 2);
      input.setSelectionRange(quoteStart, quoteStart + 2);
      input.focus();
    });
    return;
  }

  const selected = value.slice(start, end);
  const replaced = selected
    .split("\n")
    .map((line) => (line.startsWith("> ") ? line : `> ${line}`))
    .join("\n");
  composer.content = replaceRange(value, start, end, replaced);
  nextTick(() => {
    input.setSelectionRange(start, start + replaced.length);
    input.focus();
  });
}

function insertTable() {
  const sel = getInputSelection();
  if (!sel) {
    return;
  }
  headingMenuOpen.value = false;

  const { input, start, end } = sel;
  const value = composer.content || "";
  const table = `| 标题1 | 标题2 | 标题3 |\n| --- | --- | --- |\n| 内容 | 内容 | 内容 |\n`;
  const insert =
    start === 0 || value[start - 1] === "\n" ? table : `\n${table}`;
  composer.content = replaceRange(value, start, end, insert);
  nextTick(() => {
    const pickStart = start + (insert.startsWith("\n") ? 2 : 0) + 2;
    input.setSelectionRange(pickStart, pickStart + 3);
    input.focus();
  });
}

function applyList(kind) {
  const sel = getInputSelection();
  if (!sel) {
    return;
  }
  headingMenuOpen.value = false;

  const { input, start, end } = sel;
  const value = composer.content || "";
  const marker = kind === "ordered" ? "1. " : "- ";

  if (start === end) {
    const lineStart = value.lastIndexOf("\n", Math.max(0, start - 1)) + 1;
    const lineEnd = value.indexOf("\n", start) === -1
      ? value.length
      : value.indexOf("\n", start);
    const line = value.slice(lineStart, lineEnd);
    const indent = line.match(/^\s*/)?.[0] || "";
    const trimmed = line.trimStart();
    const stripped = trimmed
      .replace(/^\d+\.\s+/, "")
      .replace(/^-+\s+/, "");
    const hasText = stripped.trim().length > 0;

    if (hasText) {
      const newLine = `${indent}${marker}${stripped}`;
      composer.content = replaceRange(value, lineStart, lineEnd, newLine);
      nextTick(() => {
        const textStart = lineStart + indent.length + marker.length;
        input.setSelectionRange(textStart, textStart + stripped.length);
        input.focus();
      });
      return;
    }

    const insert = `${indent}${marker}文本`;
    composer.content = replaceRange(value, lineStart, lineEnd, insert);
    nextTick(() => {
      const textStart = lineStart + indent.length + marker.length;
      input.setSelectionRange(textStart, textStart + 2);
      input.focus();
    });
    return;
  }

  const blockStart = value.lastIndexOf("\n", Math.max(0, start - 1)) + 1;
  const blockEnd = value.indexOf("\n", end) === -1
    ? value.length
    : value.indexOf("\n", end);
  const block = value.slice(blockStart, blockEnd);
  const lines = block.split("\n");
  let index = 1;
  const replaced = lines
    .map((line) => {
      if (line.trim().length === 0) {
        return line;
      }
      const indent = line.match(/^\s*/)?.[0] || "";
      let content = line.trimStart();
      content = content.replace(/^\d+\.\s+/, "").replace(/^-+\s+/, "");
      if (kind === "ordered") {
        const current = index;
        index += 1;
        return `${indent}${current}. ${content}`;
      }
      return `${indent}- ${content}`;
    })
    .join("\n");
  composer.content = replaceRange(value, blockStart, blockEnd, replaced);
  nextTick(() => {
    input.setSelectionRange(blockStart, blockStart + replaced.length);
    input.focus();
  });
}

function indentSelection(add) {
  const sel = getInputSelection();
  if (!sel) {
    return;
  }
  headingMenuOpen.value = false;

  const { input, start, end } = sel;
  const value = composer.content || "";
  const blockStart = value.lastIndexOf("\n", Math.max(0, start - 1)) + 1;
  const blockEnd =
    start === end
      ? start
      : value.indexOf("\n", end) === -1
        ? value.length
        : value.indexOf("\n", end);
  const block = value.slice(blockStart, blockEnd);
  const replaced = block
    .split("\n")
    .map((line) => {
      if (add) {
        return line.length === 0 ? line : `  ${line}`;
      }
      if (line.startsWith("  ")) {
        return line.slice(2);
      }
      if (line.startsWith("\t")) {
        return line.slice(1);
      }
      return line;
    })
    .join("\n");
  composer.content = replaceRange(value, blockStart, blockEnd, replaced);
  nextTick(() => {
    const delta = replaced.length - block.length;
    if (start === end) {
      const newPos = Math.max(0, Math.min(value.length + delta, start + delta));
      input.setSelectionRange(newPos, newPos);
    } else {
      const newStart = start;
      const newEnd = Math.max(newStart, end + delta);
      input.setSelectionRange(newStart, newEnd);
    }
    input.focus();
  });
}

function handleComposerKeydown(event) {
  if (event.ctrlKey || event.metaKey) {
    const key = event.key.toLowerCase();
    if (key === "b") {
      event.preventDefault();
      wrapInline("**", "粗体");
      return;
    }
    if (key === "i") {
      event.preventDefault();
      wrapInline("*", "斜体");
      return;
    }
  }

  if (event.key === "Tab") {
    event.preventDefault();
    indentSelection(!event.shiftKey);
    return;
  }

  if (event.key !== "Enter") {
    return;
  }
  const input = composerInput.value;
  if (!input) {
    return;
  }
  const start = input.selectionStart || 0;
  const end = input.selectionEnd || 0;
  if (start !== end) {
    return;
  }

  const value = composer.content || "";
  const lineStart = value.lastIndexOf("\n", Math.max(0, start - 1)) + 1;
  const lineEnd = value.indexOf("\n", start) === -1
    ? value.length
    : value.indexOf("\n", start);
  const line = value.slice(lineStart, lineEnd);

  if (start !== lineEnd) {
    return;
  }

  const orderedMatch = line.match(/^(\s*)(\d+)\.\s*(.*)$/);
  const unorderedMatch = line.match(/^(\s*)-\s*(.*)$/);
  if (!orderedMatch && !unorderedMatch) {
    return;
  }

  const indent = orderedMatch ? orderedMatch[1] : unorderedMatch[1];
  const content = orderedMatch ? orderedMatch[3] : unorderedMatch[2];

  if (content.trim().length === 0) {
    event.preventDefault();
    composer.content = replaceRange(value, lineStart, lineEnd, "");
    nextTick(() => {
      input.setSelectionRange(lineStart, lineStart);
      input.focus();
    });
    return;
  }

  event.preventDefault();
  if (orderedMatch) {
    const nextNumber = Number(orderedMatch[2]) + 1;
    const insert = `\n${indent}${nextNumber}. `;
    const nextPos = start + insert.length;
    composer.content = replaceRange(value, start, start, insert);
    nextTick(() => {
      input.setSelectionRange(nextPos, nextPos);
      input.focus();
    });
    return;
  }

  const insert = `\n${indent}- `;
  const nextPos = start + insert.length;
  composer.content = replaceRange(value, start, start, insert);
  nextTick(() => {
    input.setSelectionRange(nextPos, nextPos);
    input.focus();
  });
}

function openMediaDialog() {
  mediaDialogOpen.value = true;
}

function closeMediaDialog() {
  mediaDialogOpen.value = false;
}

function triggerMediaUpload() {
  if (mediaInput.value) {
    mediaInput.value.click();
  }
}

function triggerFileUpload() {
  if (fileInput.value) {
    fileInput.value.click();
  }
}

async function onMediaChange(event) {
  const files = Array.from(event.target.files || []);
  const fromMediaPicker = event.target === mediaInput.value;
  event.target.value = "";
  if (files.length === 0) {
    return;
  }

  const input = composerInput.value;
  const hasInput = Boolean(input);
  let insertPos = hasInput
    ? input.selectionStart || 0
    : (composer.content || "").length;

  for (const file of files) {
    const queueItem = {
      id: `${Date.now()}_${Math.random().toString(16).slice(2)}`,
      name: file.name,
      progress: 0,
    };
    uploadQueue.value = [...uploadQueue.value, queueItem];
    const progressTimer = setInterval(() => {
      uploadQueue.value = uploadQueue.value.map((item) => {
        if (item.id !== queueItem.id) {
          return item;
        }
        const next = Math.min(item.progress + 6, 90);
        return { ...item, progress: next };
      });
    }, 200);
    const finalizeProgress = () => {
      clearInterval(progressTimer);
      uploadQueue.value = uploadQueue.value.map((item) =>
        item.id === queueItem.id ? { ...item, progress: 100 } : item,
      );
      setTimeout(() => {
        uploadQueue.value = uploadQueue.value.filter((item) => item.id !== queueItem.id);
      }, 600);
    };

    uploadingCount.value += 1;
    try {
      const { data } = await uploadMedia(file);
      composer.media.push({
        url: data.url,
        mediaType: data.mediaType,
        originalName: data.originalName,
      });

      const name = data.originalName || "媒体";
      const url = data.url;
      const markdown =
        data.mediaType === "IMAGE"
          ? `![${name}](${url})`
          : data.mediaType === "VIDEO"
            ? `<video class="md-video" controls src="${url}"></video>`
            : `[${name}](${url})`;
      const content = composer.content || "";
      const needsNewline = insertPos > 0 && content[insertPos - 1] !== "\n";
      const prefix = needsNewline ? "\n" : "";
      const insertText = `${prefix}${markdown}\n`;
      composer.content = replaceRange(content, insertPos, insertPos, insertText);

      if (hasInput) {
        const nameStart =
          insertPos + prefix.length + (data.mediaType === "IMAGE" ? 2 : 1);
        const nameEnd = nameStart + name.length;
        nextTick(() => {
          input.setSelectionRange(nameStart, nameEnd);
          input.focus();
        });
      }

      insertPos += insertText.length;
      finalizeProgress();
    } catch (err) {
      composerError.value = err?.response?.data?.message || "媒体上传失败";
      clearInterval(progressTimer);
      uploadQueue.value = uploadQueue.value.filter((item) => item.id !== queueItem.id);
    } finally {
      uploadingCount.value -= 1;
    }
  }

  if (fromMediaPicker) {
    mediaDialogOpen.value = false;
  }
}

function removeMedia(index) {
  composer.media.splice(index, 1);
}

function loadUser() {
  try {
    const raw = JSON.parse(localStorage.getItem("gcsc_user") || "{}");
    return {
      username: raw.username || "",
      displayName: raw.displayName || "",
      role: raw.role || "STUDENT",
      studentNo: raw.studentNo || "",
      className: raw.className || "",
      college: raw.college || "",
    };
  } catch {
    return {
      username: "",
      displayName: "",
      role: "STUDENT",
      studentNo: "",
      className: "",
      college: "",
    };
  }
}

function saveUser(data) {
  const user = {
    username: data.username,
    displayName: data.displayName,
    role: data.role || "STUDENT",
    studentNo: data.studentNo || "",
    className: data.className || "",
    college: data.college || "",
  };
  Object.assign(profile, user);
  localStorage.setItem("gcsc_user", JSON.stringify(user));
}

function formatTime(timestamp) {
  if (!timestamp) {
    return "";
  }
  const date =
    typeof timestamp === "string"
      ? new Date(timestamp)
      : new Date(Number(timestamp));
  return date.toLocaleString("zh-CN", {
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function applyPostImageLayout() {
  nextTick(() => {
    const images = document.querySelectorAll(".post-content .md-image");
    images.forEach((img) => {
      if (img.dataset.aspectReady) {
        return;
      }
      const apply = () => {
        const width = img.naturalWidth || img.width;
        const height = img.naturalHeight || img.height;
        if (!width || !height) {
          return;
        }
        const ratio = width / height;
        img.classList.toggle("full", ratio >= 1.4);
        img.classList.toggle("half", ratio < 1.4);
        img.dataset.aspectReady = "1";
      };
      if (img.complete) {
        apply();
      } else {
        img.addEventListener("load", apply, { once: true });
      }
    });
  });
}

function postAvatarText(post) {
  const name = post?.authorName || post?.authorUsername || "同学";
  return name.slice(0, 1).toUpperCase();
}

function canEditPost(post) {
  if (!post || !profile.username) {
    return false;
  }
  return post.authorUsername === profile.username;
}

function canDeletePost(post) {
  if (canEditPost(post)) {
    return true;
  }
  return profile.role === "TEACHER" || profile.role === "ADMIN";
}

function openDeleteDialog(post) {
  if (!post || !post.id) {
    return;
  }
  deleteTarget.value = post;
  deleteDialogOpen.value = true;
}

function closeDeleteDialog() {
  if (deleteBusy.value) {
    return;
  }
  deleteDialogOpen.value = false;
  deleteTarget.value = null;
}

async function confirmDelete() {
  const post = deleteTarget.value;
  if (!post || !post.id) {
    closeDeleteDialog();
    return;
  }
  deleteBusy.value = true;
  try {
    await deletePost(post.id);
    posts.value = posts.value.filter((item) => item.id !== post.id);
    showToast("已删除");
  } catch (err) {
    if (err?.response?.status === 401) {
      showToast("登录已过期，请重新登录");
      return;
    }
    showToast(err?.response?.data?.message || "删除失败");
  } finally {
    deleteBusy.value = false;
    closeDeleteDialog();
  }
}

function logout() {
  localStorage.removeItem("gcsc_token");
  localStorage.removeItem("gcsc_user");
  router.push("/login");
}

function escapeHtml(text) {
  return text
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}

function renderMarkdown(text) {
  if (!text) {
    return "";
  }
  const rawBlocks = [];
  const rawToken = (index) => `__RAW_${index}__`;
  const withRaw = text.replace(/<video[\s\S]*?<\/video>/gi, (match) => {
    const token = rawToken(rawBlocks.length);
    rawBlocks.push(match);
    return token;
  });
  const lines = withRaw.replace(/\r\n?/g, "\n").split("\n");
  const out = [];

  function normalizeUrl(raw) {
    const trimmed = raw.trim();
    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
      return trimmed;
    }
    if (trimmed.startsWith("/")) {
      return `${API_BASE}${trimmed}`;
    }
    return trimmed;
  }

  function inline(s) {
    const parts = [];
    const pattern = /!\[([^\]]*)\]\(([^)]+)\)|\[([^\]]+)\]\(([^)]+)\)/g;
    let lastIndex = 0;
    let match;
    while ((match = pattern.exec(s)) !== null) {
      const before = s.slice(lastIndex, match.index);
      parts.push(escapeHtml(before));
      if (match[1] !== undefined) {
        const alt = escapeHtml(match[1]);
        const src = normalizeUrl(match[2].trim());
        parts.push(`<img class="md-image" alt="${alt}" src="${src}">`);
      } else {
        const text = escapeHtml(match[3]);
        const href = normalizeUrl(match[4].trim());
        parts.push(
          `<a class="md-link" href="${href}" target="_blank" rel="noreferrer">${text}</a>`,
        );
      }
      lastIndex = match.index + match[0].length;
    }
    parts.push(escapeHtml(s.slice(lastIndex)));
    let v = parts.join("");
    v = v.replace(/\*\*(.+?)\*\*/g, "<strong>$1</strong>");
    v = v.replace(/\*(.+?)\*/g, "<em>$1</em>");
    return v;
  }

  function splitTableRow(row) {
    const trimmed = row.trim().replace(/^\|/, "").replace(/\|$/, "");
    return trimmed.split("|").map((c) => c.trim());
  }

  function indentToNbsp(indent) {
    if (!indent) {
      return "";
    }
    return "\u00A0".repeat(indent.length);
  }

  function isTableSep(row) {
    const t = row.trim().replace(/^\|/, "").replace(/\|$/, "");
    if (!t.includes("-")) {
      return false;
    }
    return t.split("|").every((cell) => /^:?-{3,}:?$/.test(cell.trim()));
  }

  let i = 0;
  while (i < lines.length) {
    const line = lines[i];

    if (line.trim() === "") {
      out.push("<br>");
      i += 1;
      continue;
    }

    const imageOnly = line.match(/^\s*!\[([^\]]*)\]\(([^)]+)\)\s*$/);
    if (imageOnly) {
      const alt = escapeHtml(imageOnly[1] || "");
      const src = normalizeUrl(imageOnly[2].trim());
      out.push(`<img class="md-image" alt="${alt}" src="${src}">`);
      i += 1;
      continue;
    }

    if (
      line.includes("|") &&
      i + 1 < lines.length &&
      isTableSep(lines[i + 1])
    ) {
      const headerCells = splitTableRow(line);
      i += 2;
      const rows = [];
      while (
        i < lines.length &&
        lines[i].trim() !== "" &&
        lines[i].includes("|")
      ) {
        rows.push(splitTableRow(lines[i]));
        i += 1;
      }

      const thead = `<thead><tr>${headerCells.map((c) => `<th>${inline(c)}</th>`).join("")}</tr></thead>`;
      const tbodyRows = rows
        .map(
          (row) =>
            `<tr>${row.map((c) => `<td>${inline(c)}</td>`).join("")}</tr>`,
        )
        .join("");
      out.push(
        `<table class="md-table">${thead}<tbody>${tbodyRows}</tbody></table>`,
      );
      continue;
    }

    if (/^\s*-\s+/.test(line)) {
      const items = [];
      while (i < lines.length && /^\s*-\s+/.test(lines[i])) {
        const match = lines[i].match(/^(\s*)-\s+(.*)$/);
        const indent = match ? match[1] : "";
        const content = match ? match[2] : lines[i].replace(/^\s*-\s+/, "");
        items.push(indentToNbsp(indent) + content);
        i += 1;
      }
      out.push(
        `<ul>${items.map((t) => `<li>${inline(t)}</li>`).join("")}</ul>`,
      );
      continue;
    }

    if (/^\s*\d+\.\s+/.test(line)) {
      const items = [];
      while (i < lines.length && /^\s*\d+\.\s+/.test(lines[i])) {
        const match = lines[i].match(/^(\s*)\d+\.\s+(.*)$/);
        const indent = match ? match[1] : "";
        const content = match ? match[2] : lines[i].replace(/^\s*\d+\.\s+/, "");
        items.push(indentToNbsp(indent) + content);
        i += 1;
      }
      out.push(
        `<ol>${items.map((t) => `<li>${inline(t)}</li>`).join("")}</ol>`,
      );
      continue;
    }

    if (/^\s*>\s+/.test(line)) {
      const q = [];
      while (i < lines.length && /^\s*>\s+/.test(lines[i])) {
        q.push(lines[i].replace(/^\s*>\s+/, ""));
        i += 1;
      }
      out.push(
        `<blockquote class="md-quote"><span class="md-quote-text">${inline(
          q.join("\n"),
        )}</span></blockquote>`,
      );
      continue;
    }

    const headingMatch = line.match(/^(#{1,6})\s*(.*)$/);
    if (headingMatch) {
      const level = headingMatch[1].length;
      const content = inline(headingMatch[2] || "");
      out.push(`<h${level}>${content}</h${level}>`);
      i += 1;
      continue;
    }

    const paragraph = [];
    while (i < lines.length) {
      const l = lines[i];
      if (l.trim() === "") {
        break;
      }
      if (l.includes("|") && i + 1 < lines.length && isTableSep(lines[i + 1])) {
        break;
      }
      if (
        /^\s*-\s+/.test(l) ||
        /^\s*\d+\.\s+/.test(l) ||
        /^\s*>\s+/.test(l) ||
        /^(#{1,6})\s+/.test(l)
      ) {
        break;
      }
      paragraph.push(l);
      i += 1;
    }
    out.push(
      `<div class="md-paragraph">${inline(paragraph.join("\n"))}</div>`,
    );
  }

  let html = out.join("");
  html = html.replace(/!\[([^\]]*)\]\(([^)]+)\)/g, (_, alt, url) => {
    const src = normalizeUrl(url.trim());
    return `<img class="md-image" alt="${escapeHtml(alt)}" src="${src}">`;
  });
  html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, (_, text, url) => {
    const href = normalizeUrl(url.trim());
    return `<a class="md-link" href="${href}" target="_blank" rel="noreferrer">${escapeHtml(text)}</a>`;
  });
  rawBlocks.forEach((raw, index) => {
    html = html.replace(rawToken(index), raw);
  });
  return html
    .replace(
      /src="(\/uploads\/[^"]+)"/g,
      (_, url) => `src="${normalizeUrl(url)}"`,
    )
    .replace(
      /href="(\/uploads\/[^"]+)"/g,
      (_, url) => `href="${normalizeUrl(url)}"`,
    );
}

function resolveMediaUrl(url) {
  if (!url) {
    return "";
  }
  if (url.startsWith("http://") || url.startsWith("https://")) {
    return url;
  }
  return `${API_BASE}${url}`;
}
</script>
