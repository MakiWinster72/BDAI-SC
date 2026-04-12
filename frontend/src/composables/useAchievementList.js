/**
 * useAchievementList - 成就列表 composable
 *
 * 管理成就列表状态、详情弹窗、删除确认框。
 */
import { computed, ref } from "vue";
import { useRoute } from "vue-router";
import { getAchievements, deleteAchievement } from "../api/achievements";
import { dedupeAchievements } from "../utils/achievement";

const achievements = ref([]);
const activeCategory = ref("all");
const viewOpen = ref(false);
const viewItem = ref(null);
const viewClosing = ref(false);
const viewExitUp = ref(false);
const viewLoading = ref(false);
const deleteDialogOpen = ref(false);
const deleteBusy = ref(false);
const errorMessage = ref("");

const activeStudentQuery = computed(() => {
  const route = useRoute();
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
    String(value || "").trim().toLowerCase();
  const targetName = normalizeText(studentName);
  const targetNo = normalizeText(studentNo);
  return baseList.filter((item) => {
    const fields = item.fields || {};
    const nameMatch =
      !targetName ||
      normalizeText(fields.studentName).includes(targetName);
    const noMatch =
      !targetNo ||
      normalizeText(fields.studentNo).includes(targetNo);
    return nameMatch && noMatch;
  });
});

async function fetchAchievements() {
  viewLoading.value = true;
  errorMessage.value = "";
  try {
    const data = await getAchievements();
    achievements.value = dedupeAchievements(data || []);
  } catch (err) {
    errorMessage.value = "加载失败，请稍后重试";
    console.error("fetchAchievements error", err);
  } finally {
    viewLoading.value = false;
  }
}

function openDetail(item) {
  viewItem.value = item;
  viewOpen.value = true;
  viewClosing.value = false;
}

function closeView() {
  viewOpen.value = false;
  viewClosing.value = true;
  setTimeout(() => {
    viewItem.value = null;
    viewClosing.value = false;
  }, 260);
}

function openDelete() {
  if (deleteDialogOpen.value) return;
  deleteDialogOpen.value = true;
}

function closeDelete() {
  deleteDialogOpen.value = false;
}

async function confirmDelete() {
  const item = viewItem.value;
  if (!item) return;
  deleteBusy.value = true;
  try {
    await deleteAchievement(item.category, item.id);
    achievements.value = achievements.value.filter((a) => a.id !== item.id);
    closeView();
    closeDelete();
  } catch (err) {
    errorMessage.value = "删除失败，请稍后重试";
    console.error("confirmDelete error", err);
  } finally {
    deleteBusy.value = false;
  }
}

export function useAchievementList() {
  return {
    // state
    achievements,
    activeCategory,
    filteredAchievements,
    viewOpen,
    viewItem,
    viewClosing,
    viewExitUp,
    viewLoading,
    deleteDialogOpen,
    deleteBusy,
    errorMessage,
    // computed
    activeStudentQuery,
    isEmbedded,
    // methods
    fetchAchievements,
    openDetail,
    closeView,
    openDelete,
    closeDelete,
    confirmDelete,
  };
}
