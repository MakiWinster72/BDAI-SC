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
    default: "导出预览",
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
  onExportSuccess: {
    type: Function,
    default: null,
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
    }, 420);
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
  }, 420);
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
    if (props.onExportSuccess) {
      props.onExportSuccess();
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
  <!-- Backdrop: shared between main dialog and preview panel -->
  <div
    :class="['sheet-overlay', { open: open || exportPreviewOpen }]"
    @click.self="exportPreviewOpen ? closeExportPreview() : requestClose()"
  ></div>

  <!-- Main Export Dialog -->
  <section
    class="export-dialog"
    :class="{
      open,
      closing: exportDialogClosing,
      'split-right': exportPreviewOpen,
    }"
    role="dialog"
    aria-modal="true"
    :aria-label="title"
  >
    <header class="export-dialog-header">
      <div class="export-dialog-title-row">
        <h2 class="export-dialog-title">{{ title }}</h2>
        <label class="export-all-toggle">
          <input
            type="checkbox"
            :checked="isAllSelected"
            @change="toggleAllSelections($event.target.checked)"
          />
          <span>全选</span>
        </label>
      </div>
      <button class="ghost-button export-dialog-close" type="button" @click="requestClose" aria-label="关闭">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
          <path d="M18 6L6 18M6 6l12 12"/>
        </svg>
      </button>
    </header>

    <div class="export-dialog-body">
      <!-- Format selector -->
      <section v-if="showPdfOption" class="export-format-row">
        <span class="export-format-label">导出格式</span>
        <div class="export-format-options">
          <label class="export-format-option">
            <input v-model="exportFormat" type="radio" value="excel" />
            <span>Excel (.xlsx)</span>
          </label>
          <label class="export-format-option">
            <input v-model="exportFormat" type="radio" value="pdf" />
            <span>PDF (.pdf)</span>
          </label>
        </div>
      </section>

      <!-- PDF notice -->
      <section v-if="exportFormat === 'pdf'" class="export-notice">
        <svg class="export-notice-icon" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
          <circle cx="12" cy="12" r="10"/>
          <path d="M12 16v-4M12 8h.01"/>
        </svg>
        <span>将使用个人信息简历式 PDF 导出，不按 Excel 字段表格方式导出。</span>
      </section>

      <!-- Field groups -->
      <div
        v-if="showFieldSelection"
        class="export-groups"
      >
        <div
          v-for="group in exportGroups"
          :key="group.id"
          class="export-group"
        >
          <div class="export-group-header">
            <label class="export-group-check">
              <input
                type="checkbox"
                :checked="isGroupSelected(group)"
                @change="toggleGroupSelection(group, $event.target.checked)"
              />
              <span class="export-group-name">{{ group.label }}</span>
            </label>
          </div>

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
    </div>

    <footer class="export-dialog-footer">
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
        class="action-button"
        type="button"
        :disabled="exporting"
        @click="handleConfirm"
      >
        <span v-if="exporting" class="spinner spinner-sm" aria-hidden="true"></span>
        {{ exporting ? "导出中..." : `确认导出${exportFormat === 'pdf' ? ' PDF' : ''}` }}
      </button>
    </footer>
  </section>

  <!-- Preview Panel: slides in from the right when split mode -->
  <section
    class="export-preview-panel"
    :class="{ open: exportPreviewOpen, closing: exportPreviewClosing }"
    role="dialog"
    aria-modal="true"
    :aria-label="previewTitle"
  >
    <header class="export-preview-header">
      <h2 class="export-preview-title">{{ previewTitle }}</h2>
      <button class="ghost-button" type="button" @click="closeExportPreview" aria-label="关闭预览">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
          <path d="M18 6L6 18M6 6l12 12"/>
        </svg>
      </button>
    </header>

    <div class="export-preview-body">
      <div v-if="previewLoading" class="export-preview-loading" role="status" aria-live="polite">
        <div class="spinner spinner-sm" style="border-color: var(--line); border-top-color: var(--primary);" aria-hidden="true"></div>
        <span>加载预览中...</span>
      </div>
      <div v-else-if="!previewSheets.length" class="export-preview-empty">
        暂无可预览内容
      </div>
      <div v-else class="export-preview-content">
        <div class="export-preview-table-wrap">
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

        <div v-if="previewSheets.length > 1" class="export-preview-tabs" role="tablist">
          <button
            v-for="sheet in previewSheets"
            :key="sheet.id"
            class="export-preview-tab"
            :class="{ active: sheet.id === previewActiveSheet }"
            type="button"
            role="tab"
            :aria-selected="sheet.id === previewActiveSheet"
            @click="setPreviewSheet(sheet.id)"
          >
            {{ sheet.label }}
          </button>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
