<template>
  <div class="brand-header-wrapper">
    <header class="brand-header" ref="headerRef">
      <!-- Left: Logo + Wordmark -->
      <div class="brand-logo-group">
        <img src="/assets/icons/xylogo.png" alt="XY" class="brand-logo-icon" />
        <img
          src="/assets/icons/xylogo_with_text.png"
          alt="大数据与人工智能学院"
          class="brand-logo-text"
        />
      </div>
    </header>

    <!-- Right: User Profile Chip (outside header to avoid transform taint) -->
    <div
      ref="chipRef"
      class="brand-profile-chip"
      :class="{ 'chip-expanded': expanded }"
      @click="onChipClick"
      @pointerdown="onChipInteraction"
    >
      <!-- Avatar / Click indicator -->
      <div class="chip-avatar-wrap">
        <Transition name="chip-swap" mode="out-in">
          <div v-if="!isMobile || expanded" key="avatar" class="chip-avatar">
            <img
              v-if="profile.avatarUrl"
              :src="resolveMediaUrl(profile.avatarUrl)"
              alt="头像"
            />
            <span v-else>{{ avatarText }}</span>
          </div>
          <img
            v-else
            key="click"
            src="/assets/icons/click.svg"
            alt="点击展开"
            class="chip-click-icon"
          />
        </Transition>
      </div>

      <!-- Name & Role (always visible) -->
      <div class="chip-info">
        <span class="chip-name">
          {{ profile.displayName || profile.username || "同学" }}
        </span>
        <span class="chip-role">{{ roleLabel }}</span>
      </div>

      <!-- Divider + Actions (collapsed on mobile) -->
      <div class="chip-tail" :class="{ 'chip-tail--visible': expanded || !isMobile }">
        <div class="chip-divider" aria-hidden="true" />

        <div class="chip-actions">
          <button
            v-if="profile.role === 'ADMIN'"
            class="chip-btn chip-btn--primary"
            type="button"
            aria-label="管理后台"
            @click.stop="$emit('menu-click', 'admin')"
          >
            <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
              <path
                d="M3 5h14M3 10h14M3 15h8"
                stroke="currentColor"
                stroke-width="1.6"
                stroke-linecap="round"
              />
            </svg>
            <span>管理</span>
          </button>
          <button
            class="chip-btn chip-btn--ghost"
            type="button"
            aria-label="设置"
            @click.stop="$emit('settings-click')"
          >
            <img
              src="/assets/icons/settings.svg"
              alt="设置"
              class="settings-icon"
            />
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
import { resolveMediaUrl } from "../utils/media";

const props = defineProps({
  profile: {
    type: Object,
    required: true,
  },
});

defineEmits(["settings-click", "menu-click"]);

const headerRef = ref(null);
const chipRef = ref(null);
const isHidden = ref(false);
const expanded = ref(false);
const isMobile = ref(false);
let collapseTimer = null;

const MOBILE_BREAKPOINT = 840;
const COLLAPSE_DELAY = 3000;

function checkMobile() {
  isMobile.value = window.innerWidth < MOBILE_BREAKPOINT;
}

function scheduleCollapse() {
  clearTimeout(collapseTimer);
  if (expanded.value && isMobile.value) {
    collapseTimer = setTimeout(() => {
      expanded.value = false;
    }, COLLAPSE_DELAY);
  }
}

function onChipClick() {
  if (!isMobile.value) return;
  expanded.value = !expanded.value;
}

function onChipInteraction() {
  scheduleCollapse();
}

function onDocumentClick(e) {
  if (!expanded.value || !isMobile.value) return;
  if (chipRef.value && !chipRef.value.contains(e.target)) {
    expanded.value = false;
  }
}

// Chip scroll-driven positioning
// Natural chip position within banner (banner 180px, padding-bottom 45px, chip 58px):
//   180 - 45 - 29 = 106px from viewport top
const CHIP_BANNER_TOP = 106;
const CHIP_FIXED_TOP = 20;
const COLOR_START = 40;  // scrollY where chip color starts leaving banner
const COLOR_END = 140;   // scrollY where chip is fully on light bg

// Scroll-out threshold: 20% of viewport height
const SCROLL_THRESHOLD = 0.2;

function lerp(a, b, t) {
  return a + (b - a) * Math.min(1, Math.max(0, t));
}

function lerpChannel(hexA, hexB, t) {
  // hexA, hexB like "ff", "2d"
  return Math.round(lerp(parseInt(hexA, 16), parseInt(hexB, 16), t));
}

