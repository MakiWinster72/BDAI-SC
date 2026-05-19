<script setup>
import { computed } from "vue";

const props = defineProps({
  form: {
    type: Object,
    required: true,
  },
  attachmentTypeOptions: {
    type: Array,
    default: () => [],
  },
  errorMessage: {
    type: String,
    default: "",
  },
  saving: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(["update-form-field", "reset", "save"]);

function createFieldModel(key) {
  return computed({
    get: () => props.form[key],
    set: (value) => emit("update-form-field", { key, value }),
  });
}

function extFieldKey(typeKey) {
  if (typeKey === "document") return "attachmentDocumentExts";
  if (typeKey === "video") return "attachmentVideoExts";
  if (typeKey === "image") return "attachmentImageExts";
  return "attachmentArchiveExts";
}

function getExtPlaceholder(typeKey) {
  if (typeKey === "document") return "docx, doc, pdf";
  if (typeKey === "video") return "mp4, mov";
  if (typeKey === "image") return "jpeg, jpg, png";
  return "zip, rar, 7z";
}

const imageMaxCount = createFieldModel("imageMaxCount");
const imageMaxSizeMb = createFieldModel("imageMaxSizeMb");
const attachmentMaxCount = createFieldModel("attachmentMaxCount");
const attachmentMaxSizeMb = createFieldModel("attachmentMaxSizeMb");
const supportingDocMaxCount = createFieldModel("supportingDocMaxCount");
const supportingDocMaxSizeMb = createFieldModel("supportingDocMaxSizeMb");
</script>

<template>
  <div class="upload-section">
    <div class="card admin-card">
      <div class="card-header">
        <div class="card-header-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
            <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
          </svg>
        </div>
        <div>
          <div class="card-kicker">系统设置</div>
          <h2 class="card-title">成果页面限制</h2>
        </div>
      </div>
      <div class="card-body">
        <div class="setting-group">
          <div class="setting-group-label">
            <span class="group-index">01</span>
            <span class="group-title">图片设置</span>
          </div>
          <div class="field-row">
            <div class="field-cell">
              <label class="field-label" for="img-count">最多上传图片数</label>
              <div class="input-wrap">
                <input
                  id="img-count"
                  v-model.number="imageMaxCount"
                  class="text-input"
                  type="number"
                  min="1"
                  max="9"
                  aria-label="最多上传图片数"
                />
                <span class="input-unit">张</span>
              </div>
            </div>
            <div class="field-cell">
              <label class="field-label" for="img-size">单张图片最大大小</label>
              <div class="input-wrap">
                <input
                  id="img-size"
                  v-model.number="imageMaxSizeMb"
                  class="text-input"
                  type="number"
                  min="1"
                  max="200"
                  aria-label="单张图片最大大小 MB"
                />
                <span class="input-unit">MB</span>
              </div>
            </div>
          </div>
        </div>

        <div class="setting-group">
          <div class="setting-group-label">
            <span class="group-index">02</span>
            <span class="group-title">附件设置</span>
          </div>
          <div class="field-row">
            <div class="field-cell">
              <label class="field-label" for="att-count">最多上传附件数</label>
              <div class="input-wrap">
                <input
                  id="att-count"
                  v-model.number="attachmentMaxCount"
                  class="text-input"
                  type="number"
                  min="1"
                  max="20"
                  aria-label="最多上传附件数"
                />
                <span class="input-unit">个</span>
              </div>
            </div>
            <div class="field-cell">
              <label class="field-label" for="att-size">单个附件最大大小</label>
              <div class="input-wrap">
                <input
                  id="att-size"
                  v-model.number="attachmentMaxSizeMb"
                  class="text-input"
                  type="number"
                  min="1"
                  max="200"
                  aria-label="单个附件最大大小 MB"
                />
                <span class="input-unit">MB</span>
              </div>
            </div>
          </div>

          <div class="ext-section">
            <div class="ext-label-row">
              <label class="field-label">支持的附件后缀</label>
              <span class="field-hint">多个后缀用英文逗号隔开；留空则该类型不开放</span>
            </div>
            <div class="ext-grid">
              <label
                v-for="item in attachmentTypeOptions"
                :key="item.key"
                class="ext-card"
              >
                <div class="ext-card-head">
                  <img class="ext-icon" :src="item.icon" alt="" aria-hidden="true" />
                  <span class="ext-title">{{ item.label }}</span>
                </div>
                <input
                  :value="form[extFieldKey(item.key)]"
                  class="ext-input"
                  type="text"
                  :aria-label="`${item.label}后缀`"
                  :placeholder="getExtPlaceholder(item.key)"
                  @input="emit('update-form-field', { key: extFieldKey(item.key), value: $event.target.value })"
                />
              </label>
            </div>
          </div>
        </div>

        <div v-if="errorMessage" class="msg-banner error" role="alert">
          <svg class="msg-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-2.694-.833-3.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z" />
          </svg>
          {{ errorMessage }}
        </div>

        <div class="card-actions">
          <button
            class="btn btn-ghost"
            type="button"
            @click="emit('reset')"
          >
            重置
          </button>
          <button
            class="btn btn-primary"
            type="button"
            :disabled="saving"
            @click="emit('save')"
          >
            <svg v-if="saving" class="btn-spinner" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
              <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83" stroke-linecap="round" />
            </svg>
            {{ saving ? "保存中…" : "保存设置" }}
          </button>
        </div>
      </div>
    </div>

    <div class="card admin-card">
      <div class="card-header">
        <div class="card-header-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
          </svg>
        </div>
        <div>
          <div class="card-kicker">系统设置</div>
          <h2 class="card-title">证明资料</h2>
        </div>
      </div>
      <div class="card-body">
        <div class="setting-group">
          <div class="field-row">
            <div class="field-cell">
              <label class="field-label" for="sd-count">最多上传数量</label>
              <div class="input-wrap">
                <input
                  id="sd-count"
                  v-model.number="supportingDocMaxCount"
                  class="text-input"
                  type="number"
                  min="1"
                  max="20"
                  aria-label="证明资料最多上传数量"
                />
                <span class="input-unit">个</span>
              </div>
            </div>
            <div class="field-cell">
              <label class="field-label" for="sd-size">单个文件最大大小</label>
              <div class="input-wrap">
                <input
                  id="sd-size"
                  v-model.number="supportingDocMaxSizeMb"
                  class="text-input"
                  type="number"
                  min="1"
                  max="200"
                  aria-label="证明资料单个文件最大大小 MB"
                />
                <span class="input-unit">MB</span>
              </div>
            </div>
          </div>
        </div>
        <div class="card-actions">
          <button
            class="btn btn-primary"
            type="button"
            :disabled="saving"
            @click="emit('save')"
          >
            <svg v-if="saving" class="btn-spinner" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
              <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83" stroke-linecap="round" />
            </svg>
            {{ saving ? "保存中…" : "保存设置" }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
