<template>
  <div class="dashboard-layout">
    <transition name="publisher-backdrop">
      <div
        v-if="sidebarOpen"
        class="mobile-sidebar-backdrop"
        @click="closeSidebar"
      ></div>
    </transition>
    <aside class="dashboard-left" :class="{ open: sidebarOpen }">
      <section class="profile-card">
        <div class="profile-row profile-main">
          <div class="profile-avatar">
            <img
              v-if="info.avatarUrl"
              :src="resolveMediaUrl(info.avatarUrl)"
              alt="头像"
            />
            <span v-else>{{ avatarText }}</span>
          </div>
          <div class="profile-name-wrap">
            <p class="profile-name">
              {{ profile.displayName || profile.username || "同学" }}
            </p>
            <p class="profile-role">{{ roleLabel }}</p>
          </div>
          <button
            class="profile-settings"
            type="button"
            aria-label="设置"
            @click="goToSettings"
          >
            <img src="/assets/icons/settings.svg" alt="设置" />
          </button>
        </div>
        <div class="profile-row">学号：{{ profile.studentNo || "未填写" }}</div>
        <div class="profile-row">班级：{{ profile.className || "未填写" }}</div>
        <div class="profile-row">学院：{{ profile.college || "未填写" }}</div>
      </section>

      <section class="menu-card">
        <button
          v-for="item in menuItems"
          :key="item.key"
          class="menu-item"
          :class="{
            active: activeMenu === item.key,
            disabled: !isMenuEnabled(item.key),
          }"
          type="button"
          :disabled="!isMenuEnabled(item.key)"
          @click="handleMenuClick(item.key)"
        >
          {{ item.label }}
        </button>
      </section>
    </aside>

    <main class="dashboard-right">
      <header class="feed-header">
        <h1 class="feed-title">我的信息</h1>
      </header>

      <section class="info-shell">
        <div class="info-hero">
          <button
            class="avatar-square"
            type="button"
            :disabled="!isEditing"
            @click="triggerAvatarUpload"
          >
            <img
              v-if="info.avatarUrl"
              :src="resolveMediaUrl(info.avatarUrl)"
              alt="头像"
            />
            <span v-else>点击设置头像</span>
          </button>
          <input
            ref="avatarInput"
            type="file"
            accept="image/*"
            hidden
            @change="onAvatarChange"
          />
          <div class="info-hero-text">
            <div class="info-hero-title">基础信息</div>
            <div class="info-hero-subtitle">请使用真实照片，确保五官清晰。</div>
          </div>
          <div class="info-actions">
            <button class="ghost-button" type="button" @click="enterEdit">
              编辑
            </button>
            <button class="action-button" type="button" @click="confirmEdit">
              确认
            </button>
          </div>
        </div>

        <div class="info-card">
          <div class="info-section-title">学籍信息</div>
          <div class="info-form-grid">
            <label class="field-card">
              <span class="info-label">名字</span>
              <input
                v-model="info.name"
                class="info-input"
                type="text"
                placeholder="请输入名字"
                :disabled="!isEditing"
              />
            </label>
            <label class="field-card">
              <span class="info-label">学号</span>
              <input
                v-model="info.studentNo"
                class="info-input"
                type="text"
                placeholder="请输入学号"
                :disabled="!isEditing"
              />
            </label>
            <label class="field-card">
              <span class="info-label">年级</span>
              <select
                v-model="info.classYear"
                class="info-input"
                :disabled="!isEditing"
              >
                <option disabled value="">选择年级</option>
                <option
                  v-for="year in classYearOptions"
                  :key="year"
                  :value="year"
                >
                  {{ year }}
                </option>
              </select>
            </label>
            <label class="field-card">
              <span class="info-label">学院</span>
              <input
                v-model="info.college"
                class="info-input"
                type="text"
                disabled
              />
            </label>
            <label class="field-card field-full">
              <span class="info-label">班级</span>
              <div class="class-inline">
                <select
                  v-model="info.classMajor"
                  class="info-input"
                  :disabled="!isEditing || !classMajorOptions.length"
                >
                  <option disabled value="">选择专业</option>
                  <option
                    v-for="major in classMajorOptions"
                    :key="major"
                    :value="major"
                  >
                    {{ major }}
                  </option>
                </select>
                <input
                  v-model.number="info.classNo"
                  class="info-input class-num"
                  type="number"
                  min="1"
                  max="10"
                  step="1"
                  placeholder="数字"
                  :disabled="!isEditing"
                />
                <span class="class-text">班</span>
              </div>
            </label>
            <label class="field-card">
              <span class="info-label">入学时间</span>
              <input
                v-model="info.enrollmentDate"
                class="info-input"
                type="date"
                lang="zh-CN"
                :disabled="!isEditing"
              />
            </label>
            <label class="field-card">
              <span class="info-label">学生类别</span>
              <select
                v-model="info.studentCategory"
                class="info-input"
                :disabled="!isEditing"
              >
                <option disabled value="">选择学生类别</option>
                <option
                  v-for="item in studentCategoryOptions"
                  :key="item"
                  :value="item"
                >
                  {{ item }}
                </option>
              </select>
            </label>
            <label class="field-card">
              <span class="info-label">班主任</span>
              <input
                v-model="info.classTeacher"
                class="info-input"
                type="text"
                placeholder="请输入班主任"
                :disabled="!isEditing"
              />
            </label>
            <label class="field-card">
              <span class="info-label">辅导员</span>
              <input
                v-model="info.counselor"
                class="info-input"
                type="text"
                placeholder="请输入辅导员"
                :disabled="!isEditing"
              />
            </label>
          </div>
        </div>

        <div class="info-card">
          <div class="info-section-title">个人证件与联系方式</div>
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
            <label class="field-card">
              <span class="info-label">手机号码</span>
              <input
                v-model="info.phone"
                class="info-input"
                type="tel"
                placeholder="请输入手机号"
                maxlength="11"
                inputmode="numeric"
                @input="handleDigitsInput('phone', 11, $event)"
                :disabled="!isEditing"
              />
            </label>
            <label class="field-card">
              <span class="info-label">身份证件号</span>
              <input
                v-model="info.idNo"
                class="info-input"
                type="text"
                placeholder="请输入身份证件号"
                maxlength="18"
                inputmode="text"
                @input="handleIdNoInput"
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
              <input
                v-model="info.address"
                class="info-input"
                type="text"
                placeholder="请输入住址"
                :disabled="!isEditing"
              />
            </label>
          </div>
        </div>

        <div class="info-card">
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
            <label class="field-card field-full" v-if="info.offCampusLiving">
              <span class="info-label">外居住详细地址</span>
              <!-- TODO: 做地址选择器 -->
              <input
                v-model="info.offCampusAddress"
                class="info-input"
                type="text"
                placeholder="请输入详细地址"
                :disabled="!isEditing"
              />
            </label>
            <label class="field-card" v-if="!info.offCampusLiving">
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
            <label class="field-card" v-if="!info.offCampusLiving">
              <!-- TODO: 等待佩佩姐发文件 -->
              <span class="info-label">住宿楼栋</span>
              <input
                v-model="info.dormBuilding"
                class="info-input"
                type="text"
                placeholder="如：1号楼"
                :disabled="dormBuildingDisabled"
              />
            </label>
            <label class="field-card" v-if="!info.offCampusLiving">
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

        <div class="info-card">
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
            <label class="field-card">
              <span class="info-label">提交入团申请书时间</span>
              <input
                v-model="info.leagueApplicationDate"
                class="info-input"
                type="date"
                lang="zh-CN"
                :disabled="leagueApplicationDisabled"
              />
            </label>
            <label class="field-card">
              <span class="info-label">入团时间</span>
              <div class="info-inline info-inline-date">
                <input
                  v-model="info.leagueJoinDate"
                  class="info-input"
                  type="date"
                  lang="zh-CN"
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
            <label class="field-card">
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
            <label class="field-card">
              <span class="info-label">提交入党申请书时间</span>
              <input
                v-model="info.applicationDate"
                class="info-input"
                type="date"
                lang="zh-CN"
                :disabled="applicationDateDisabled"
              />
            </label>
            <label class="field-card">
              <span class="info-label">确定积极分子时间</span>
              <div class="info-inline info-inline-date">
                <input
                  v-model="info.activistDate"
                  class="info-input"
                  type="date"
                  lang="zh-CN"
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
            <label class="field-card">
              <span class="info-label">上党课时间</span>
              <div class="info-inline info-inline-date">
                <input
                  v-model="info.partyTrainingDate"
                  class="info-input"
                  type="date"
                  lang="zh-CN"
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
            <label class="field-card">
              <span class="info-label">确定发展对象时间</span>
              <div class="info-inline info-inline-date">
                <input
                  v-model="info.developmentTargetDate"
                  class="info-input"
                  type="date"
                  lang="zh-CN"
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
            <label class="field-card">
              <span class="info-label">接收为预备党员时间</span>
              <div class="info-inline info-inline-date">
                <input
                  v-model="info.probationaryMemberDate"
                  class="info-input"
                  type="date"
                  lang="zh-CN"
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
            <label class="field-card">
              <span class="info-label">转为正式党员时间</span>
              <div class="info-inline info-inline-date">
                <input
                  v-model="info.fullMemberDate"
                  class="info-input"
                  type="date"
                  lang="zh-CN"
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
          </div>
        </div>

        <div class="info-card">
          <div class="info-section-title">教育经历</div>
          <div class="info-hint">从小学开始填</div>
          <div ref="educationTableWrap" class="education-table-wrap">
            <table class="education-table">
              <thead>
                <tr>
                  <th>时间段</th>
                  <th>学校名称</th>
                  <th>学历</th>
                  <th>证明人</th>
                </tr>
              </thead>
              <transition-group name="education-row" tag="tbody">
                <tr v-for="(item, index) in educationItems" :key="`edu-${index}`">
                  <td>
                    <div class="education-period">
                      <input
                        v-model="item.startDate"
                        class="info-input"
                        type="date"
                        lang="zh-CN"
                        :disabled="isEducationRowDisabled(index)"
                      />
                      <span class="education-sep">至</span>
                      <input
                        v-model="item.endDate"
                        class="info-input"
                        type="date"
                        lang="zh-CN"
                        :disabled="isEducationRowDisabled(index) || item.isCurrent"
                      />
                      <label class="info-choice info-choice-muted">
                        <input
                          v-model="item.isCurrent"
                          type="checkbox"
                          :disabled="isEducationRowDisabled(index) || isEducationCurrentDisabled(item)"
                          @change="handleEducationCurrentChange(item, index)"
                        />
                        至今
                      </label>
                    </div>
                  </td>
                  <td>
                    <input
                      v-model="item.schoolName"
                      class="info-input"
                      type="text"
                      placeholder="学校名称"
                      :disabled="isEducationRowDisabled(index)"
                    />
                  </td>
                  <td>
                    <input
                      v-model="item.educationLevel"
                      class="info-input"
                      type="text"
                      placeholder="学历"
                      :disabled="isEducationRowDisabled(index)"
                    />
                  </td>
                  <td>
                    <input
                      v-model="item.witness"
                      class="info-input"
                      type="text"
                      placeholder="证明人"
                      :disabled="isEducationRowDisabled(index)"
                    />
                  </td>
                </tr>
              </transition-group>
            </table>
            <div class="education-controls-wrap">
              <div class="education-controls">
                <button
                  class="education-control"
                  type="button"
                  :disabled="!isEditing"
                  aria-label="增加一行"
                  @click="addEducationRow"
                >
                  +
                </button>
                <button
                  class="education-control"
                  type="button"
                  :disabled="!isEditing || educationItems.length <= 1"
                  aria-label="减少一行"
                  @click="removeEducationRow"
                >
                  −
                </button>
              </div>
            </div>
          </div>
        </div>

        <div class="info-card">
          <!-- TODO: 单亲/离异等待现场演示求助 -->
          <div class="info-section-title">家庭信息</div>
          <div class="info-form-grid family-grid">
            <div class="family-section-title">父亲</div>
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
                @input="handleDigitsInput('fatherPhone', 11, $event)"
                :disabled="!isEditing"
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
              <div class="info-hint">
                填写公司名字即可，若开小店则填写个体户，若无固定单位则填写散工，无业则写在家
              </div>
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
              <div class="info-hint">无业则填写“务农”</div>
            </label>
            <div class="family-section-title">母亲</div>
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
                @input="handleDigitsInput('motherPhone', 11, $event)"
                :disabled="!isEditing"
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

        <div class="info-card">
          <div class="info-section-title">紧急联系人</div>
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
                @input="handleDigitsInput('emergencyPhone', 11, $event)"
                :disabled="!isEditing"
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
      </section>
      <div class="mobile-capsule">
        <div class="capsule-left">
          <div
            class="capsule-action"
            role="button"
            tabindex="0"
            @click="openSidebar"
          >
            <span class="capsule-icon" aria-hidden="true">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path d="M4 6h16M4 12h16M4 18h16" />
              </svg>
            </span>
          </div>
        </div>
        <div class="capsule-right"></div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { reactive, computed, ref, onMounted, watch, nextTick } from "vue";
