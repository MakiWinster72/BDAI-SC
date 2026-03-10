<template>
  <div class="dashboard-layout">
    <aside class="dashboard-left">
      <section class="profile-card">
        <div class="profile-row profile-main">
          <div class="profile-avatar">{{ avatarText }}</div>
          <div class="profile-name-wrap">
            <p class="profile-name">{{ profile.displayName || profile.username || '同学' }}</p>
            <p class="profile-role">{{ roleLabel }}</p>
          </div>
        </div>
        <div class="profile-row">学号：{{ profile.studentNo || '未填写' }}</div>
        <div class="profile-row">班级：{{ profile.className || '未填写' }}</div>
        <div class="profile-row">学院：{{ profile.college || '未填写' }}</div>
      </section>

      <section class="menu-card">
        <button
          v-for="item in menuItems"
          :key="item.key"
          class="menu-item"
          :class="{ active: activeMenu === item.key, disabled: !isMenuEnabled(item.key) }"
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
        <button class="ghost-button" type="button" @click="logout">退出登录</button>
      </header>

      <section
        v-if="canPost && showComposer"
        class="composer-shell"
        :class="{ open: composerOpen }"
      >
        <button class="composer-pill" type="button" @click="openComposer">
          <span class="pill-dot"></span>
          <span class="pill-text">分享一条校园动态...</span>
          <span class="pill-tags" v-if="isGoodNewsMode">喜报🎉</span>
        </button>

        <div class="composer-card" v-show="composerOpen">
          <div class="composer-toolbar">
            <button class="tool-button" type="button" @click="applyMarkdown('bold')"><strong>B</strong></button>
            <button class="tool-button" type="button" @click="applyMarkdown('italic')"><em>I</em></button>
            <button class="tool-button" type="button" @click="triggerMedia">媒体</button>
            <span v-if="isUploading" class="tool-tip">上传中...</span>
          </div>
          <textarea
            ref="composerInput"
            v-model.trim="composer.content"
            class="composer-input"
            maxlength="600"
            placeholder="写点什么...支持 Markdown：**加粗**、*斜体*"
          />
          <input ref="uploadInput" type="file" accept="image/*,video/*" multiple hidden @change="onMediaChange" />

          <div v-if="composer.media.length" class="composer-media">
            <div v-for="(item, index) in composer.media" :key="item.url" class="media-chip">
              <span>{{ item.originalName || '媒体' }}</span>
              <button type="button" @click="removeMedia(index)">移除</button>
            </div>
          </div>

          <div class="composer-bar">
            <label v-if="!isGoodNewsMode"><input v-model="composer.isPrivate" type="checkbox" /> 仅自己可见（记录）</label>
            <label><input v-model="composer.isAchievement" type="checkbox" /> 标记为个人成就</label>
            <button class="action-button publish-btn" type="button" :disabled="composerBusy" @click="publishPost">
              {{ composerBusy ? '发布中...' : '发布' }}
            </button>
          </div>
          <p v-if="composerError" class="form-tip">{{ composerError }}</p>
        </div>
      </section>

      <section class="feed-waterfall">
        <article v-for="post in visiblePosts" :key="post.id" class="post-card">
          <div class="post-top">
            <span class="tag role">{{ roleText(post.authorRole) }}</span>
            <span v-if="post.goodNews" class="tag good">喜报🎉</span>
            <span v-if="post.achievement" class="tag achie">个人成就</span>
            <span v-if="post.privatePost" class="tag private">记录</span>
          </div>
          <p class="post-content" v-html="renderMarkdown(post.content)"></p>
          <div v-if="post.media && post.media.length" class="post-media">
            <div v-for="item in post.media" :key="item.id || item.url" class="media-item">
              <img v-if="item.mediaType === 'IMAGE'" :src="resolveMediaUrl(item.url)" :alt="item.originalName || 'image'" />
              <video v-else controls :src="resolveMediaUrl(item.url)"></video>
            </div>
          </div>
          <div class="post-meta">
            <span>{{ post.authorName }}</span>
            <span>{{ formatTime(post.createdAt) }}</span>
          </div>
        </article>

        <div v-if="visiblePosts.length === 0 && !loadingPosts" class="empty-tip">当前分区还没有内容。</div>
        <div v-if="loadingPosts" class="empty-tip">加载中...</div>
      </section>

      <div class="footer-capsule">
        <button class="footer-action" type="button">{{ footerActionText }}</button>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getMe } from '../api/auth'
