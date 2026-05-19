<script setup>
import YearPicker from "@/components/YearPicker.vue";

defineProps({
  info: {
    type: Object,
    required: true,
  },
  isEditing: {
    type: Boolean,
    default: false,
  },
  today: {
    type: String,
    required: true,
  },
  studentCategoryOptions: {
    type: Array,
    default: () => [],
  },
  classMajorOptions: {
    type: Array,
    default: () => [],
  },
});
</script>

<template>
  <div class="card info-card">
    <div class="info-section-title">学籍信息</div>
    <div class="info-form-grid">
      <label class="field-card">
        <span class="info-label">名字</span>
        <input
          v-model="info.name"
          class="info-input"
          type="text"
          placeholder="请输入名字"
          :disabled="!isEditing"
        />
      </label>
      <label class="field-card">
        <span class="info-label">学号</span>
        <input
          v-model="info.studentNo"
          class="info-input"
          type="text"
          placeholder="请输入学号"
          disabled
        />
      </label>
      <label class="field-card">
        <span class="info-label">年级</span>
        <YearPicker
          v-model="info.classYear"
          placeholder="今年"
          :disabled="!isEditing"
        />
      </label>
      <label class="field-card">
        <span class="info-label">学生类别</span>
        <select
          v-model="info.studentCategory"
          class="info-input"
          :disabled="!isEditing"
        >
          <option disabled value="">选择学生类别</option>
          <option
            v-for="item in studentCategoryOptions"
            :key="item"
            :value="item"
          >
            {{ item }}
          </option>
        </select>
      </label>
      <label class="field-card field-full">
        <span class="info-label">班级</span>
        <div class="class-inline">
          <select
            v-model="info.classMajor"
            class="info-input"
            :disabled="
              !isEditing ||
              !info.college ||
              !info.studentCategory ||
              !classMajorOptions.length
            "
          >
            <option disabled value="">选择专业</option>
            <option
              v-for="major in classMajorOptions"
              :key="major"
              :value="major"
            >
              {{ major }}
            </option>
          </select>
          <select
            v-model.number="info.classNo"
            class="info-input class-num"
            :disabled="!isEditing || info.studentCategory === '研究生'"
          >
            <option disabled value="">班</option>
            <option v-for="n in 10" :key="n" :value="n">
              {{ n }}
            </option>
          </select>
          <span class="class-text">班</span>
        </div>
      </label>
      <label class="field-card">
        <span class="info-label">班主任</span>
        <input
          v-model="info.classTeacher"
          class="info-input"
          type="text"
          placeholder="请输入班主任"
          :disabled="!isEditing"
        />
      </label>
      <label class="field-card">
        <span class="info-label">辅导员</span>
        <input
          v-model="info.counselor"
          class="info-input"
          type="text"
          placeholder="请输入辅导员"
          :disabled="!isEditing"
        />
      </label>
      <label class="field-card field-full">
        <span class="info-label">入学时间</span>
        <input
          v-model="info.enrollmentDate"
          class="info-input"
          type="date"
          lang="zh-CN"
          :max="today"
          :disabled="!isEditing"
        />
      </label>
    </div>
  </div>
</template>
