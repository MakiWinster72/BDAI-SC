import {
  parseJsonArray,
  resolveMediaObjectUrl,
  resolveMediaUrl,
  stripMediaUrl,
} from "@/utils/media";
import {
  categoryFieldMap,
  IMAGE_URLS_FIELD,
  ATTACHMENTS_FIELD,
} from "@/constants/achievementConstants";

export function resolveImageUrls(item, imageMaxCount = 9) {
  const urls = [];
  if (item?.imageUrl) {
    urls.push(stripMediaUrl(item.imageUrl));
  }
  const rawField = item?.fields?.[IMAGE_URLS_FIELD];
  const parsed = parseJsonArray(rawField);
  parsed.forEach((url) => {
    const resolved = stripMediaUrl(url);
    if (resolved && !urls.includes(resolved)) {
      urls.push(resolved);
    }
  });
  return urls.slice(0, imageMaxCount);
}

export function resolveAttachments(fields = {}) {
  const raw = fields[ATTACHMENTS_FIELD];
  const parsed = parseJsonArray(raw);
  return parsed
    .map((item) => ({
      url: stripMediaUrl(item.url),
      name: item.name || item.originalName || "附件",
      mediaType: item.mediaType || "",
    }))
    .filter((item) => item.url);
}

export function normalizeAchievement(item, imageMaxCount = 9) {
  const config = categoryFieldMap[item.category] || null;
  const fields = item.fields || {};
  const imageUrls = resolveImageUrls(item, imageMaxCount);
  const attachments = resolveAttachments(fields);
  const titleKey = config?.titleKey;
  const dateKey = config?.dateKey;
  const dateLabel =
    config?.fields.find((field) => field.key === dateKey)?.label || "";
  const fieldLines = config
    ? config.fields.map(
        (field) => `${field.label}：${fields[field.key] || "-"}`,
      )
    : [];
  return {
    id: item.id,
    title: titleKey ? fields[titleKey] : "",
    dateLabel,
    dateValue: dateKey ? fields[dateKey] : "",
    fields,
    fieldLines,
    previewFields: fieldLines.slice(0, 2),
    image: imageUrls[0] || "",
    imageUrls,
    rawImageUrls: imageUrls,
    attachments,
    rawAttachments: attachments,
    category: item.category || "",
  };
}

export async function hydrateAchievementMedia(list) {
  await Promise.all(
    list.map(async (item) => {
      item.imageUrls = await Promise.all(
        (item.rawImageUrls || item.imageUrls || []).map((url) =>
          resolveMediaObjectUrl(url).catch(() => resolveMediaUrl(url)),
        ),
      );
      item.image = item.imageUrls[0] || "";
      item.attachments = await Promise.all(
        (item.rawAttachments || item.attachments || []).map(async (file) => {
          const rawUrl = file.url;
          return {
            ...file,
            rawUrl,
            url: await resolveMediaObjectUrl(file.url).catch(() =>
              resolveMediaUrl(file.url),
            ),
          };
        }),
      );
    }),
  );
  return list;
}

export function resolveImageUrlsFromPayload(payload) {
  const raw = payload?.fields?.[IMAGE_URLS_FIELD];
  if (!raw) {
    return payload?.imageUrl ? [payload.imageUrl] : [];
  }
  try {
    return Array.isArray(JSON.parse(raw)) ? JSON.parse(raw) : [];
  } catch {
    return payload?.imageUrl ? [payload.imageUrl] : [];
  }
}
