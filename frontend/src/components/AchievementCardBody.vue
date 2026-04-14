<script setup>
import {
  formatContestAwardLine,
  formatContestAwardPill,
  formatContestCategoryLine,
  formatContestDate,
  formatContestMembers,
  formatContestOrganizer,
  formatPaperAuthors,
  formatPatentInventor,
  formatWorksCategory,
  formatWorksDate,
  formatWorksOccasion,
  formatWorksTag,
  text,
} from "../utils/achievementFormatters";

const props = defineProps({
  item: {
    type: Object,
    required: true,
  },
})
</script>

<template>
  <div v-if="item.category === 'contest'" class="achievement-card-body">
    <div class="achievement-card-title-row">
      <div class="achievement-card-title">{{ item.title || '-' }}</div>
      <span v-if="formatContestAwardPill(item.fields)" class="achievement-award-pill">
        {{ formatContestAwardPill(item.fields) }}
      </span>
    </div>
    <div class="achievement-card-date-italic">
      {{ formatContestDate(item.fields) }}
    </div>
    <div class="achievement-card-text">
      {{ formatContestCategoryLine(item.fields) }}
    </div>
    <div class="achievement-card-text">
      {{ formatContestMembers(item.fields) }}
    </div>
    <div class="achievement-card-text">
      {{ formatContestAwardLine(item.fields) }}
    </div>
    <div class="achievement-card-organizer">
      {{ formatContestOrganizer(item.fields) }}
    </div>
  </div>

  <div v-else-if="item.category === 'paper'" class="achievement-card-body">
    <div class="achievement-card-title achievement-paper-title">
      {{ item.title || '-' }}
    </div>
    <div class="achievement-card-text">
      {{ text(item.fields.journalName) }}
    </div>
    <div class="achievement-card-text">
      {{ formatPaperAuthors(item.fields) }}
    </div>
    <div class="achievement-card-text">
      {{ text(item.fields.publishDate) }}
    </div>
    <div class="achievement-paper-tag" aria-hidden="true">学术论文</div>
  </div>

  <div v-else-if="item.category === 'journal'" class="achievement-card-body">
    <div class="achievement-card-title achievement-paper-title">
      {{ item.title || '-' }}
    </div>
    <div class="achievement-card-text">
      {{ text(item.fields.publicationName) }}
    </div>
    <div class="achievement-card-text">
      {{ text(item.fields.studentName) }}
    </div>
    <div class="achievement-card-text">
      {{ text(item.fields.publishDate) }}
    </div>
    <div class="achievement-journal-tag" aria-hidden="true">
      期刊作品
    </div>
  </div>

  <div v-else-if="item.category === 'patent'" class="achievement-card-body">
    <div class="achievement-card-title achievement-paper-title">
      {{ item.title || '-' }}
    </div>
    <div class="achievement-card-text">
      {{ text(item.fields.grantNo) }}
    </div>
    <div class="achievement-card-text">
      {{ formatPatentInventor(item.fields) }}
    </div>
    <div class="achievement-card-text">
      {{ text(item.fields.grantDate) }}
    </div>
    <div class="achievement-patent-tag" aria-hidden="true">
      {{ text(item.fields.patentType, '专利') }}
    </div>
  </div>

  <div v-else-if="item.category === 'certificate'" class="achievement-card-body">
    <div class="achievement-card-title achievement-paper-title">
      {{ item.title || '-' }}
    </div>
    <div class="achievement-card-text">
      {{ text(item.fields.certificateType) }}
    </div>
    <div class="achievement-card-text">
      {{ text(item.fields.studentName) }}
    </div>
    <div class="achievement-card-text">
      {{ text(item.fields.obtainDate) }}
    </div>
    <div class="achievement-certificate-tag" aria-hidden="true">
      职业资格证书
    </div>
  </div>

  <div v-else-if="item.category === 'research'" class="achievement-card-body">
    <div class="achievement-card-title achievement-paper-title">
      {{ item.title || '-' }}
    </div>
    <div class="achievement-card-text">
      {{ `项目负责人：${text(item.fields.projectLeader)}` }}
    </div>
    <div class="achievement-card-text">
      {{ `教师工号：${text(item.fields.teacherNo)}` }}
    </div>
    <div class="achievement-research-tag" aria-hidden="true">
      科研项目
    </div>
  </div>

  <div v-else-if="item.category === 'works'" class="achievement-card-body">
    <div class="achievement-card-title achievement-paper-title">
      {{ item.title || '-' }}
    </div>
    <div class="achievement-card-meta">
      <span class="achievement-card-meta-item">{{ formatWorksCategory(item.fields) }}</span>
      <span class="achievement-card-meta-dot">·</span>
      <span class="achievement-card-meta-item">{{ formatWorksDate(item.fields) }}</span>
    </div>
    <div class="achievement-card-text" v-if="item.fields.publishOccasion">
      {{ formatWorksOccasion(item.fields) }}
    </div>
    <div class="achievement-card-text" v-if="item.fields.studentName">
      {{ item.fields.studentName }}
    </div>
    <div class="achievement-works-tag" aria-hidden="true">
      {{ formatWorksTag(item.fields) }}
    </div>
  </div>

  <div v-else class="achievement-card-body">
    <div class="achievement-card-title">{{ item.title || '-' }}</div>
    <div v-if="item.dateLabel" class="achievement-card-dates">
      <span>{{ item.dateLabel }}：{{ item.dateValue || '-' }}</span>
    </div>
    <div
      v-for="line in item.previewFields"
      :key="line"
      class="achievement-card-text"
    >
      {{ line }}
    </div>
  </div>
</template>
