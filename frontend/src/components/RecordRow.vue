<script setup>
/**
 * RecordRow - 教育经历/班干经历行组件
 *
 * 共用同一结构: 时间段(开始日期-结束日期-至今勾选) + 若干字段
 *
 * Props:
 *   type       - 'education' | 'cadre'
 *   item       - 行数据对象
 *   index      - 行索引
 *   disabled   - 是否整体禁用
 *   today      - 今天日期 (max attribute)
 */

const props = defineProps({
  type: {
    type: String,
    required: true,
    validator: (v) => ['education', 'cadre'].includes(v),
  },
  item: {
    type: Object,
    required: true,
  },
  index: {
    type: Number,
    required: true,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  today: {
    type: String,
    default: '',
  },
})

const emit = defineEmits(['update:item', 'current-change'])

// Common date disabled: always false since parent controls overall state
function dateDisabled(indexOffset = 0) {
  return props.disabled
}

function endDateDisabled() {
  return props.disabled || props.item.isCurrent
}

function onStartDateChange(e) {
  emit('update:item', { ...props.item, startDate: e.target.value })
}

function onEndDateChange(e) {
  emit('update:item', { ...props.item, endDate: e.target.value })
}

function onCurrentChange(e) {
  emit('current-change', { item: props.item, checked: e.target.checked, index: props.index })
}

function onFieldChange(key, e) {
  emit('update:item', { ...props.item, [key]: e.target.value })
}
</script>

<template>
  <section class="record-card">
    <div :class="['record-grid', type === 'education' ? 'record-grid-education' : 'record-grid-cadre']">
      <!-- Time period -->
      <label class="record-field record-field-time">
        <span class="info-label">时间段</span>
        <div class="education-period">
          <div class="education-period-row">
            <input
              :value="item.startDate"
              class="info-input"
              type="date"
              lang="zh-CN"
              :max="today"
              :disabled="dateDisabled()"
              @change="onStartDateChange"
            />
            <span class="education-sep">至</span>
            <input
              :value="item.endDate"
              class="info-input"
              type="date"
              lang="zh-CN"
              :max="today"
              :disabled="endDateDisabled()"
              @change="onEndDateChange"
            />
          </div>
          <label class="info-choice info-choice-muted record-current">
            <input
              :checked="item.isCurrent"
              type="checkbox"
              :disabled="disabled"
              @change="onCurrentChange"
            />
            至今
          </label>
        </div>
      </label>

      <!-- Education fields -->
      <template v-if="type === 'education'">
        <label class="record-field record-field-school">
          <span class="info-label">学校名称</span>
          <input
            :value="item.schoolName"
            class="info-input"
            type="text"
            placeholder="学校名称"
            :disabled="disabled"
            @change="onFieldChange('schoolName', $event)"
          />
        </label>
        <label class="record-field record-field-compact">
          <span class="info-label">学历</span>
          <input
            :value="item.educationLevel"
            class="info-input"
            type="text"
            placeholder="学历"
            :disabled="disabled"
            @change="onFieldChange('educationLevel', $event)"
          />
        </label>
        <label class="record-field record-field-compact">
          <span class="info-label">证明人</span>
          <input
            :value="item.witness"
            class="info-input"
            type="text"
            placeholder="证明人"
            :disabled="disabled"
            @change="onFieldChange('witness', $event)"
          />
        </label>
      </template>

      <!-- Cadre fields -->
      <template v-else-if="type === 'cadre'">
        <label class="record-field record-field-department">
          <span class="info-label">社团部门/班级</span>
          <input
            :value="item.department"
            class="info-input"
            type="text"
            placeholder="部门/班级"
            :disabled="disabled"
            @change="onFieldChange('department', $event)"
          />
        </label>
        <label class="record-field record-field-position">
          <span class="info-label">职位</span>
          <input
            :value="item.position"
            class="info-input"
            type="text"
            placeholder="职位"
            :disabled="disabled"
            @change="onFieldChange('position', $event)"
          />
        </label>
        <label class="record-field record-field-full">
          <span class="info-label">描述</span>
          <textarea
            :value="item.description"
            class="info-input"
            rows="2"
            placeholder="简述你在该职位的职责/成就"
            :disabled="disabled"
            @change="onFieldChange('description', $event)"
          ></textarea>
        </label>
      </template>
    </div>
  </section>
</template>