/* ── Export Dialog ─────────────────────────── */
/* Uses sheet-overlay from dialogs.css for backdrop */

.export-dialog {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, calc(-50% + 48px)) scale(0.97);
  width: min(860px, calc(100vw - 32px));
  max-height: 85vh;
  border-radius: 22px;
  border: 1px solid var(--line);
  background: var(--card);
  box-shadow: var(--shadow-glow);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  z-index: 1010;
  opacity: 0;
  pointer-events: none;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition:
    transform 0.42s cubic-bezier(0.22, 1, 0.36, 1),
    opacity 0.38s ease,
    left 0.42s cubic-bezier(0.22, 1, 0.36, 1),
    width 0.42s cubic-bezier(0.22, 1, 0.36, 1);
}

.export-dialog.open {
  transform: translate(-50%, -50%) scale(1);
  opacity: 1;
  pointer-events: auto;
}

.export-dialog.closing {
  transform: translate(-50%, calc(-50% + 48px)) scale(0.97);
  opacity: 0;
}

/* Split mode: dialog slides to left, preview panel appears on right */
.export-dialog.split-right {
  left: 16px;
  width: calc(50vw - 48px);
  transform: translateY(calc(-50% + 48px)) scale(0.97);
}

.export-dialog.split-right.open {
  transform: translateY(-50%) scale(1);
}

.export-dialog.split-right.closing {
  transform: translateY(calc(-50% + 48px)) scale(0.97);
}

/* ── Header ──────────────────────────────── */
.export-dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 18px 20px 14px;
  border-bottom: 1px solid var(--line);
  flex-shrink: 0;
}

.export-dialog-title-row {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.export-dialog-title {
  font-size: 17px;
  font-weight: 800;
  color: var(--primary-dark);
  margin: 0;
  white-space: nowrap;
}

.export-all-toggle {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  font-size: 13px;
  color: var(--text-sub);
  cursor: pointer;
  white-space: nowrap;
}

.export-dialog-close {
  width: 36px;
  height: 36px;
  min-width: 36px;
  padding: 0;
  border-radius: 10px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ── Body ──────────────────────────────── */
.export-dialog-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 14px 20px 8px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  scrollbar-width: thin;
  scrollbar-color: var(--line) transparent;
}

.export-dialog-body::-webkit-scrollbar {
  width: 4px;
}

.export-dialog-body::-webkit-scrollbar-track {
  background: transparent;
}

.export-dialog-body::-webkit-scrollbar-thumb {
  background: var(--line-strong, #d0c0dc);
  border-radius: 999px;
}

/* ── Format Selector ────────────────────── */
.export-format-row {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 14px;
  border-radius: 14px;
  border: 1px solid var(--line);
  background: var(--primary-surface);
  flex-wrap: wrap;
}

.export-format-label {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-sub);
  white-space: nowrap;
}

.export-format-options {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.export-format-option {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 600;
  color: var(--primary-dark);
  cursor: pointer;
}

/* ── Notice ────────────────────────────── */
.export-notice {
  display: flex;
  align-items: flex-start;
  gap: 9px;
  padding: 11px 14px;
  border-radius: 12px;
  border: 1px dashed var(--primary-light);
  background: var(--primary-surface);
  font-size: 13px;
  color: var(--text-sub);
  line-height: 1.55;
}

.export-notice-icon {
  color: var(--primary);
  flex-shrink: 0;
  margin-top: 1px;
}

/* ── Field Groups ───────────────────────── */
.export-groups {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.export-group {
  border-radius: 14px;
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.65);
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.export-group-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.export-group-check {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 700;
  color: var(--primary-dark);
  cursor: pointer;
}

.export-group-name {
  font-size: 14px;
}

.export-group-options {
  display: flex;
  flex-wrap: wrap;
  gap: 6px 14px;
}

.export-option-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px 14px;
  width: 100%;
}

.export-option {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  font-size: 13px;
  color: var(--text-main);
  cursor: pointer;
}

/* ── Footer ─────────────────────────────── */
.export-dialog-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  padding: 14px 20px 16px;
  border-top: 1px solid var(--line);
  flex-shrink: 0;
}

.export-dialog-footer .ghost-button {
  flex: 1;
  height: 40px;
  font-size: 14px;
  border-radius: 10px;
  max-width: 140px;
}

.export-dialog-footer .action-button {
  flex: 1;
  height: 40px;
  font-size: 14px;
  border-radius: 10px;
  max-width: 140px;
}

/* ── Preview Panel ─────────────────────── */
.export-preview-panel {
  position: fixed;
  top: 50%;
  right: 16px;
  transform: translateY(calc(-50% + 40px)) scale(0.97);
  width: calc(50vw - 16px);
  height: 85vh;
  border-radius: 22px;
  border: 1px solid var(--line);
  background: var(--card);
  box-shadow: var(--shadow-glow);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  z-index: 1020;
  opacity: 0;
  pointer-events: none;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition:
    transform 0.42s cubic-bezier(0.22, 1, 0.36, 1),
    opacity 0.38s ease;
}

.export-preview-panel.open {
  transform: translateY(-50%) scale(1);
  opacity: 1;
  pointer-events: auto;
}

.export-preview-panel.closing {
  transform: translateY(calc(-50% + 40px)) scale(0.97);
  opacity: 0;
}

/* ── Preview Header ─────────────────────── */
.export-preview-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 18px 18px 14px;
  border-bottom: 1px solid var(--line);
  flex-shrink: 0;
}

