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
    <span v-if="item.fields.projectType" class="detail-category-tag">
      {{ item.fields.projectType }}
    </span>
  </div>

  <div class="detail-date">{{ text(item.fields.projectStartDate) }}</div>

  <div
    v-if="item.fields.collegeName || item.fields.recommendedLevel"
    class="detail-info-strip"
  >
    <div v-if="item.fields.collegeName" class="detail-strip-item">
      <span class="strip-icon">◎</span>
      <span>{{ item.fields.collegeName }}</span>
    </div>
    <div v-if="item.fields.recommendedLevel" class="detail-strip-item">
      <span class="strip-icon">◈</span>
      <span>{{ item.fields.recommendedLevel }}</span>
    </div>
  </div>

  <div class="detail-group">
    <div class="detail-group-label">项目信息</div>
    <div v-if="item.fields.projectLeader" class="detail-row">
      <span class="detail-label">项目负责人</span>
      <span class="detail-value">{{ item.fields.projectLeader }}</span>
    </div>
    <div v-if="item.fields.instructorName" class="detail-row">
      <span class="detail-label">指导教师</span>
      <span class="detail-value">{{ item.fields.instructorName }}</span>
    </div>
    <div v-if="item.fields.isKeyArea" class="detail-row">
      <span class="detail-label">重点领域</span>
      <span class="detail-value">{{ item.fields.isKeyArea }}</span>
    </div>
  </div>

  <div v-if="item.fields.finalStatus" class="detail-group">
    <div class="detail-group-label">结项情况</div>
    <div class="detail-row">
      <span class="detail-label">结项状态</span>
      <span class="detail-value">{{ item.fields.finalStatus }}</span>
    </div>
  </div>

  <div v-if="item.fields.remark" class="detail-note">
    <div class="detail-note-label">备注</div>
    <div class="detail-note-text">{{ item.fields.remark }}</div>
  </div>
</template>
