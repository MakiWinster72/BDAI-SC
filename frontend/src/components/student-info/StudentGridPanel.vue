<script setup>
import { ref } from "vue";
import { AgGridVue } from "ag-grid-vue3";
import "ag-grid-community/styles/ag-grid.css";
import "ag-grid-community/styles/ag-theme-quartz.css";

defineProps({
  rowData: {
    type: Array,
    default: () => [],
  },
  columnDefs: {
    type: Array,
    default: () => [],
  },
  defaultColDef: {
    type: Object,
    default: () => ({}),
  },
  localeText: {
    type: Object,
    default: () => ({}),
  },
  localeTextFunc: {
    type: Function,
    required: true,
  },
  fullscreen: {
    type: Boolean,
    default: false,
  },
});

const panelRef = ref(null);

defineExpose({
  element: panelRef,
});
</script>

<template>
  <div
    ref="panelRef"
    class="student-grid-wrap"
    :class="{ fullscreen }"
  >
    <AgGridVue
      class="ag-theme-quartz student-grid"
      :row-data="rowData"
      :column-defs="columnDefs"
      :default-col-def="defaultColDef"
      :locale-text="localeText"
      :locale-text-func="localeTextFunc"
      :animate-rows="true"
      :pagination="false"
      :suppress-cell-focus="true"
    />
  </div>
</template>