.export-preview-title {
  font-size: 15px;
  font-weight: 800;
  color: var(--primary-dark);
  margin: 0;
}

.export-preview-header .ghost-button {
  width: 36px;
  height: 36px;
  min-width: 36px;
  padding: 0;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ── Preview Body ─────────────────────── */
.export-preview-body {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  padding: 14px 16px 14px;
  gap: 12px;
}

.export-preview-loading,
.export-preview-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  flex: 1;
  font-size: 13px;
  color: var(--text-sub);
}

.export-preview-content {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  overflow: hidden;
}

.export-preview-table-wrap {
  flex: 1;
  min-height: 0;
  border-radius: 14px;
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.88);
  overflow: auto;
  scrollbar-width: thin;
  scrollbar-color: var(--line) transparent;
}

.export-preview-table-wrap::-webkit-scrollbar {
  width: 4px;
  height: 4px;
}

.export-preview-table-wrap::-webkit-scrollbar-thumb {
  background: var(--line-strong, #d0c0dc);
  border-radius: 999px;
}

.export-preview-table {
  border-collapse: collapse;
  width: 100%;
  font-size: 12px;
  color: var(--text-main);
  font-variant-numeric: tabular-nums;
}

.export-preview-table th,
.export-preview-table td {
  border: 1px solid var(--line);
  padding: 7px 10px;
  white-space: nowrap;
  text-align: left;
}

.export-preview-table th {
  background: var(--primary-surface);
  font-weight: 700;
  color: var(--primary-dark);
  position: sticky;
  top: 0;
  z-index: 1;
}

.export-preview-tabs {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  flex-shrink: 0;
}

.export-preview-tab {
  border: 1px solid var(--line-strong);
  background: var(--card);
  color: var(--primary-dark);
  height: 34px;
  padding: 0 12px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s ease, border-color 0.15s ease;
  white-space: nowrap;
}

.export-preview-tab:hover:not(.active) {
  background: var(--card-hover);
  border-color: var(--primary);
}

.export-preview-tab.active {
  background: var(--primary-surface);
  border-color: var(--primary);
  color: var(--primary-dark);
}

.export-preview-tab:focus-visible {
  outline: 2px solid var(--primary);
  outline-offset: 2px;
}

/* ── Responsive ────────────────────────── */
@media (max-width: 1100px) {
  .export-preview-panel {
    top: auto;
    bottom: 0;
    right: 0;
    left: 0;
    width: 100%;
    height: 70vh;
    border-radius: 22px 22px 0 0;
    transform: translateY(100%) scale(0.97);
  }

  .export-preview-panel.open {
    transform: translateY(0) scale(1);
  }

  .export-preview-panel.closing {
    transform: translateY(100%) scale(0.97);
  }
}

@media (max-width: 520px) {
  .export-dialog {
    top: auto;
    bottom: 0;
    left: 0;
    right: 0;
    width: 100%;
    max-height: 88vh;
    border-radius: 22px 22px 0 0;
    transform: translateY(100%) scale(0.97);
  }

  .export-dialog.open {
    transform: translateY(0) scale(1);
  }

  .export-dialog.closing {
    transform: translateY(100%) scale(0.97);
  }

  .export-dialog.split-right {
    top: auto;
    bottom: 0;
    left: 0;
    right: 0;
    width: 100%;
    height: 88vh;
    transform: translateY(100%) scale(0.97);
  }

  .export-dialog.split-right.open {
    transform: translateY(0) scale(1);
  }

  .export-dialog.split-right.closing {
    transform: translateY(100%) scale(0.97);
  }

  .export-preview-panel {
    height: 70vh;
  }
}

/* ── Reduced Motion ────────────────────── */
@media (prefers-reduced-motion: reduce) {
  .export-dialog,
  .export-dialog.open,
  .export-dialog.closing,
  .export-preview-panel,
  .export-preview-panel.open,
  .export-preview-panel.closing {
    transition: none;
    animation: none;
  }
}
</style>
