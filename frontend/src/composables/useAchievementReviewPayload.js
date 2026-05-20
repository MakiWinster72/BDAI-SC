import { resolveMediaUrl, parseJsonArray } from "@/utils/media";
import {
  categoryFieldMap,
  achievementEntries,
  ATTACHMENTS_FIELD,
} from "@/constants/achievementConstants";
import { resolveImageUrlsFromPayload } from "@/utils/achievementListModel";

export function stringifyChangeValue(value) {
  if (Array.isArray(value)) {
    const filtered = value
      .filter(Boolean)
      .map((item) => String(item).trim())
      .filter(Boolean);
    return filtered.length ? filtered.join("、") : "-";
  }
  const text = String(value ?? "").trim();
  return text || "-";
}

export function resolveAttachmentsFromPayload(payload) {
  return parseJsonArray(payload?.fields?.[ATTACHMENTS_FIELD])
    .map((item) => ({
      url: resolveMediaUrl(item?.url || ""),
      name: item?.name || item?.originalName || "附件",
      mediaType: item?.mediaType || "",
    }))
    .filter((item) => item.url);
}

export function buildAchievementDraftSourceFromPayload(payload) {
  return {
    fields: payload?.fields || {},
    imageUrl: payload?.imageUrl || "",
    imageUrls: resolveImageUrlsFromPayload(payload).map((url) =>
      resolveMediaUrl(url),
    ),
    attachments: resolveAttachmentsFromPayload(payload),
  };
}

export function buildAchievementReviewSnapshot({ category, source }) {
  const config = categoryFieldMap[category] || null;
  const fields = source?.fields || {};
  const fieldConfigList = Array.isArray(config?.fields) ? config.fields : [];
  const titleKey = config?.titleKey || "";
  const dateKey = config?.dateKey || "";
  const dateField =
    fieldConfigList.find((field) => field.key === dateKey) || null;
  const imageUrls = Array.isArray(source?.imageUrls)
    ? source.imageUrls.filter(Boolean).map((url) => resolveMediaUrl(url))
    : resolveImageUrlsFromPayload({
        imageUrl: source?.imageUrl || source?.image || "",
        fields,
      });
  const attachments = Array.isArray(source?.attachments)
    ? source.attachments
        .map((item) => ({
          url: resolveMediaUrl(item?.url || ""),
          name: item?.name || "附件",
          mediaType: item?.mediaType || "",
        }))
        .filter((item) => item.url)
    : resolveAttachmentsFromPayload({ fields });

  return {
    category,
    categoryLabel:
      achievementEntries.find((entry) => entry.key === category)?.label ||
      "成就记录",
    title: stringifyChangeValue(fields[titleKey]),
    dateLabel: dateField?.label || "",
    dateValue: dateKey ? stringifyChangeValue(fields[dateKey]) : "",
    fieldEntries: fieldConfigList.map((field) => ({
      key: field.key,
      label: field.label,
      value: stringifyChangeValue(fields[field.key]),
    })),
    imageUrls,
    attachments,
  };
}

export function buildAchievementReviewPayloadSnapshot({
  category,
  beforeItem = null,
  afterItem = null,
}) {
  return {
    type: "achievement-review",
    category,
    before: beforeItem
      ? buildAchievementReviewSnapshot({ category, source: beforeItem })
      : null,
    after: afterItem
      ? buildAchievementReviewSnapshot({ category, source: afterItem })
      : null,
  };
}

export function buildAchievementChanges({ category, payload, existingItem }) {
  const config = categoryFieldMap[category] || null;
  if (!config) {
    return [];
  }
  const nextFields = payload?.fields || {};
  const previousFields = existingItem?.fields || {};
  const changes = [];

  config.fields.forEach((field) => {
    const before = stringifyChangeValue(previousFields[field.key]);
    const after = stringifyChangeValue(nextFields[field.key]);
    if (!existingItem && after === "-") {
      return;
    }
    if (existingItem && before === after) {
      return;
    }
    changes.push({
      section: "成就信息",
      label: field.label,
      before,
      after,
    });
  });

  const previousImages = stringifyChangeValue(existingItem?.imageUrls || []);
  const nextImages = stringifyChangeValue(resolveImageUrlsFromPayload(payload));
  if (
    (!existingItem && nextImages !== "-") ||
    (existingItem && previousImages !== nextImages)
  ) {
    changes.push({
      section: "多媒体",
      label: "图片",
      before: previousImages,
      after: nextImages,
    });
  }

  const previousAttachments = stringifyChangeValue(
    (existingItem?.attachments || []).map(
      (item) => item.name || item.url || "",
    ),
  );
  const nextAttachments = stringifyChangeValue(
    (payload?.fields?.[ATTACHMENTS_FIELD]
      ? JSON.parse(payload.fields[ATTACHMENTS_FIELD])
      : []
    ).map((item) => item.name || item.url || ""),
  );
  if (
    (!existingItem && nextAttachments !== "-") ||
    (existingItem && previousAttachments !== nextAttachments)
  ) {
    changes.push({
      section: "多媒体",
      label: "附件",
      before: previousAttachments,
      after: nextAttachments,
    });
  }

  return changes;
}

export function useAchievementReviewPayload() {
  return {
    buildAchievementChanges,
    buildAchievementDraftSourceFromPayload,
    buildAchievementReviewPayloadSnapshot,
    buildAchievementReviewSnapshot,
    stringifyChangeValue,
    resolveAttachmentsFromPayload,
  };
}
