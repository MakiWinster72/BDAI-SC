import * as docxLib from "docx-preview";

export async function renderDocx(url) {
  try {
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`Failed to fetch document: ${response.status}`);
    }
    const blob = await response.blob();

    const container = document.createElement("div");
    container.className = "docx-container";
    container.style.cssText = `
      width: 100%;
      height: 100%;
      overflow: auto;
      background: #fff;
      border-radius: 8px;
      padding: 24px;
      box-sizing: border-box;
    `;

    await docxLib.renderAsync(blob, container, undefined, {
      className: "docx-wrapper",
      inWrapper: true,
      ignoreWidth: false,
      ignoreHeight: false,
      ignoreFonts: false,
      breakPages: true,
      ignoreLastRenderedPageBreak: true,
      experimental: false,
      trimXmlDeclaration: true,
      useBase64URL: true,
      useMathMLPolyfill: false,
      renderChanges: false,
      renderHeaders: true,
      renderFooters: true,
      renderFootnotes: true,
      renderEndnotes: true,
    });

    // language tag handling
    const langTags = container.querySelectorAll('[lang]');
    langTags.forEach(tag => {
      const lang = tag.getAttribute('lang');
      if (lang) {
        tag.style.direction = /[\u0600-\u06FF]/.test(tag.textContent) ? 'rtl' : 'ltr';
      }
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
