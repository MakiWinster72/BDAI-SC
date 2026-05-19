<script setup>
import { computed } from "vue";

const props = defineProps({
  settings: {
    type: Object,
    required: true,
  },
  message: {
    type: String,
    default: "",
  },
});

const emit = defineEmits(["update-setting", "save", "step-threshold"]);

function createSettingModel(key) {
  return computed({
    get: () => props.settings[key],
    set: (value) => emit("update-setting", { key, value }),
  });
}

const allowRegistration = createSettingModel("allowRegistration");
const delayedThresholdDays = createSettingModel("delayedThresholdDays");
</script>

<template>
  <div class="admin-panel-single">
    <div class="card admin-card">
      <div class="card-header">
        <div class="card-header-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4" />
          </svg>
        </div>
        <div>
          <div class="card-kicker">系统</div>
          <h2 class="card-title">其他设置</h2>
        </div>
      </div>
      <div class="card-body">
        <div class="toggle-section">
          <div class="toggle-row">
            <div class="toggle-copy">
              <span class="toggle-title">开放注册</span>
              <span class="toggle-hint">关闭后，用户将无法自行注册账号</span>
            </div>
            <label class="toggle-switch" :aria-label="`开放注册: ${settings.allowRegistration ? '已开启' : '已关闭'}`">
              <input
                v-model="allowRegistration"
                type="checkbox"
                role="switch"
                :aria-checked="settings.allowRegistration"
                @change="emit('save')"
              />
              <span class="toggle-track">
                <span class="toggle-thumb"></span>
              </span>
            </label>
          </div>
          <div class="toggle-row">
            <div class="toggle-copy">
              <span class="toggle-title">待处理自动滞后</span>
              <span class="toggle-hint">超过指定天数未处理的请求自动移入已滞后标签</span>
            </div>
            <div class="stepper-wrap">
              <button
                class="stepper-btn"
                type="button"
                aria-label="减少天数"
                :disabled="settings.delayedThresholdDays <= 1"
                @click="emit('step-threshold', -1)"
              >
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M5 12h14" /></svg>
              </button>
              <input
                v-model.number="delayedThresholdDays"
                class="stepper-input"
                type="number"
                min="1"
                max="30"
                aria-label="待处理自动滞后天数"
                @change="emit('save')"
              />
              <button
                class="stepper-btn"
                type="button"
                aria-label="增加天数"
                :disabled="settings.delayedThresholdDays >= 30"
                @click="emit('step-threshold', 1)"
              >
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M12 5v14M5 12h14" /></svg>
              </button>
              <span class="stepper-unit">天</span>
            </div>
          </div>
        </div>
        <Transition name="msg-fade">
          <div v-if="message" :class="['msg-banner', 'success']" role="status">{{ message }}</div>
        </Transition>
      </div>
    </div>
  </div>
</template>