function lerpHexColor(light, dark, t) {
  // light, dark: hex strings without #, e.g. "ffffff", "2d1a3e"
  const r = lerpChannel(light.substring(0, 2), dark.substring(0, 2), t);
  const g = lerpChannel(light.substring(2, 4), dark.substring(2, 4), t);
  const b = lerpChannel(light.substring(4, 6), dark.substring(4, 6), t);
  return `#${r.toString(16).padStart(2, '0')}${g.toString(16).padStart(2, '0')}${b.toString(16).padStart(2, '0')}`;
}

function updateChip(scrollY) {
  if (!chipRef.value) return;

  const safeTop = 'env(safe-area-inset-top, 0px)';
  const bannerTop = isMobile.value ? CHIP_BANNER_TOP : 75;
  const fixedTop = CHIP_FIXED_TOP;
  const rawTop = Math.max(fixedTop, bannerTop - scrollY);
  chipRef.value.style.top = `calc(${rawTop}px + ${safeTop})`;

  // Color transition: 0 = on dark banner, 1 = on light page
  const cp = (scrollY - COLOR_START) / (COLOR_END - COLOR_START);
  const t = Math.min(1, Math.max(0, cp));

  const nameColor = lerpHexColor('ffffff', '2d1a3e', t);
  const subColor = lerpHexColor('ffffff', '5c5266', t);
  const ghostColor = lerpHexColor('ffffff', '2d1a3e', t);
  const borderColor = t > 0.5
    ? `rgba(${Math.round(lerp(255, 0, t))}, ${Math.round(lerp(255, 0, t))}, ${Math.round(lerp(255, 0, t))}, ${lerp(0.18, 0.095, t).toFixed(3)})`
    : `rgba(255, 255, 255, ${lerp(0.18, 0.095, t).toFixed(3)})`;
  const bgAlpha = lerp(0.10, 0.68, t).toFixed(3);
  const shadowAlpha = lerp(0.30, 0.08, t).toFixed(3);
  const dividerAlpha = lerp(0.12, 0.08, t).toFixed(3);

  const el = chipRef.value;
  el.style.setProperty('--chip-name-color', nameColor);
  el.style.setProperty('--chip-sub-color', subColor);
  el.style.setProperty('--chip-ghost-color', ghostColor);
  el.style.setProperty('--chip-border-color', borderColor);
  el.style.setProperty('--chip-bg-alpha', bgAlpha);
  el.style.setProperty('--chip-shadow-alpha', shadowAlpha);
  el.style.setProperty('--chip-divider-alpha', dividerAlpha);

  // Settings icon: invert(1) = white on banner, invert(0) = dark on light bg
  el.style.setProperty('--chip-icon-invert', (1 - t).toFixed(3));
}

function onScroll() {
  const scrollY = window.scrollY;
  const vh = window.innerHeight;

  updateChip(scrollY);

  const progress =
    Math.min(1, scrollY / vh - SCROLL_THRESHOLD) / (1 - SCROLL_THRESHOLD);

  if (!headerRef.value) return;

  if (progress >= 1) {
    headerRef.value.style.transform = "translateY(-100%)";
    headerRef.value.style.opacity = "0";
    isHidden.value = true;
  } else if (progress > 0) {
    headerRef.value.style.transform = `translateY(-${progress * 100}%)`;
    headerRef.value.style.opacity = `${1 - progress}`;
    isHidden.value = false;
  } else {
    headerRef.value.style.transform = "";
    headerRef.value.style.opacity = "";
    isHidden.value = false;
  }
}

onMounted(() => {
  checkMobile();
  window.addEventListener("scroll", onScroll, { passive: true });
  window.addEventListener("resize", checkMobile);
  document.addEventListener("click", onDocumentClick);
});

onUnmounted(() => {
  clearTimeout(collapseTimer);
  window.removeEventListener("scroll", onScroll);
  window.removeEventListener("resize", checkMobile);
  document.removeEventListener("click", onDocumentClick);
});

watch(expanded, () => {
  if (expanded.value) {
    scheduleCollapse();
  } else {
    clearTimeout(collapseTimer);
  }
});

const avatarText = computed(() => {
  const name = props.profile.displayName || props.profile.username || "同学";
  return name.slice(0, 1).toUpperCase();
});

const roleLabel = computed(() => {
  if (props.profile.role === "ADMIN") return "管理员";
  if (props.profile.role === "TEACHER") return "教师";
  return "学生";
});
</script>

<style scoped>
/* ── Header Wrapper ──────────────────────────────────────── */
.brand-header-wrapper {
  margin: -28px -28px 0;
  overflow: hidden;
}

/* ── Header Shell ──────────────────────────────────────── */
.brand-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  padding: 0 30px 45px;
  height: 180px;
  background-image: url("/assets/statics/banner.png");
  background-size: cover;
  background-position: center;
  border-radius: 0;
  position: relative;
  overflow: hidden;
  isolation: isolate;
  z-index: 10;
  will-change: transform, opacity;
  transition: opacity 60ms ease;
}

