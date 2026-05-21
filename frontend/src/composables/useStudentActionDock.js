import { onMounted, onUnmounted, ref } from "vue";

const MOBILE_QUERY = "(max-width: 840px)";
/** 浮层底边与胶囊顶边之间的间距 */
const GAP_ABOVE_CAPSULE_PX = 40;
const DOCK_ESTIMATE_HEIGHT_PX = 48;

function queryCapsule() {
  return document.querySelector(".mobile-capsule");
}

function isLayoutVisible(el) {
  if (!el) {
    return false;
  }
  const style = window.getComputedStyle(el);
  if (style.display === "none" || style.visibility === "hidden") {
    return false;
  }
  const rect = el.getBoundingClientRect();
  return rect.width > 0 && rect.height > 0;
}

function queryVisibleActionDock() {
  const docks = document.querySelectorAll(".student-action-dock");
  let best = null;
  let bestBottom = -1;

  docks.forEach((dock) => {
    const rect = dock.getBoundingClientRect();
    if (rect.width < 1 || rect.height < 1) {
      return;
    }
    if (rect.bottom > bestBottom) {
      bestBottom = rect.bottom;
      best = dock;
    }
  });

  return best;
}

function resolveDesktopBottom() {
  const footer = document.querySelector(".dashboard-footer-wrap");
  if (!footer) {
    return "calc(24px + env(safe-area-inset-bottom, 0px))";
  }
  const footerRect = footer.getBoundingClientRect();
  const viewH = window.innerHeight;
  if (footerRect.top < viewH) {
    return `${viewH - footerRect.top + 16}px`;
  }
  return "calc(24px + env(safe-area-inset-bottom, 0px))";
}

function resolveMobileTop() {
  const capsule = queryCapsule();
  if (!isLayoutVisible(capsule)) {
    return null;
  }

  const capsuleRect = capsule.getBoundingClientRect();
  const dock = queryVisibleActionDock();
  const dockHeight = dock?.offsetHeight || DOCK_ESTIMATE_HEIGHT_PX;
  const topPx = capsuleRect.top - GAP_ABOVE_CAPSULE_PX - dockHeight;

  return `${Math.max(8, Math.round(topPx))}px`;
}

function nudgeUpIfOverlapping(styleRef) {
  const capsule = queryCapsule();
  const dock = queryVisibleActionDock();
  if (!isLayoutVisible(capsule) || !dock) {
    return;
  }

  const capsuleRect = capsule.getBoundingClientRect();
  const dockRect = dock.getBoundingClientRect();
  const limitBottom = capsuleRect.top - GAP_ABOVE_CAPSULE_PX;
  const overlap = dockRect.bottom - limitBottom;

  if (overlap <= 0.5) {
    return;
  }

  const currentTop = Number.parseFloat(styleRef.value.top);
  if (Number.isNaN(currentTop)) {
    return;
  }

  styleRef.value = {
    top: `${Math.round(currentTop - overlap - 4)}px`,
    bottom: "auto",
  };
}

export function useStudentActionDock() {
  const actionDockStyle = ref({
    bottom: `calc(${DOCK_ESTIMATE_HEIGHT_PX + 72}px + env(safe-area-inset-bottom, 0px))`,
    top: "auto",
  });

  let resizeObserver = null;
  const observedDocks = new Set();
  let mobileMedia = null;

  function applyPosition() {
    const isMobile = window.matchMedia(MOBILE_QUERY).matches;

    if (!isMobile) {
      actionDockStyle.value = {
        bottom: resolveDesktopBottom(),
        top: "auto",
      };
      return;
    }

    const top = resolveMobileTop();
    if (top == null) {
      actionDockStyle.value = {
        bottom: resolveDesktopBottom(),
        top: "auto",
      };
      return;
    }

    actionDockStyle.value = { top, bottom: "auto" };
  }

  function updateActionDockPosition() {
    applyPosition();

    if (!window.matchMedia(MOBILE_QUERY).matches) {
      return;
    }

    requestAnimationFrame(() => {
      applyPosition();
      nudgeUpIfOverlapping(actionDockStyle);
      requestAnimationFrame(() => {
        applyPosition();
        nudgeUpIfOverlapping(actionDockStyle);
      });
    });
  }

  function scheduleUpdate() {
    requestAnimationFrame(updateActionDockPosition);
  }

  function syncDockObservers() {
    if (!resizeObserver) {
      return;
    }

    document.querySelectorAll(".student-action-dock").forEach((dock) => {
      if (!observedDocks.has(dock)) {
        resizeObserver.observe(dock);
        observedDocks.add(dock);
      }
    });
  }

  onMounted(() => {
    window.addEventListener("scroll", scheduleUpdate, { passive: true });
    window.addEventListener("resize", scheduleUpdate);

    mobileMedia = window.matchMedia(MOBILE_QUERY);
    mobileMedia.addEventListener("change", scheduleUpdate);

    if (typeof ResizeObserver !== "undefined") {
      resizeObserver = new ResizeObserver(() => {
        syncDockObservers();
        scheduleUpdate();
      });
      const capsule = queryCapsule();
      if (capsule) {
        resizeObserver.observe(capsule);
      }
    }

    scheduleUpdate();
  });

  onUnmounted(() => {
    window.removeEventListener("scroll", scheduleUpdate);
    window.removeEventListener("resize", scheduleUpdate);
    mobileMedia?.removeEventListener("change", scheduleUpdate);
    resizeObserver?.disconnect();
    resizeObserver = null;
    observedDocks.clear();
  });

  function observeActionDock() {
    syncDockObservers();
    scheduleUpdate();
  }

  return {
    actionDockStyle,
    updateActionDockBottom: scheduleUpdate,
    observeActionDock,
  };
}
