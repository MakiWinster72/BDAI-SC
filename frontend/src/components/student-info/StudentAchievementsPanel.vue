<script setup>
import { ref, watch } from "vue";
import LoadingIndicator from "@/components/LoadingIndicator.vue";

const props = defineProps({
  open: { type: Boolean, default: false },
  closing: { type: Boolean, default: false },
  achievementUrl: { type: String, required: true },
  hasStudent: { type: Boolean, default: false },
});

const emit = defineEmits(["close"]);

const iframeLoading = ref(false);

watch(
  () => [props.open, props.achievementUrl, props.hasStudent],
  ([open, url, hasStudent]) => {
    iframeLoading.value = Boolean(open && hasStudent && url);
  },
  { immediate: true },
);

function handleIframeLoad() {
  iframeLoading.value = false;
}
</script>

<template>
  <section
    class="student-achievements-view"
    :class="{ open, closing }"
    :aria-hidden="!open"
  >
    <div class="student-detail-handle" />
    <header class="student-detail-header">
      <div class="student-detail-title">个人成就</div>
      <button class="student-detail-close" type="button" @click="emit('close')">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
          <path d="M18 6L6 18M6 6l12 12" />
        </svg>
      </button>
    </header>
    <div v-if="hasStudent" class="student-achievements-body">
      <div v-if="iframeLoading" class="student-achievements-loading">
        <LoadingIndicator label="加载个人成就中…" size="lg" block />
      </div>
      <iframe
        class="student-achievements-frame"
        :key="achievementUrl"
        :src="achievementUrl"
        title="学生个人成就"
        @load="handleIframeLoad"
      />
    </div>
    <div v-else class="empty-tip">
      <LoadingIndicator v-if="open" label="准备加载…" size="md" block />
    </div>
  </section>
</template>

<style scoped>
@import "@/assets/styles/student-info-view-scoped.css";
</style>
