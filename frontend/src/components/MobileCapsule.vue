<template>
  <Transition name="capsule-slide">
    <div v-show="!hidden" class="mobile-capsule" :class="{ 'has-right': $slots.right }">
      <div class="capsule-left">
        <div
          class="capsule-action capsule-menu"
          role="button"
          tabindex="0"
          aria-label="打开菜单"
          @click="$emit('open-sidebar')"
          @keydown.enter="$emit('open-sidebar')"
          @keydown.space.prevent="$emit('open-sidebar')"
        >
          <span class="capsule-icon" aria-hidden="true">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <path d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </span>
        </div>
      </div>
      <div v-if="$slots.right" class="capsule-divider" aria-hidden="true"></div>
      <div class="capsule-right">
        <slot name="right" />
      </div>
    </div>
  </Transition>
</template>

<script setup>
defineEmits(["open-sidebar"]);
defineProps({
  hidden: {
    type: Boolean,
    default: false,
  },
});
</script>
