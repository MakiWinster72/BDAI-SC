/**
 * media - 媒体类型检测与 URL 处理工具
 *
 * 在多个视图和组件中重复定义（SettingsView、AchievementsView、NotificationsView 等），
 * 统一管理后复用。
 */
import { API_BASE } from '../api/request';

// ── URL 处理 ────────────────────────────────────────────

/**
 * 解析媒体 URL，相对路径自动补全 API_BASE
 * @param {string} url
 * @returns {string}
 */
export function resolveMediaUrl(url) {
  if (!url) return '';
  if (url.startsWith('http://') || url.startsWith('https://')) return url;
  return `${API_BASE}${url}`;
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
