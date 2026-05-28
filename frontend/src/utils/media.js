/**
 * media - 媒体类型检测与 URL 处理工具
 *
 * 在多个视图和组件中重复定义（SettingsView、AchievementsView、NotificationsView 等），
 * 统一管理后复用。
 */
import request, { API_BASE } from '@/api/request';

const privateMediaBlobUrls = new Map();
const MEDIA_API_PREFIX = '/api/media';

// ── URL 处理 ────────────────────────────────────────────

/**
 * 解析媒体 URL，相对路径自动补全 API_BASE
 * @param {string} url
 * @returns {string}
 */
export function resolveMediaUrl(url) {
  if (!url) return '';
  if (isUploadUrl(url)) return resolveMediaApiUrl(url);
  if (url.startsWith('http://') || url.startsWith('https://')) return url;
  return `${API_BASE}${url}`;
}

export function isPrivateUploadUrl(url) {
  const path = normalizeUploadPath(url);
  return (
    path.startsWith('/uploads/') ||
    path.startsWith(`${MEDIA_API_PREFIX}/uploads/`)
  );
}

export function resolveProtectedMediaUrl(url) {
  if (!url) return '';
  if (url.startsWith('blob:') || url.startsWith('data:')) return url;
  if (!isPrivateUploadUrl(url)) return resolveMediaUrl(url);
  return resolveMediaApiUrl(url);
}

/** 与 fetch Response 兼容，供 PDF/导出等复用 */
class MediaBlobResponse {
  constructor(blob, status = 200) {
    this.blobData = blob;
    this.status = status;
    this.ok = status >= 200 && status < 300;
  }

  blob() {
    return Promise.resolve(this.blobData);
  }
}

/**
 * 受保护媒体走 axios（与登录/API 同源、同 Token 拦截器），避免生产环境 fetch 跨域不带 JWT。
 */
export async function fetchMedia(url, options = {}) {
  if (!isPrivateUploadUrl(url)) {
    return fetch(resolveProtectedMediaUrl(url), {
      ...options,
      mode: options.mode || 'cors',
    });
  }

  const path = toMediaRequestPath(url);
  try {
    const response = await request.get(path, {
      responseType: 'blob',
      timeout: options.timeout ?? 120000,
      headers: options.headers,
      skipAuthRedirect: true,
      skipErrorToast: true,
    });
    return new MediaBlobResponse(response.data, response.status);
  } catch (error) {
    const status = error?.response?.status ?? 0;
    const blob = error?.response?.data;
    if (blob instanceof Blob) {
      return new MediaBlobResponse(blob, status);
    }
    return new MediaBlobResponse(new Blob(), status);
  }
}

export async function resolveMediaObjectUrl(url) {
  if (!url) return '';
  if (!isPrivateUploadUrl(url)) return resolveMediaUrl(url);
  const cacheKey = normalizeUploadPath(url);
  const cached = privateMediaBlobUrls.get(cacheKey);
  if (cached) return cached;

  const response = await fetchMedia(url);
  if (!response.ok) {
    throw new Error(`媒体加载失败: ${response.status}`);
  }
  const blob = await response.blob();
  const blobUrl = URL.createObjectURL(blob);
  privateMediaBlobUrls.set(cacheKey, blobUrl);
  return blobUrl;
}

export async function downloadMedia(url, filename = 'download') {
  const targetUrl = await resolveMediaObjectUrl(url);
  const link = document.createElement('a');
  link.href = targetUrl;
  link.download = filename;
  link.rel = 'noopener noreferrer';
  document.body.appendChild(link);
  link.click();
  link.remove();
}

export function revokePrivateMediaObjectUrls() {
  privateMediaBlobUrls.forEach((url) => URL.revokeObjectURL(url));
  privateMediaBlobUrls.clear();
}

function resolveMediaApiUrl(url) {
  const path = toMediaRequestPath(url);
  return `${API_BASE}${path}`;
}

/** axios 请求用相对路径，与 baseURL 一致 */
function toMediaRequestPath(url) {
  const path = normalizeUploadPath(url);
  if (path.startsWith(`${MEDIA_API_PREFIX}/uploads/`)) {
    return path;
  }
  return `${MEDIA_API_PREFIX}${toUploadPath(path)}`;
}

function normalizeUploadPath(url) {
  if (!url) return '';
  if (url.startsWith('http://') || url.startsWith('https://')) {
    try {
      return new URL(url).pathname;
    } catch {
      return '';
    }
  }
  if (url.startsWith(API_BASE)) {
    return url.replace(API_BASE, '');
  }
  return url;
}

