<script setup>
import { computed } from "vue";
import { text } from "../../../utils/achievementFormatters";

const props = defineProps({
  item: {
    type: Object,
    required: true,
  },
  schema: {
    type: Object,
    required: true,
  },
});

const fields = computed(() => props.item.fields || {});
const headerTag = computed(() => {
  const tag = props.schema.tag;
  return typeof tag === "function" ? tag(fields.value, props.item) : tag || "";
});
const groups = computed(() =>
  (props.schema.groups || [])
    .map((group) => {
      const rows = (group.rows || [])
        .filter((entry) => !entry.hidden || !entry.hidden(fields.value, props.item))
        .map((entry) => ({
          label: entry.label,
          value:
            typeof entry.value === "function"
              ? entry.value(fields.value, props.item)
              : text(entry.value),
        }))
        .filter((entry) => entry.value !== "");

      return {
        label: group.label || "",
        rows,
      };
    })
    .filter((group) => group.rows.length),
);
</script>

<template>
  <div class="detail-header">
    <div class="detail-title">{{ item.title || "-" }}</div>
    <span v-if="headerTag" class="detail-category-tag">{{ headerTag }}</span>
  </div>

  <div
    v-for="(group, index) in groups"
    :key="`${group.label}-${index}`"
    class="detail-group"
  >
    <div v-if="group.label" class="detail-group-label">{{ group.label }}</div>
    <div
      v-for="row in group.rows"
      :key="`${group.label}-${row.label}`"
      class="detail-row"
    >
      <span class="detail-label">{{ row.label }}</span>
      <span class="detail-value">{{ row.value || "-" }}</span>
    </div>
  </div>
</template>
