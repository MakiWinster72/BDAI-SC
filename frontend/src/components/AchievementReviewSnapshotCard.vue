<script setup>
import { computed } from "vue";

const props = defineProps({
  snapshot: {
    type: Object,
    default: null,
  },
  emptyText: {
    type: String,
    default: "暂无记录",
  },
});

const visibleFields = computed(() =>
  Array.isArray(props.snapshot?.fieldEntries)
    ? props.snapshot.fieldEntries.filter(
        (field) => String(field?.value || "").trim() && field.value !== "-",
      )
    : [],
);

function resolveMediaTypeByExtension(name = "") {
  const cleanName = String(name).toLowerCase();
  const parts = cleanName.split(".");
  return parts.length > 1 ? parts.pop() || "" : "";
}

function attachmentIcon(file) {
  const ext = resolveMediaTypeByExtension(file?.name || file?.url || "");
  if (["pdf"].includes(ext)) return "/assets/icons/pdf.svg";
  if (["xls", "xlsx"].includes(ext)) return "/assets/icons/excel.svg";
  if (["ppt", "pptx"].includes(ext)) return "/assets/icons/ppt.svg";
  if (["zip", "rar", "7z"].includes(ext)) return "/assets/icons/zip.svg";
  if (["mp4", "mov", "webm"].includes(ext)) return "/assets/icons/video.svg";
  if (["jpeg", "jpg", "png", "heif"].includes(ext)) return "/assets/icons/image.svg";
  return "/assets/icons/doc.svg";
}

function openAsset(url) {
  if (!url || typeof window === "undefined") {
    return;
  }
  window.open(url, "_blank", "noopener,noreferrer");
}
</script>

<template>
  <article class="review-achievement-sheet" :class="{ 'is-empty': !snapshot }">
    <template v-if="snapshot">
      <div class="review-achievement-head">
        <span class="review-achievement-category">
          {{ snapshot.categoryLabel || "成就记录" }}
        </span>
      </div>

      <h3 class="review-achievement-title">{{ snapshot.title || "-" }}</h3>

      <div v-if="snapshot.imageUrls?.length" class="review-achievement-gallery">
        <button
          v-for="(url, index) in snapshot.imageUrls"
          :key="`${url}-${index}`"
          class="review-achievement-image"
          type="button"
          @click="openAsset(url)"
        >
          <img :src="url" alt="成就图片" />
        </button>
      </div>

      <div v-if="visibleFields.length" class="review-achievement-fields">
        <div
          v-for="field in visibleFields"
          :key="field.key || field.label"
          class="review-achievement-row"
        >
          <span class="review-achievement-label">{{ field.label }}</span>
          <span class="review-achievement-value">{{ field.value }}</span>
        </div>
      </div>

      <div v-if="snapshot.attachments?.length" class="review-achievement-attachments">
        <div class="review-achievement-section">附件</div>
        <div class="review-achievement-file-list">
          <button
            v-for="(file, index) in snapshot.attachments"
            :key="`${file.url}-${index}`"
            class="review-achievement-file"
            type="button"
            @click="openAsset(file.url)"
          >
            <img :src="attachmentIcon(file)" alt="" />
            <span class="review-achievement-file-name">{{ file.name }}</span>
          </button>
        </div>
      </div>
    </template>

    <div v-else class="review-achievement-empty">
      {{ emptyText }}
    </div>
  </article>
</template>

<style scoped>
.review-achievement-sheet {
  min-height: 100%;
  border-radius: 22px;
  border: 1px solid rgba(255, 255, 255, 0.34);
  background: linear-gradient(
    140deg,
    rgba(205, 255, 249, 0.92),
    rgba(197, 217, 226, 0.78)
  );
  box-shadow: 0 24px 60px rgba(100, 12, 114, 0.16);
  padding: 18px;
  display: grid;
  gap: 16px;
}

.review-achievement-sheet.is-empty {
  background: linear-gradient(
    145deg,
    rgba(255, 255, 255, 0.7),
    rgba(232, 242, 245, 0.72)
  );
}

.review-achievement-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.review-achievement-category {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(100, 12, 114, 0.12);
  color: #0f555d;
  font-size: 12px;
  font-weight: 700;
}


.review-achievement-title {
  margin: 0;
  color: #0d4047;
  font-size: 22px;
  line-height: 1.35;
}

.review-achievement-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 10px;
}

.review-achievement-image {
  border: 0;
  border-radius: 16px;
  overflow: hidden;
  padding: 0;
  background: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  aspect-ratio: 4 / 3;
  box-shadow: 0 10px 24px rgba(100, 12, 114, 0.12);
}

.review-achievement-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.review-achievement-fields {
  display: grid;
  gap: 10px;
}

.review-achievement-row {
  display: grid;
  gap: 4px;
  border-radius: 14px;
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.48);
}

.review-achievement-label,
.review-achievement-section {
  color: #6b838a;
  font-size: 12px;
  font-weight: 700;
}

.review-achievement-value {
  color: #123f46;
  font-size: 14px;
  line-height: 1.65;
  word-break: break-word;
  white-space: pre-wrap;
}

.review-achievement-attachments {
  display: grid;
  gap: 10px;
}

.review-achievement-file-list {
  display: grid;
  gap: 10px;
}

.review-achievement-file {
  border: 0;
  border-radius: 14px;
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.62);
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  text-align: left;
}

.review-achievement-file img {
  width: 24px;
  height: 24px;
  flex-shrink: 0;
}

.review-achievement-file-name {
  color: #123f46;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}

.review-achievement-empty {
  min-height: 280px;
  border: 1px dashed rgba(100, 12, 114, 0.18);
  border-radius: 18px;
  display: grid;
  place-items: center;
  color: #6b838a;
  font-size: 15px;
  background: rgba(255, 255, 255, 0.34);
}
</style>
