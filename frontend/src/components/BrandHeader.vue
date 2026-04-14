<template>
  <header class="brand-header" ref="headerRef">
    <!-- Left: Logo + Wordmark -->
    <div class="brand-logo-group">
      <img
        src="/assets/icons/xylogo.png"
        alt="XY"
        class="brand-logo-icon"
      />
      <img
        src="/assets/icons/xylogo_with_text.png"
        alt="大数据与人工智能学院"
        class="brand-logo-text"
      />
    </div>

    <!-- Right: User Profile Chip -->
    <div class="brand-profile-chip">
      <!-- Avatar -->
      <div class="chip-avatar">
        <img
          v-if="profile.avatarUrl"
          :src="resolveMediaUrl(profile.avatarUrl)"
          alt="头像"
        />
        <span v-else>{{ avatarText }}</span>
      </div>

      <!-- Name & Role -->
      <div class="chip-info">
        <span class="chip-name">
          {{ profile.displayName || profile.username || "同学" }}
        </span>
        <span class="chip-role">{{ roleLabel }}</span>
      </div>

      <!-- Divider -->
      <div class="chip-divider" aria-hidden="true" />

      <!-- Actions -->
      <div class="chip-actions">
        <button
          v-if="profile.role === 'ADMIN'"
          class="chip-btn chip-btn--primary"
          type="button"
          aria-label="管理后台"
          @click="$emit('menu-click', 'admin')"
        >
          <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
            <path d="M3 5h14M3 10h14M3 15h8" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
          </svg>
          <span>管理</span>
        </button>
        <button
          class="chip-btn chip-btn--ghost"
          type="button"
          aria-label="设置"
          @click="$emit('settings-click')"
        >
          <img src="/assets/icons/settings.svg" alt="设置" class="settings-icon" />
        </button>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from "vue";
import { resolveMediaUrl } from "../utils/media";

const props = defineProps({
  profile: {
    type: Object,
    required: true,
  },
});

defineEmits(["settings-click", "menu-click"]);

const headerRef = ref(null);
const isHidden = ref(false);

// Scroll-out threshold: 20% of viewport height
const SCROLL_THRESHOLD = 0.2; // 20vh

function onScroll() {
  const scrollY = window.scrollY;
  const vh = window.innerHeight;

  // Phase 1: scrolling 0 → THRESHOLD — header stays visible (sticky)
  // Phase 2: scrolling THRESHOLD → 100vh — header slides out proportionally
  const progress = Math.min(1, scrollY / vh - SCROLL_THRESHOLD) / (1 - SCROLL_THRESHOLD);

  if (!headerRef.value) return;

  if (progress >= 1) {
    // Fully scrolled past — snap to hidden
    headerRef.value.style.transform = "translateY(-100%)";
    headerRef.value.style.opacity = "0";
    isHidden.value = true;
  } else if (progress > 0) {
    // Scroll-linked slide-out
    headerRef.value.style.transform = `translateY(-${progress * 100}%)`;
    headerRef.value.style.opacity = `${1 - progress}`;
    isHidden.value = false;
  } else {
    // Initial sticky zone
    headerRef.value.style.transform = "";
    headerRef.value.style.opacity = "";
    isHidden.value = false;
  }
}

onMounted(() => {
  window.addEventListener("scroll", onScroll, { passive: true });
});

onUnmounted(() => {
  window.removeEventListener("scroll", onScroll);
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
/* ── Header Shell ──────────────────────────────────────── */
.brand-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 34px;
  height: 86px;
  background: linear-gradient(135deg, #1a0a2e 0%, #2d1050 50%, #5c0f7a 100%);
  border: 1px solid rgba(212, 156, 59, 0.15);
  border-radius: 20px;
  position: relative;
  overflow: hidden;
  z-index: 10;
  /* GPU 加速，避免重排 */
  will-change: transform, opacity;
  /* 隐出末段的微淡 */
  transition: opacity 60ms ease;
}

/* Subtle top highlight line */
.brand-header::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(
    90deg,
    transparent 0%,
    rgba(255, 255, 255, 0.2) 30%,
    rgba(255, 255, 255, 0.25) 50%,
    rgba(255, 255, 255, 0.2) 70%,
    transparent 100%
  );
}

/* ── Logo Group ────────────────────────────────────────── */
.brand-logo-group {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.brand-logo-icon {
  height: 72px;
  width: auto;
  object-fit: contain;
  filter: drop-shadow(0 2px 8px rgba(100, 12, 114, 0.4));
}

.brand-logo-text {
  height: 43px;
  width: auto;
  object-fit: contain;
  opacity: 0.95;
}

/* ── Profile Chip ──────────────────────────────────────── */
.brand-profile-chip {
  display: flex;
  align-items: center;
  gap: 0;
  height: 58px;
  padding: 7px 10px 7px 7px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 999px;
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  box-shadow:
    0 4px 16px rgba(0, 0, 0, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
  transition: background 200ms ease, box-shadow 200ms ease;
}

.brand-profile-chip:hover {
  background: rgba(255, 255, 255, 0.11);
  box-shadow:
    0 6px 20px rgba(0, 0, 0, 0.25),
    inset 0 1px 0 rgba(255, 255, 255, 0.15);
}

/* ── Avatar ────────────────────────────────────────────── */
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
  color: rgba(255, 255, 255, 0.95);
  line-height: 1.2;
  letter-spacing: 0.01em;
}

.chip-role {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.5);
  line-height: 1;
}

/* ── Divider ───────────────────────────────────────────── */
.chip-divider {
  width: 1px;
  height: 29px;
  background: rgba(255, 255, 255, 0.12);
  margin: 0 4px;
  flex-shrink: 0;
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
  color: rgba(255, 255, 255, 0.6);
}

.chip-btn--ghost:hover {
  background: rgba(255, 255, 255, 0.12);
  color: rgba(255, 255, 255, 0.9);
}

.settings-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
  filter: brightness(0) invert(1);
}
</style>
