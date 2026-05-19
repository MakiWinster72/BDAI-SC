import { computed, nextTick, ref } from "vue";

const EDUCATION_CLEAR_FIELDS = [
  "startDate",
  "endDate",
  "schoolName",
  "educationLevel",
  "witness",
];
const CADRE_CLEAR_FIELDS = [
  "startDate",
  "endDate",
  "department",
  "position",
  "description",
];

export function useProfileExperienceTable({
  items,
  isEditing,
  createItem,
  clearFields,
  isRowEmpty,
}) {
  const tableWrapRef = ref(null);

  const hasCurrent = computed(() => items.some((entry) => entry.isCurrent));
  const currentIndex = computed(() =>
    items.findIndex((entry) => entry.isCurrent),
  );

  function isRowDisabled(index) {
    if (!isEditing.value) {
      return true;
    }
    const activeIndex = currentIndex.value;
    return activeIndex !== -1 && index > activeIndex;
  }

  function clearRowsAfter(index) {
    items.slice(index + 1).forEach((entry) => {
      clearFields.forEach((field) => {
        if (field === "isCurrent") {
          entry.isCurrent = false;
          return;
        }
        entry[field] = "";
      });
    });
  }

  function pruneRowsAfter(index) {
    if (items.length <= index + 1) {
      return;
    }
    if (!isRowEmpty) {
      items.splice(index + 1);
      return;
    }
    const kept = items.slice(0, index + 1);
    items.slice(index + 1).forEach((entry) => {
      if (!isRowEmpty(entry)) {
        kept.push(entry);
      }
    });
    if (kept.length !== items.length) {
      items.splice(0, items.length, ...kept);
    }
  }

  async function animateHeightWithUpdate(updateFn) {
    const el = tableWrapRef.value;
    if (!el) {
      updateFn();
      return;
    }
    const startHeight = el.getBoundingClientRect().height;
    updateFn();
    await nextTick();
    const targetHeight = el.getBoundingClientRect().height;
    el.style.height = `${startHeight}px`;
    el.style.overflow = "hidden";
    requestAnimationFrame(() => {
      el.style.transition = "height 260ms ease";
      el.style.height = `${targetHeight}px`;
    });
    const cleanup = () => {
      el.style.height = "";
      el.style.transition = "";
      el.style.overflow = "";
      el.removeEventListener("transitionend", cleanup);
    };
    el.addEventListener("transitionend", cleanup);
  }

  async function handleCurrentChange({ checked, index }) {
    if (!checked) {
      return;
    }
    const item = items[index];
    item.endDate = "";
    await animateHeightWithUpdate(() => {
      clearRowsAfter(index);
      pruneRowsAfter(index);
    });
  }

  async function addRow() {
    await animateHeightWithUpdate(() => {
      items.push(createItem());
    });
  }

  async function removeRow() {
    if (items.length <= 1) {
      return;
    }
    await animateHeightWithUpdate(() => {
      items.pop();
    });
  }

  async function clearRows() {
    await animateHeightWithUpdate(() => {
      items.splice(0, items.length, createItem());
    });
  }

  return {
    tableWrapRef,
    isRowDisabled,
    handleCurrentChange,
    addRow,
    removeRow,
    clearRows,
  };
}

export function useProfileEducationTable(
  items,
  isEditing,
  createEducationItem,
  isEducationRowEmpty,
) {
  return useProfileExperienceTable({
    items,
    isEditing,
    createItem: createEducationItem,
    clearFields: [...EDUCATION_CLEAR_FIELDS, "isCurrent"],
    isRowEmpty: isEducationRowEmpty,
  });
}

export function useProfileCadreTable(
  items,
  isEditing,
  createCadreItem,
  isCadreRowEmpty,
) {
  return useProfileExperienceTable({
    items,
    isEditing,
    createItem: createCadreItem,
    clearFields: [...CADRE_CLEAR_FIELDS, "isCurrent"],
    isRowEmpty: isCadreRowEmpty,
  });
}
