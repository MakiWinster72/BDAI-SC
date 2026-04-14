/**
 * useAchievementPreview - 成就媒体预览 composable
 *
 * 管理成就图片/视频/文档/表格/PDF 的预览状态和渲染逻辑。
 * 状态自包含，watcher 由调用方在 view 层处理。
 */
import { computed, nextTick, ref, watch } from "vue";
import { renderDocx } from "../utils/docxRenderer";
import { renderSheet } from "../utils/sheetRenderer";
import { renderPdf } from "../utils/pdfRenderer";
import {
  isMediaVideo,
  isMediaDocument,
  isMediaSheet,
  isMediaPdf,
} from "../utils/media";

const previewImages = ref([]);
const previewIndex = ref(0);
const previewVisible = ref(false);
const previewType = ref("image");
const previewContent = ref("");
const previewLoading = ref(false);
const previewWorkbook = ref(null);
const slideDirection = ref("right");

function showPreview(urls, index = 0) {
  previewImages.value = urls;
  previewIndex.value = index;
  previewVisible.value = true;
  document.body.style.overflow = "hidden";
  previewContent.value = "";
  previewLoading.value = false;
  if (urls.length > 0) {
    const url = urls[index];
    if (isMediaVideo(url)) {
      previewType.value = "video";
    } else if (isMediaDocument(url)) {
      previewType.value = "document";
      loadDocumentPreview(url);
    } else if (isMediaSheet(url)) {
      previewType.value = "sheet";
      loadSheetPreview(url);
    } else if (isMediaPdf(url)) {
      previewType.value = "pdf";
      loadPdfPreview(url);
    } else {
      previewType.value = "image";
    }
  }
}

function hidePreview() {
  previewVisible.value = false;
  document.body.style.overflow = "";
}

function previewPrev() {
  if (previewIndex.value > 0) {
    slideDirection.value = "left";
    previewIndex.value--;
    const url = previewImages.value[previewIndex.value];
    detectPreviewType(url);
  }
}

function previewNext() {
  if (previewIndex.value < previewImages.value.length - 1) {
    slideDirection.value = "right";
    previewIndex.value++;
    const url = previewImages.value[previewIndex.value];
    detectPreviewType(url);
  }
}

function detectPreviewType(url) {
  previewContent.value = "";
  previewLoading.value = false;
  if (isMediaVideo(url)) {
    previewType.value = "video";
  } else if (isMediaDocument(url)) {
    previewType.value = "document";
    loadDocumentPreview(url);
  } else if (isMediaSheet(url)) {
    previewType.value = "sheet";
    loadSheetPreview(url);
  } else if (isMediaPdf(url)) {
    previewType.value = "pdf";
    loadPdfPreview(url);
  } else {
    previewType.value = "image";
  }
}

async function loadDocumentPreview(url) {
  previewLoading.value = true;
  try {
    const html = await renderDocx(url);
    previewContent.value = html;
  } catch {
    previewContent.value = '<p class="viewer-error">文档加载失败</p>';
  } finally {
    previewLoading.value = false;
  }
}

async function loadSheetPreview(url) {
  previewLoading.value = true;
  try {
    const result = await renderSheet(url);
    previewWorkbook.value = result.workbook;
    previewContent.value = result.html;
  } catch {
    previewContent.value = '<p class="viewer-error">表格加载失败</p>';
  } finally {
    previewLoading.value = false;
  }
}

async function loadPdfPreview(url) {
  previewLoading.value = true;
  try {
    const html = await renderPdf(url);
    previewContent.value = html;
  } catch {
    previewContent.value = '<p class="viewer-error">PDF加载失败</p>';
  } finally {
    previewLoading.value = false;
  }
}

async function switchSheet(sheetIndex) {
  if (!previewWorkbook.value) return;
  previewLoading.value = true;
  try {
    const Xlsx = await import("xlsx");
    const sheets = previewWorkbook.value.SheetNames;
    const name = sheets[sheetIndex];
    const sheet = previewWorkbook.value.Sheets[name];
    const html = Xlsx.utils.sheet_to_html(sheet, { header: 1 });
    const container = document.querySelector(".viewer-document");
    if (container) {
      const tabs = sheets
        .map(
          (s, i) =>
            `<button class="sheet-tab ${i === sheetIndex ? "active" : ""}" onclick="window.__switchSheet(${i})">${s}</button>`,
        )
        .join("");
      container.innerHTML = `<div class="sheet-tabs">${tabs}</div><div class="sheet-content">${html}</div>`;
    }
  } catch (e) {
    console.error("switchSheet error", e);
  } finally {
    previewLoading.value = false;
  }
}

export function useAchievementPreview() {
  return {
    // state
    previewImages,
    previewIndex,
    previewVisible,
    previewType,
    previewContent,
    previewLoading,
    previewWorkbook,
    slideDirection,
    // methods
    showPreview,
    hidePreview,
    previewPrev,
    previewNext,
    loadDocumentPreview,
    loadSheetPreview,
    loadPdfPreview,
    switchSheet,
  };
}
