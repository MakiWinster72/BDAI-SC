// PDF renderer — 使用 object 内嵌预览，并返回 blobUrl 供调用方释放
import { fetchMedia } from "./media";

export async function renderPdf(url) {
  const response = await fetchMedia(url);
  if (!response.ok) {
    throw new Error(`Failed to fetch PDF: ${response.status}`);
  }
  const blob = await response.blob();
  const blobUrl = URL.createObjectURL(blob);
  const html = `<object data="${blobUrl}" type="application/pdf" class="pdf-iframe" title="PDF Preview">
    <div class="pdf-error">
      <div class="pdf-error-text">当前浏览器无法内嵌预览 PDF</div>
      <div class="pdf-error-hint">请下载后查看</div>
    </div>
  </object>`;
  return { html, blobUrl };
}
