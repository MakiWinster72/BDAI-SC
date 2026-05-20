<template>
  <div
    v-if="deleteDialogOpen"
    class="dialog-backdrop"
    @click="emit('close')"
  ></div>
  <section v-if="deleteDialogOpen" class="dialog-card" @click.stop>
    <header class="dialog-header">确认删除</header>
    <div class="dialog-body">删除后无法恢复，确定要删除这条动态吗？</div>
    <div class="dialog-actions">
      <button class="ghost-button" type="button" @click="emit('close')">
        取消
      </button>
      <button
        class="action-button"
        type="button"
        :disabled="deleteBusy"
        @click="emit('confirm')"
      >
        确定删除
      </button>
    </div>
  </section>
</template>

<script setup>
defineProps({
  deleteDialogOpen: { type: Boolean, required: true },
  deleteBusy: { type: Boolean, required: true },
});

const emit = defineEmits(["close", "confirm"]);
</script>
