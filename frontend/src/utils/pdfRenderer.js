// PDF renderer - returns iframe HTML that fills the viewer area
export async function renderPdf(url) {
  try {
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`Failed to fetch PDF: ${response.status}`);
    }
    const blob = await response.blob();
    const blobUrl = URL.createObjectURL(blob);
    return `<iframe src="${blobUrl}" class="pdf-iframe" title="PDF Preview"></iframe>`;
  } catch (error) {
    console.error("Error rendering PDF:", error);
    return `<div class="pdf-error">
      <div class="pdf-error-icon">📑</div>
      <div class="pdf-error-text">PDF 加载失败</div>
      <div class="pdf-error-hint">请下载后查看</div>
    </div>`;
  }
}
