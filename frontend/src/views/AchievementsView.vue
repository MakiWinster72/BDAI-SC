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
        <h1 class="feed-title">个人成就</h1>
      </header>

      <div v-if="!achievements.length" class="empty-tip">
        还没有成就，点击右上角添加。
      </div>

      <section class="achievement-list">
        <article v-for="item in achievements" :key="item.id" class="achievement-card">
          <div class="achievement-card-image">
            <img v-if="item.image" :src="item.image" alt="成就图片" />
            <div v-else class="achievement-card-placeholder">未上传图片</div>
          </div>
          <div class="achievement-card-body">
            <h2 class="achievement-card-title">{{ item.name }}</h2>
            <div class="achievement-card-dates">
              <span>起止：{{ item.startDate || "-" }} 至 {{ item.endDate || "-" }}</span>
              <span>获得：{{ item.awardDate || "-" }}</span>
            </div>
            <div class="achievement-card-text">
              <strong>成就描述：</strong>{{ item.description || "-" }}
            </div>
            <div class="achievement-card-text">
              <strong>个人感想：</strong>{{ item.thoughts || "-" }}
            </div>
          </div>
        </article>
      </section>

      <button class="footer-action" type="button" @click="openEditor">
        添加成就
      </button>

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
          <div class="publisher-title">新增成就</div>
          <button class="publisher-close" type="button" @click="closeEditor">
            关闭
          </button>
        </header>
        <div class="achievement-grid">
          <button class="achievement-image" type="button" @click="triggerImage">
            <img v-if="imagePreview" :src="imagePreview" alt="成就图片" />
            <span v-else>选择图片</span>
          </button>

          <div class="achievement-fields">
            <div class="field-row">
              <label class="field-label">成就名字</label>
              <input v-model="form.name" class="form-input" type="text" placeholder="请输入成就名称" />
            </div>

            <div class="field-row">
              <label class="field-label">起止日期</label>
              <div class="date-range">
                <label class="date-input">
                  <span class="date-label">开始</span>
                  <input v-model="form.startDate" class="date-input-field" type="date" />
                </label>
                <label class="date-input">
                  <span class="date-label">结束</span>
                  <input v-model="form.endDate" class="date-input-field" type="date" />
                </label>
              </div>
              <label class="field-label">获得日期</label>
              <label class="date-input single">
                <span class="date-label">获得</span>
                <input v-model="form.awardDate" class="date-input-field" type="date" />
              </label>
            </div>

            <div class="field-row">
              <label class="field-label">成就描述</label>
              <textarea
                v-model="form.description"
                class="publisher-input"
                rows="3"
                placeholder="描述该成就的背景或亮点"
              ></textarea>
            </div>

            <div class="field-row">
              <label class="field-label">个人感想</label>
              <textarea
                v-model="form.thoughts"
                class="publisher-input"
                rows="3"
                placeholder="写下你的感受或收获"
              ></textarea>
            </div>

            <div class="achievement-actions">
              <button class="ghost-button" type="button" @click="closeEditor">
                取消
              </button>
              <button class="action-button" type="button" @click="saveAchievement">
                保存成就
              </button>
            </div>
          </div>
        </div>
      </section>

      <input
        ref="imageInput"
        type="file"
        accept="image/*"
        hidden
        @change="onImageChange"
      />
    </main>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const profile = reactive(loadUser());
const activeMenu = ref("achievements");
const editorOpen = ref(false);
const imageInput = ref(null);
const imagePreview = ref("");

const achievements = ref([]);

const form = reactive({
  name: "",
  startDate: "",
  endDate: "",
  awardDate: "",
  description: "",
  thoughts: "",
  image: "",
});

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

function isMenuEnabled(key) {
  return ["campus", "good-news", "records", "achievements"].includes(key);
}

function handleMenuClick(key) {
  if (!isMenuEnabled(key)) {
    return;
  }
  if (key === "achievements") {
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

function openEditor() {
  editorOpen.value = true;
}

function closeEditor() {
  editorOpen.value = false;
}

function resetForm() {
  form.name = "";
  form.startDate = "";
  form.endDate = "";
  form.awardDate = "";
  form.description = "";
  form.thoughts = "";
  form.image = "";
  imagePreview.value = "";
}

function saveAchievement() {
  const payload = {
    id: Date.now(),
    name: form.name.trim(),
    startDate: form.startDate,
    endDate: form.endDate,
    awardDate: form.awardDate,
    description: form.description.trim(),
    thoughts: form.thoughts.trim(),
    image: form.image,
  };
  achievements.value = [payload, ...achievements.value];
  resetForm();
  closeEditor();
}

function triggerImage() {
  imageInput.value && imageInput.value.click();
}

function onImageChange(event) {
  const [file] = Array.from(event.target.files || []);
  event.target.value = "";
  if (!file) {
    return;
  }
  const reader = new FileReader();
  reader.onload = () => {
    const src = String(reader.result || "");
    imagePreview.value = src;
    form.image = src;
  };
  reader.readAsDataURL(file);
}
</script>
