<script setup>
import { nextTick, onMounted, ref, watch } from "vue";
import LoadingIndicator from "@/components/LoadingIndicator.vue";
import StudentProfileEditor from "@/components/StudentProfileEditor.vue";

const props = defineProps({
  open: { type: Boolean, default: false },
  closing: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  student: { type: Object, default: null },
  editing: { type: Boolean, default: false },
  canEdit: { type: Boolean, default: false },
  achievementsOpen: { type: Boolean, default: false },
  achievementsClosing: { type: Boolean, default: false },
  resolveMediaUrl: { type: Function, required: true },
  saveProfile: { type: Function, required: true },
});

const emit = defineEmits([
  "close",
  "saved",
  "open-achievements",
  "export-pdf",
  "start-edit",
  "cancel-edit",
  "save",
]);

const profileEditorRef = ref(null);
const panelOpen = ref(false);

onMounted(() => {
  nextTick(() => {
    requestAnimationFrame(() => {
      panelOpen.value = true;
    });
  });
});

watch(
  () => props.closing,
  (isClosing) => {
    if (isClosing) {
      panelOpen.value = false;
    }
  },
);

function triggerSave() {
  profileEditorRef.value?.triggerSave?.();
}

function triggerPdfExport() {
  profileEditorRef.value?.triggerPdfExport?.();
}

function cancelEdit() {
  profileEditorRef.value?.cancelEdit?.();
  emit("cancel-edit");
}

function handleClose() {
  if (props.closing) {
    return;
  }
  panelOpen.value = false;
  emit("close");
}

defineExpose({ triggerSave, triggerPdfExport, cancelEdit, profileEditorRef });
</script>

<template>
  <transition name="publisher-backdrop" appear>
    <div
      v-if="panelOpen && !closing"
      class="student-detail-backdrop"
      @click="handleClose"
    />
  </transition>
  <section
    class="student-detail-view"
    :class="{
      open: panelOpen && !closing,
      closing,
      split: achievementsOpen || achievementsClosing,
    }"
    :aria-hidden="!panelOpen || closing"
  >
    <div class="student-detail-handle" />
    <header class="student-detail-header">
      <div class="student-detail-title">学生详情</div>
      <button class="student-detail-close" type="button" @click="handleClose">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
          <path d="M18 6L6 18M6 6l12 12" />
        </svg>
      </button>
    </header>

    <div class="student-detail-body">
      <LoadingIndicator v-if="loading" label="加载学生详情中…" size="lg" block />
      <StudentProfileEditor
        v-else-if="student"
        ref="profileEditorRef"
        :student="student"
        :resolve-media-url="resolveMediaUrl"
        :save-profile="saveProfile"
        :can-edit="canEdit"
        :show-achievements="true"
        :editing="editing"
        @saved="emit('saved', $event)"
        @open-achievements="emit('open-achievements')"
        @start-edit="emit('start-edit')"
        @cancel-edit="emit('cancel-edit')"
      />
      <div v-else class="empty-tip">暂无学生信息</div>
    </div>

    <div v-if="!loading && student" class="student-detail-capsule">
      <template v-if="!editing">
        <button class="capsule-action" type="button" @click="emit('open-achievements')">
          成果
        </button>
        <button class="capsule-action" type="button" @click="emit('export-pdf')">
          PDF
        </button>
        <button class="capsule-action" type="button" @click="emit('start-edit')">
          编辑
        </button>
      </template>
      <template v-else>
        <button class="capsule-action" type="button" @click="emit('cancel-edit')">
          取消
        </button>
        <button class="capsule-action capsule-action-primary" type="button" @click="emit('save')">
          保存
        </button>
      </template>
    </div>
  </section>
</template>

<style scoped>
@import "@/assets/styles/student-info-view-scoped.css";
</style>
