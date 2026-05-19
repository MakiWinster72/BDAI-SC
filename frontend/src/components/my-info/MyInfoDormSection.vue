<script setup>
import { dormCampusOptions } from "@/constants/profileOptions";
import { useProfileRegionCascade } from "@/composables/useProfileRegionCascade";

const props = defineProps({
  info: {
    type: Object,
    required: true,
  },
  isEditing: {
    type: Boolean,
    default: false,
  },
  dormBuildingOptions: {
    type: Array,
    default: () => [],
  },
  dormBuildingDisabled: {
    type: Boolean,
    default: false,
  },
  dormRoomDisabled: {
    type: Boolean,
    default: false,
  },
});

const {
  provinceOptions: offCampusProvinceOptions,
  cityOptions: offCampusCityOptions,
  countyOptions: offCampusCountyOptions,
} = useProfileRegionCascade(props.info, "offCampus");
</script>

<template>
  <div class="card info-card">
    <div class="info-section-title">住宿信息</div>
    <div class="info-form-grid">
      <div class="field-card field-full">
        <span class="info-label">是否在外居住</span>
        <div class="info-inline">
          <label class="info-choice">
            <input
              v-model="info.offCampusLiving"
              type="radio"
              :value="true"
              :disabled="!isEditing"
            />
            是
          </label>
          <label class="info-choice">
            <input
              v-model="info.offCampusLiving"
              type="radio"
              :value="false"
              :disabled="!isEditing"
            />
            否
          </label>
        </div>
      </div>
      <label v-if="info.offCampusLiving" class="field-card field-full">
        <span class="info-label">外居住详细地址</span>
        <div class="info-inline address-inline">
          <select
            v-model="info.offCampusProvince"
            class="info-input"
            :disabled="!isEditing"
          >
            <option disabled value="">选择省份</option>
            <option
              v-for="item in offCampusProvinceOptions"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </option>
          </select>
          <select
            v-model="info.offCampusCity"
            class="info-input"
            :disabled="!isEditing || !offCampusCityOptions.length"
          >
            <option disabled value="">选择城市</option>
            <option
              v-for="item in offCampusCityOptions"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </option>
          </select>
          <select
            v-model="info.offCampusCounty"
            class="info-input"
            :disabled="!isEditing || !offCampusCountyOptions.length"
          >
            <option disabled value="">选择区县</option>
            <option
              v-for="item in offCampusCountyOptions"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </option>
          </select>
        </div>
        <input
          v-model="info.offCampusDetail"
          class="info-input address-detail"
          type="text"
          placeholder="请输入详细地址，精确到门牌号"
          :disabled="!isEditing"
        />
      </label>
      <label v-if="!info.offCampusLiving" class="field-card">
        <span class="info-label">住宿校区</span>
        <select
          v-model="info.dormCampus"
          class="info-input"
          :disabled="!isEditing || info.offCampusLiving"
        >
          <option disabled value="">选择住宿校区</option>
          <option
            v-for="item in dormCampusOptions"
            :key="item"
            :value="item"
          >
            {{ item }}
          </option>
        </select>
      </label>
      <label v-if="!info.offCampusLiving" class="field-card">
        <!-- TODO: 等待佩佩姐发文件 -->
        <span class="info-label">住宿楼栋</span>
        <select
          v-model="info.dormBuilding"
          class="info-input"
          :disabled="dormBuildingDisabled"
        >
          <option disabled value="">选择住宿楼栋</option>
          <option
            v-for="item in dormBuildingOptions"
            :key="item.value"
            :value="item.value"
            :disabled="item.disabled"
          >
            {{ item.label }}
          </option>
        </select>
      </label>
      <label v-if="!info.offCampusLiving" class="field-card field-full">
        <span class="info-label">住宿房间</span>
        <div class="class-inline">
          <input
            v-model="info.dormFloor"
            class="info-input class-num"
            type="number"
            min="1"
            step="1"
            placeholder="楼层"
            :disabled="dormRoomDisabled"
          />
          <span class="class-text">层</span>
          <input
            v-model="info.dormRoomNo"
            class="info-input"
            type="text"
            placeholder="房间号"
            :disabled="dormRoomDisabled"
          />
          <span class="class-text">号</span>
        </div>
        <div class="info-hint">如：223 -> 2 层 23 号</div>
      </label>
    </div>
  </div>
</template>
