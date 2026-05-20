import { computed, ref } from "vue";
import { getAchievements } from "@/api/achievements";
import { achievementEntries } from "@/constants/achievementConstants";
import { dedupeAchievements } from "@/utils/achievement";
import {
  normalizeAchievement,
  hydrateAchievementMedia,
} from "@/utils/achievementListModel";
import { navigateWithViewTransition } from "@/utils/viewTransition";

export function useAchievementList({
  route,
  router,
  errorMessage,
  uploadLimitConfig,
}) {
  const achievements = ref([]);
  const activeCategory = ref("all");
  const achievementsOpen = ref(true);

  const activeStudentQuery = computed(() => {
    const rawName = route.query.studentName;
    const rawNo = route.query.studentNo;
    const rawEmbed = route.query.embed;
    const studentName = typeof rawName === "string" ? rawName.trim() : "";
    const studentNo = typeof rawNo === "string" ? rawNo.trim() : "";
    const embedValue = typeof rawEmbed === "string" ? rawEmbed.trim() : "";
    return { studentName, studentNo, embedValue };
  });

  const isEmbedded = computed(() => {
    const value = activeStudentQuery.value.embedValue;
    return value === "1" || value.toLowerCase() === "true";
  });

  const categoryOptions = computed(() =>
    achievementEntries.filter((entry) => entry.key !== "all"),
  );

  const activeCategoryLabel = computed(() => {
    const match = achievementEntries.find(
      (entry) => entry.key === activeCategory.value,
    );
    return match ? match.label : "全部";
  });

  const filteredAchievements = computed(() => {
    const baseList =
      activeCategory.value === "all"
        ? achievements.value
        : achievements.value.filter(
            (item) => item.category === activeCategory.value,
          );
    const { studentName, studentNo } = activeStudentQuery.value;
    if (!studentName && !studentNo) {
      return baseList;
    }
    const normalizeText = (value) =>
      String(value || "")
        .trim()
        .toLowerCase();
    const targetName = normalizeText(studentName);
    const targetNo = normalizeText(studentNo);
    return baseList.filter((item) => {
      const fields = item.fields || {};
      const itemNo = normalizeText(fields.studentNo);
      const itemName = normalizeText(fields.studentName);
      if (targetNo) {
        return itemNo ? itemNo === targetNo : true;
      }
      return itemName ? itemName === targetName : true;
    });
  });

  const emptyMessage = computed(() => {
    const { studentName, studentNo } = activeStudentQuery.value;
    if (studentName || studentNo) {
      return "该学生暂未添加任何个人成果。";
    }
    if (activeCategory.value === "all") {
      return "还没有哦~点击右下角添加。";
    }
    return "该分类暂无成就，点击右下角添加。";
  });

  const addButtonLabel = computed(() => {
    if (activeCategory.value === "all") {
      return "添加成就";
    }
    return `添加${activeCategoryLabel.value}`;
  });

  function handleAchievementEntry(key) {
    const safeKey = achievementEntries.some((entry) => entry.key === key)
      ? key
      : "all";
    activeCategory.value = safeKey;
    achievementsOpen.value = true;
    const { studentName, studentNo, embedValue } = activeStudentQuery.value;
    const query = { category: safeKey };
    if (studentName) {
      query.studentName = studentName;
    }
    if (studentNo) {
      query.studentNo = studentNo;
    }
    if (embedValue) {
      query.embed = embedValue;
    }
    navigateWithViewTransition(router, { path: "/achievements", query });
  }

  function syncCategoryFromRoute() {
    const rawCategory = route.query.category;
    const safeCategory =
      typeof rawCategory === "string" &&
      achievementEntries.some((entry) => entry.key === rawCategory)
        ? rawCategory
        : "all";
    activeCategory.value = safeCategory;
    achievementsOpen.value = true;
    if (rawCategory !== safeCategory) {
      const { studentName, studentNo, embedValue } = activeStudentQuery.value;
      const query = { category: safeCategory };
      if (studentName) {
        query.studentName = studentName;
      }
      if (studentNo) {
        query.studentNo = studentNo;
      }
      if (embedValue) {
        query.embed = embedValue;
      }
      router.replace({ path: "/achievements", query });
    }
  }

  async function fetchAchievements() {
    try {
      const params = {};
      if (activeCategory.value && activeCategory.value !== "all") {
        params.category = activeCategory.value;
      }
      const { studentName, studentNo } = activeStudentQuery.value;
      if (studentName) {
        params.studentName = studentName;
      }
      if (studentNo) {
        params.studentNo = studentNo;
      }
      const { data } = await getAchievements(params);
      const imageMaxCount = uploadLimitConfig?.imageMaxCount ?? 9;
      achievements.value = Array.isArray(data)
        ? await hydrateAchievementMedia(
            dedupeAchievements(
              data.map((item) => normalizeAchievement(item, imageMaxCount)),
            ),
          )
        : [];
      errorMessage.value = "";
    } catch (err) {
      achievements.value = [];
      errorMessage.value =
        err?.response?.data?.message || "无法获取成就列表";
    }
  }

  return {
    achievements,
    activeCategory,
    achievementsOpen,
    activeStudentQuery,
    isEmbedded,
    categoryOptions,
    activeCategoryLabel,
    filteredAchievements,
    emptyMessage,
    addButtonLabel,
    handleAchievementEntry,
    syncCategoryFromRoute,
    fetchAchievements,
  };
}
