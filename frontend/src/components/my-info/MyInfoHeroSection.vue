<script setup>
import ProtectedMediaImage from "@/components/ProtectedMediaImage.vue";
import { useProfileAvatarUpload } from "@/composables/useProfileAvatarUpload";
import { resolveMediaUrl } from "@/utils/media";

const props = defineProps({
  info: {
    type: Object,
    required: true,
  },
  isEditing: {
    type: Boolean,
    default: false,
  },
  uploadFile: {
    type: Function,
    required: true,
  },
});

const { avatarInput, triggerAvatarUpload, handleAvatarChange } = useProfileAvatarUpload({
  canUpload: () => props.isEditing,
  upload: props.uploadFile,
});

async function onAvatarChange(event) {
  const avatarUrl = await handleAvatarChange(event);
  if (avatarUrl) {
    props.info.avatarUrl = avatarUrl;
  }
}
</script>

<template>
  <div class="info-hero">
    <button
      class="avatar-square"
      type="button"
      :disabled="!isEditing"
      @click="triggerAvatarUpload"
    >
      <ProtectedMediaImage
        v-if="info.avatarUrl"
        :src="resolveMediaUrl(info.avatarUrl)"
        alt="头像"
      />
      <span v-else>点击设置头像</span>
      <input
        ref="avatarInput"
        type="file"
        accept="image/*"
        hidden
        @change="onAvatarChange"
      />
    </button>
    <div class="info-hero-text">
      <div class="info-hero-title">基础信息</div>
      <div class="info-hero-subtitle">请使用真实照片，确保五官清晰。</div>
    </div>
  </div>
</template>
