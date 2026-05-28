<script setup>
import { computed } from "vue";

const props = defineProps({
  reviewForm: {
    type: Object,
    required: true,
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

const emit = defineEmits(["update-review-field", "reset", "save"]);

function createFieldModel(key) {
  return computed({
    get: () => props.reviewForm[key],
    set: (value) => emit("update-review-field", { key, value }),
  });
}

const profileReviewEnabled = createFieldModel("profileReviewEnabled");
const profileReviewAutoApprove = createFieldModel("profileReviewAutoApprove");
const achievementReviewEnabled = createFieldModel("achievementReviewEnabled");
const achievementReviewAutoApprove = createFieldModel("achievementReviewAutoApprove");
</script>

<template>
  <div class="admin-panel-single">
    <div class="card admin-card">
      <div class="card-header">
        <div class="card-header-icon shield-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
          </svg>
        </div>
        <div>
          <div class="card-kicker">审核入口</div>
          <h2 class="card-title">审核策略设置</h2>
        </div>
      </div>
      <div class="card-body toggle-list">
        <div class="toggle-section">
          <div class="toggle-section-label">
            <span class="group-index">01</span>
            <span class="group-title">个人信息</span>
          </div>
          <div class="toggle-row" :class="{ muted: !reviewForm.profileReviewEnabled }">
            <div class="toggle-copy">
              <span class="toggle-title">开启个人信息审核</span>
              <span class="toggle-hint">开启后，学生/干部每次保存均走审核；关闭后显示「保存」并直接更新</span>
            </div>
            <label class="toggle-switch" :aria-label="`开启个人信息审核: ${reviewForm.profileReviewEnabled ? '已开启' : '已关闭'}`">
              <input
                v-model="profileReviewEnabled"
                type="checkbox"
                role="switch"
                :aria-checked="reviewForm.profileReviewEnabled"
              />
              <span class="toggle-track">
                <span class="toggle-thumb"></span>
              </span>
            </label>
          </div>
          <div class="toggle-row" :class="{ muted: !reviewForm.profileReviewEnabled }">
            <div class="toggle-copy">
              <span class="toggle-title">默认通过</span>
              <span class="toggle-hint">开启审核但自动通过，保留审核流程入口与记录</span>
            </div>
            <label class="toggle-switch" :aria-label="`默认通过: ${reviewForm.profileReviewAutoApprove ? '已开启' : '已关闭'}`">
              <input
                v-model="profileReviewAutoApprove"
                type="checkbox"
                role="switch"
                :aria-checked="reviewForm.profileReviewAutoApprove"
                :disabled="!reviewForm.profileReviewEnabled"
              />
              <span class="toggle-track">
                <span class="toggle-thumb"></span>
              </span>
            </label>
          </div>
        </div>

        <div class="toggle-section">
          <div class="toggle-section-label">
            <span class="group-index">02</span>
            <span class="group-title">成就</span>
          </div>
          <div class="toggle-row" :class="{ muted: !reviewForm.achievementReviewEnabled }">
            <div class="toggle-copy">
              <span class="toggle-title">开启成就审核</span>
              <span class="toggle-hint">关闭后，新增和修改成就都会直接生效</span>
            </div>
            <label class="toggle-switch" :aria-label="`开启成就审核: ${reviewForm.achievementReviewEnabled ? '已开启' : '已关闭'}`">
              <input
                v-model="achievementReviewEnabled"
                type="checkbox"
                role="switch"
                :aria-checked="reviewForm.achievementReviewEnabled"
              />
              <span class="toggle-track">
                <span class="toggle-thumb"></span>
              </span>
            </label>
          </div>
          <div class="toggle-row" :class="{ muted: !reviewForm.achievementReviewEnabled }">
            <div class="toggle-copy">
              <span class="toggle-title">默认通过</span>
              <span class="toggle-hint">开启审核但自动通过，适合先保留入口再平滑切换</span>
            </div>
            <label class="toggle-switch" :aria-label="`成就默认通过: ${reviewForm.achievementReviewAutoApprove ? '已开启' : '已关闭'}`">
              <input
                v-model="achievementReviewAutoApprove"
                type="checkbox"
                role="switch"
                :aria-checked="reviewForm.achievementReviewAutoApprove"
                :disabled="!reviewForm.achievementReviewEnabled"
              />
              <span class="toggle-track">
                <span class="toggle-thumb"></span>
              </span>
            </label>
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
  </div>
</template>
