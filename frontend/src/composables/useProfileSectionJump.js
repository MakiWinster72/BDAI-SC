import { onMounted, onUnmounted, ref } from "vue";
import { getMyInfoSectionJumpItems } from "@/config/myInfoSectionJumpConfig";

const MOBILE_QUERY = "(max-width: 840px)";

/** 与 .profile-jump-anchor 的 scroll-margin-top 对齐 */
const JUMP_SCROLL_OFFSET_PX = 72;

/**
 * @param {Element | null} element
 * @returns {Element}
 */
function findScrollableAncestor(element) {
  let node = element?.parentElement ?? null;

  while (node && node !== document.body && node !== document.documentElement) {
    const style = window.getComputedStyle(node);
    const overflowY = style.overflowY;
    const scrollableY =
      overflowY === "auto" ||
      overflowY === "scroll" ||
      overflowY === "overlay";

    if (scrollableY && node.scrollHeight > node.clientHeight + 1) {
      return node;
    }
    node = node.parentElement;
  }

  return document.scrollingElement || document.documentElement;
}

/**
 * @returns {{
 *   open: import('vue').Ref<boolean>,
 *   isMobile: import('vue').Ref<boolean>,
 *   items: ReturnType<typeof getMyInfoSectionJumpItems>,
 *   toggle: () => void,
 *   close: () => void,
 *   jumpTo: (sectionId: string) => void,
 * }}
 */
export function useProfileSectionJump() {
  const open = ref(false);
  const isMobile = ref(false);
  const items = getMyInfoSectionJumpItems();
  /** @type {MediaQueryList | null} */
  let media = null;

  function syncMobile() {
    isMobile.value = window.matchMedia(MOBILE_QUERY).matches;
  }

  function close() {
    open.value = false;
  }

  function toggle() {
    open.value = !open.value;
  }

  function jumpTo(sectionId) {
    const target = document.getElementById(sectionId);
    if (!target) {
      return;
    }

    const scrollRoot = findScrollableAncestor(target);
    const scrollingElement = document.scrollingElement;
    const usesWindowScroll =
      scrollRoot === document.documentElement ||
      scrollRoot === document.body ||
      scrollRoot === scrollingElement;

    if (usesWindowScroll) {
      const top =
        window.scrollY +
        target.getBoundingClientRect().top -
        JUMP_SCROLL_OFFSET_PX;
      window.scrollTo({
        top: Math.max(0, top),
        behavior: "smooth",
      });
    } else {
      const rootRect = scrollRoot.getBoundingClientRect();
      const targetRect = target.getBoundingClientRect();
      const nextTop =
        scrollRoot.scrollTop +
        (targetRect.top - rootRect.top) -
        JUMP_SCROLL_OFFSET_PX;
      scrollRoot.scrollTo({
        top: Math.max(0, nextTop),
        behavior: "smooth",
      });
    }

    if (isMobile.value) {
      close();
    }
  }

  onMounted(() => {
    media = window.matchMedia(MOBILE_QUERY);
    syncMobile();
    media.addEventListener("change", syncMobile);
  });

  onUnmounted(() => {
    media?.removeEventListener("change", syncMobile);
  });

  return {
    open,
    isMobile,
    items,
    toggle,
    close,
    jumpTo,
  };
}