import { useRouter } from "vue-router";
import { filterMenuItemsByRole, isMenuEnabled } from "../constants/menu";
import { getStudentProfile, saveStudentProfile } from "../api/profile";
import { uploadMedia } from "../api/upload";
import { API_BASE } from "../api/request";

const router = useRouter();
const FIXED_COLLEGE = "大数据与人工智能学院";

const profile = reactive(loadUser());
const activeMenu = ref("my-info");
const isEditing = ref(false);
const avatarInput = ref(null);
const sidebarOpen = ref(false);
const educationTableWrap = ref(null);

const info = reactive({
  name: profile.displayName || profile.username || "",
  avatarUrl: profile.avatarUrl || "",
  studentNo: profile.studentNo || "",
  classYear: "",
  classMajor: "",
  classNo: 1,
  className: profile.className || "",
  college: FIXED_COLLEGE,
  enrollmentDate: "",
  studentCategory: "",
  ethnicity: "",
  politicalStatus: "",
  dormCampus: "",
  dormBuilding: "",
  dormRoom: "",
  dormFloor: "",
  dormRoomNo: "",
  offCampusLiving: false,
  offCampusAddress: "",
  classTeacher: "",
  counselor: "",
  phone: "",
  address: "",
  idNo: "",
  nativePlace: "",
  leagueNo: "",
  leagueApplicationDate: "",
  leagueJoinDate: "",
  leagueJoined: false,
  leagueDeveloping: false,
  partyApplied: false,
  notDeveloped: false,
  applicationDate: "",
  activistDate: "",
  activistDeveloping: false,
  partyTrainingDate: "",
  partyTrainingPending: false,
  developmentTargetDate: "",
  developmentTargetDeveloping: false,
  probationaryMemberDate: "",
  probationaryDeveloping: false,
  fullMemberDate: "",
  fullMemberDeveloping: false,
  emergencyPhone: "",
  emergencyRelation: "",
  fatherName: "",
  fatherPhone: "",
  fatherWorkUnit: "",
  fatherTitle: "",
  motherName: "",
  motherPhone: "",
  motherWorkUnit: "",
  motherTitle: "",
});

