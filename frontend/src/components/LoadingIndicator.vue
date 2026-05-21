<script setup>
defineProps({
  label: {
    type: String,
    default: "加载中…",
  },
  size: {
    type: String,
    default: "md",
    validator: (value) => ["sm", "md", "lg"].includes(value),
  },
  block: {
    type: Boolean,
    default: false,
  },
});
</script>

<template>
  <div
    class="loading-indicator"
    :class="[`is-${size}`, { 'is-block': block }]"
    role="status"
    aria-live="polite"
  >
    <svg
      class="loading-indicator-spinner"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      stroke-width="2.5"
      aria-hidden="true"
    >
      <path
        d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83"
        stroke-linecap="round"
      />
    </svg>
    <span v-if="label" class="loading-indicator-label">{{ label }}</span>
  </div>
</template>
