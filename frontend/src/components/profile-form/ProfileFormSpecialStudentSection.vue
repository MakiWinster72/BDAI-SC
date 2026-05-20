<script setup>
import { specialStudentTypeOptions } from "@/constants/profileOptions";

defineProps({
  info: { type: Object, required: true },
  isEditing: { type: Boolean, default: false },
});
</script>

<template>
  <div class="card info-card profile-form-special-student">
    <div class="info-section-title">特殊学生类型</div>
    <div class="info-form-grid">
      <label class="field-card">
        <span class="info-label">特殊学生类别</span>
        <select
          v-model="info.specialStudentType"
          class="info-input profile-form-special-type"
          :disabled="!isEditing"
        >
          <option
            v-for="opt in specialStudentTypeOptions"
            :key="opt.value"
            :value="opt.value"
          >
            {{ opt.label }}
          </option>
        </select>
      </label>
      <label class="field-card field-full profile-form-special-remark">
        <span class="info-label">备注</span>
        <textarea
          v-model="info.specialStudentRemark"
          class="info-input remark-textarea"
          rows="2"
          placeholder="请输入备注"
          :disabled="!isEditing"
        />
      </label>
    </div>
  </div>
</template>

<style scoped>
.profile-form-special-type {
  max-width: 200px;
}

.profile-form-special-remark {
  margin-top: 4px;
}

.remark-textarea {
  height: auto;
  min-height: 60px;
  padding: 8px 12px;
  line-height: 1.4;
  font-size: 13px;
  resize: vertical;
}
</style>