const classYearOptions = Array.from({ length: 19 }, (_, index) => 2022 + index);
const majorOptionsByCollege = {
  大数据与人工智能学院: [
    "计算机科学与技术",
    "计算机科学与技术（实验区）",
    "软件工程",
    "电子商务",
    "大数据管理与应用（佛山校区全学段）",
    "大数据管理与应用（数字治理）",
  ],
};
const studentCategoryOptions = ["本科", "研究生"];
const politicalStatusOptions = ["群众", "共青团员", "中共预备党员", "中共党员"];
const dormCampusOptions = ["佛山校区", "广州校区"];
const educationItems = reactive(
  Array.from({ length: 5 }, () => createEducationItem()),
);

function createEducationItem() {
  return {
    startDate: "",
    endDate: "",
    schoolName: "",
    educationLevel: "",
    witness: "",
    isCurrent: false,
  };
}

async function addEducationRow() {
  await animateEducationHeightWithUpdate(() => {
    educationItems.push(createEducationItem());
  });
}

async function removeEducationRow() {
  if (educationItems.length <= 1) {
    return;
  }
  await animateEducationHeightWithUpdate(() => {
    educationItems.pop();
  });
}

async function animateEducationHeightWithUpdate(updateFn) {
  const el = educationTableWrap.value;
  if (!el) {
    updateFn();
    return;
  }
  const startHeight = el.getBoundingClientRect().height;
  updateFn();
  await nextTick();
  const targetHeight = el.getBoundingClientRect().height;
  el.style.height = `${startHeight}px`;
  el.style.overflow = "hidden";
  requestAnimationFrame(() => {
    el.style.transition = "height 260ms ease";
    el.style.height = `${targetHeight}px`;
  });
  const cleanup = () => {
    el.style.height = "";
    el.style.transition = "";
    el.style.overflow = "";
    el.removeEventListener("transitionend", cleanup);
  };
  el.addEventListener("transitionend", cleanup);
}

