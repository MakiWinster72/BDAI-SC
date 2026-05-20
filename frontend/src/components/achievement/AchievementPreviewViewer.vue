<template>
  <Teleport to="body">
    <Transition name="viewer" appear>
      <div class="viewer-backdrop" @click="emit('hide')">
        <div class="viewer-header" @click.stop>
          <div class="viewer-counter">
            <span class="viewer-current">{{ previewIndex + 1 }}</span>
            <span class="viewer-sep">/</span>
            <span class="viewer-total">{{ previewImages.length }}</span>
          </div>
          <button class="viewer-close" type="button" @click="emit('hide')">
            <svg
              width="20"
              height="20"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>

        <div class="viewer-stage">
          <button
            v-if="previewImages.length > 1 && previewIndex > 0"
            class="viewer-arrow viewer-prev"
            type="button"
            @click.stop="emit('prev')"
          >
            <svg
              width="24"
              height="24"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <polyline points="15 18 9 12 15 6"></polyline>
            </svg>
          </button>

          <Transition :name="'slide-' + slideDirection" mode="out-in">
            <div class="viewer-media" :key="previewIndex" @click.stop>
              <video
                v-if="previewType === 'video'"
                :src="previewImages[previewIndex]"
                class="viewer-video"
                controls
                autoplay
              ></video>
              <div
                v-else-if="
                  previewType === 'document' ||
                  previewType === 'sheet' ||
                  previewType === 'pdf'
                "
                class="viewer-document"
                :class="{ 'viewer-doc-full': previewType === 'pdf' }"
              >
                <div v-if="previewLoading" class="viewer-loading">
                  <div class="spinner spinner-lg"></div>
                  <span>加载中...</span>
                </div>
                <div v-else class="viewer-content-wrapper">
                  <div v-if="previewType === 'document'" class="viewer-tip">
                    <svg
                      width="14"
                      height="14"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                    >
                      <circle cx="12" cy="12" r="10"></circle>
                      <line x1="12" y1="16" x2="12" y2="12"></line>
                      <line x1="12" y1="8" x2="12.01" y2="8"></line>
                    </svg>
                    因渲染限制，预览与实际文件可能存在样式差异
                  </div>
                  <div v-if="previewType === 'sheet'" class="viewer-tip">
                    <svg
                      width="14"
                      height="14"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                    >
                      <circle cx="12" cy="12" r="10"></circle>
                      <line x1="12" y1="16" x2="12" y2="12"></line>
                      <line x1="12" y1="8" x2="12.01" y2="8"></line>
                    </svg>
                    点击工作表标签可切换Sheet |
                    因渲染限制，预览与实际可能存在差异
                  </div>
                  <div
                    v-html="previewContent"
                    class="viewer-content"
                    :class="{
                      'viewer-content-full':
                        previewType === 'pdf' || previewType === 'document',
                    }"
                  ></div>
                </div>
              </div>
              <img
                v-else
                :src="previewImages[previewIndex]"
                class="viewer-image"
                alt=""
                @click.stop
              />
            </div>
          </Transition>

          <button
            v-if="
              previewImages.length > 1 &&
              previewIndex < previewImages.length - 1
            "
            class="viewer-arrow viewer-next"
            type="button"
            @click.stop="emit('next')"
          >
            <svg
              width="24"
              height="24"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <polyline points="9 18 15 12 9 6"></polyline>
            </svg>
          </button>
        </div>

        <div
          v-if="previewImages.length > 1"
          class="viewer-dots"
          @click.stop
        >
          <button
            v-for="(_, i) in previewImages"
            :key="i"
            class="viewer-dot"
            :class="{ active: i === previewIndex }"
            type="button"
            @click="emit('dot', i)"
          ></button>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
defineProps({
  previewImages: { type: Array, required: true },
  previewIndex: { type: Number, required: true },
  previewType: { type: String, required: true },
  previewContent: { type: String, required: true },
  previewLoading: { type: Boolean, required: true },
  slideDirection: { type: String, required: true },
});

const emit = defineEmits(["hide", "prev", "next", "dot"]);
</script>