function isUploadUrl(url) {
  const path = normalizeUploadPath(url);
  return path.startsWith('/uploads/') || path.startsWith(`${MEDIA_API_PREFIX}/uploads/`);
}

function toUploadPath(path) {
  if (path.startsWith(`${MEDIA_API_PREFIX}/uploads/`)) {
    return path.slice(MEDIA_API_PREFIX.length);
  }
  return path;
}

/**
 * 去掉 API_BASE 前缀，转换为相对路径
 * @param {string} url
 * @returns {string}
 */
export function stripMediaUrl(url) {
  if (!url) return '';
  if (url.startsWith(API_BASE)) {
    return url.replace(API_BASE, '');
  }
  return url;
}

// ── 类型检测（按扩展名）────────────────────────────────

/**
 * 从文件名/URL 中提取扩展名（小写，不含点）
 * @param {string} name
 * @returns {string}
 */
export function resolveMediaTypeByExtension(name = '') {
  const parts = name.split('.');
  return parts.pop()?.toLowerCase() || '';
}

/** @param {string} url */
export function isMediaVideo(url) {
  return ['mp4', 'mov', 'webm'].includes(resolveMediaTypeByExtension(url));
}

/** @param {string} url */
export function isMediaDocument(url) {
  return ['doc', 'docx'].includes(resolveMediaTypeByExtension(url));
}

/** @param {string} url */
export function isMediaSheet(url) {
  return ['xls', 'xlsx'].includes(resolveMediaTypeByExtension(url));
}

/** @param {string} url */
export function isMediaPdf(url) {
  return ['pdf'].includes(resolveMediaTypeByExtension(url));
}

/**
 * 推断预览类型（支持 blob URL：需结合文件名 / mediaType）
 * @param {string} url
 * @param {{ name?: string, mediaType?: string }} [hint]
 * @returns {'video' | 'document' | 'sheet' | 'pdf' | 'image'}
 */
export function resolvePreviewKind(url, hint = {}) {
  const tokens = [
    resolveMediaTypeByExtension(url),
    resolveMediaTypeByExtension(hint.name || ''),
    (hint.mediaType || "").toLowerCase(),
  ].filter(Boolean);

  for (const token of tokens) {
    const kind = previewKindFromToken(token);
    if (kind) {
      return kind;
    }
  }
  return "image";
}

function previewKindFromToken(token) {
  const value = token.toLowerCase();
  if (value.includes("pdf")) {
    return "pdf";
  }
  if (
    value.includes("video") ||
    ["mp4", "mov", "webm"].includes(value)
  ) {
    return "video";
  }
  if (["doc", "docx"].includes(value) || value.includes("word")) {
    return "document";
  }
  if (
    ["xls", "xlsx"].includes(value) ||
    value.includes("excel") ||
    (value.includes("sheet") && !value.includes("spreadsheet"))
  ) {
    return "sheet";
  }
  if (value.includes("spreadsheet") || value.includes("excel")) {
    return "sheet";
  }
  return null;
}

// ── 文件对象类型检测 ───────────────────────────────────

/** @param {{ name: string }} file */
export function isVideoFile(file) {
  return ['mp4', 'mov', 'webm'].includes(resolveMediaTypeByExtension(file?.name || ''));
}

/** @param {{ name: string }} file */
export function isDocumentFile(file) {
  return ['doc', 'docx'].includes(resolveMediaTypeByExtension(file?.name || ''));
}

/** @param {{ name: string }} file */
export function isSheetFile(file) {
  return ['xls', 'xlsx'].includes(resolveMediaTypeByExtension(file?.name || ''));
}

/** @param {{ name: string }} file */
export function isPdfFile(file) {
  return ['pdf'].includes(resolveMediaTypeByExtension(file?.name || ''));
}

/** @param {{ name: string }} file */
export function isPptxFile(file) {
  return ['ppt', 'pptx'].includes(resolveMediaTypeByExtension(file?.name || ''));
}

/** @param {string} url */
export function isVideoUrl(url) {
  return isMediaVideo(url);
}

// ── JSON 解析 ───────────────────────────────────────────

/**
 * 安全解析 JSON 数组
 * @param {string} value
 * @returns {any[]}
 */
export function parseJsonArray(value) {
  if (!value) return [];
  try {
    const parsed = JSON.parse(value);
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

// ── 格式化 ─────────────────────────────────────────────

/**
 * 格式化文件大小
 * @param {number} value - bytes
 * @returns {string}
 */
export function formatFileSize(value) {
  if (!value) return '0 B';
  if (value < 1024) return `${value} B`;
  if (value < 1024 * 1024) return `${(value / 1024).toFixed(1)} KB`;
  return `${(value / (1024 * 1024)).toFixed(1)} MB`;
}
