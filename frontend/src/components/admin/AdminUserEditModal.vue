<script setup>
import { ROLE_OPTIONS, STUDENT_CATEGORY_OPTIONS } from "@/config/adminConfig";

defineProps({
  modal: { type: Object, required: true },
  currentMajorOptions: { type: Array, default: () => [] },
});

const emit = defineEmits(["close", "save", "delete", "add-class", "remove-class"]);
</script>

<template>
  <Teleport to="body">
    <div
      :class="['sheet-overlay', { open: modal.visible }]"
      role="dialog"
      aria-modal="true"
      aria-label="编辑用户"
      @click.self="emit('close')"
    >
      <div class="sheet-modal user-edit-modal" @click.stop>
        <div class="modal-top-bar">
          <div class="modal-title-group">
            <h3 class="modal-title">编辑用户</h3>
            <p class="modal-subtitle">正在编辑：{{ modal.user?.displayName }}</p>
          </div>
          <button class="modal-close-btn" type="button" aria-label="关闭" @click="emit('close')">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
              <path d="M18 6L6 18M6 6l12 12" />
            </svg>
          </button>
        </div>

        <div class="modal-body">
          <div class="modal-field">
            <label class="modal-label" for="edit-username">用户名</label>
            <input
              id="edit-username"
              v-model="modal.form.username"
              class="modal-input"
              type="text"
              placeholder="留空则不修改"
            />
          </div>
          <div class="modal-field">
            <label class="modal-label" for="edit-password">密码</label>
            <input
              id="edit-password"
              v-model="modal.form.password"
              class="modal-input"
              type="password"
              placeholder="留空则不修改"
            />
          </div>
          <div class="modal-field">
            <label class="modal-label" for="edit-role">角色</label>
            <select id="edit-role" v-model="modal.form.role" class="modal-select">
              <option v-for="opt in ROLE_OPTIONS" :key="opt.value" :value="opt.value">
                {{ opt.label }}
              </option>
            </select>
          </div>
          <div class="modal-field">
            <label class="modal-label" for="edit-remark">备注</label>
            <input
              id="edit-remark"
              v-model="modal.form.remark"
              class="modal-input"
              type="text"
              placeholder="如：班主任、班长、团支书等"
            />
          </div>
          <Transition name="msg-fade">
            <div v-if="modal.form.role === 'TEACHER'" class="modal-field">
              <label class="modal-label">负责班级</label>
              <div class="class-select-hint">添加该教师负责的班级</div>
              <div v-if="modal.form.assignedClasses.length > 0" class="assigned-classes-list">
                <div
                  v-for="cls in modal.form.assignedClasses"
                  :key="cls"
                  class="assigned-class-item"
                >
                  <span>{{ cls }}</span>
                  <button
                    type="button"
                    class="remove-class-btn"
                    @click="emit('remove-class', cls)"
                  >
                    ×
                  </button>
                </div>
              </div>
              <div class="add-class-form">
                <input
                  v-model.number="modal.form.newClassYear"
                  class="modal-input class-year-input"
                  type="number"
                  min="2000"
                  max="2100"
                  placeholder="年级"
                />
                <select
                  v-model="modal.form.newClassCategory"
                  class="modal-select class-category-select"
                >
                  <option
                    v-for="cat in STUDENT_CATEGORY_OPTIONS"
                    :key="cat"
                    :value="cat"
                  >
                    {{ cat }}
                  </option>
                </select>
                <select v-model="modal.form.newClassMajor" class="modal-select class-major-select">
                  <option value="">选择专业</option>
                  <option v-for="major in currentMajorOptions" :key="major" :value="major">
                    {{ major }}
                  </option>
                </select>
                <input
                  v-model.number="modal.form.newClassNo"
                  class="modal-input class-no-input"
                  type="number"
                  min="1"
                  max="20"
                  placeholder="班号"
                />
                <button type="button" class="add-class-btn" @click="emit('add-class')">
                  添加
                </button>
              </div>
            </div>
          </Transition>
          <Transition name="msg-fade">
            <div v-if="modal.error" class="msg-banner error modal-error" role="alert">
              {{ modal.error }}
            </div>
          </Transition>
        </div>

        <div class="modal-footer">
          <button class="btn btn-danger-ghost" type="button" @click="emit('delete', modal.user)">
            删除用户
          </button>
          <div class="modal-footer-btns">
            <button class="btn btn-ghost" type="button" @click="emit('close')">取消</button>
            <button
              class="btn btn-primary"
              type="button"
              :disabled="modal.saving"
              @click="emit('save')"
            >
              {{ modal.saving ? "保存中…" : "保存" }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
@import "@/assets/styles/admin-user-modals.css";
</style>
