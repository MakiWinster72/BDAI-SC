<template>
  <transition name="publisher-backdrop" appear>
    <div
      v-if="panelOpen && !viewClosing"
      class="achievement-backdrop"
      @click="handleClose"
    ></div>
  </transition>
  <section
    class="achievement-view"
    :class="{
      open: panelOpen && !viewClosing,
      closing: viewClosing,
      'exit-up': viewExitUp,
    }"
    :aria-hidden="!panelOpen || viewClosing"
  >
    <header class="publisher-header">
      <div class="publisher-title">成果查看</div>
      <button class="publisher-close" type="button" @click="handleClose">
        关闭
      </button>
    </header>
    <div v-if="viewLoading" class="empty-tip">加载中...</div>
    <AchievementDetailRenderer
      v-else-if="viewItem"
      :item="viewItem"
      :attachment-icon="attachmentIcon"
      :is-video-file="isVideoFile"
      :is-document-file="isDocumentFile"
      :is-sheet-file="isSheetFile"
      :is-pdf-file="isPdfFile"
      :is-allowed-image="isAllowedImage"
      :is-pptx-file="isPptxFile"
      @preview="(urls, index, hint) => emit('preview', urls, index, hint)"
      @download="emit('download', $event)"
      @edit="emit('edit')"
      @delete="emit('delete')"
    />
  </section>
</template>

<script setup>
import { nextTick, onMounted, ref, watch } from "vue";
import AchievementDetailRenderer from "@/components/achievement/AchievementDetailRenderer.vue";

const props = defineProps({
  viewClosing: { type: Boolean, required: true },
  viewExitUp: { type: Boolean, required: true },
  viewLoading: { type: Boolean, required: true },
  viewItem: { type: Object, default: null },
  attachmentIcon: { type: Function, required: true },
  isVideoFile: { type: Function, required: true },
  isDocumentFile: { type: Function, required: true },
  isSheetFile: { type: Function, required: true },
  isPdfFile: { type: Function, required: true },
  isAllowedImage: { type: Function, required: true },
  isPptxFile: { type: Function, required: true },
});

const emit = defineEmits(["close", "preview", "download", "edit", "delete"]);

const panelOpen = ref(false);

onMounted(() => {
  nextTick(() => {
    requestAnimationFrame(() => {
      panelOpen.value = true;
    });
  });
});

watch(
  () => props.viewClosing,
  (closing) => {
    if (closing) {
      panelOpen.value = false;
    }
  },
);

function handleClose() {
  if (props.viewClosing) {
    return;
  }
  panelOpen.value = false;
  emit("close");
}
</script>
