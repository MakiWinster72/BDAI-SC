/**
 * achievement - 成就相关转换工具函数
 *
 * 从 AchievementsView 中提取的可复用函数。
 * 注意：resolveMediaUrl 已在 media.js 中，stripMediaUrl 已在 media.js 中。
 */
import {
  resolveMediaUrl,
  stripMediaUrl,
  parseJsonArray,
  resolveMediaTypeByExtension,
} from './media';
import {
  categoryFieldMap,
  attachmentIconMap,
  IMAGE_URLS_FIELD,
  ATTACHMENTS_FIELD,
} from '../constants/achievementConstants';

// ── 去重 & 归一化 ─────────────────────────────────────

/**
 * 按 category:id 去重
 * @param {any[]} list
 * @returns {any[]}
 */
export function dedupeAchievements(list) {
  const seen = new Set();
  return list.filter((item) => {
    if (!item || item.id == null) return true;
    const key = `${item.category || ''}:${item.id}`;
    if (seen.has(key)) return false;
    seen.add(key);
    return true;
  });
}

/**
 * 将 API 返回的原始成就对象转换为视图模型
 * @param {object} item
 * @returns {object}
 */
export function normalizeAchievement(item) {
  const config = categoryFieldMap[item.category] || null;
  const fields = item.fields || {};
  const imageUrls = resolveImageUrls(item);
  const attachments = resolveAttachments(fields);
  const titleKey = config?.titleKey;
  const dateKey = config?.dateKey;
  const dateLabel =
    config?.fields.find((f) => f.key === dateKey)?.label || '';
  const fieldLines = config
    ? config.fields.map(
        (field) => `${field.label}：${fields[field.key] || '-'}`,
      )
    : [];
  return {
    id: item.id,
    title: titleKey ? fields[titleKey] : '',
    dateLabel,
    dateValue: dateKey ? fields[dateKey] : '',
    fields,
    fieldLines,
    previewFields: fieldLines.slice(0, 2),
    image: imageUrls[0] || '',
    imageUrls,
    attachments,
    category: item.category || '',
  };
}

// ── URL 解析 ────────────────────────────────────────────

/**
 * 从 payload 中提取图片 URL 数组
 * @param {object} payload
 * @returns {string[]}
 */
export function resolveImageUrlsFromPayload(payload) {
  const raw = payload?.fields?.[IMAGE_URLS_FIELD];
  const fromField = parseJsonArray(raw);
  const fromImageUrl = payload?.fields?.imageUrl
    ? [payload.fields.imageUrl]
    : [];
  return [...fromField, ...fromImageUrl].filter(Boolean);
}

/**
 * 从字段中解析附件对象数组
 * @param {object} fields
 * @returns {Array<{ name: string, url: string, size?: number }>}
 */
export function resolveAttachmentsFromPayload(payload) {
  return parseJsonArray(payload?.fields?.[ATTACHMENTS_FIELD])
    .map((a) => ({
      name: a.name || '',
      url: resolveMediaUrl(a.url),
      size: a.size,
    }))
    .filter((a) => a.url);
}

/**
 * 从成就项中解析所有图片 URL
 * @param {object} item
 * @returns {string[]}
 */
export function resolveImageUrls(item) {
  const rawField = item?.fields?.[IMAGE_URLS_FIELD];
  const fromField = rawField ? parseJsonArray(rawField) : [];
  const fromImageUrl = item?.imageUrl ? [item.imageUrl] : [];
  return [...fromField, ...fromImageUrl]
    .map((url) => resolveMediaUrl(url))
    .filter(Boolean);
}

/**
 * 从 fields 中解析附件列表
 * @param {object} fields
 * @returns {Array<{ name: string, url: string, size?: number }>}
 */
export function resolveAttachments(fields = {}) {
  const raw = fields[ATTACHMENTS_FIELD];
  return parseJsonArray(raw)
    .map((a) => ({
      name: a.name || '',
      url: resolveMediaUrl(a.url),
      size: a.size,
    }))
    .filter((a) => a.url);
}

// ── 变更描述 ────────────────────────────────────────────

/**
 * 将字段值转为展示字符串
 * @param {any} value
 * @returns {string}
 */
export function stringifyChangeValue(value) {
  if (value === null || value === undefined || value === '') return '-';
  if (Array.isArray(value)) return value.join('、') || '-';
  return String(value);
}

/**
 * 构建成就变更记录（用于提交审核）
 * @param {{ category: string, payload: object, existingItem?: object }} args
 * @returns {Array<{ section: string, label: string, before: string, after: string }>}
 */
export function buildAchievementChanges({ category, payload, existingItem }) {
  const config = categoryFieldMap[category];
  if (!config) return [];
  const existing = existingItem?.fields || {};
  const next = payload?.fields || {};
  const changes = [];
  for (const field of config.fields) {
    const before = stringifyChangeValue(existing[field.key]);
    const after = stringifyChangeValue(next[field.key]);
    if (before !== after) {
      changes.push({
        section: '成就信息',
        label: field.label,
        before,
        after,
      });
    }
  }
  return changes;
}

/**
 * @param {{ name: string, url: string, size?: number }} file
 * @returns {string}
 */
export function attachmentIcon(file) {
  const ext = resolveMediaTypeByExtension(file.name || file.url || '');
  return attachmentIconMap[ext] || '/assets/icons/doc.svg';
}

// ── 审核快照 ────────────────────────────────────────────

/**
 * 构建草稿源对象
 * @param {object} payload
 * @returns {object}
 */
export function buildAchievementDraftSourceFromPayload(payload) {
  return {
    imageUrls: resolveImageUrlsFromPayload(payload),
    attachments: resolveAttachmentsFromPayload(payload),
  };
}

/**
 * 构建单个字段条目的展示字符串
 * @param {{ category: string, field: object, value: any }} args
 * @returns {string}
 */
export function buildAchievementReviewSnapshotEntry({ category, field, value }) {
  if (!value && value !== 0) return '';
  if (field.key === 'remark') return value;
  return stringifyChangeValue(value);
}

/**
 * 构建审核快照（用于通知消息）
 * @param {{ category: string, source: object }} args
 * @returns {{ category: string, entries: Array<{ label: string, value: string }> }}
 */
export function buildAchievementReviewSnapshot({ category, source }) {
  const config = categoryFieldMap[category];
  if (!config) return { category, entries: [] };
  const fields = source?.payload?.fields || {};
  const entries = config.fields
    .map((field) => ({
      label: field.label,
      value: buildAchievementReviewSnapshotEntry({ category, field, value: fields[field.key] }),
    }))
    .filter((e) => e.value);
  return { category, entries };
}

/**
 * 构建完整的变更前后快照
 * @param {{ category: string, beforeItem: object, afterItem: object }} args
 * @returns {{ category: string, before: object, after: object }}
 */
export function buildAchievementReviewPayloadSnapshot({
  category,
  beforeItem,
  afterItem,
}) {
  return {
    category,
    before: beforeItem
      ? normalizeAchievement(beforeItem)
      : null,
    after: afterItem
      ? normalizeAchievement(afterItem)
      : null,
  };
}
