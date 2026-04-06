<script setup>
import { computed, reactive, ref, watch } from "vue";
import {
  buildPreviewSheets,
  createExportSelections,
  exportGroups,
  exportStudentRowsToExcel,
  familyRows,
  fetchAchievementsForStudents,
  getSelectedExportKeys,
} from "../utils/studentProfileExport";

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  title: {
    type: String,
    default: "导出信息选择",
  },
  previewTitle: {
    type: String,
    default: "导出预览(仅显示前三条)",
  },
  filenamePrefix: {
    type: String,
    default: "students_export",
  },
  emptyMessage: {
    type: String,
    default: "请先选择学生再导出。",
  },
  previewLimit: {
    type: Number,
    default: 3,
  },
  enablePdf: {
    type: Boolean,
    default: false,
  },
  exportPdf: {
    type: Function,
    default: null,
  },
  loadRows: {
    type: Function,
    required: true,
  },
});

const emit = defineEmits(["close"]);

const exportSelections = reactive(createExportSelections());
const exporting = ref(false);
const exportFormat = ref("excel");
const exportDialogClosing = ref(false);
const exportPreviewOpen = ref(false);
const exportPreviewClosing = ref(false);
const previewActiveSheet = ref("main");
const previewLoading = ref(false);
const previewRows = ref([]);
const previewAchievementData = ref([]);
let previewRequestId = 0;

const showPdfOption = computed(
  () => props.enablePdf && typeof props.exportPdf === "function",
);
const showFieldSelection = computed(() => exportFormat.value !== "pdf");

const isAllSelected = computed(() =>
  exportGroups.every((group) =>
    group.fields.every((field) => exportSelections[field.key]),
  ),
);
const previewSelectedKeys = computed(() => getSelectedExportKeys(exportSelections));
const previewSheets = computed(() =>
  buildPreviewSheets(
    previewRows.value,
    previewSelectedKeys.value,
    previewAchievementData.value,
  ),
);
const activePreviewTable = computed(() => {
  const sheet = previewSheets.value.find(
    (item) => item.id === previewActiveSheet.value,
  );
  return sheet?.table || [];
});
const previewHeader = computed(() => activePreviewTable.value[0] || []);
const previewTableRows = computed(() => activePreviewTable.value.slice(1, 6));

watch(
  () => props.open,
  (open) => {
    if (open) {
      exportDialogClosing.value = false;
      if (!showPdfOption.value) {
        exportFormat.value = "excel";
      }
      return;
    }
    exportDialogClosing.value = true;
    exportPreviewOpen.value = false;
    exportPreviewClosing.value = false;
    setTimeout(() => {
      exportDialogClosing.value = false;
    }, 260);
  },
);

watch(previewSheets, (sheets) => {
  if (!sheets.length) {
    previewActiveSheet.value = "main";
    return;
  }
  if (!sheets.some((sheet) => sheet.id === previewActiveSheet.value)) {
    previewActiveSheet.value = sheets[0].id;
  }
});

watch(exportPreviewOpen, (open) => {
  if (open) {
    refreshPreviewData();
  }
});

watch(showPdfOption, (enabled) => {
  if (!enabled && exportFormat.value === "pdf") {
    exportFormat.value = "excel";
  }
});

watch(
  exportSelections,
  () => {
    if (exportPreviewOpen.value && showFieldSelection.value) {
      refreshPreviewData();
    }
  },
  { deep: true },
);

function requestClose() {
  emit("close");
}

function toggleGroupSelection(group, checked) {
  group.fields.forEach((field) => {
    exportSelections[field.key] = checked;
  });
}

function toggleAllSelections(checked) {
  exportGroups.forEach((group) => {
    toggleGroupSelection(group, checked);
  });
}

function isGroupSelected(group) {
  return group.fields.every((field) => exportSelections[field.key]);
}

function openExportPreview() {
  if (!showFieldSelection.value) {
    return;
  }
  if (!getSelectedExportKeys(exportSelections).size) {
    window.alert("请选择至少一个导出字段。");
    return;
  }
  exportPreviewOpen.value = true;
  exportPreviewClosing.value = false;
}

function closeExportPreview() {
  exportPreviewOpen.value = false;
  exportPreviewClosing.value = true;
  setTimeout(() => {
    exportPreviewClosing.value = false;
  }, 260);
}

function setPreviewSheet(id) {
  previewActiveSheet.value = id;
}

