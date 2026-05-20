<script setup>
defineProps({
  open: { type: Boolean, default: false },
  totalItems: { type: Number, default: 0 },
});

const emit = defineEmits(["close", "confirm"]);
</script>

<template>
  <div :class="['sheet-overlay', { open }]" @click.self="emit('close')">
    <div class="sheet-modal" @click.stop>
      <header class="sheet-modal-header">
        <div class="sheet-modal-title">提示</div>
      </header>
      <div class="sheet-modal-body">
        当前学生数量为 {{ totalItems }} 人，表格视图加载大量数据可能会造成卡顿。<br />
        建议使用导出功能下载表格查看。<br />
        是否继续切换到表格视图？
      </div>
      <div class="sheet-modal-actions">
        <button class="ghost-button" type="button" @click="emit('close')">取消</button>
        <button class="action-button" type="button" @click="emit('confirm')">继续</button>
      </div>
    </div>
  </div>
</template>
