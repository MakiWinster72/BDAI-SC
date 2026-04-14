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
  onExportComplete: {
    type: Function,
    default: null,
  },
  onExportError: {
    type: Function,
    default: null,
  },
});

const { exportResumePdf, pdfExporting } = useStudentPdfExport();

const isDisabled = computed(
  () => props.disabled || pdfExporting.value || (!props.student && !props.getStudent),
);

async function handleExport() {
  const student = props.getStudent ? props.getStudent() : props.student;
  if (!student) {
    console.warn("[ExportPdfButton] no student, aborting");
    return;
  }
  console.log("[ExportPdfButton] starting export for student:", student.fullName || student.name);
  try {
    await exportResumePdf({
      student,
      resolveMediaUrl: props.resolveMediaUrl,
      onComplete: (success) => {
        console.log("[ExportPdfButton] onComplete called, success=", success);
        try {
          if (success && props.onExportComplete) {
            console.log("[ExportPdfButton] calling onExportComplete");
            props.onExportComplete();
          } else if (!success && props.onExportError) {
            console.log("[ExportPdfButton] calling onExportError");
            props.onExportError();
          } else {
            console.warn("[ExportPdfButton] onComplete but no handler: success=", success);
          }
        } catch (cbErr) {
          console.error("[ExportPdfButton] callback error:", cbErr);
        }
      },
    });
  } catch (err) {
    console.error("[ExportPdfButton] export error:", err);
    if (props.onExportError) {
      props.onExportError();
    }
  }
}
</script>
