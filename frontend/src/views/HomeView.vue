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
          <div class="post-top">
            <span class="tag role">{{ roleText(post.authorRole) }}</span>
            <span v-if="post.goodNews" class="tag good">喜报🎉</span>
            <span v-if="post.achievement" class="tag achie">个人成就</span>
            <span v-if="post.privatePost" class="tag private">记录</span>
          </div>
          <div class="post-content" v-html="renderMarkdown(post.content)"></div>
          <div
            v-if="
              post.media &&
              post.media.some(
                (m) => m.mediaType === 'IMAGE' || m.mediaType === 'VIDEO',
              )
            "
            class="post-media"
          >
            <div
              v-for="item in post.media.filter(
                (m) => m.mediaType === 'IMAGE' || m.mediaType === 'VIDEO',
              )"
              :key="item.id || item.url"
              class="media-item"
            >
              <img
                v-if="item.mediaType === 'IMAGE'"
                :src="resolveMediaUrl(item.url)"
                :alt="item.originalName || 'image'"
              />
              <video
                v-else-if="item.mediaType === 'VIDEO'"
                controls
                :src="resolveMediaUrl(item.url)"
              ></video>
            </div>
          </div>
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
          <div class="post-meta">
            <span>{{ post.authorName }}</span>
            <span>{{ formatTime(post.createdAt) }}</span>
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

      <div
        v-show="publisherOpen"
        class="publisher-backdrop"
        @click="closePublisher"
      ></div>
      <section
        class="publisher-sheet"
        :class="{ open: publisherOpen }"
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

        <div class="publisher-body">
          <div class="publisher-section-title">内容</div>
          <div class="publisher-section-divider"></div>
          <textarea
            ref="composerInput"
            v-model="composer.content"
            class="publisher-input"
            maxlength="2000"
            placeholder="支持 Markdown：标题、引用、列表、表格、加粗、斜体..."
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
          <label class="publisher-check" v-if="!isGoodNewsMode"
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

      <div
        v-show="mediaDialogOpen"
        class="media-dialog-backdrop"
        @click="closeMediaDialog"
      ></div>
      <section v-show="mediaDialogOpen" class="media-dialog" @click.stop>
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
        </div>
      </section>
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
import { useRouter } from "vue-router";
import { getMe } from "../api/auth";
import { createPost, getPosts, uploadMedia } from "../api/posts";

const router = useRouter();
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
  ["campus", "good-news"].includes(activeMenu.value),
);
const showComposer = computed(() =>
  ["campus", "good-news"].includes(activeMenu.value),
);
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

watch(activeMenu, () => {
  if (isFeedMenu.value) {
    fetchPosts();
  }
  publisherOpen.value = false;
  headingMenuOpen.value = false;
  resetComposerState();
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
  return key === "campus" || key === "good-news";
}

function handleMenuClick(key) {
  if (!isMenuEnabled(key)) {
    return;
  }
  activeMenu.value = key;
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
    privatePost: isGoodNewsMode.value ? false : composer.isPrivate,
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
  const selected = value.slice(start, end);
  const lines = (start === end ? ["列表项"] : selected.split("\n")).filter(
    (l) => l.length > 0,
  );
  const replaced =
    lines
      .map((line, idx) => {
        if (kind === "ordered") {
          return `${idx + 1}. ${line.replace(/^\d+\.\s+/, "")}`;
        }
        return `- ${line.replace(/^-+\s+/, "")}`;
      })
      .join("\n") + (start === end ? "\n" : "");
  composer.content = replaceRange(value, start, end, replaced);
  nextTick(() => {
    input.setSelectionRange(start, start + replaced.length);
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
    input.setSelectionRange(blockStart, blockStart + replaced.length);
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

  for (const file of files) {
    uploadingCount.value += 1;
    try {
      const { data } = await uploadMedia(file);
      composer.media.push({
        url: data.url,
        mediaType: data.mediaType,
        originalName: data.originalName,
      });
    } catch (err) {
      composerError.value = err?.response?.data?.message || "媒体上传失败";
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

function roleText(role) {
  return roleLabelMap[role] || "学生";
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
  const lines = text.split("\n");
  const out = [];

  function inline(s) {
    let v = escapeHtml(s);
    v = v.replace(/\*\*(.+?)\*\*/g, "<strong>$1</strong>");
    v = v.replace(/\*(.+?)\*/g, "<em>$1</em>");
    return v;
  }

  function splitTableRow(row) {
    const trimmed = row.trim().replace(/^\|/, "").replace(/\|$/, "");
    return trimmed.split("|").map((c) => c.trim());
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
        items.push(lines[i].replace(/^\s*-\s+/, ""));
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
        items.push(lines[i].replace(/^\s*\d+\.\s+/, ""));
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

  return out.join("");
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
