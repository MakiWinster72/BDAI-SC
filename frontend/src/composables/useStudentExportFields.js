import { computed, reactive } from "vue";
import {
  createExportSelections,
  exportGroups,
  familyRows,
} from "@/utils/studentProfileExport";

export function useStudentExportFields() {
  const exportSelections = reactive(createExportSelections());

  const isAllSelected = computed(() =>
    exportGroups.every((group) =>
      group.fields.every((field) => exportSelections[field.key]),
    ),
  );

  function isGroupSelected(group) {
    return group.fields.every((field) => exportSelections[field.key]);
  }

  function isGroupChecked(group) {
    return isGroupSelected(group);
  }

  function toggleGroupSelection(group, checked) {
    group.fields.forEach((field) => {
      exportSelections[field.key] = checked;
    });
  }

  function toggleAllSelections(checked) {
    exportGroups.forEach((group) => {
      group.fields.forEach((field) => {
        exportSelections[field.key] = checked;
      });
    });
  }

  return {
    exportSelections,
    exportGroups,
    familyRows,
    isAllSelected,
    isGroupSelected,
    isGroupChecked,
    toggleGroupSelection,
    toggleAllSelections,
  };
}
