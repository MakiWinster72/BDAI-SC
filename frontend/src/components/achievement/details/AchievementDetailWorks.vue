<script setup>
import { text } from "../../../utils/achievementFormatters";

defineProps({
  item: {
    type: Object,
    required: true,
  },
});
</script>

<template>
  <div class="detail-header">
    <div class="detail-title">{{ item.title || "-" }}</div>
    <div class="detail-badges">
      <span class="detail-category-tag">
        {{ text(item.fields.workCategory, "创作作品") }}
      </span>
      <span v-if="item.fields.workType" class="detail-type-badge">
        {{ item.fields.workType }}
      </span>
    </div>
  </div>
  <div class="detail-date">{{ text(item.fields.publishDate) }}</div>

  <div class="detail-info-strip">
    <div v-if="item.fields.impactScope" class="detail-strip-item">
      <span class="strip-icon">◎</span>
      <span>{{ item.fields.impactScope }}</span>
    </div>
    <div v-if="item.fields.publishOccasion" class="detail-strip-item">
      <span class="strip-icon">◈</span>
      <span>{{ item.fields.publishOccasion }}</span>
    </div>
  </div>

  <div
    v-if="item.fields.studentName || item.fields.studentNo"
    class="detail-group"
  >
    <div class="detail-group-label">创作者</div>
    <div v-if="item.fields.studentName" class="detail-row">
      <span class="detail-label">姓名</span>
      <span class="detail-value">{{ item.fields.studentName }}</span>
    </div>
    <div v-if="item.fields.studentNo" class="detail-row">
      <span class="detail-label">学号</span>
      <span class="detail-value">{{ item.fields.studentNo }}</span>
    </div>
  </div>

  <div v-if="item.fields.organizer" class="detail-group">
    <div class="detail-group-label">主办单位</div>
    <div class="detail-row">
      <span class="detail-value">{{ item.fields.organizer }}</span>
    </div>
  </div>

  <div v-if="item.fields.note" class="detail-note">
    <div class="detail-note-label">说明</div>
    <div class="detail-note-text">{{ item.fields.note }}</div>
  </div>
</template>