const menuItems = computed(() => filterMenuItemsByRole(profile.role));

const avatarText = computed(() => {
  const name = profile.displayName || profile.username || "同学";
  return name.slice(0, 1).toUpperCase();
});

const roleLabel = computed(() => {
  if (profile.role === "ADMIN") {
    return "管理员";
  }
  if (profile.role === "TEACHER") {
    return "老师";
  }
  return "学生";
});

const classMajorOptions = computed(() => {
  return majorOptionsByCollege[info.college] || [];
});

const hasEducationCurrent = computed(() =>
  educationItems.some((entry) => entry.isCurrent),
);
const currentEducationIndex = computed(() =>
  educationItems.findIndex((entry) => entry.isCurrent),
);

function handleEducationCurrentChange(item, index) {
  if (item.isCurrent) {
    item.endDate = "";
    clearEducationRowsAfter(index);
  }
}

function isEducationCurrentDisabled(item) {
  if (item.isCurrent) {
    return false;
  }
  return hasEducationCurrent.value;
}

function isEducationRowDisabled(index) {
  if (!isEditing.value) {
    return true;
  }
  const currentIndex = currentEducationIndex.value;
  return currentIndex !== -1 && index > currentIndex;
}

function clearEducationRowsAfter(index) {
  educationItems.slice(index + 1).forEach((entry) => {
    entry.startDate = "";
    entry.endDate = "";
    entry.schoolName = "";
    entry.educationLevel = "";
    entry.witness = "";
    entry.isCurrent = false;
  });
}

