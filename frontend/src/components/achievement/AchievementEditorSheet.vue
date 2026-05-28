<template>
  <transition name="publisher-backdrop" appear>
    <div
      v-if="sheetOpen"
      class="achievement-backdrop"
      @click="handleClose"
    ></div>
  </transition>
  <section
    class="achievement-sheet"
    :class="{ open: sheetOpen }"
    :aria-hidden="!sheetOpen"
  >
    <header class="publisher-header">
      <div class="publisher-title">
        {{ editId ? "编辑成就" : "新增成就" }}
      </div>
      <button class="publisher-close" type="button" @click="handleClose">
        关闭
      </button>
    </header>
    <div class="achievement-grid" :class="{ 'has-media': form.category }">
      <transition name="panel-fade">
        <div v-show="form.category" class="achievement-media-panel">
          <div class="achievement-section-label">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              aria-hidden="true"
            >
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
              <circle cx="8.5" cy="8.5" r="1.5" />
              <polyline points="21 15 16 10 5 21" />
            </svg>
            图片（可选）
          </div>
          <div class="media-subtitle">
            最多 {{ imageMaxCount }} 张 · 单张不超过 {{ mediaLimitLabel }}
          </div>

          <div
            v-if="imagePreviews.length === 0"
            class="media-empty-state"
            @click="emit('trigger-image')"
          >
            <div class="media-empty-icon">+</div>
            <div class="media-empty-text">点击添加图片</div>
          </div>

          <div v-else-if="imagePreviews.length === 1" class="media-single">
            <button
              class="media-single-item"
              type="button"
              @click="emit('select-image', 0)"
            >
              <img :src="imagePreviews[0]" alt="成果图片" />
              <span class="media-remove" @click.stop="emit('remove-image', 0)"
                >移除</span
              >
            </button>
            <button
              class="media-add-btn"
              type="button"
              @click="emit('trigger-image')"
            >
              <span>+</span>
            </button>
          </div>

          <div v-else-if="imagePreviews.length === 2" class="media-two">
            <button
              v-for="(image, index) in imagePreviews"
              :key="`${image}-${index}`"
              class="media-thumb"
              type="button"
              @click="emit('select-image', index)"
            >
              <img :src="image" alt="成果图片" />
              <span
                class="media-remove"
                @click.stop="emit('remove-image', index)"
                >移除</span
              >
            </button>
            <button
              class="media-thumb media-add"
              type="button"
              @click="emit('trigger-image')"
            >
              <span>+</span>
            </button>
          </div>

          <div v-else class="media-grid">
            <button
              v-for="(image, index) in imagePreviews"
              :key="`${image}-${index}`"
              class="media-thumb"
              type="button"
              @click="emit('select-image', index)"
            >
              <img :src="image" alt="成果图片" />
              <span class="media-remove" @click.stop="emit('remove-image', index)">
                移除
              </span>
            </button>
            <button
              v-if="imagePreviews.length < imageMaxCount"
              class="media-thumb media-add"
              type="button"
              @click="emit('trigger-image')"
            >
              <span>+</span>
            </button>
          </div>
        </div>
      </transition>

      <div class="achievement-fields">
        <div v-if="!editId" class="achievement-category-row">
          <label class="field-label">成果分类</label>
          <select v-model="form.category">
            <option disabled value="">请选择分类</option>
            <option
              v-for="entry in categoryOptions"
              :key="entry.key"
              :value="entry.key"
            >
              {{ entry.label }}
            </option>
          </select>
        </div>
        <div v-if="!editId && activeCategoryHint" class="achievement-hint">
          <button
            class="achievement-hint-toggle"
            type="button"
            :aria-expanded="!hintCollapsed"
            @click="emit('toggle-hint')"
          >
            <span class="achievement-hint-toggle-label">
              <svg
                width="13"
                height="13"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                aria-hidden="true"
              >
                <circle cx="12" cy="12" r="10" />
                <line x1="12" y1="8" x2="12" y2="12" />
                <line x1="12" y1="16" x2="12.01" y2="16" />
              </svg>
              填写说明
            </span>
            <svg
              class="achievement-hint-chevron"
              :class="{ collapsed: hintCollapsed }"
              width="12"
              height="12"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2.5"
              aria-hidden="true"
            >
              <polyline points="18 15 12 9 6 15" />
            </svg>
          </button>
          <Transition name="hint-expand">
            <div v-show="!hintCollapsed" class="achievement-hint-body">
              <ol class="achievement-hint-list">
                <li v-for="item in activeCategoryHint" :key="item">
                  {{ item }}
                </li>
              </ol>
            </div>
          </Transition>
        </div>
        <div v-if="!activeFormConfig" class="empty-tip">
          请选择成果分类后填写对应信息。
        </div>
        <div v-else class="achievement-dynamic-fields">
          <div
            v-for="field in activeFormConfig.fields"
            :key="field.key"
            class="field-row"
          >
            <label class="field-label">
              {{ field.label }}
              <span
                v-if="field.hint"
                class="field-label-hint"
                :title="field.hint"
                >?</span
              >
            </label>
            <input
              v-if="field.kind === 'input'"
              v-model="form.fields[field.key]"
              class="form-input"
              :type="field.type || 'text'"
              :placeholder="field.placeholder || ''"
            />
            <div v-else-if="field.kind === 'radio'" class="radio-group">
              <label
                v-for="opt in field.options"
                :key="opt.value"
                class="radio-label"
              >
                <input
                  type="radio"
                  :value="opt.value"
                  v-model="form.fields[field.key]"
                />
                {{ opt.label }}
              </label>
            </div>
            <textarea
              v-else
              v-model="form.fields[field.key]"
              class="publisher-input"
              :rows="field.rows || 2"
              :placeholder="field.placeholder || ''"
            ></textarea>
          </div>
        </div>
        <transition name="panel-fade">
          <div v-show="form.category" class="achievement-attachments-panel">
            <div class="achievement-section-label">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                aria-hidden="true"
              >
                <path
                  d="M21.44 11.05l-9.19 9.19a6 6 0 01-8.49-8.49l9.19-9.19a4 4 0 015.66 5.66l-9.2 9.19a2 2 0 01-2.83-2.83l8.49-8.48"
                />
              </svg>
              附件（可选）
            </div>
            <div class="media-subtitle">
              最多 {{ attachmentMaxCount }} 个 · 单个不超过
              {{ attachmentLimitLabel }}
            </div>
            <div v-if="!attachmentPreviews.length" class="media-empty">
              暂无附件
            </div>
            <div v-else class="attachment-list">
              <div
                v-for="(file, index) in attachmentPreviews"
                :key="`${file.url}-${index}`"
                class="attachment-item"
              >
                <img :src="attachmentIcon(file)" alt="" />
                <div class="attachment-name">{{ file.name }}</div>
                <button
                  class="attachment-remove"
                  type="button"
                  @click="emit('remove-attachment', index)"
                >
                  移除
                </button>
              </div>
            </div>
            <div class="attachment-formats" @click="emit('trigger-attachment')">
              <div class="format-row">
                <div
                  v-for="item in enabledAttachmentTypes.slice(0, 2)"
                  :key="item.key"
                  class="format-item"
                >
                  <img class="format-icon" :src="item.icon" alt="" />
                  <span class="format-label">{{ item.label }}</span>
                  <span class="format-exts">{{ item.exts.join("/") }}</span>
                </div>
              </div>
              <div class="format-row">
                <div
                  v-for="item in enabledAttachmentTypes.slice(2, 4)"
                  :key="item.key"
                  class="format-item"
                >
                  <img class="format-icon" :src="item.icon" alt="" />
                  <span class="format-label">{{ item.label }}</span>
                  <span class="format-exts">{{ item.exts.join("/") }}</span>
                </div>
              </div>
            </div>
          </div>
        </transition>

        <div class="achievement-actions">
          <button class="ghost-button" type="button" @click="handleClose">
            取消
          </button>
          <button class="action-button" type="button" @click="emit('save')">
            {{ saveActionLabel }}
          </button>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { nextTick, onMounted, ref } from "vue";

