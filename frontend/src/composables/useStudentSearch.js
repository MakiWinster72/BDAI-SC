import { computed, reactive, watch } from "vue";
import {
  majorOptionsByCategory,
  specialStudentTypeOptions,
  studentCategoryOptions,
  studentSearchClassYearOptions,
} from "../constants/profileOptions";

const specialStudentTypeLabelMap = Object.fromEntries(
  specialStudentTypeOptions
    .filter((item) => item.value)
    .map((item) => [item.value, item.label]),
);

export function useStudentSearch() {
  const filters = reactive({
    classYear: "",
    studentCategory: "",
    major: "",
    classNo: "",
    isHk: false,
    isMo: false,
    isTw: false,
    specialStudentType: "",
    keyword: "",
  });

  const availableMajors = computed(() => {
    if (!filters.studentCategory) {
      return [];
    }
    return majorOptionsByCategory[filters.studentCategory] || [];
  });

  const hasActiveFilters = computed(() =>
    Boolean(
      filters.classYear ||
        filters.studentCategory ||
        filters.major ||
        filters.classNo ||
        filters.isHk ||
        filters.isMo ||
        filters.isTw ||
        filters.specialStudentType ||
        filters.keyword,
    ),
  );

  watch(
    () => filters.studentCategory,
    () => {
      filters.major = "";
    },
  );

  watch(
    () => filters.isHk,
    (value) => {
      if (value) {
        filters.isMo = false;
        filters.isTw = false;
      }
    },
  );

  watch(
    () => filters.isMo,
    (value) => {
      if (value) {
        filters.isHk = false;
        filters.isTw = false;
      }
    },
  );

  watch(
    () => filters.isTw,
    (value) => {
      if (value) {
        filters.isHk = false;
        filters.isMo = false;
      }
    },
  );

  function toggleHmt(key) {
    const others =
      key === "isHk"
        ? ["isMo", "isTw"]
        : key === "isMo"
          ? ["isHk", "isTw"]
          : ["isHk", "isMo"];
    if (filters[key]) {
      filters[key] = false;
      return;
    }
    others.forEach((item) => {
      filters[item] = false;
    });
    filters[key] = true;
  }

  function resetFilters() {
    filters.classYear = "";
    filters.studentCategory = "";
    filters.major = "";
    filters.classNo = "";
    filters.isHk = false;
    filters.isMo = false;
    filters.isTw = false;
    filters.specialStudentType = "";
    filters.keyword = "";
  }

  function buildSearchParams(page, size) {
    const params = { page, size };
    if (filters.classYear) {
      params.classYear = filters.classYear;
    }
    if (filters.studentCategory) {
      params.studentCategory = filters.studentCategory;
    }
    if (filters.major) {
      params.major = filters.major;
    }
    if (filters.classNo) {
      params.classNo = Number(filters.classNo);
    }
    if (filters.isHk) {
      params.isHk = true;
    }
    if (filters.isMo) {
      params.isMo = true;
    }
    if (filters.isTw) {
      params.isTw = true;
    }
    if (filters.specialStudentType) {
      params.specialStudentType = filters.specialStudentType;
    }
    if (filters.keyword && filters.keyword.trim()) {
      params.keyword = filters.keyword.trim();
    }
    return params;
  }

  function getSpecialStudentTypeLabel(type) {
    return specialStudentTypeLabelMap[type] || type || "";
  }

  return {
    filters,
    classYearOptions: studentSearchClassYearOptions,
    studentCategoryOptions,
    specialStudentTypeOptions,
    availableMajors,
    hasActiveFilters,
    toggleHmt,
    resetFilters,
    buildSearchParams,
    getSpecialStudentTypeLabel,
  };
}
