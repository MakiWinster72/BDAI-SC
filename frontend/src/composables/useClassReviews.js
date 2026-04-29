import { listAchievementReviewRequests } from "../api/achievementReviewRequests";
import { listProfileReviewRequests } from "../api/profileReviewRequests";

// Shared state for class reviews
const classReviewStore = {
  entries: [],
  activeCategory: "all",
  activeEntry: null,
  loaded: false,
};

export function useClassReviews() {
  async function fetchClassReviews() {
    if (classReviewStore.loaded) return classReviewStore.entries;

    try {
      const [achRes, profRes] = await Promise.all([
        listAchievementReviewRequests(),
        listProfileReviewRequests(),
      ]);
      const achList = (achRes.data || []).map(r => ({ ...r, resourceType: 'achievement' }));
      const profList = (profRes.data || []).map(r => ({ ...r, resourceType: 'profile' }));

      // Filter by current user's class
      const myClass = getCurrentUserClass();
      classReviewStore.entries = [...achList, ...profList].filter(r => {
        if (r.status !== 'pending') return false;
        const requesterClass = r.requester?.className;
        if (!requesterClass || !myClass) return false;
        return requesterClass.trim() === myClass.trim();
      });
      classReviewStore.loaded = true;
    } catch (e) {
      console.error(e);
    }

    return classReviewStore.entries;
  }

  function getCurrentUserClass() {
    try {
      const user = JSON.parse(localStorage.getItem('bdai_sc_user') || '{}');
      return user.className || null;
    } catch {
      return null;
    }
  }

  function getEntries() {
    return classReviewStore.entries;
  }

  function getPendingEntries() {
    const myClass = getCurrentUserClass();
    if (!myClass) return [];

    return classReviewStore.entries.filter(r => {
      if (r.status !== 'pending') return false;
      const requesterClass = r.requester?.className;
      if (!requesterClass) return false;
      return requesterClass.trim() === myClass.trim();
    });
  }

  function setActiveCategory(category) {
    classReviewStore.activeCategory = category;
  }

  function getActiveCategory() {
    return classReviewStore.activeCategory;
  }

  function findEntryById(entryId) {
    // entryId format: "id-resourceType"
    const [id, type] = String(entryId).split('-');
    return classReviewStore.entries.find(r =>
      String(r.id) === String(id) && r.resourceType === type
    ) || null;
  }

  return {
    fetchClassReviews,
    getEntries,
    getPendingEntries,
    setActiveCategory,
    getActiveCategory,
    findEntryById,
  };
}
