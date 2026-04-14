import * as XLSX from "xlsx";

export async function renderSheet(url) {
  try {
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`Failed to fetch spreadsheet: ${response.status}`);
    }
    const blob = await response.blob();
    const arrayBuffer = await blob.arrayBuffer();

    const workbook = XLSX.read(arrayBuffer, { type: "array", cellDates: true });
    const sheetNames = workbook.SheetNames;
    const firstSheet = sheetNames[0];

    if (!firstSheet) {
      throw new Error("No sheets found");
    }

    const sheet = workbook.Sheets[firstSheet];
    let html = XLSX.utils.sheet_to_html(sheet, {
      header: true,
      footer: false,
      editable: false,
    });

    // Clean up all boolean text values (true/false) that XLSX may render as cell content
    // Also clean common variations like "True", "False", "TRUE", "FALSE"
    const cleanBool = (str) => {
      return str
        // Handle all TD/TH cells with boolean content
        .replace(/<td[^>]*>\s*true\s*<\/td>/gi, '<td></td>')
        .replace(/<td[^>]*>\s*false\s*<\/td>/gi, '<td></td>')
        .replace(/<td[^>]*>\s*True\s*<\/td>/gi, '<td></td>')
        .replace(/<td[^>]*>\s*False\s*<\/td>/gi, '<td></td>')
        .replace(/<td[^>]*>\s*TRUE\s*<\/td>/gi, '<td></td>')
        .replace(/<td[^>]*>\s*FALSE\s*<\/td>/gi, '<td></td>')
        // Handle self-closing TD variants
        .replace(/<td([^>]*)>\s*true\s*<\//gi, '<td$1></td>')
        .replace(/<td([^>]*)>\s*false\s*<\//gi, '<td$1></td>')
        // Remove any remaining true/false text appearing as text nodes between tags
        .replace(/>\s*true\s*</gi, '><')
        .replace(/>\s*false\s*</gi, '><')
        // Strip any standalone true/false that leaked outside tags (before </table>, etc.)
        .replace(/>\s*true\s*$/gm, '>')
        .replace(/>\s*false\s*$/gm, '>')
        .replace(/^\s*true\s*</gm, '<')
        .replace(/^\s*false\s*</gm, '<');
    };

    html = cleanBool(html);

    const tabs = sheetNames.map((name, i) => `
      <div class="sheet-tab ${i === 0 ? 'active' : ''}" data-sheet="${i}">
        ${escapeHtml(name)}
      </div>
    `).join('');

    const fullHtml = `
      <div class="sheet-container">
        <div class="sheet-tabs">${tabs}</div>
        <div class="sheet-content">
          <div class="sheet-table-wrapper">${html}</div>
        </div>
        <div class="sheet-footer">
          <span>${sheetNames.length} 个工作表</span>
        </div>
      </div>
    `;

    return { html: fullHtml, workbook };
  } catch (error) {
    console.error("Error rendering spreadsheet:", error);
    return {
      html: `<div class="sheet-error">
        <div class="sheet-error-icon">📊</div>
        <div class="sheet-error-text">表格加载失败</div>
        <div class="sheet-error-hint">请下载后查看</div>
      </div>`,
      workbook: null
    };
  }
}

function escapeHtml(str) {
  return str
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}
