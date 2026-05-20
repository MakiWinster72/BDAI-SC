<script setup>
defineProps({
  modal: { type: Object, required: true },
  importFileRef: { type: Object, default: null },
});

const emit = defineEmits(["close", "create", "import-file"]);
</script>

<template>
  <Teleport to="body">
    <div
      :class="['sheet-overlay', { open: modal.visible }]"
      role="dialog"
      aria-modal="true"
      aria-label="添加用户"
      @click.self="emit('close')"
    >
      <div class="add-user-layout" @click.stop>
        <div class="sheet-modal user-edit-modal add-user-modal">
          <div class="modal-top-bar">
            <div class="modal-title-group">
              <h3 class="modal-title">添加用户</h3>
              <p class="modal-subtitle">默认角色为学生</p>
            </div>
            <button class="modal-close-btn" type="button" aria-label="关闭" @click="emit('close')">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                <path d="M18 6L6 18M6 6l12 12" />
              </svg>
            </button>
          </div>

          <div class="modal-body">
            <div class="modal-field">
              <label class="modal-label" for="add-users-textarea">用户信息</label>
              <textarea
                id="add-users-textarea"
                v-model="modal.textarea"
                class="modal-textarea"
                rows="8"
                placeholder="每行一个用户：显示名称,学号(用于登录),密码&#10;示例：&#10;张三,2024001,password123&#10;李四,2024002,password456"
              />
              <div class="field-hint add-user-hint">
                <strong>注意：使用英文逗号，前后不要有空格</strong>
              </div>
              <div class="modal-field add-user-import-row">
                <input
                  id="import-file"
                  ref="importFileRef"
                  type="file"
                  accept=".xlsx,.xls,.csv,.txt"
                  class="file-input admin-import-hidden"
                  @change="emit('import-file', $event)"
                />
                <button class="btn btn-ghost" type="button" @click="importFileRef?.click?.()">
                  <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" />
                  </svg>
                  从文件导入
                </button>
                <span class="field-hint add-user-format-hint">支持格式：xlsx、csv、txt</span>
              </div>
            </div>
            <Transition name="msg-fade">
              <div v-if="modal.error" class="msg-banner error modal-error" role="alert">
                {{ modal.error }}
              </div>
            </Transition>
          </div>

          <div class="modal-footer">
            <div class="modal-footer-btns add-user-footer-btns">
              <button class="btn btn-ghost" type="button" @click="emit('close')">取消</button>
              <button
                class="btn btn-primary"
                type="button"
                :disabled="modal.saving"
                @click="emit('create')"
              >
                {{ modal.saving ? "创建中…" : "创建用户" }}
              </button>
            </div>
          </div>
        </div>

        <div class="add-user-example-panel">
          <div class="example-image-item">
            <span class="example-image-label">xlsx 格式示例</span>
            <img src="/assets/images/xlsx格式示例.png" alt="xlsx 格式示例" />
          </div>
          <div class="example-image-item">
            <span class="example-image-label">csv/txt 格式示例</span>
            <img src="/assets/images/csv-txt格式示例.png" alt="csv/txt 格式示例" />
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
@import "@/assets/styles/admin-user-modals.css";

.add-user-hint {
  margin-top: 8px;
}

.add-user-import-row {
  margin-top: 12px;
}

.admin-import-hidden {
  display: none;
}

.add-user-format-hint {
  margin-left: 8px;
}

.add-user-footer-btns {
  margin-left: auto;
}
</style>