const dormBuildingDisabled = computed(
  () => !isEditing.value || info.offCampusLiving || !info.dormCampus,
);
const dormRoomDisabled = computed(
  () => dormBuildingDisabled.value || !info.dormBuilding,
);

const leagueApplicationDisabled = computed(
  () => !isEditing.value || !info.leagueJoined,
);
const leagueJoinDisabled = computed(
  () =>
    leagueApplicationDisabled.value ||
    !info.leagueApplicationDate ||
    info.leagueDeveloping,
);
const leagueNoDisabled = computed(
  () =>
    !isEditing.value ||
    !info.leagueJoinDate ||
    info.leagueDeveloping ||
    !info.leagueJoined,
);
const partyAppliedDisabled = computed(
  () => !isEditing.value || !info.leagueJoinDate || info.leagueDeveloping,
);
const applicationDateDisabled = computed(
  () => !isEditing.value || partyAppliedDisabled.value || !info.partyApplied,
);
const activistDateDisabled = computed(
  () =>
    !isEditing.value ||
    !info.applicationDate ||
    info.activistDeveloping ||
    partyAppliedDisabled.value ||
    !info.partyApplied,
);
const partyTrainingDisabled = computed(
  () =>
    !isEditing.value ||
    !info.activistDate ||
    info.activistDeveloping ||
    info.partyTrainingPending ||
    partyAppliedDisabled.value ||
    !info.partyApplied,
);
const developmentTargetDisabled = computed(
  () =>
    !isEditing.value ||
    !info.partyTrainingDate ||
    info.partyTrainingPending ||
    info.developmentTargetDeveloping ||
    info.activistDeveloping ||
    partyAppliedDisabled.value ||
    !info.partyApplied,
);
const probationaryDisabled = computed(
  () =>
    !isEditing.value ||
    !info.developmentTargetDate ||
    info.developmentTargetDeveloping ||
    info.probationaryDeveloping ||
    info.partyTrainingPending ||
    info.activistDeveloping ||
    partyAppliedDisabled.value ||
    !info.partyApplied,
);
const fullMemberDisabled = computed(
  () =>
    !isEditing.value ||
    !info.probationaryMemberDate ||
    info.probationaryDeveloping ||
    info.fullMemberDeveloping ||
    info.developmentTargetDeveloping ||
    info.partyTrainingPending ||
    info.activistDeveloping ||
    partyAppliedDisabled.value ||
    !info.partyApplied,
);

function handleMenuClick(key) {
  if (!isMenuEnabled(key)) {
    return;
  }
  sidebarOpen.value = false;
  if (key === "my-info") {
    return;
  }
  if (key === "achievements") {
    router.push("/achievements");
    return;
  }
  if (key === "student-info") {
    router.push("/student-info");
    return;
  }
  router.push("/myinfos");
}

function openSidebar() {
  sidebarOpen.value = true;
}

function closeSidebar() {
  sidebarOpen.value = false;
}

function resolveMediaUrl(url) {
  if (!url) {
    return "";
  }
  if (url.startsWith("http://") || url.startsWith("https://")) {
    return url;
  }
  return `${API_BASE}${url}`;
}

function goToSettings() {
  router.push("/settings");
}

function handleDigitsInput(field, maxLength, event) {
  const raw = event.target.value || "";
  const next = raw.replace(/\D/g, "").slice(0, maxLength);
  info[field] = next;
}

function handleIdNoInput(event) {
  const raw = (event.target.value || "").toUpperCase();
  const cleaned = raw.replace(/[^0-9X]/g, "");
  const digits = cleaned.replace(/X/g, "").slice(0, 18);
  if (raw.endsWith("X")) {
    info.idNo = `${digits.slice(0, 17)}X`.slice(0, 18);
    return;
  }
  info.idNo = digits;
}

function triggerAvatarUpload() {
  avatarInput.value && avatarInput.value.click();
}

