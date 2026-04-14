import * as docxLib from "docx-preview";

export async function renderDocx(url) {
  try {
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`Failed to fetch document: ${response.status}`);
    }
    const blob = await response.blob();

    const container = document.createElement("div");
    container.className = "docx-wrapper";

    await docxLib.renderAsync(blob, container, undefined, {
      className: "docx-wrapper",
      inWrapper: false,
      ignoreWidth: false,
      ignoreHeight: false,
      ignoreFonts: false,
      breakPages: false,
      ignoreLastRenderedPageBreak: true,
      experimental: false,
      trimXmlDeclaration: true,
      useBase64URL: true,
      useMathMLPolyfill: false,
      renderChanges: false,
      renderHeaders: false,
      renderFooters: false,
      renderFootnotes: false,
      renderEndnotes: false,
    });

    return container.innerHTML;
  } catch (error) {
    console.error("Error rendering docx:", error);
    return `<div class="docx-error">
      <div class="docx-error-icon">📄</div>
      <div class="docx-error-text">文档加载失败</div>
      <div class="docx-error-hint">请下载后查看</div>
    </div>`;
  }
}
