<script setup>
import DateFieldInput from "@/components/DateFieldInput.vue";

defineProps({
  info: { type: Object, required: true },
  isEditing: { type: Boolean, default: false },
  today: { type: String, required: true },
  leagueApplicationDisabled: { type: Boolean, default: false },
  leagueJoinDisabled: { type: Boolean, default: false },
  leagueNoDisabled: { type: Boolean, default: false },
  partyAppliedDisabled: { type: Boolean, default: false },
  applicationDateDisabled: { type: Boolean, default: false },
  activistDateDisabled: { type: Boolean, default: false },
  partyTrainingDisabled: { type: Boolean, default: false },
  developmentTargetDisabled: { type: Boolean, default: false },
  probationaryDisabled: { type: Boolean, default: false },
  fullMemberDisabled: { type: Boolean, default: false },
});
</script>

<template>
  <div class="card info-card">
    <div class="info-section-title">团组织与入党信息</div>
    <div class="info-form-grid three">
      <div class="field-card field-full">
        <span class="info-label">是否入团</span>
        <div class="info-inline">
          <label class="info-choice">
            <input
              v-model="info.leagueJoined"
              type="radio"
              :value="true"
              :disabled="!isEditing"
            />
            是
          </label>
          <label class="info-choice">
            <input
              v-model="info.leagueJoined"
              type="radio"
              :value="false"
              :disabled="!isEditing"
            />
            否
          </label>
        </div>
      </div>
      <template v-if="info.leagueJoined">
        <label class="field-card field-full">
          <span class="info-label">提交入团申请书时间</span>
          <DateFieldInput
            v-model="info.leagueApplicationDate"
            :max="today"
            :disabled="leagueApplicationDisabled"
          />
        </label>
        <label class="field-card field-full">
          <span class="info-label">入团时间</span>
          <div class="info-inline">
            <DateFieldInput
              v-model="info.leagueJoinDate"
              :max="today"
              :disabled="leagueJoinDisabled"
            />
            <label class="info-choice info-choice-muted">
              <input
                v-model="info.leagueDeveloping"
                type="checkbox"
                :disabled="leagueApplicationDisabled"
              />
              正在发展
            </label>
          </div>
        </label>
        <label class="field-card field-full">
          <span class="info-label">团号</span>
          <input
            v-model="info.leagueNo"
            class="info-input"
            type="text"
            placeholder="请输入团号"
            :disabled="leagueNoDisabled"
          />
        </label>
        <div class="field-card field-full">
          <span class="info-label">是否申请入党</span>
          <div class="info-inline">
            <label class="info-choice">
              <input
                v-model="info.partyApplied"
                type="radio"
                :value="true"
                :disabled="partyAppliedDisabled"
              />
              是
            </label>
            <label class="info-choice">
              <input
                v-model="info.partyApplied"
                type="radio"
                :value="false"
                :disabled="partyAppliedDisabled"
              />
              否
            </label>
          </div>
        </div>
        <template v-if="info.partyApplied">
          <label class="field-card field-full">
            <span class="info-label">提交入党申请书时间</span>
            <DateFieldInput
              v-model="info.applicationDate"
              :max="today"
              :disabled="applicationDateDisabled"
            />
          </label>
          <label class="field-card field-full">
            <span class="info-label">确定积极分子时间</span>
            <div class="info-inline">
              <DateFieldInput
                v-model="info.activistDate"
                :max="today"
                :disabled="activistDateDisabled"
              />
              <label class="info-choice info-choice-muted">
                <input
                  v-model="info.activistDeveloping"
                  type="checkbox"
                  :disabled="applicationDateDisabled"
                />
                正在发展
              </label>
            </div>
          </label>
          <label class="field-card field-full">
            <span class="info-label">上党课时间</span>
            <div class="info-inline">
              <DateFieldInput
                v-model="info.partyTrainingDate"
                :max="today"
                :disabled="partyTrainingDisabled"
              />
              <label class="info-choice info-choice-muted">
                <input
                  v-model="info.partyTrainingPending"
                  type="checkbox"
                  :disabled="activistDateDisabled"
                />
                暂未报名
              </label>
            </div>
          </label>
          <label class="field-card field-full">
            <span class="info-label">确定发展对象时间</span>
            <div class="info-inline">
              <DateFieldInput
                v-model="info.developmentTargetDate"
                :max="today"
                :disabled="developmentTargetDisabled"
              />
              <label class="info-choice info-choice-muted">
                <input
                  v-model="info.developmentTargetDeveloping"
                  type="checkbox"
                  :disabled="partyTrainingDisabled"
                />
                正在发展
              </label>
            </div>
          </label>
          <label class="field-card field-full">
            <span class="info-label">接收为预备党员时间</span>
            <div class="info-inline">
              <DateFieldInput
                v-model="info.probationaryMemberDate"
                :max="today"
                :disabled="probationaryDisabled"
              />
              <label class="info-choice info-choice-muted">
                <input
                  v-model="info.probationaryDeveloping"
                  type="checkbox"
                  :disabled="developmentTargetDisabled"
                />
                正在发展
              </label>
            </div>
          </label>
          <label class="field-card field-full">
            <span class="info-label">转为正式党员时间</span>
            <div class="info-inline">
              <DateFieldInput
                v-model="info.fullMemberDate"
                :max="today"
                :disabled="fullMemberDisabled"
              />
              <label class="info-choice info-choice-muted">
                <input
                  v-model="info.fullMemberDeveloping"
                  type="checkbox"
                  :disabled="probationaryDisabled"
                />
                正在发展
              </label>
            </div>
          </label>
        </template>
      </template>
    </div>
  </div>
</template>
