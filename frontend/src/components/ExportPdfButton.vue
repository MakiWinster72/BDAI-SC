<template>
  <button
    :class="buttonClass"
    type="button"
    :disabled="isDisabled"
    @click="handleExport"
  >
    {{ pdfExporting ? loadingText : label }}
  </button>
</template>

<script setup>
import { computed } from "vue";
import { useStudentPdfExport } from "../composables/useStudentPdfExport";

const props = defineProps({
  student: {
    type: Object,
    default: null,
  },
  getStudent: {
    type: Function,
    default: null,
  },
  resolveMediaUrl: {
    type: Function,
    default: null,
  },
  label: {
    type: String,
    default: "导出PDF",
  },
  buttonClass: {
    type: String,
    default: "action-button",
  },
  loadingText: {
    type: String,
    default: "导出中...",
  },
  disabled: {
    type: Boolean,
    default: false,
  },
});

const { exportResumePdf, pdfExporting } = useStudentPdfExport();

const isDisabled = computed(
  () => props.disabled || pdfExporting.value || (!props.student && !props.getStudent),
);

async function handleExport() {
  const student = props.getStudent ? props.getStudent() : props.student;
  if (!student) {
    return;
  }
  await exportResumePdf({
    student,
    resolveMediaUrl: props.resolveMediaUrl,
  });
}
</script>
