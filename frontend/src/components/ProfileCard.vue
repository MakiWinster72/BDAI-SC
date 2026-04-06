<template>
  <section class="profile-card" :class="{ compact }">
    <div class="profile-row profile-main">
      <div class="profile-avatar">
        <img
          v-if="profile.avatarUrl"
          :src="resolveMediaUrl(profile.avatarUrl)"
          alt="头像"
        />
        <span v-else>{{ avatarText }}</span>
      </div>
      <div class="profile-name-wrap">
        <p class="profile-name">
          {{ profile.displayName || profile.username || "同学" }}
        </p>
        <p class="profile-role">{{ roleLabel }}</p>
      </div>
      <div class="profile-actions">
        <button
          v-if="profile.role === 'ADMIN'"
          class="profile-manage"
          type="button"
          @click="$emit('menu-click', 'admin')"
        >
          管理
        </button>
        <button
          class="profile-settings"
          type="button"
          aria-label="设置"
          @click="$emit('settings-click')"
        >
          <img src="/assets/icons/settings.svg" alt="设置" />
        </button>
      </div>
    </div>
    <div v-if="compact" class="profile-meta-line">
      <span class="profile-meta-chip">学号：{{ profile.studentNo || "未填写" }}</span>
      <span class="profile-meta-chip">班级：{{ profile.className || "未填写" }}</span>
      <span class="profile-meta-chip">{{ profileExtraLabel }}：{{ profileExtraValue || "未填写" }}</span>
    </div>
    <template v-else>
      <div class="profile-row">学号：{{ profile.studentNo || "未填写" }}</div>
      <div class="profile-row">班级：{{ profile.className || "未填写" }}</div>
      <div class="profile-row">{{ profileExtraLabel }}：{{ profileExtraValue || "未填写" }}</div>
    </template>
  </section>
</template>

<script setup>
import { computed } from "vue";
import { API_BASE } from "../api/request";

const props = defineProps({
  profile: {
    type: Object,
    required: true,
  },
  profileExtraLabel: {
    type: String,
    default: "学院",
  },
  compact: {
    type: Boolean,
    default: false,
  },
});

defineEmits(["settings-click", "menu-click"]);

const avatarText = computed(() => {
  const name = props.profile.displayName || props.profile.username || "同学";
  return name.slice(0, 1).toUpperCase();
});

const roleLabel = computed(() => {
  if (props.profile.role === "ADMIN") {
    return "管理员";
  }
  if (props.profile.role === "TEACHER") {
    return "教师";
  }
  return "学生";
});

const profileExtraValue = computed(() => {
  return props.profile.studentCategory || props.profile.college || "";
});

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
