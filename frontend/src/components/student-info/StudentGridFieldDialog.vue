<script setup>
import OverlayPanel from "@/components/OverlayPanel.vue";

const props = defineProps({
  open: { type: Boolean, default: false },
  closing: { type: Boolean, default: false },
  exportSelections: { type: Object, required: true },
  exportGroups: { type: Array, required: true },
  familyRows: { type: Array, required: true },
  isAllSelected: { type: Boolean, default: false },
  isGroupChecked: { type: Function, required: true },
});
const emit = defineEmits(["close", "toggle-all", "toggle-group"]);

function toggleAll(checked) {
  emit("toggle-all", checked);
}
function toggleGroup(group, checked) {
  emit("toggle-group", group, checked);
}
</script>

<template>
    <OverlayPanel
      :open="open"
      :closing="closing"
      title="选择显示字段"
      aria-label="选择显示字段"
      size="wide"
      @close="emit('close')"
    >
      <template #header>
        <div class="overlay-custom-header">
          <span class="overlay-custom-title">选择显示字段</span>
          <label class="export-all-toggle">
            <input
              type="checkbox"
              :checked="isAllSelected"
              @change="toggleAll($event.target.checked)"
            />
            <span>全选</span>
          </label>
        </div>
      </template>
      <div class="export-dialog-body">
        <div v-for="group in exportGroups" :key="group.id" class="export-group">
          <label class="export-group-title">
            <span>{{ group.label }}</span>
            <input
              type="checkbox"
              :checked="isGroupChecked(group)"
              @change="toggleGroup(group, $event.target.checked)"
            />
          </label>
          <div class="export-group-options">
            <template v-if="group.id === 'family'">
              <div
                v-for="(row, index) in familyRows"
                :key="`grid-family-row-${index}`"
                class="export-option-row"
              >
                <label
                  v-for="field in row"
                  :key="field.key"
                  class="export-option"
                >
                  <input
                    v-model="exportSelections[field.key]"
                    type="checkbox"
                  />
                  <span>{{ field.label }}</span>
                </label>
              </div>
            </template>
            <template v-else>
              <label
                v-for="field in group.fields"
                :key="field.key"
                class="export-option"
              >
                <input v-model="exportSelections[field.key]" type="checkbox" />
                <span>{{ field.label }}</span>
              </label>
            </template>
          </div>
        </div>
      </div>
    </OverlayPanel>

</template>

<style scoped>
@import "@/assets/styles/student-info-view-scoped.css";
</style>