import { createPost, getPosts, uploadMedia } from '../api/posts'

const router = useRouter()
const API_BASE = 'http://localhost:8080'

const menuItems = computed(() => [
  { key: 'campus', label: '校园生活' },
  { key: 'good-news', label: '喜报🎉' },
  { key: 'records', label: '记录' },
  { key: 'achievements', label: '个人成就' },
  { key: 'my-info', label: '我的信息' },
  { key: 'contacts', label: '教师/部门联系方式' },
  { key: 'student-info', label: '学生信息' },
  { key: 'admin', label: '后台管理' }
])

const menuLabelMap = {
  campus: '校园生活',
  'good-news': '喜报🎉',
  records: '记录',
  achievements: '个人成就',
  'my-info': '我的信息',
  contacts: '教师/部门联系方式',
  'student-info': '学生信息',
  admin: '后台管理'
}

const roleLabelMap = {
  STUDENT: '学生',
  TEACHER: '教师',
  ADMIN: '管理员'
}

const activeMenu = ref('campus')
const profile = reactive(loadUser())
const posts = ref([])
const loadingPosts = ref(false)

const composerOpen = ref(false)
const composerBusy = ref(false)
const composerError = ref('')
const uploadingCount = ref(0)
const uploadInput = ref(null)
const composerInput = ref(null)

const composer = reactive({
  content: '',
  isPrivate: false,
  isAchievement: false,
  media: []
})

const roleLabel = computed(() => roleLabelMap[profile.role] || '学生')
const avatarText = computed(() => {
  const name = profile.displayName || profile.username || '同学'
  return name.slice(0, 1).toUpperCase()
})
const currentMenuLabel = computed(() => menuLabelMap[activeMenu.value] || '校园生活')
const canPost = computed(() => profile.role === 'STUDENT' || profile.role === 'TEACHER')
const isFeedMenu = computed(() => ['campus', 'good-news'].includes(activeMenu.value))
const showComposer = computed(() => ['campus', 'good-news'].includes(activeMenu.value))
const isGoodNewsMode = computed(() => activeMenu.value === 'good-news')
const isUploading = computed(() => uploadingCount.value > 0)
const footerActionText = computed(() => {
  if (activeMenu.value === 'good-news') {
    return '发布喜报'
  }
  if (activeMenu.value === 'campus') {
    return '发布动态'
  }
  return '发布'
})

const visiblePosts = computed(() => posts.value)

watch(activeMenu, () => {
  if (isFeedMenu.value) {
    fetchPosts()
  }
  if (!showComposer.value) {
    composerOpen.value = false
  }
  resetComposerState()
})

onMounted(async () => {
  try {
    const { data } = await getMe()
    saveUser(data)
    await fetchPosts()
  } catch {
    localStorage.removeItem('gcsc_token')
    localStorage.removeItem('gcsc_user')
    router.push('/login')
  }
})

async function fetchPosts() {
  loadingPosts.value = true
  try {
    const { data } = await getPosts(activeMenu.value)
    posts.value = Array.isArray(data) ? data : []
  } catch {
    posts.value = []
  } finally {
    loadingPosts.value = false
  }
}

function openComposer() {
  if (!composerOpen.value) {
    composerOpen.value = true
    nextTick(() => composerInput.value && composerInput.value.focus())
  }
}

function isMenuEnabled(key) {
  return key === 'campus' || key === 'good-news'
}

function handleMenuClick(key) {
  if (!isMenuEnabled(key)) {
    return
  }
  activeMenu.value = key
}

