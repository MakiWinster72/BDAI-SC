import { computed, ref } from "vue";

const FILTER_DRAG_THRESHOLD = 80;

export function useMobileFilterSheet() {
  const open = ref(false);
  const filterTouchStartY = ref(0);
  const filterIsDragging = ref(false);
  const filterDragTranslateY = ref(0);

  const sheetStyle = computed(() => ({
    transform:
      filterDragTranslateY.value > 0
        ? `translateY(${filterDragTranslateY.value}px) scale(${1 - filterDragTranslateY.value / 2000})`
        : "",
    transition: filterIsDragging.value ? "none" : "",
    "transform-origin": "bottom center",
  }));

  function openSheet() {
    open.value = true;
    document.body.style.overflow = "hidden";
  }

  function closeSheet() {
    open.value = false;
    document.body.style.overflow = "";
    filterIsDragging.value = false;
    filterDragTranslateY.value = 0;
  }

  function handleTouchStart(event) {
    filterTouchStartY.value = event.touches[0].clientY;
    filterIsDragging.value = true;
  }

  function handleTouchMove(event) {
    if (!filterIsDragging.value) {
      return;
    }
    const delta = event.touches[0].clientY - filterTouchStartY.value;
    if (delta > 0) {
      filterDragTranslateY.value = delta;
    }
  }

  function handleTouchEnd() {
    if (!filterIsDragging.value) {
      return;
    }
    if (filterDragTranslateY.value > FILTER_DRAG_THRESHOLD) {
      closeSheet();
    }
    filterIsDragging.value = false;
    filterDragTranslateY.value = 0;
  }

  return {
    open,
    sheetStyle,
    openSheet,
    closeSheet,
    handleTouchStart,
    handleTouchMove,
    handleTouchEnd,
  };
}
