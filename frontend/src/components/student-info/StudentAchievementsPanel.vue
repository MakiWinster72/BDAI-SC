<script setup>
defineProps({
  open: { type: Boolean, default: false },
  closing: { type: Boolean, default: false },
  achievementUrl: { type: String, required: true },
  hasStudent: { type: Boolean, default: false },
});

const emit = defineEmits(["close"]);
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
      <iframe
        class="student-achievements-frame"
        :key="achievementUrl"
        :src="achievementUrl"
        title="学生个人成就"
      />
    </div>
  </section>
</template>

<style scoped>
@import "@/assets/styles/student-info-view-scoped.css";
</style>