async function onAvatarChange(event) {
  const [file] = Array.from(event.target.files || []);
  event.target.value = "";
  if (!file) {
    return;
  }
  try {
    const { data } = await uploadMedia(file);
    if (data?.mediaType !== "IMAGE") {
      return;
    }
    info.avatarUrl = data.url || "";
  } catch (err) {
    console.error(err);
  }
}

function enterEdit() {
  isEditing.value = true;
}

async function confirmEdit() {
  const className = buildClassName(
    info.classYear,
    info.classMajor,
    info.classNo,
    info.className,
  );
  const dormRoom = buildDormRoom(
    info.dormFloor,
    info.dormRoomNo,
    info.dormRoom,
  );
  const educationExperiences = educationItems.map((item) => ({
    startDate: item.startDate,
    endDate: item.endDate,
    schoolName: item.schoolName,
    educationLevel: item.educationLevel,
    witness: item.witness,
    isCurrent: item.isCurrent,
  }));
  const payload = {
    fullName: info.name,
    avatarUrl: info.avatarUrl,
    studentNo: info.studentNo,
    classYear: info.classYear || null,
    classMajor: info.classMajor,
    classNo: info.classNo,
    className,
    college: FIXED_COLLEGE,
    enrollmentDate: info.enrollmentDate || null,
    studentCategory: info.studentCategory,
    ethnicity: info.ethnicity,
    politicalStatus: info.politicalStatus,
    dormCampus: info.dormCampus,
    dormBuilding: info.dormBuilding,
    dormRoom,
    offCampusLiving: info.offCampusLiving,
    offCampusAddress: info.offCampusAddress,
    classTeacher: info.classTeacher,
    counselor: info.counselor,
    phone: info.phone,
    address: info.address,
    idNo: info.idNo,
    nativePlace: info.nativePlace,
    leagueNo: info.leagueNo,
    leagueApplicationDate: info.leagueApplicationDate || null,
    leagueJoinDate: info.leagueJoinDate || null,
    leagueJoined: info.leagueJoined,
    leagueDeveloping: info.leagueDeveloping,
    partyApplied: info.partyApplied,
    notDeveloped: info.notDeveloped,
    applicationDate: info.applicationDate || null,
    activistDate: info.activistDate || null,
    activistDeveloping: info.activistDeveloping,
    partyTrainingDate: info.partyTrainingDate || null,
    partyTrainingPending: info.partyTrainingPending,
    developmentTargetDate: info.developmentTargetDate || null,
    developmentTargetDeveloping: info.developmentTargetDeveloping,
    probationaryMemberDate: info.probationaryMemberDate || null,
    probationaryDeveloping: info.probationaryDeveloping,
    fullMemberDate: info.fullMemberDate || null,
    fullMemberDeveloping: info.fullMemberDeveloping,
    emergencyPhone: info.emergencyPhone,
    emergencyRelation: info.emergencyRelation,
    fatherName: info.fatherName,
    fatherPhone: info.fatherPhone,
    fatherWorkUnit: info.fatherWorkUnit,
    fatherTitle: info.fatherTitle,
    motherName: info.motherName,
    motherPhone: info.motherPhone,
    motherWorkUnit: info.motherWorkUnit,
    motherTitle: info.motherTitle,
    educationExperiences,
  };
  if (info.offCampusLiving) {
    payload.dormCampus = null;
    payload.dormBuilding = null;
    payload.dormRoom = null;
  } else {
    payload.offCampusAddress = null;
  }
  if (leagueApplicationDisabled.value) {
    payload.leagueApplicationDate = null;
  }
  if (leagueJoinDisabled.value) {
    payload.leagueJoinDate = null;
  }
  if (leagueNoDisabled.value) {
    payload.leagueNo = null;
  }
  if (partyAppliedDisabled.value) {
    payload.partyApplied = false;
  }
  if (applicationDateDisabled.value) {
    payload.applicationDate = null;
  }
  if (activistDateDisabled.value) {
    payload.activistDate = null;
  }
  if (partyTrainingDisabled.value) {
    payload.partyTrainingDate = null;
  }
  if (developmentTargetDisabled.value) {
    payload.developmentTargetDate = null;
  }
  if (probationaryDisabled.value) {
    payload.probationaryMemberDate = null;
  }
  if (fullMemberDisabled.value) {
    payload.fullMemberDate = null;
  }
  try {
    const { data } = await saveStudentProfile(payload);
    applyProfileResponse(data);
    isEditing.value = false;
  } catch (err) {
    console.error(err);
  }
}

