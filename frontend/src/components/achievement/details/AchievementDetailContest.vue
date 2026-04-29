<script setup>
import {
  formatContestAwardPill,
  formatContestDate,
  joinText,
  text,
} from "../../../utils/achievementFormatters";

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
    <span v-if="formatContestAwardPill(item.fields)" class="detail-award-pill">
      {{ formatContestAwardPill(item.fields) }}
    </span>
  </div>
  <div class="detail-date">{{ formatContestDate(item.fields) }}</div>

  <div class="detail-group">
    <div class="detail-group-label">基本信息</div>
    <div class="detail-row">
      <span class="detail-label">学号</span>
      <span class="detail-value">{{ text(item.fields.studentNo) }}</span>
    </div>
    <div class="detail-row">
      <span class="detail-label">姓名</span>
      <span class="detail-value">{{ text(item.fields.studentName) }}</span>
    </div>
  </div>

  <div class="detail-group">
    <div class="detail-group-label">参赛信息</div>
    <div class="detail-row">
      <span class="detail-label">竞赛名称</span>
      <span class="detail-value">{{ text(item.fields.contestName) }}</span>
    </div>
    <div class="detail-row">
      <span class="detail-label">竞赛类别</span>
      <span class="detail-value">
        {{ joinText([item.fields.contestCategory, item.fields.contestType]) }}
      </span>
    </div>
    <div class="detail-row">
      <span class="detail-label">获奖类别</span>
      <span class="detail-value">
        {{ joinText([item.fields.awardCategory, item.fields.awardLevel]) }}
      </span>
    </div>
  </div>

  <div class="detail-group">
    <div class="detail-group-label">团队信息</div>
    <div class="detail-row">
      <span class="detail-label">团队成员</span>
      <span class="detail-value">
        {{ joinText([item.fields.studentName, item.fields.teamMembers], "、") }}
      </span>
    </div>
    <div v-if="item.fields.instructors" class="detail-row">
      <span class="detail-label">指导老师</span>
      <span class="detail-value">{{ item.fields.instructors }}</span>
    </div>
  </div>

  <div class="detail-group">
    <div class="detail-group-label">获奖信息</div>
    <div class="detail-row">
      <span class="detail-label">获奖人数</span>
      <span class="detail-value">{{ text(item.fields.awardCount) }}</span>
    </div>
    <div class="detail-row">
      <span class="detail-label">备注</span>
      <span class="detail-value">{{ text(item.fields.remark) }}</span>
    </div>
  </div>

  <div class="detail-group">
    <div class="detail-row">
      <span class="detail-label">主办单位</span>
      <span class="detail-value">{{ text(item.fields.organizer) }}</span>
    </div>
  </div>
</template>