/* Overlay: dark vignette → content-blend fade at bottom */
.brand-header::before {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    rgba(0, 0, 0, 0.12) 0%,
    rgba(0, 0, 0, 0.02) 10%,
    rgba(0, 0, 0, 0.18) 60%,
    rgba(0, 0, 0, 0.32) 70%,
    rgba(26, 10, 46, 0.6) 80%,
    rgb(247, 244, 250) 100%
  );
  z-index: 1;
}

/* ── Logo Group ────────────────────────────────────────── */
.brand-logo-group {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
  position: relative;
  z-index: 2;
}

.brand-logo-icon {
  height: 62px;
  width: auto;
  object-fit: contain;
  filter: drop-shadow(0 2px 10px rgba(0, 0, 0, 0.5));
}

.brand-logo-text {
  height: 37px;
  width: auto;
  object-fit: contain;
  opacity: 0.95;
  filter: drop-shadow(0 2px 8px rgba(0, 0, 0, 0.45));
}

/* ── Profile Chip ──────────────────────────────────────── */
.brand-profile-chip {
  position: fixed;
  /* top set by JS on scroll; CSS sets initial position before JS runs */
  top: calc(75px + env(safe-area-inset-top, 0px));
  right: calc(28px + env(safe-area-inset-right, 0px));
  z-index: 35;
  display: flex;
  align-items: center;
  gap: 0;
  height: 58px;
  padding: 7px 10px 7px 7px;
  /* custom properties override as chip leaves banner */
  background: rgba(255, 255, 255, var(--chip-bg-alpha, 0.1));
  border: 1px solid var(--chip-border-color, rgba(255, 255, 255, 0.18));
  border-radius: 999px;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  box-shadow:
    0 4px 20px rgba(0, 0, 0, var(--chip-shadow-alpha, 0.3)),
    inset 0 1px 0 rgba(255, 255, 255, 0.12);
  transition:
    background 200ms ease,
    box-shadow 200ms ease,
    border-color 200ms ease;
  touch-action: manipulation;
}

.brand-profile-chip:hover {
  background: rgba(255, 255, 255, calc(var(--chip-bg-alpha, 0.1) + 0.04));
  box-shadow:
    0 6px 20px rgba(0, 0, 0, calc(var(--chip-shadow-alpha, 0.3) * 0.85)),
    inset 0 1px 0 rgba(255, 255, 255, 0.15);
}

/* ── Avatar ────────────────────────────────────────────── */
.chip-avatar-wrap {
  flex-shrink: 0;
}

.chip-avatar {
  width: 43px;
  height: 43px;
  border-radius: 999px;
  background: rgba(212, 156, 59, 0.25);
  display: grid;
  place-items: center;
  font-weight: 700;
  font-size: 17px;
  color: #f5d89a;
  overflow: hidden;
  flex-shrink: 0;
  border: 1.5px solid rgba(212, 156, 59, 0.35);
  transition: border-color 200ms ease;
}

.chip-click-icon {
  width: 43px;
  height: 43px;
  flex-shrink: 0;
  opacity: 0.65;
}

/* ── Chip content swap transition ─────────────────────── */
.chip-swap-enter-active,
.chip-swap-leave-active {
  transition:
    opacity 200ms ease,
    transform 200ms ease;
}

.chip-swap-enter-from {
  opacity: 0;
  transform: scale(0.85);
}

.chip-swap-leave-to {
  opacity: 0;
  transform: scale(0.85);
}

.brand-profile-chip:hover .chip-avatar {
  border-color: rgba(212, 156, 59, 0.55);
}

.chip-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* ── Name & Role ───────────────────────────────────────── */
.chip-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0 12px 0 12px;
  gap: 2px;
}

.chip-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--chip-name-color, rgba(255, 255, 255, 0.95));
  line-height: 1.2;
  letter-spacing: 0.01em;
}

.chip-role {
  font-size: 13px;
  color: var(--chip-sub-color, rgba(255, 255, 255, 0.5));
  line-height: 1;
}

/* ── Divider ───────────────────────────────────────────── */
.chip-divider {
  width: 1px;
  height: 29px;
  background: rgba(255, 255, 255, var(--chip-divider-alpha, 0.12));
  margin: 0 4px;
  flex-shrink: 0;
}

/* ── Tail (divider + actions, collapsible on mobile) ──── */
.chip-tail {
  display: flex;
  align-items: center;
  overflow: hidden;
  max-width: 240px;
  opacity: 1;
  transition:
    max-width 420ms cubic-bezier(0.22, 1, 0.36, 1),
    opacity 340ms ease;
}