function buildClassName(year, major, no, fallback) {
  if (fallback) {
    return fallback;
  }
  const safeYear = year ? `${year}级` : "";
  const safeMajor = major || "";
  const safeNo = no ? `${no}班` : "";
  const result = `${safeYear}${safeMajor}${safeNo}`.trim();
  return result || "";
}

function buildDormRoom(floor, roomNo, fallback) {
  const safeFloor = String(floor || "").trim();
  const safeRoomNo = String(roomNo || "").trim();
  if (safeFloor || safeRoomNo) {
    return `${safeFloor}层${safeRoomNo}号`.trim();
  }
  return fallback || "";
}

function parseDormRoom(rawValue) {
  const raw = String(rawValue || "").trim();
  if (!raw) {
    return { floor: "", roomNo: "" };
  }
  const digits = raw.replace(/\D/g, "");
  if (!digits) {
    return { floor: "", roomNo: "" };
  }
  if (digits.length <= 2) {
    return { floor: "", roomNo: digits };
  }
  const floor = digits.slice(0, digits.length - 2);
  const roomNo = digits.slice(-2);
  return { floor, roomNo };
}

function applyProfileResponse(data) {
  if (!data) {
    return;
  }
  info.name = data.fullName || data.displayName || "";
  info.avatarUrl = data.avatarUrl || profile.avatarUrl || "";
  info.studentNo = data.studentNo || "";
  info.classYear = data.classYear || "";
  info.classMajor = data.classMajor || "";
  info.classNo = data.classNo ?? 1;
  info.className = data.className || "";
  info.college = FIXED_COLLEGE;
  info.enrollmentDate = data.enrollmentDate || "";
  info.studentCategory = data.studentCategory || "";
  info.ethnicity = data.ethnicity || "";
  info.politicalStatus = data.politicalStatus || "";
  info.dormCampus = data.dormCampus || "";
  info.dormBuilding = data.dormBuilding || "";
  info.dormRoom = data.dormRoom || "";
  const parsedDormRoom = parseDormRoom(info.dormRoom);
  info.dormFloor = parsedDormRoom.floor;
  info.dormRoomNo = parsedDormRoom.roomNo;
  info.offCampusLiving = Boolean(data.offCampusLiving);
  info.offCampusAddress = data.offCampusAddress || "";
  info.classTeacher = data.classTeacher || "";
  info.counselor = data.counselor || "";
  info.phone = data.phone || "";
  info.address = data.address || "";
  info.idNo = data.idNo || "";
  info.nativePlace = data.nativePlace || "";
  info.leagueNo = data.leagueNo || "";
  info.leagueApplicationDate = data.leagueApplicationDate || "";
  info.leagueJoinDate = data.leagueJoinDate || "";
  info.leagueJoined = Boolean(data.leagueJoined);
  info.leagueDeveloping = Boolean(data.leagueDeveloping);
  info.partyApplied = Boolean(data.partyApplied);
  info.notDeveloped = Boolean(data.notDeveloped);
  info.applicationDate = data.applicationDate || "";
  info.activistDate = data.activistDate || "";
  info.activistDeveloping = Boolean(data.activistDeveloping);
  info.partyTrainingDate = data.partyTrainingDate || "";
  info.partyTrainingPending = Boolean(data.partyTrainingPending);
  info.developmentTargetDate = data.developmentTargetDate || "";
  info.developmentTargetDeveloping = Boolean(data.developmentTargetDeveloping);
  info.probationaryMemberDate = data.probationaryMemberDate || "";
  info.probationaryDeveloping = Boolean(data.probationaryDeveloping);
  info.fullMemberDate = data.fullMemberDate || "";
  info.fullMemberDeveloping = Boolean(data.fullMemberDeveloping);
  info.emergencyPhone = data.emergencyPhone || "";
  info.emergencyRelation = data.emergencyRelation || "";
  info.fatherName = data.fatherName || "";
  info.fatherPhone = data.fatherPhone || "";
  info.fatherWorkUnit = data.fatherWorkUnit || "";
  info.fatherTitle = data.fatherTitle || "";
  info.motherName = data.motherName || "";
  info.motherPhone = data.motherPhone || "";
  info.motherWorkUnit = data.motherWorkUnit || "";
  info.motherTitle = data.motherTitle || "";
  applyEducationExperiences(data.educationExperiences);

  profile.displayName = data.displayName || profile.displayName;
  profile.username = data.username || profile.username;
  profile.avatarUrl = data.avatarUrl || profile.avatarUrl;
  profile.studentNo = data.studentNo || profile.studentNo;
  profile.className = data.className || profile.className;
  profile.college = FIXED_COLLEGE;

  saveUser(profile);
}

