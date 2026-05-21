import { nextTick, onBeforeUnmount, ref, watch } from "vue";
import { renderDocx } from "@/utils/docxRenderer";
import { renderSheet } from "@/utils/sheetRenderer";
import { renderPdf } from "@/utils/pdfRenderer";
import { resolvePreviewKind } from "@/utils/media";

const el = "div";

function errorHtml(className, message) {
  return `<${el} class="${className}"><${el}>${message}</${el}></${el}>`;
}

export function useAchievementPreview() {
  const previewImages = ref([]);
  const previewIndex = ref(0);
  const previewVisible = ref(false);
  const previewType = ref("image");
  const previewContent = ref("");
  const previewLoading = ref(false);
  const previewWorkbook = ref(null);
  const slideDirection = ref("right");
  const previewHint = ref({});
  let pdfBlobUrl = null;

  function revokePdfBlob() {
    if (pdfBlobUrl) {
      URL.revokeObjectURL(pdfBlobUrl);
      pdfBlobUrl = null;
    }
  }

  async function loadDocumentPreview(url) {
    previewLoading.value = true;
    try {
      previewContent.value = await renderDocx(url);
    } catch {
      previewContent.value = errorHtml("docx-error", "加载失败");
    } finally {
      previewLoading.value = false;
    }
  }

  async function loadSheetPreview(url) {
    previewLoading.value = true;
    try {
      const result = await renderSheet(url);
      previewContent.value = result.html;
      if (result.workbook) {
        previewWorkbook.value = result.workbook;
      }
    } catch {
      previewContent.value = errorHtml("sheet-error", "加载失败");
    } finally {
      previewLoading.value = false;
    }
  }

  async function loadPdfPreview(url) {
    previewLoading.value = true;
    revokePdfBlob();
    try {
      const { html, blobUrl } = await renderPdf(url);
      pdfBlobUrl = blobUrl;
      previewContent.value = html;
    } catch {
      previewContent.value = errorHtml("pdf-error", "加载失败");
    } finally {
      previewLoading.value = false;
    }
  }

  async function switchSheet(sheetIndex) {
    if (!previewWorkbook.value) {
      return;
    }
    previewLoading.value = true;
    try {
      const XLSX = await import("xlsx");
      const sheetNames = previewWorkbook.value.SheetNames;
      const sheet = previewWorkbook.value.Sheets[sheetNames[sheetIndex]];
      const html = XLSX.utils.sheet_to_html(sheet, {
        header: true,
        footer: false,
        editable: false,
      });
      const tabs = sheetNames
        .map(
          (name, i) =>
            `<${el} class="sheet-tab ${i === sheetIndex ? "active" : ""}" data-sheet="${i}">${name}</${el}>`,
        )
        .join("");
      previewContent.value = `<${el} class="sheet-container"><${el} class="sheet-tabs">${tabs}</${el}><${el} class="sheet-content"><${el} class="sheet-table-wrapper">${html}</${el}></${el}><${el} class="sheet-footer"><span>${sheetNames.length} 个工作表</span></${el}></${el}>`;
    } catch {
      previewContent.value = errorHtml("sheet-error", "切换失败");
    } finally {
      previewLoading.value = false;
    }
  }

  function applyPreviewAtIndex(index) {
    const urls = previewImages.value;
    if (!urls.length) {
      return;
    }
    const url = urls[index];
    if (!url) {
      return;
    }
    previewContent.value = "";
    previewLoading.value = false;
    previewWorkbook.value = null;
    if (previewType.value === "pdf") {
      revokePdfBlob();
    }

    const kind = resolvePreviewKind(url, previewHint.value);
    if (kind === "video") {
      previewType.value = "video";
      return;
    }
    if (kind === "document") {
      previewType.value = "document";
      loadDocumentPreview(url);
      return;
    }
    if (kind === "sheet") {
      previewType.value = "sheet";
      loadSheetPreview(url);
      return;
    }
    if (kind === "pdf") {
      previewType.value = "pdf";
      loadPdfPreview(url);
      return;
    }
    previewType.value = "image";
  }

  function showPreview(urls, index = 0, hint = {}) {
    previewImages.value = urls;
    previewIndex.value = index;
    previewHint.value = hint || {};
    previewVisible.value = true;
    document.body.style.overflow = "hidden";
    applyPreviewAtIndex(index);
  }

  function hidePreview() {
    previewVisible.value = false;
    document.body.style.overflow = "";
    previewHint.value = {};
    revokePdfBlob();
  }

  function previewPrev() {
    if (previewIndex.value > 0) {
      slideDirection.value = "left";
      previewIndex.value--;
    }
  }

  function previewNext() {
    if (previewIndex.value < previewImages.value.length - 1) {
      slideDirection.value = "right";
      previewIndex.value++;
    }
  }

  function goToPreviewDot(index) {
    slideDirection.value = index > previewIndex.value ? "right" : "left";
    previewIndex.value = index;
  }

  watch(previewIndex, (index) => {
    if (!previewVisible.value) {
      return;
    }
    applyPreviewAtIndex(index);
  });

  watch(previewContent, () => {
    if (previewType.value === "sheet") {
      nextTick(() => {
        const container = document.querySelector(".viewer-document");
        if (container) {
          container.onclick = (e) => {
            const tab = e.target.closest(".sheet-tab");
            if (tab) {
              const idx = parseInt(tab.dataset.sheet, 10);
              if (!Number.isNaN(idx)) {
                switchSheet(idx);
              }
            }
          };
        }
      });
    }
  });

  onBeforeUnmount(() => {
    hidePreview();
    if (window.__switchSheet) {
      delete window.__switchSheet;
    }
  });

  function bindSheetSwitcher() {
    window.__switchSheet = (index) => {
      switchSheet(index);
    };
  }

  return {
    previewImages,
    previewIndex,
    previewVisible,
    previewType,
    previewContent,
    previewLoading,
    slideDirection,
    showPreview,
    hidePreview,
    previewPrev,
    previewNext,
    goToPreviewDot,
    bindSheetSwitcher,
  };
}