async function refreshPreviewData() {
  if (!exportPreviewOpen.value || !showFieldSelection.value) {
    return;
  }
  previewLoading.value = true;
  const requestId = (previewRequestId += 1);
  try {
    const rows = await props.loadRows(props.previewLimit);
    if (requestId !== previewRequestId) {
      return;
    }
    const safeRows = Array.isArray(rows) ? rows.filter(Boolean) : [];
    previewRows.value = safeRows;

    const selectedKeys = getSelectedExportKeys(exportSelections);
    const hasAchievement = exportGroups
      .find((group) => group.id === "achievement")
      ?.fields.some((field) => selectedKeys.has(field.key));

    if (hasAchievement && safeRows.length) {
      previewAchievementData.value = await fetchAchievementsForStudents(safeRows);
      return;
    }
    previewAchievementData.value = [];
  } catch {
    if (requestId !== previewRequestId) {
      return;
    }
    previewRows.value = [];
    previewAchievementData.value = [];
  } finally {
    if (requestId === previewRequestId) {
      previewLoading.value = false;
    }
  }
}

async function handleConfirm() {
  if (exporting.value) {
    return;
  }
  const selectedKeys = getSelectedExportKeys(exportSelections);
  if (showFieldSelection.value && !selectedKeys.size) {
    window.alert("请选择至少一个导出字段。");
    return;
  }

  exporting.value = true;
  try {
    const rows = await props.loadRows();
    const safeRows = Array.isArray(rows) ? rows.filter(Boolean) : [];
    if (!safeRows.length) {
      window.alert(props.emptyMessage);
      return;
    }
    if (exportFormat.value === "pdf") {
      await props.exportPdf(safeRows);
    } else {
      await exportStudentRowsToExcel(safeRows, selectedKeys, {
        filenamePrefix: props.filenamePrefix,
      });
    }
    requestClose();
  } catch (error) {
    console.error(error);
    window.alert(error?.message || "导出失败，请稍后再试。");
  } finally {
    exporting.value = false;
  }
}
</script>

<template>
  <transition name="export-dialog-backdrop">
    <div
      v-if="open"
      class="export-dialog-backdrop"
      @click="requestClose"
    ></div>
  </transition>

  <section
    class="export-dialog"
    :class="{
      open,
      closing: exportDialogClosing,
      split: exportPreviewOpen || exportPreviewClosing,
    }"
  >
    <header class="export-dialog-header">
      <div class="export-dialog-title">
        {{ title }}
        <label class="export-all-toggle">
          <input
            type="checkbox"
            :checked="isAllSelected"
            @change="toggleAllSelections($event.target.checked)"
          />
          <span>全选</span>
        </label>
      </div>
      <button class="ghost-button" type="button" @click="requestClose">
        关闭
      </button>
    </header>

    <div class="export-dialog-body">
      <section v-if="showPdfOption" class="export-format-group">
        <div class="export-format-title">导出格式</div>
        <label class="export-format-option">
          <input v-model="exportFormat" type="radio" value="excel" />
          <span>Excel (.xlsx)</span>
        </label>
        <label class="export-format-option">
          <input v-model="exportFormat" type="radio" value="pdf" />
          <span>PDF (.pdf)</span>
        </label>
      </section>

      <section v-if="exportFormat === 'pdf'" class="export-format-note">
        <div class="export-format-note-title">PDF 导出说明</div>
        <div class="export-format-note-text">
          将使用现有的个人信息简历式 PDF 导出方案，不按 Excel 字段表格方式导出。
        </div>
      </section>

      <div
        v-if="showFieldSelection"
        v-for="group in exportGroups"
        :key="group.id"
        class="export-group"
      >
        <label class="export-group-title">
          <span>{{ group.label }}</span>
          <input
            type="checkbox"
            :checked="isGroupSelected(group)"
            @change="toggleGroupSelection(group, $event.target.checked)"
          />
        </label>

        <div class="export-group-options">
          <template v-if="group.id === 'family'">
            <div
              v-for="(row, index) in familyRows"
              :key="`family-row-${index}`"
              class="export-option-row"
            >
              <label
                v-for="field in row"
                :key="field.key"
                class="export-option"
              >
                <input v-model="exportSelections[field.key]" type="checkbox" />
                <span>{{ field.label }}</span>
              </label>
            </div>
          </template>

          <template v-else>
            <label
              v-for="field in group.fields"
              :key="field.key"
              class="export-option"
            >
              <input v-model="exportSelections[field.key]" type="checkbox" />
              <span>{{ field.label }}</span>
            </label>
          </template>
        </div>
      </div>
    </div>

    <footer class="export-dialog-actions">
      <button class="ghost-button" type="button" @click="requestClose">
        取消
      </button>
      <button
        v-if="showFieldSelection"
        class="ghost-button"
        type="button"
        @click="exportPreviewOpen ? closeExportPreview() : openExportPreview()"
      >
        {{ exportPreviewOpen ? "关闭预览" : "预览" }}
      </button>
      <button
        class="action-button export-confirm"
        type="button"
        :disabled="exporting"
        @click="handleConfirm"
      >
        {{ exporting ? "导出中..." : `确认导出${exportFormat === "pdf" ? " PDF" : " Excel"}` }}
      </button>
    </footer>
  </section>

  <section
    class="export-preview-view"
    :class="{ open: exportPreviewOpen, closing: exportPreviewClosing }"
    :aria-hidden="!exportPreviewOpen"
  >
    <header class="export-preview-header">
      <div class="export-preview-title">{{ previewTitle }}</div>
      <button class="ghost-button" type="button" @click="closeExportPreview">
        关闭
      </button>
    </header>

    <div class="export-preview-body">
      <div v-if="previewLoading" class="empty-tip">加载预览中...</div>
      <div v-else-if="!previewSheets.length" class="empty-tip">暂无可预览内容</div>
      <div v-else class="export-preview-grid">
        <table class="export-preview-table">
          <thead>
            <tr>
              <th v-for="(cell, index) in previewHeader" :key="index">
                {{ cell }}
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, rowIndex) in previewTableRows" :key="rowIndex">
              <td v-for="(cell, cellIndex) in row" :key="cellIndex">
                {{ cell }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="previewSheets.length" class="export-preview-tabs">
        <button
          v-for="sheet in previewSheets"
          :key="sheet.id"
          class="export-preview-tab"
          :class="{ active: sheet.id === previewActiveSheet }"
          type="button"
          @click="setPreviewSheet(sheet.id)"
        >
          {{ sheet.label }}
        </button>
      </div>
    </div>
  </section>
</template>

<style scoped>
.export-dialog-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(3, 18, 20, 0.35);
  backdrop-filter: blur(8px);
  z-index: 80;
}

