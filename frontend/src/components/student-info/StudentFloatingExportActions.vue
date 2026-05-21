<script setup>
defineProps({
  visible: { type: Boolean, default: false },
  exportLabel: { type: String, default: "导出" },
  dockStyle: {
    type: Object,
    default: () => ({
      bottom: "calc(88px + env(safe-area-inset-bottom, 0px))",
      top: "auto",
    }),
  },
});

const emit = defineEmits(["cancel", "export"]);
</script>

<template>
  <Teleport to="body">
    <Transition name="select-float">
      <div
        v-if="visible"
        class="student-action-dock floating-actions-dock"
        :style="dockStyle"
      >
        <button
          class="floating-btn floating-btn-cancel"
          type="button"
          @click="emit('cancel')"
        >
          取消选择
        </button>
        <button
          class="floating-btn floating-btn-export"
          type="button"
          @click="emit('export')"
        >
          {{ exportLabel }}
        </button>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
@import "@/assets/styles/student-info-view-scoped.css";
</style>
