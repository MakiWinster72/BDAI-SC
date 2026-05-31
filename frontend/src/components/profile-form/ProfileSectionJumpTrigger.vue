<script setup>
defineProps({
  variant: {
    type: String,
    default: "ghost",
    validator: (value) => ["ghost", "capsule"].includes(value),
  },
  active: {
    type: Boolean,
    default: false,
  },
  showLabel: {
    type: Boolean,
    default: false,
  },
});

defineEmits(["click"]);
</script>

<template>
  <button
    type="button"
    class="ghost-button profile-jump-trigger"
    :class="[
      variant === 'capsule'
        ? 'profile-jump-trigger--capsule'
        : 'profile-jump-trigger--ghost',
      { 'profile-jump-trigger--active': active },
    ]"
    aria-label="打开页面大纲"
    :aria-expanded="active"
    @click="$emit('click')"
  >
    <span class="profile-jump-trigger-icon" aria-hidden="true">
      <svg
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="1.8"
        stroke-linecap="round"
        stroke-linejoin="round"
      >
        <path d="M9 6h11M9 12h11M9 18h11" />
        <path d="M5 6h.01M5 12h.01M5 18h.01" />
      </svg>
    </span>
    <span v-if="showLabel" class="profile-jump-trigger-label">标题</span>
  </button>
</template>

<style src="@/assets/styles/profile-section-jump-trigger.css"></style>
