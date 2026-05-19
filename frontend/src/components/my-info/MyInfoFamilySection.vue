<script setup>
import { ref } from "vue";
import { applyDigitsInput } from "@/utils/profileFormInput";

const props = defineProps({
  info: { type: Object, required: true },
  isEditing: { type: Boolean, default: false },
});

const workUnitHintOpen = ref(false);

function handleDigitsInput(field, maxLength, event) {
  applyDigitsInput(props.info, field, maxLength, event);
}
</script>

<template>
  <div class="card info-card">
    <!-- TODO: 单亲/离异等待现场演示求助 -->
    <div class="info-section-title">
      家庭信息
      <button
        class="hint-button"
        type="button"
        aria-label="填写说明"
        @click="workUnitHintOpen = true"
      >
        ?
      </button>
    </div>
    <transition name="dialog-fade">
      <div
        v-if="workUnitHintOpen"
        class="dialog-backdrop"
        @click="workUnitHintOpen = false"
      ></div>
    </transition>
    <transition name="dialog-pop">
      <section v-if="workUnitHintOpen" class="dialog-card" @click.stop>
        <header class="dialog-header">填写说明</header>
        <div class="dialog-body">
          <div class="hint-item">
            <span class="hint-label">工作单位：</span>无
            <span class="hint-sep">|</span>
            <span class="hint-label">职务：</span>待业/务农
          </div>
          <div class="hint-item">
            <span class="hint-label">工作单位：</span>无固定单位
            <span class="hint-sep">|</span>
            <span class="hint-label">职务：</span>散工
          </div>
          <div class="hint-item">
            <span class="hint-label">工作单位：</span>个体户
            <span class="hint-sep">|</span>
            <span class="hint-label">职务：</span>店主
          </div>
        </div>
        <div class="dialog-actions">
          <button
            class="ghost-button"
            type="button"
            @click="workUnitHintOpen = false"
          >
            知道了
          </button>
        </div>
      </section>
    </transition>
    <div class="info-form-grid family-grid">
      <div class="family-section-title">父亲（监护人）</div>
      <label class="field-card">
        <span class="info-label">姓名</span>
        <input
          v-model="info.fatherName"
          class="info-input"
          type="text"
          placeholder="请输入父亲姓名"
          :disabled="!isEditing"
        />
      </label>
      <label class="field-card">
        <span class="info-label">手机号码</span>
        <input
          v-model="info.fatherPhone"
          class="info-input"
          type="tel"
          placeholder="请输入父亲手机号码"
          maxlength="11"
          inputmode="numeric"
          :disabled="!isEditing"
          @input="handleDigitsInput('fatherPhone', 11, $event)"
        />
      </label>
      <label class="field-card">
        <span class="info-label">工作单位</span>
        <input
          v-model="info.fatherWorkUnit"
          class="info-input"
          type="text"
          placeholder="请输入父亲工作单位"
          :disabled="!isEditing"
        />
      </label>
      <label class="field-card">
        <span class="info-label">职务</span>
        <input
          v-model="info.fatherTitle"
          class="info-input"
          type="text"
          placeholder="请输入父亲职务"
          :disabled="!isEditing"
        />
      </label>
      <div class="family-section-title">母亲（监护人2）</div>
      <label class="field-card">
        <span class="info-label">姓名</span>
        <input
          v-model="info.motherName"
          class="info-input"
          type="text"
          placeholder="请输入母亲姓名"
          :disabled="!isEditing"
        />
      </label>
      <label class="field-card">
        <span class="info-label">手机号码</span>
        <input
          v-model="info.motherPhone"
          class="info-input"
          type="tel"
          placeholder="请输入母亲手机号码"
          maxlength="11"
          inputmode="numeric"
          :disabled="!isEditing"
          @input="handleDigitsInput('motherPhone', 11, $event)"
        />
      </label>
      <label class="field-card">
        <span class="info-label">工作单位</span>
        <input
          v-model="info.motherWorkUnit"
          class="info-input"
          type="text"
          placeholder="请输入母亲工作单位"
          :disabled="!isEditing"
        />
      </label>
      <label class="field-card">
        <span class="info-label">职务</span>
        <input
          v-model="info.motherTitle"
          class="info-input"
          type="text"
          placeholder="请输入母亲职务"
          :disabled="!isEditing"
        />
      </label>
    </div>
  </div>

  <div class="card info-card">
    <div class="info-section-title">紧急联系人（除亲戚外）</div>
    <div class="info-form-grid">
      <label class="field-card">
        <span class="info-label">紧急联系人电话</span>
        <input
          v-model="info.emergencyPhone"
          class="info-input"
          type="tel"
          placeholder="请输入紧急联系人电话"
          maxlength="11"
          inputmode="numeric"
          :disabled="!isEditing"
          @input="handleDigitsInput('emergencyPhone', 11, $event)"
        />
      </label>
      <label class="field-card">
        <span class="info-label">紧急联系人的关系</span>
        <input
          v-model="info.emergencyRelation"
          class="info-input"
          type="text"
          placeholder="如父母、亲属"
          :disabled="!isEditing"
        />
      </label>
    </div>
  </div>
</template>