.export-dialog {
  position: fixed;
  left: 50%;
  bottom: 16px;
  width: min(860px, calc(100vw - 32px));
  transform: translate(-50%, 120%) scale(0.98);
  opacity: 0;
  filter: blur(6px);
  pointer-events: none;
  border-radius: 22px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  background: linear-gradient(
    140deg,
    rgba(205, 255, 249, 0.96),
    rgba(197, 217, 226, 0.85)
  );
  box-shadow: 0 26px 60px rgba(3, 107, 114, 0.22);
  backdrop-filter: blur(12px);
  z-index: 90;
  transition:
    transform 0.9s cubic-bezier(0.22, 1, 0.36, 1),
    opacity 0.75s ease,
    filter 0.75s ease,
    left 0.75s cubic-bezier(0.22, 1, 0.36, 1),
    width 0.75s cubic-bezier(0.22, 1, 0.36, 1);
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.export-dialog.open {
  transform: translate(-50%, 0) scale(1);
  opacity: 1;
  filter: blur(0px);
  pointer-events: auto;
}

.export-dialog.closing {
  transform: translate(-50%, 120%) scale(0.98);
  opacity: 0;
  filter: blur(6px);
}

.export-dialog.split {
  left: 8px;
  width: calc(50vw - 16px);
  transform: translate(0, 120%) scale(0.98);
}

.export-dialog.split.open {
  transform: translate(0, 0) scale(1);
}

.export-dialog.split.closing {
  transform: translate(0, 120%) scale(0.98);
}

.export-dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 18px 10px;
  border-bottom: 1px solid rgba(3, 107, 114, 0.12);
}

.export-dialog-title {
  font-size: 16px;
  font-weight: 700;
  color: #0f4d55;
  display: inline-flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
}

.export-all-toggle {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #2d5d64;
}

.export-dialog-body {
  padding: 14px 18px 8px;
  overflow: auto;
  display: grid;
  gap: 14px;
}

.export-group {
  border-radius: 14px;
  border: 1px solid rgba(3, 107, 114, 0.12);
  background: rgba(255, 255, 255, 0.6);
  padding: 12px;
  display: grid;
  gap: 10px;
}

.export-format-group {
  border-radius: 14px;
  border: 1px solid rgba(3, 107, 114, 0.12);
  background: rgba(255, 255, 255, 0.72);
  padding: 12px;
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
}

