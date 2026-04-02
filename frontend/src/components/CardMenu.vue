<template>
  <section ref="menuCardRef" class="menu-card">
    <div
      class="menu-active-indicator"
      :class="{ visible: indicatorVisible, ready: indicatorReady }"
      :style="indicatorStyle"
      aria-hidden="true"
    ></div>
    <template v-for="item in menuItems" :key="item.key">
      <!-- achievements drawer -->
      <div
        v-if="item.key === 'achievements'"
        class="menu-drawer"
        :class="{ open: achievementsOpen }"
      >
        <button
          class="menu-item menu-drawer-trigger"
          :class="{
            active: activeMenu === item.key,
            disabled: !isMenuEnabled(item.key),
          }"
          type="button"
          :disabled="!isMenuEnabled(item.key)"
          :ref="(el) => setMenuItemRef(item.key, el)"
          @click="handleAchievementsClick"
        >
          <span>{{ item.label }}</span>
          <span v-if="showAchievementsDrawer" class="menu-drawer-caret" aria-hidden="true"></span>
        </button>
        <transition name="menu-drawer-panel">
          <div
            v-show="showAchievementsDrawer && drawerVisible"
            class="menu-drawer-panel"
            :class="drawerAnimClass"
          >
            <div
              class="menu-drawer-indicator"
              :style="drawerIndicatorStyle"
              aria-hidden="true"
            ></div>
            <button
              v-for="(entry, index) in achievementEntries"
              :key="entry.key"
              class="menu-drawer-item"
              :class="{ active: activeAchievement === entry.key }"
              type="button"
              :style="{ '--drawer-index': index }"
              @click="$emit('achievement-entry-click', entry.key)"
            >
              {{ entry.label }}
            </button>
          </div>
        </transition>
      </div>
      <!-- simple menu item -->
      <button
        v-else
        class="menu-item"
        :class="{
          active: activeMenu === item.key,
          disabled: !isMenuEnabled(item.key),
        }"
        type="button"
        :disabled="!isMenuEnabled(item.key)"
        :ref="(el) => setMenuItemRef(item.key, el)"
        @click="handleMenuClick(item.key)"
      >
        {{ item.label }}
      </button>
    </template>
  </section>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { filterMenuItemsByRole, isMenuEnabled } from "../constants/menu";

