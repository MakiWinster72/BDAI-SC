<script setup>
import { text, joinText } from "../../../utils/achievementFormatters";

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
    <span v-if="item.fields.projectCategory" class="detail-category-tag">
      {{ item.fields.projectCategory }}
    </span>
  </div>

  <div class="detail-date">{{ text(item.fields.activityDate) }}</div>

  <div
    v-if="item.fields.college || item.fields.educationLevel"
    class="detail-info-strip"
  >
    <div v-if="item.fields.college" class="detail-strip-item">
      <span class="strip-icon">◎</span>
      <span>{{ item.fields.college }}</span>
    </div>
    <div v-if="item.fields.educationLevel" class="detail-strip-item">
      <span class="strip-icon">◈</span>
      <span>{{ item.fields.educationLevel }}</span>
    </div>
  </div>

  <div class="detail-group">
    <div class="detail-group-label">项目信息</div>
    <div v-if="item.fields.projectDomain" class="detail-row">
      <span class="detail-label">所属领域</span>
      <span class="detail-value">{{ item.fields.projectDomain }}</span>
    </div>
    <div v-if="item.fields.projectLeader" class="detail-row">
      <span class="detail-label">项目负责人</span>
      <span class="detail-value">{{ item.fields.projectLeader }}</span>
    </div>
    <div v-if="item.fields.leaderStudentNo" class="detail-row">
      <span class="detail-label">负责人学号</span>
      <span class="detail-value">{{ item.fields.leaderStudentNo }}</span>
    </div>
    <div v-if="item.fields.teamSize" class="detail-row">
      <span class="detail-label">项目人数</span>
      <span class="detail-value">{{ item.fields.teamSize }}</span>
    </div>
    <div v-if="item.fields.plannedLevel" class="detail-row">
      <span class="detail-label">拟立项等级</span>
      <span class="detail-value">{{ item.fields.plannedLevel }}</span>
    </div>
  </div>

  <div
    v-if="item.fields.teamMembers || item.fields.instructors"
    class="detail-group"
  >
    <div class="detail-group-label">团队信息</div>
    <div v-if="item.fields.teamMembers" class="detail-row">
      <span class="detail-label">项目成员</span>
      <span class="detail-value">{{ item.fields.teamMembers }}</span>
    </div>
    <div v-if="item.fields.instructors" class="detail-row">
      <span class="detail-label">指导老师</span>
      <span class="detail-value">{{ item.fields.instructors }}</span>
    </div>
  </div>

  <div v-if="item.fields.finalLevel" class="detail-group">
    <div class="detail-group-label">结项信息</div>
    <div class="detail-row">
      <span class="detail-label">结项等级</span>
      <span class="detail-value">{{ item.fields.finalLevel }}</span>
    </div>
  </div>

  <div v-if="item.fields.remark" class="detail-note">
    <div class="detail-note-label">备注</div>
    <div class="detail-note-text">{{ item.fields.remark }}</div>
  </div>
</template>