function resetComposerState() {
  composer.content = ''
  composer.isPrivate = false
  composer.isAchievement = false
  composer.media = []
  composerError.value = ''
}

async function publishPost() {
  if (composerBusy.value) {
    return
  }
  composerError.value = ''

  const payload = {
    content: composer.content,
    goodNews: isGoodNewsMode.value,
    privatePost: isGoodNewsMode.value ? false : composer.isPrivate,
    achievement: composer.isAchievement,
    media: composer.media
  }

  if (!payload.content && payload.media.length === 0) {
    composerError.value = '内容或媒体至少填写一项'
    return
  }

  composerBusy.value = true
  try {
    const { data } = await createPost(payload)
    posts.value = [data, ...posts.value]
    resetComposerState()
    composerOpen.value = false
  } catch (err) {
    composerError.value = err?.response?.data?.message || '发布失败，请稍后再试'
  } finally {
    composerBusy.value = false
  }
}

function applyMarkdown(type) {
  const input = composerInput.value
  if (!input) {
    return
  }
  const start = input.selectionStart
  const end = input.selectionEnd
  const value = composer.content
  const selected = value.slice(start, end)
  const marker = type === 'bold' ? '**' : '*'
  const wrapped = `${marker}${selected || (type === 'bold' ? '加粗' : '斜体')}${marker}`
  composer.content = value.slice(0, start) + wrapped + value.slice(end)
  nextTick(() => {
    const cursor = start + wrapped.length
    input.setSelectionRange(cursor, cursor)
    input.focus()
  })
}

function triggerMedia() {
  if (uploadInput.value) {
    uploadInput.value.click()
  }
}

async function onMediaChange(event) {
  const files = Array.from(event.target.files || [])
  event.target.value = ''
  if (files.length === 0) {
    return
  }

  for (const file of files) {
    uploadingCount.value += 1
    try {
      const { data } = await uploadMedia(file)
      composer.media.push({
        url: data.url,
        mediaType: data.mediaType,
        originalName: data.originalName
      })
    } catch {
      composerError.value = '媒体上传失败'
    } finally {
      uploadingCount.value -= 1
    }
  }
}

function removeMedia(index) {
  composer.media.splice(index, 1)
}

function loadUser() {
  try {
    const raw = JSON.parse(localStorage.getItem('gcsc_user') || '{}')
    return {
      username: raw.username || '',
      displayName: raw.displayName || '',
      role: raw.role || 'STUDENT',
      studentNo: raw.studentNo || '',
      className: raw.className || '',
      college: raw.college || ''
    }
  } catch {
    return {
      username: '',
      displayName: '',
      role: 'STUDENT',
      studentNo: '',
      className: '',
      college: ''
    }
  }
}

function saveUser(data) {
  const user = {
    username: data.username,
    displayName: data.displayName,
    role: data.role || 'STUDENT',
    studentNo: data.studentNo || '',
    className: data.className || '',
    college: data.college || ''
  }
  Object.assign(profile, user)
  localStorage.setItem('gcsc_user', JSON.stringify(user))
}

function formatTime(timestamp) {
  if (!timestamp) {
    return ''
  }
  const date = typeof timestamp === 'string' ? new Date(timestamp) : new Date(Number(timestamp))
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function roleText(role) {
  return roleLabelMap[role] || '学生'
}

function logout() {
  localStorage.removeItem('gcsc_token')
  localStorage.removeItem('gcsc_user')
  router.push('/login')
}

function escapeHtml(text) {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;')
}

function renderMarkdown(text) {
  if (!text) {
    return ''
  }
  let safe = escapeHtml(text)
  safe = safe.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  safe = safe.replace(/\*(.+?)\*/g, '<em>$1</em>')
  safe = safe.replace(/\n/g, '<br>')
  return safe
}

function resolveMediaUrl(url) {
  if (!url) {
    return ''
  }
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  return `${API_BASE}${url}`
}
</script>
