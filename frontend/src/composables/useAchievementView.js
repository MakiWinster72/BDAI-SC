import { ref } from "vue";
import { deleteAchievement } from "@/api/achievements";
import { useToast } from "@/composables/useToast";

export function useAchievementView({
  achievements,
  errorMessage,
  findPendingAchievementReview,
  onEditFromView,
  viewItem: viewItemRef,
}) {
  const { info: toastInfo } = useToast();
  const viewOpen = ref(false);
  const viewClosing = ref(false);
  const viewExitUp = ref(false);
  const viewLoading = ref(false);
  const viewItem = viewItemRef || ref(null);
  const deleteDialogOpen = ref(false);
  const deleteBusy = ref(false);

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
    }, 500);
  }

  async function editFromView() {
    if (!viewItem.value) {
      return;
    }
    const pending = await findPendingAchievementReview(
      viewItem.value.id,
      viewItem.value.category,
    );
    if (pending) {
      toastInfo("请等待通过审核后，再进行编辑或前往取消申请");
      return;
    }
    onEditFromView(viewItem.value);
    viewOpen.value = false;
    viewClosing.value = true;
    setTimeout(() => {
      viewItem.value = null;
      viewClosing.value = false;
    }, 500);
  }

  async function openDelete() {
    if (deleteDialogOpen.value) {
      return;
    }
    if (viewItem.value) {
      const pending = await findPendingAchievementReview(
        viewItem.value.id,
        viewItem.value.category,
      );
      if (pending) {
        toastInfo("请等待通过审核后，再进行删除或前往取消申请");
        return;
      }
    }
    deleteDialogOpen.value = true;
  }

  function closeDelete() {
    if (deleteBusy.value || !deleteDialogOpen.value) {
      return;
    }
    deleteDialogOpen.value = false;
  }

  async function confirmDelete() {
    if (!viewItem.value) {
      closeDelete();
      return;
    }
    deleteBusy.value = true;
    try {
      await deleteAchievement(viewItem.value.category, viewItem.value.id);
      achievements.value = achievements.value.filter(
        (item) => item.id !== viewItem.value.id,
      );
      closeView();
    } catch (err) {
      errorMessage.value = err?.response?.data?.message || "删除失败";
    } finally {
      deleteBusy.value = false;
      closeDelete();
    }
  }

  return {
    viewOpen,
    viewClosing,
    viewExitUp,
    viewLoading,
    viewItem,
    deleteDialogOpen,
    deleteBusy,
    openDetail,
    closeView,
    editFromView,
    openDelete,
    closeDelete,
    confirmDelete,
  };
}
