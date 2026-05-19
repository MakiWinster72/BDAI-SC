<script setup>
import { toRef } from "vue";
import RecordRow from "@/components/RecordRow.vue";
import {
  createCadreItem,
  isCadreRowEmpty,
} from "@/composables/useProfileFormModel";
import { useProfileCadreTable } from "@/composables/useProfileExperienceTable";

const props = defineProps({
  items: { type: Array, required: true },
  isEditing: { type: Boolean, default: false },
  today: { type: String, required: true },
});

const isEditingRef = toRef(props, "isEditing");
const {
  tableWrapRef,
  isRowDisabled,
  handleCurrentChange,
  addRow,
  removeRow,
  clearRows,
} = useProfileCadreTable(
  props.items,
  isEditingRef,
  createCadreItem,
  isCadreRowEmpty,
);
</script>

<template>
  <div class="card info-card">
    <div class="info-section-title">学生干部经历</div>
    <div ref="tableWrapRef" class="record-list-wrap">
      <transition-group name="education-row" tag="div" class="record-list">
        <RecordRow
          v-for="(item, index) in items"
          :key="`cadre-${index}`"
          type="cadre"
          :item="item"
          :index="index"
          :disabled="isRowDisabled(index)"
          :today="today"
          @update:item="items[index] = $event"
          @current-change="handleCurrentChange"
        />
      </transition-group>
      <div class="record-controls">
        <button
          class="record-ctl"
          type="button"
          :disabled="!isEditing"
          aria-label="增加一行"
          @click="addRow"
        >
          +
        </button>
        <button
          class="record-ctl"
          type="button"
          :disabled="!isEditing || items.length <= 1"
          aria-label="减少一行"
          @click="removeRow"
        >
          −
        </button>
        <button
          class="record-ctl record-ctl-clear"
          type="button"
          :disabled="!isEditing"
          aria-label="清空干部经历"
          @click="clearRows"
        >
          清空
        </button>
      </div>
    </div>
  </div>
</template>
