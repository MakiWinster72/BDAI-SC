<script setup>
defineOptions({ inheritAttrs: false });

const model = defineModel({ type: String, default: "" });

defineProps({
  inputClass: {
    type: String,
    default: "info-input",
  },
});

function openPicker(event) {
  const input = event.currentTarget;
  if (!input || input.disabled) {
    return;
  }
  if (typeof input.showPicker === "function") {
    try {
      input.showPicker();
    } catch {
      // 部分浏览器在不支持的用户手势场景下会抛错，忽略即可
    }
  }
}
</script>

<template>
  <input
    v-model="model"
    type="date"
    :class="inputClass"
    v-bind="$attrs"
    @click="openPicker"
  />
</template>