const props = defineProps({
  profile: {
    type: Object,
    required: true,
  },
  activeMenu: {
    type: String,
    default: "",
  },
  activeAchievement: {
    type: String,
    default: "all",
  },
  showAchievementsDrawer: {
    type: Boolean,
    default: true,
  },
  achievementsOpen: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits([
  "menu-click",
  "achievement-entry-click",
  "toggle-achievements",
]);

const menuItems = computed(() => filterMenuItemsByRole(props.profile.role));

const achievementEntries = [
  { key: "all", label: "全部" },
  { key: "contest", label: "学科竞赛、文体艺术" },
  { key: "paper", label: "发表学术论文" },
  { key: "journal", label: "发表期刊作品" },
  { key: "patent", label: "专利(著作权)授权数(项)" },
  { key: "certificate", label: "职业资格证书" },
  { key: "research", label: "学生参与教师科研项目情况" },
  { key: "works", label: "创作、表演的代表性作品" },
  { key: "doubleHundred", label: "双百工程" },
  { key: "ieerTraining", label: "大学生创新创业训练计划项目" },
];

const activeAchievementIndex = computed(() => {
  const index = achievementEntries.findIndex(
    (entry) => entry.key === props.activeAchievement,
  );
  return index === -1 ? 0 : index;
});

const drawerIndicatorStyle = computed(() => ({
  transform: `translateY(calc(${activeAchievementIndex.value} * (var(--drawer-item-height) + var(--drawer-item-gap))))`,
}));

const menuItemRefs = new Map();
const menuCardRef = ref(null);
const indicatorStyle = ref({});
const indicatorVisible = ref(false);
const indicatorReady = ref(false);

// 抽屉开闭动画状态
const drawerVisible = ref(false);
const drawerAnimClass = ref("");
let closeTimer = null;
let pendingNavigationTimer = null;
let drawerSyncTimer = null;
let resizeFrame = 0;
let indicatorLockTimer = null;
const indicatorLocked = ref(false);
const pendingExternalKey = ref("");

function setMenuItemRef(key, el) {
  if (el) {
    menuItemRefs.set(key, el);
    return;
  }
  menuItemRefs.delete(key);
}

function applyIndicator(key, immediate = false) {
  const el = menuItemRefs.get(key);
  const container = menuCardRef.value;
  if (!el || !container) {
    indicatorVisible.value = false;
    return;
  }
  const itemRect = el.getBoundingClientRect();
  const containerRect = container.getBoundingClientRect();
  indicatorVisible.value = true;
  indicatorReady.value = !immediate;
  indicatorStyle.value = {
    transform: `translateY(${itemRect.top - containerRect.top}px)`,
    height: `${itemRect.height}px`,
  };
}

async function syncIndicator(immediate = false) {
  await nextTick();
  if (!props.activeMenu) {
    indicatorVisible.value = false;
    return;
  }
  applyIndicator(props.activeMenu, immediate);
  sessionStorage.removeItem("gcsc_menu_indicator_from");
}

function queueIndicatorSync(immediate = false, delay = 0) {
  clearTimeout(drawerSyncTimer);
  drawerSyncTimer = setTimeout(() => {
    if (indicatorLocked.value || pendingExternalKey.value) {
      return;
    }
    syncIndicator(immediate);
  }, delay);
}

function lockIndicator(duration = 260) {
  indicatorLocked.value = true;
  clearTimeout(indicatorLockTimer);
  indicatorLockTimer = setTimeout(() => {
    indicatorLocked.value = false;
    syncIndicator(false);
  }, duration);
}

function openDrawer() {
  clearTimeout(closeTimer);
  drawerVisible.value = false;
  drawerAnimClass.value = "";
  queueMicrotask(() => {
    drawerVisible.value = true;
    requestAnimationFrame(() => {
      drawerAnimClass.value = "is-open";
    });
    setTimeout(() => {
      drawerAnimClass.value = "";
    }, 260);
  });
}

function closeDrawer() {
  drawerAnimClass.value = "is-closing";
  clearTimeout(closeTimer);
  closeTimer = setTimeout(() => {
    drawerAnimClass.value = "";
    drawerVisible.value = false;
  }, 260);
}

watch(
  () => props.achievementsOpen,
  (open) => {
    if (!props.showAchievementsDrawer) return;
    if (indicatorLocked.value || pendingExternalKey.value) {
      return;
    }
    if (open) {
      openDrawer();
      queueIndicatorSync(false, 40);
      queueIndicatorSync(false, 250);
    } else {
      closeDrawer();
      queueIndicatorSync(false, 40);
      queueIndicatorSync(false, 230);
    }
  },
);

onMounted(() => {
  if (props.achievementsOpen && props.showAchievementsDrawer) {
    openDrawer();
  }
  syncIndicator(true);
});

onBeforeUnmount(() => {
  clearTimeout(closeTimer);
  clearTimeout(pendingNavigationTimer);
  clearTimeout(drawerSyncTimer);
  clearTimeout(indicatorLockTimer);
  cancelAnimationFrame(resizeFrame);
  if (typeof window !== "undefined") {
    window.removeEventListener("resize", handleResize);
  }
});

watch(
  () => props.activeMenu,
  async (nextKey, prevKey) => {
    if (!nextKey) {
      indicatorVisible.value = false;
      return;
    }
    if (pendingExternalKey.value) {
      return;
    }
    if (prevKey === "achievements" && nextKey !== "achievements") {
      indicatorVisible.value = false;
      queueIndicatorSync(false, 270);
      return;
    }
    if (nextKey === "achievements") {
      queueIndicatorSync(false, 30);
      queueIndicatorSync(false, 220);
      return;
    }
    await syncIndicator(false);
  },
);

watch(menuItems, () => {
  syncIndicator(true);
});

watch(
  () => props.activeAchievement,
  () => {
    queueIndicatorSync(false, 0);
  },
);

if (typeof window !== "undefined") {
  window.addEventListener("resize", handleResize, { passive: true });
}

function handleResize() {
  cancelAnimationFrame(resizeFrame);
  resizeFrame = requestAnimationFrame(() => {
    syncIndicator(true);
  });
}

function handleMenuClick(key) {
  sessionStorage.setItem("gcsc_menu_indicator_from", props.activeMenu || "");
  clearTimeout(pendingNavigationTimer);
  if (
    key !== "achievements" &&
    props.showAchievementsDrawer &&
    props.achievementsOpen &&
    props.activeMenu === "achievements" &&
    drawerVisible.value
  ) {
    pendingExternalKey.value = key;
    lockIndicator(300);
    indicatorVisible.value = false;
    closeDrawer();
    pendingNavigationTimer = setTimeout(() => {
      emit("menu-click", key);
      requestAnimationFrame(() => {
        requestAnimationFrame(() => {
          pendingExternalKey.value = "";
          syncIndicator(false);
        });
      });
    }, 270);
    return;
  }
  emit("menu-click", key);
}

function handleAchievementsClick() {
  sessionStorage.setItem("gcsc_menu_indicator_from", props.activeMenu || "");
  if (props.showAchievementsDrawer) {
    emit("toggle-achievements");
  } else {
    emit("menu-click", "achievements");
  }
}
</script>