function applyEducationExperiences(rawItems) {
  const nextItems = Array.isArray(rawItems) ? rawItems : [];
  const normalized = nextItems.map((item) => ({
    startDate: item?.startDate || "",
    endDate: item?.isCurrent ? "" : item?.endDate || "",
    schoolName: item?.schoolName || "",
    educationLevel: item?.educationLevel || "",
    witness: item?.witness || "",
    isCurrent: Boolean(item?.isCurrent),
  }));
  while (normalized.length < 5) {
    normalized.push(createEducationItem());
  }
  educationItems.splice(0, educationItems.length, ...normalized);
}

function saveUser(data) {
  const user = {
    username: data.username,
    displayName: data.displayName,
    avatarUrl: data.avatarUrl || "",
    role: data.role || profile.role || "STUDENT",
    studentNo: data.studentNo || "",
    className: data.className || "",
    college: FIXED_COLLEGE,
  };
  localStorage.setItem("gcsc_user", JSON.stringify(user));
}

onMounted(async () => {
  try {
    const { data } = await getStudentProfile();
    applyProfileResponse(data);
  } catch (err) {
    console.error(err);
  }
});

watch(
  () => info.offCampusLiving,
  (next) => {
    if (next) {
      info.dormCampus = "";
      info.dormBuilding = "";
      info.dormRoom = "";
      info.dormFloor = "";
      info.dormRoomNo = "";
    } else {
      info.offCampusAddress = "";
    }
  },
);

watch(
  () => info.college,
  (college) => {
    if (!majorOptionsByCollege[college]) {
      info.classMajor = "";
      return;
    }
    if (!majorOptionsByCollege[college].includes(info.classMajor)) {
      info.classMajor = "";
    }
  },
);

watch(
  () => info.leagueDeveloping,
  (next) => {
    if (!next) {
      return;
    }
    info.leagueJoinDate = "";
    info.leagueNo = "";
    info.partyApplied = false;
    info.applicationDate = "";
    info.activistDate = "";
    info.partyTrainingDate = "";
    info.developmentTargetDate = "";
    info.probationaryMemberDate = "";
    info.fullMemberDate = "";
    info.activistDeveloping = false;
    info.partyTrainingPending = false;
    info.developmentTargetDeveloping = false;
    info.probationaryDeveloping = false;
    info.fullMemberDeveloping = false;
  },
);

watch(
  () => info.activistDeveloping,
  (next) => {
    if (!next) {
      return;
    }
    info.activistDate = "";
    info.partyTrainingDate = "";
    info.developmentTargetDate = "";
    info.probationaryMemberDate = "";
    info.fullMemberDate = "";
    info.partyTrainingPending = false;
    info.developmentTargetDeveloping = false;
    info.probationaryDeveloping = false;
    info.fullMemberDeveloping = false;
  },
);

watch(
  () => info.partyTrainingPending,
  (next) => {
    if (!next) {
      return;
    }
    info.partyTrainingDate = "";
    info.developmentTargetDate = "";
    info.probationaryMemberDate = "";
    info.fullMemberDate = "";
    info.developmentTargetDeveloping = false;
    info.probationaryDeveloping = false;
    info.fullMemberDeveloping = false;
  },
);

watch(
  () => info.developmentTargetDeveloping,
  (next) => {
    if (!next) {
      return;
    }
    info.developmentTargetDate = "";
    info.probationaryMemberDate = "";
    info.fullMemberDate = "";
    info.probationaryDeveloping = false;
    info.fullMemberDeveloping = false;
  },
);

watch(
  () => info.probationaryDeveloping,
  (next) => {
    if (!next) {
      return;
    }
    info.probationaryMemberDate = "";
    info.fullMemberDate = "";
    info.fullMemberDeveloping = false;
  },
);

watch(
  () => info.fullMemberDeveloping,
  (next) => {
    if (!next) {
      return;
    }
    info.fullMemberDate = "";
  },
);


function loadUser() {
  try {
    const raw = JSON.parse(localStorage.getItem("gcsc_user") || "{}");
    return {
      username: raw.username || "",
      displayName: raw.displayName || "",
      avatarUrl: raw.avatarUrl || "",
      role: raw.role || "STUDENT",
      studentNo: raw.studentNo || "",
      className: raw.className || "",
      college: FIXED_COLLEGE,
    };
  } catch {
    return {
      username: "",
      displayName: "",
      avatarUrl: "",
      role: "STUDENT",
      studentNo: "",
      className: "",
      college: FIXED_COLLEGE,
    };
  }
}
</script>
