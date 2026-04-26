<script setup>
import { computed } from "vue";
import { achievementDetailSchemas } from "../../constants/achievementDetailSchemas";
import AchievementDetailContest from "./details/AchievementDetailContest.vue";
import AchievementDetailSchemaContent from "./details/AchievementDetailSchemaContent.vue";
import AchievementDetailWorks from "./details/AchievementDetailWorks.vue";
import AchievementDetailDoubleHundred from "./details/AchievementDetailDoubleHundred.vue";
import AchievementDetailIeerTraining from "./details/AchievementDetailIeerTraining.vue";

const props = defineProps({
  item: {
    type: Object,
    required: true,
  },
  attachmentIcon: {
    type: Function,
    required: true,
  },
  isVideoFile: {
    type: Function,
    required: true,
  },
  isDocumentFile: {
    type: Function,
    required: true,
  },
  isSheetFile: {
    type: Function,
    required: true,
  },
  isPdfFile: {
    type: Function,
    required: true,
  },
  isAllowedImage: {
    type: Function,
    required: true,
  },
  isPptxFile: {
    type: Function,
    required: true,
  },
});

const emit = defineEmits(["preview", "edit", "delete"]);
const activeSchema = computed(() => achievementDetailSchemas[props.item.category] || null);
const hasSchemaDetail = computed(() => Boolean(activeSchema.value));

const detailComponent = computed(() => {
  if (props.item.category === "contest") {
    return AchievementDetailContest;
  }
  if (props.item.category === "works") {
    return AchievementDetailWorks;
  }
  if (props.item.category === "doubleHundred") {
    return AchievementDetailDoubleHundred;
  }
  if (props.item.category === "ieerTraining") {
    return AchievementDetailIeerTraining;
  }
  if (hasSchemaDetail.value) {
    return AchievementDetailSchemaContent;
  }
  return null;
});

const detailComponentProps = computed(() => {
  const baseProps = { item: props.item };
  if (detailComponent.value === AchievementDetailSchemaContent && activeSchema.value) {
    return {
      ...baseProps,
      schema: activeSchema.value,
    };
  }
  return baseProps;
});

function openAttachmentPreview(url) {
  emit("preview", [url], 0);
}
</script>

<template>
  <div class="achievement-view-body" :class="{ 'no-media': !item.imageUrls?.length }">
    <div v-if="!item.imageUrls?.length" class="achievement-view-media"></div>

    <div v-else-if="item.imageUrls.length === 1" class="achievement-view-media">
      <img
        class="media-single-img"
        :src="item.imageUrls[0]"
        alt="成就图片"
        @click="emit('preview', item.imageUrls, 0)"
      />
    </div>

    <div
      v-else-if="item.imageUrls.length === 2"
      class="achievement-view-media achievement-view-media-2"
    >
      <div class="media-2-cell" @click="emit('preview', item.imageUrls, 0)">
        <img :src="item.imageUrls[0]" alt="成就图片" />
      </div>
      <div class="media-2-cell" @click="emit('preview', item.imageUrls, 1)">
        <img :src="item.imageUrls[1]" alt="成就图片" />
      </div>
    </div>

    <div v-else class="achievement-view-media achievement-view-media-3">
      <div class="media-3-grid">
        <div class="media-3-cell media-3-top-left" @click="emit('preview', item.imageUrls, 0)">
          <img :src="item.imageUrls[0]" alt="成就图片" />
        </div>
        <div class="media-3-cell media-3-top-right" @click="emit('preview', item.imageUrls, 1)">
          <img :src="item.imageUrls[1]" alt="成就图片" />
        </div>
        <div class="media-3-cell media-3-bottom" @click="emit('preview', item.imageUrls, 2)">
          <img :src="item.imageUrls[2]" alt="成就图片" />
        </div>
      </div>
    </div>

    <div class="achievement-detail-body">
      <component
        v-if="detailComponent"
        :is="detailComponent"
        v-bind="detailComponentProps"
      />

      <template v-else>
        <div class="detail-header">
          <div class="detail-title">{{ item.title || "-" }}</div>
        </div>
        <div v-if="item.dateLabel" class="detail-date">
          {{ item.dateLabel }}：{{ item.dateValue || "-" }}
        </div>
        <div v-for="line in item.fieldLines" :key="line" class="detail-row">
          <span class="detail-value">{{ line }}</span>
        </div>
      </template>

      <div
        v-if="item.attachments && item.attachments.length"
        class="detail-attachments"
      >
        <div class="detail-attachments-title">附件</div>
        <div class="attachment-list">
          <div
            v-for="(file, index) in item.attachments"
            :key="`${file.url}-${index}`"
            class="attachment-item"
          >
            <img :src="attachmentIcon(file)" alt="" />
            <div class="attachment-name">{{ file.name }}</div>
            <button
              v-if="
                isVideoFile(file) ||
                isDocumentFile(file) ||
                isSheetFile(file) ||
                isPdfFile(file)
              "
              class="attachment-link"
              @click="openAttachmentPreview(file.url)"
            >
              预览
            </button>
            <button
              v-else-if="isAllowedImage(file)"
              class="attachment-link"
              @click="openAttachmentPreview(file.url)"
            >
              预览
            </button>
            <a
              v-else-if="!isPptxFile(file)"
              class="attachment-link"
              :href="file.url"
              target="_blank"
              rel="noopener noreferrer"
            >
              查看
            </a>
            <a
              class="attachment-download"
              :href="file.url"
              download
              rel="noopener noreferrer"
              title="下载"
            >
              <svg
                width="14"
                height="14"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                <polyline points="7 10 12 15 17 10"></polyline>
                <line x1="12" y1="15" x2="12" y2="3"></line>
              </svg>
            </a>
          </div>
        </div>
      </div>

      <div class="detail-actions">
        <button class="post-action" type="button" @click="emit('edit')">
          编辑
        </button>
        <button
          class="post-action danger"
          type="button"
          @click.stop="emit('delete')"
        >
          删除
        </button>
      </div>
    </div>
  </div>
</template>