const SHEET_CLOSE_MS = 480;

defineProps({
  editId: { type: [Number, String], default: null },
  form: { type: Object, required: true },
  imagePreviews: { type: Array, required: true },
  hintCollapsed: { type: Boolean, required: true },
  activeCategoryHint: { type: Array, default: null },
  activeFormConfig: { type: Object, default: null },
  categoryOptions: { type: Array, required: true },
  attachmentPreviews: { type: Array, required: true },
  attachmentIcon: { type: Function, required: true },
  imageMaxCount: { type: Number, required: true },
  attachmentMaxCount: { type: Number, required: true },
  mediaLimitLabel: { type: String, required: true },
  attachmentLimitLabel: { type: String, required: true },
  enabledAttachmentTypes: { type: Array, required: true },
  saveActionLabel: { type: String, default: "保存" },
});

const emit = defineEmits([
  "close",
  "save",
  "toggle-hint",
  "trigger-image",
  "trigger-attachment",
  "select-image",
  "remove-image",
  "remove-attachment",
]);

const sheetOpen = ref(false);

onMounted(() => {
  nextTick(() => {
    requestAnimationFrame(() => {
      sheetOpen.value = true;
    });
  });
});

function handleClose() {
  if (!sheetOpen.value) {
    return;
  }
  sheetOpen.value = false;
  setTimeout(() => emit("close"), SHEET_CLOSE_MS);
}
</script>