.export-format-title {
  font-size: 14px;
  font-weight: 700;
  color: #0f4d55;
}

.export-format-option {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #2d5d64;
}

.export-format-note {
  border-radius: 14px;
  border: 1px dashed rgba(3, 107, 114, 0.24);
  background: rgba(255, 255, 255, 0.62);
  padding: 12px;
  display: grid;
  gap: 6px;
}

.export-format-note-title {
  font-size: 14px;
  font-weight: 700;
  color: #0f4d55;
}

.export-format-note-text {
  font-size: 13px;
  color: #547179;
  line-height: 1.5;
}

.export-group-title {
  font-size: 14px;
  font-weight: 700;
  color: #0f4d55;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.export-group-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 14px;
}

.export-option-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 14px;
  width: 100%;
}

.export-option {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #2d5d64;
}

.export-dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 12px 18px 16px;
  border-top: 1px solid rgba(3, 107, 114, 0.12);
  align-items: center;
}

.export-dialog-actions .ghost-button,
.export-dialog-actions .export-confirm {
  width: auto;
  min-width: 110px;
  height: 40px;
}

.export-confirm {
  padding: 0 22px;
}

.export-preview-view {
  position: fixed;
  right: 8px;
  bottom: 16px;
  width: calc(50vw - 16px);
  height: 80vh;
  transform: translate(0, 120%) scale(0.98);
  opacity: 0;
  filter: blur(6px);
  pointer-events: none;
  border-radius: 22px;
  border: 1px solid rgba(255, 255, 255, 0.35);
  background: linear-gradient(
    140deg,
    rgba(205, 255, 249, 0.92),
    rgba(197, 217, 226, 0.78)
  );
  box-shadow: 0 28px 70px rgba(3, 107, 114, 0.22);
  backdrop-filter: blur(12px);
  z-index: 92;
  transition:
    transform 0.9s cubic-bezier(0.22, 1, 0.36, 1),
    opacity 0.75s ease,
    filter 0.75s ease;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 12px 14px 16px;
}

.export-preview-view.open {
  transform: translate(0, 0) scale(1);
  opacity: 1;
  filter: blur(0px);
  pointer-events: auto;
}

.export-preview-view.closing {
  transform: translate(0, 120%) scale(0.98);
  opacity: 0;
  filter: blur(6px);
}

.export-preview-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.export-preview-title {
  font-size: 15px;
  font-weight: 700;
  color: #0f4d55;
}

.export-preview-body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 10px;
}

.export-preview-grid {
  flex: 1;
  min-height: 0;
  border-radius: 16px;
  border: 1px solid rgba(3, 107, 114, 0.12);
  background: rgba(255, 255, 255, 0.92);
  overflow: auto;
}

.export-preview-table {
  border-collapse: collapse;
  width: 100%;
  font-size: 12px;
  color: #1f4f57;
}

.export-preview-table th,
.export-preview-table td {
  border: 1px solid rgba(3, 107, 114, 0.12);
  padding: 6px 8px;
  white-space: nowrap;
}

.export-preview-table th {
  background: rgba(205, 255, 249, 0.8);
  font-weight: 700;
  position: sticky;
  top: 0;
  z-index: 1;
}

.export-preview-tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.export-preview-tab {
  border-radius: 10px;
  border: 1px solid rgba(3, 107, 114, 0.25);
  background: rgba(255, 255, 255, 0.7);
  color: #0f4d55;
  height: 32px;
  padding: 0 12px;
  cursor: pointer;
  font-weight: 600;
  font-size: 12px;
}

.export-preview-tab.active {
  background: rgba(205, 255, 249, 0.9);
  border-color: rgba(3, 107, 114, 0.45);
}

@media (max-width: 1100px) {
  .export-dialog.split {
    left: 50%;
    width: min(980px, calc(100vw - 32px));
    transform: translate(-50%, 120%) scale(0.98);
  }

  .export-dialog.split.open {
    transform: translate(-50%, 0) scale(1);
  }

  .export-dialog.split.closing {
    transform: translate(-50%, 120%) scale(0.98);
  }

  .export-preview-view {
    left: 50%;
    right: auto;
    width: min(980px, calc(100vw - 32px));
    height: 80vh;
    transform: translate(-50%, 120%) scale(0.98);
  }

  .export-preview-view.open {
    transform: translate(-50%, 0) scale(1);
  }

  .export-preview-view.closing {
    transform: translate(-50%, 120%) scale(0.98);
  }
}
</style>
