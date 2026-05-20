<script setup>
defineProps({
  visible: { type: Boolean, default: false },
  exportLabel: { type: String, default: "导出" },
  bottom: { type: String, default: "24px" },
});

const emit = defineEmits(["cancel", "export"]);
</script>

<template>
  <div ref="rootRef" class="floating-actions" :style="{ bottom }">
    <Transition name="floating-action">
      <button
        v-if="visible"
        class="floating-btn floating-btn-cancel"
        type="button"
        @click="emit('cancel')"
      >
        取消选择
      </button>
    </Transition>
    <button
      v-if="visible"
      class="floating-btn floating-btn-export"
      type="button"
      @click="emit('export')"
    >
      {{ exportLabel }}
    </button>
  </div>
</template>

<style scoped>
@import "@/assets/styles/student-info-view-scoped.css";
</style>