.chip-tail:not(.chip-tail--visible) {
  max-width: 0;
  opacity: 0;
}

/* ── Actions ──────────────────────────────────────────── */
.chip-actions {
  display: flex;
  align-items: center;
  gap: 5px;
  padding-left: 5px;
}

.chip-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border-radius: 999px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  border: none;
  transition:
    background 180ms ease,
    color 180ms ease,
    transform 120ms ease;
  -webkit-tap-highlight-color: transparent;
}

.chip-btn:active {
  transform: scale(0.96);
}

/* Primary (Manage) */
.chip-btn--primary {
  height: 36px;
  padding: 0 14px;
  background: linear-gradient(135deg, #d49a3b 0%, #c8892a 100%);
  color: #fff;
  box-shadow:
    0 2px 8px rgba(212, 156, 59, 0.35),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.chip-btn--primary:hover {
  background: linear-gradient(135deg, #e0a844 0%, #d49a3b 100%);
  box-shadow:
    0 4px 12px rgba(212, 156, 59, 0.45),
    inset 0 1px 0 rgba(255, 255, 255, 0.25);
}

.chip-btn--primary svg {
  width: 17px;
  height: 17px;
  flex-shrink: 0;
}

/* Ghost (Settings) */
.chip-btn--ghost {
  width: 36px;
  height: 36px;
  padding: 0;
  background: rgba(255, 255, 255, 0.06);
  color: var(--chip-ghost-color, rgba(255, 255, 255, 0.6));
}

.chip-btn--ghost:hover {
  background: rgba(255, 255, 255, 0.12);
  color: var(--chip-ghost-color, rgba(255, 255, 255, 0.9));
  opacity: 0.85;
}

.settings-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
  /* invert(1)=white on dark banner → invert(0)=dark on light bg */
  filter: brightness(0) invert(var(--chip-icon-invert, 1));
}

/* ── Mobile: shrink chip ────────────────────────────── */
@media (max-width: 840px) {
  .brand-header-wrapper {
    margin: -20px -20px 0;
  }

  .brand-logo-group {
    display: none;
  }

  .brand-profile-chip {
    top: calc(106px + env(safe-area-inset-top, 0px));
    right: calc(20px + env(safe-area-inset-right, 0px));
  }

  .brand-profile-chip:not(.chip-expanded) {
    cursor: pointer;
    -webkit-tap-highlight-color: transparent;
    user-select: none;
  }
}

@media (max-width: 720px) {
  .brand-profile-chip {
    height: 50px;
    padding: 5px 9px 5px 5px;
  }

  .chip-avatar {
    width: 38px;
    height: 38px;
    font-size: 16px;
  }

  .chip-click-icon {
    width: 38px;
    height: 38px;
  }

  .chip-info {
    padding: 0 10px 0 10px;
    gap: 1px;
  }

  .chip-name {
    font-size: 14px;
  }

  .chip-role {
    font-size: 11px;
  }

  .chip-divider {
    height: 24px;
    margin: 0 3px;
  }

  .chip-btn--primary {
    height: 32px;
    padding: 0 12px;
    font-size: 13px;
    gap: 5px;
  }

  .chip-btn--primary svg {
    width: 15px;
    height: 15px;
  }

  .chip-btn--ghost {
    width: 32px;
    height: 32px;
  }

  .settings-icon {
    width: 16px;
    height: 16px;
  }

  .chip-actions {
    gap: 4px;
    padding-left: 3px;
  }
}

@media (max-width: 480px) {
  .brand-header-wrapper {
    margin: -14px -14px 0;
  }

  .brand-profile-chip {
    height: 44px;
    padding: 4px 7px 4px 4px;
    right: calc(14px + env(safe-area-inset-right, 0px));
  }

  .chip-avatar {
    width: 34px;
    height: 34px;
    font-size: 14px;
  }

  .chip-click-icon {
    width: 34px;
    height: 34px;
  }

  .chip-info {
    padding: 0 7px 0 7px;
  }

  .chip-name {
    font-size: 13px;
  }

  .chip-role {
    font-size: 10px;
  }

  .chip-divider {
    height: 20px;
  }

  .chip-btn--primary {
    height: 30px;
    padding: 0 10px;
    font-size: 12px;
    gap: 4px;
  }

  .chip-btn--primary svg {
    width: 14px;
    height: 14px;
  }

  .chip-btn--ghost {
    width: 30px;
    height: 30px;
  }

  .settings-icon {
    width: 15px;
    height: 15px;
  }

  .chip-actions {
    gap: 3px;
    padding-left: 2px;
  }
}
</style>
