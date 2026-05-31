<script setup>
import { toRef } from "vue";
import {
  idTypeOptions,
  politicalStatusOptions,
} from "@/constants/profileOptions";
import { useProfileIdNo } from "@/composables/useProfileIdNo";
import { useProfileRegionCascade } from "@/composables/useProfileRegionCascade";
import DateFieldInput from "@/components/DateFieldInput.vue";

const props = defineProps({
  info: {
    type: Object,
    required: true,
  },
  isEditing: {
    type: Boolean,
    default: false,
  },
  today: {
    type: String,
    required: true,
  },
});

const isEditingRef = toRef(props, "isEditing");
const { idNoMaxLength, idNoHint, handleIdNoInput, handlePhoneDigitsInput } =
  useProfileIdNo(props.info, isEditingRef);

const {
  provinceOptions: addressProvinceOptions,
  cityOptions: addressCityOptions,
  countyOptions: addressCountyOptions,
} = useProfileRegionCascade(props.info, "address");
</script>

<template>
  <div class="card info-card">
    <div
      id="profile-section-identity"
      class="info-section-title profile-jump-anchor"
    >
      个人证件与联系方式
    </div>
    <div class="info-form-grid">
      <label class="field-card">
        <span class="info-label">民族</span>
        <div class="class-inline">
          <input
            v-model="info.ethnicity"
            class="info-input"
            type="text"
            placeholder="请输入民族"
            :disabled="!isEditing"
          />
          <span class="class-text">族</span>
        </div>
      </label>
      <label class="field-card">
        <span class="info-label">政治面貌</span>
        <select
          v-model="info.politicalStatus"
          class="info-input"
          :disabled="!isEditing"
        >
          <option disabled value="">选择政治面貌</option>
          <option
            v-for="item in politicalStatusOptions"
            :key="item"
            :value="item"
          >
            {{ item }}
          </option>
        </select>
      </label>
      <label class="field-card field-full">
        <span class="info-label">手机号码 / 备用联系方式</span>
        <div class="class-inline contact-row">
          <input
            v-model="info.phone"
            class="info-input"
            type="tel"
            placeholder="手机号"
            maxlength="11"
            inputmode="numeric"
            :disabled="!isEditing"
            @input="handlePhoneDigitsInput"
          />
          <input
            v-model="info.backupContact"
            class="info-input"
            type="text"
            placeholder="微信/QQ/邮箱"
            :disabled="!isEditing"
          />
        </div>
      </label>
      <label class="field-card field-full">
        <span class="info-label">证件类型 / 证件号码</span>
        <div class="class-inline id-type-inline id-row">
          <select
            v-model="info.idType"
            class="info-input"
            :disabled="!isEditing"
          >
            <option v-for="item in idTypeOptions" :key="item" :value="item">
              {{ item }}
            </option>
          </select>
          <input
            v-model="info.idNo"
            class="info-input"
            type="text"
            placeholder="证件号码"
            :maxlength="idNoMaxLength"
            inputmode="text"
            :disabled="!isEditing"
            @input="handleIdNoInput"
          />
        </div>
        <div v-if="idNoHint" class="info-hint">
          {{ idNoHint }}，如与真实证件不符请联系管理员
        </div>
      </label>
      <label class="field-card">
        <span class="info-label">出生年月</span>
        <DateFieldInput
          v-model="info.birthDate"
          :max="today"
          :disabled="!isEditing"
        />
      </label>
      <label class="field-card">
        <!-- TODO: 做地址选择器 -->
        <span class="info-label">籍贯</span>
        <input
          v-model="info.nativePlace"
          class="info-input"
          type="text"
          placeholder="例：广东广州"
          :disabled="!isEditing"
        />
      </label>
      <label class="field-card field-full">
        <!-- TODO: 做地址选择器 -->
        <span class="info-label">住址</span>
        <div class="info-inline address-inline">
          <select
            v-model="info.addressProvince"
            class="info-input"
            :disabled="!isEditing"
          >
            <option disabled value="">选择省份</option>
            <option
              v-for="item in addressProvinceOptions"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </option>
          </select>
          <select
            v-model="info.addressCity"
            class="info-input"
            :disabled="!isEditing || !addressCityOptions.length"
          >
            <option disabled value="">选择城市</option>
            <option
              v-for="item in addressCityOptions"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </option>
          </select>
          <select
            v-model="info.addressCounty"
            class="info-input"
            :disabled="!isEditing || !addressCountyOptions.length"
          >
            <option disabled value="">选择区县</option>
            <option
              v-for="item in addressCountyOptions"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </option>
          </select>
        </div>
        <input
          v-model="info.addressDetail"
          class="info-input address-detail"
          type="text"
          placeholder="请输入详细地址，精确到门牌号"
          :disabled="!isEditing"
        />
      </label>
    </div>
  </div>
</template>
