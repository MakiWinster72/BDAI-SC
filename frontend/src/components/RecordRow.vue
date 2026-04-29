<script setup>
/**
 * RecordRow - 教育经历 / 学生干部经历行组件
 *
 * Flat design: no nested card borders, clean row with separator.
 * Props/emits contract unchanged.
 */

const props = defineProps({
  type: {
    type: String,
    required: true,
    validator: (v) => ['education', 'cadre'].includes(v),
  },
  item: { type: Object, required: true },
  index: { type: Number, required: true },
  disabled: { type: Boolean, default: false },
  today:  { type: String, default: '' },
})

const emit = defineEmits(['update:item', 'current-change'])

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
  <section class="record-row">
    <div :class="['record-grid', type === 'education' ? 'record-grid-edu' : 'record-grid-cdr']">
      <!-- ── Time period (shared) ────────────────────────────── -->
      <div class="record-field record-field-time">
        <span class="info-label">时间段</span>
        <div class="record-period">
          <input
            :value="item.startDate"
            class="info-input"
            type="date"
            lang="zh-CN"
            :max="today"
            :disabled="disabled"
            @change="onStartDateChange"
          />
          <span class="record-sep">至</span>
          <input
            :value="item.endDate"
            class="info-input"
            type="date"
            lang="zh-CN"
            :max="today"
            :disabled="endDateDisabled()"
            @change="onEndDateChange"
          />
          <label class="info-choice info-choice-muted">
            <input
              :checked="item.isCurrent"
              type="checkbox"
              :disabled="disabled"
              @change="onCurrentChange"
            />
            至今
          </label>
        </div>
      </div>

      <!-- ── Education fields ────────────────────────────────── -->
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
        <label class="record-field record-field-level">
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
        <label class="record-field record-field-witness">
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

      <!-- ── Cadre fields ────────────────────────────────────── -->
      <template v-else-if="type === 'cadre'">
        <label class="record-field record-field-dept">
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
        <label class="record-field record-field-pos">
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
        <label class="record-field record-field-desc">
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
