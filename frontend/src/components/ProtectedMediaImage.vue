<script setup>
import { computed } from "vue";
import { useResolvedMediaUrl } from "../composables/useResolvedMediaUrl";

const props = defineProps({
  src: {
    type: String,
    default: "",
  },
  alt: {
    type: String,
    default: "",
  },
});

const sourceUrl = computed(() => props.src);
const { resolvedUrl } = useResolvedMediaUrl(sourceUrl);
</script>

<template>
  <img v-if="resolvedUrl" :src="resolvedUrl" :alt="alt" />
</template>
