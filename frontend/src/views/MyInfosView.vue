<template>
  <main class="dashboard-right">
    <header class="feed-header">
      <h1 class="feed-title">我的信息</h1>
    </header>

    <section class="info-shell" :class="{ 'info-shell-editing': isEditing }">
      <ProfileFormHeroSection
        :info="info"
        :is-editing="isEditing"
        :upload-file="uploadProfileAvatar"
      />

      <!-- 桌面端悬浮操作栏 -->
      <div class="info-floating-bar">
        <transition name="float-btn" mode="out-in">
          <div v-if="isEditing" class="float-btn-group" key="editing">
            <button class="ghost-button" type="button" @click="cancelEdit">
              取消
            </button>
            <button class="action-button" type="button" @click="confirmEdit">
              {{ saveActionLabel }}
            </button>
          </div>
          <div v-else class="float-btn-group" key="viewing">
            <ExportPdfButton
              :get-student="buildPdfStudentSnapshot"
              :resolve-media-url="resolveMediaUrl"
              button-class="ghost-button"
              @export-complete="toastSuccess('PDF 导出成功！')"
              @export-error="toastError('PDF 导出失败')"
            />
            <button
              class="action-button"
              type="button"
              @click="enterEdit"
            >
              编辑
            </button>
          </div>
        </transition>
      </div>

      <ProfileFormSchoolSection
        :info="info"
        :is-editing="isEditing"
        :today="today"
        :student-category-options="studentCategoryOptions"
        :class-major-options="classMajorOptions"
      />

      <ProfileFormIdentitySection
        :info="info"
        :is-editing="isEditing"
        :today="today"
      />

      <ProfileFormDormSection
        :info="info"
        :is-editing="isEditing"
        :dorm-building-options="dormBuildingOptions"
        :dorm-building-disabled="dormBuildingDisabled"
        :dorm-room-disabled="dormRoomDisabled"
      />

      <ProfileFormPartySection
        :info="info"
        :is-editing="isEditing"
        :today="today"
        :league-application-disabled="leagueApplicationDisabled"
        :league-join-disabled="leagueJoinDisabled"
        :league-no-disabled="leagueNoDisabled"
        :party-applied-disabled="partyAppliedDisabled"
        :application-date-disabled="applicationDateDisabled"
        :activist-date-disabled="activistDateDisabled"
        :party-training-disabled="partyTrainingDisabled"
        :development-target-disabled="developmentTargetDisabled"
        :probationary-disabled="probationaryDisabled"
        :full-member-disabled="fullMemberDisabled"
      />

      <ProfileFormEducationSection
        :items="educationItems"
        :is-editing="isEditing"
        :today="today"
      />

      <ProfileFormCadreSection
        :items="cadreItems"
        :is-editing="isEditing"
        :today="today"
      />

      <ProfileFormFamilySection :info="info" :is-editing="isEditing" />

    </section>
    <MobileCapsule @open-sidebar="openDashboardSidebar">
      <template v-if="isEditing" #right>
        <div class="capsule-action" @click="cancelEdit">取消</div>
        <div class="capsule-primary" @click="confirmEdit">
          {{ saveActionLabel }}
        </div>
      </template>
      <template v-else #right>
        <ExportPdfButton
          :get-student="buildPdfStudentSnapshot"
          :resolve-media-url="resolveMediaUrl"
          button-class="ghost-button capsule-ghost"
          @export-complete="toastSuccess('PDF 导出成功！')"
          @export-error="toastError('PDF 导出失败')"
        />
        <button
          class="action-button capsule-action-button"
          type="button"
          @click="enterEdit"
        >
          编辑
        </button>
      </template>
    </MobileCapsule>
  </main>
</template>

<script setup>
import { reactive, computed, ref, onMounted } from "vue";
import ExportPdfButton from "@/components/ExportPdfButton.vue";
import MobileCapsule from "@/components/MobileCapsule.vue";
import ProfileFormHeroSection from "@/components/profile-form/ProfileFormHeroSection.vue";
import ProfileFormIdentitySection from "@/components/profile-form/ProfileFormIdentitySection.vue";
import ProfileFormDormSection from "@/components/profile-form/ProfileFormDormSection.vue";
import ProfileFormSchoolSection from "@/components/profile-form/ProfileFormSchoolSection.vue";
import ProfileFormPartySection from "@/components/profile-form/ProfileFormPartySection.vue";
import ProfileFormEducationSection from "@/components/profile-form/ProfileFormEducationSection.vue";
import ProfileFormCadreSection from "@/components/profile-form/ProfileFormCadreSection.vue";
import ProfileFormFamilySection from "@/components/profile-form/ProfileFormFamilySection.vue";
import {
  FIXED_COLLEGE,
  majorOptionsByCategory,
  studentCategoryOptions,
} from "@/constants/profileOptions";
import { getStudentProfile, saveStudentProfile } from "@/api/profile";
import { uploadMedia } from "@/api/upload";
import { useUploadProgress } from "@/composables/useUploadProgress";
import {
  createProfileExperienceRows,
  createProfileInfo,
  isCadreRowEmpty,
  isEducationRowEmpty,
} from "@/composables/useProfileFormModel";
import { useProfileApplier } from "@/composables/useProfileApplier";
import { useProfileCategoryWatch } from "@/composables/useProfileCategoryWatch";
import { useProfileDormFields } from "@/composables/useProfileDormFields";
import { useProfilePartyFields } from "@/composables/useProfilePartyFields";
import { useProfileSnapshot } from "@/composables/useProfileSnapshot";
import { resolveMediaUrl } from "@/utils/media";
import { getProfileTodayString } from "@/utils/profileFormDate";
import { buildProfileSavePayload } from "@/utils/profilePayload";
import { useDashboardShell } from "@/composables/useDashboardShell";
import { useNotifications } from "@/composables/useNotifications";
import { useReviewSettings } from "@/composables/useReviewSettings";
import { useToast } from "@/composables/useToast";
import { loadUser } from "@/utils/userStorage";
import {
  checkProfileSaveRequiresReview,
  getProfileSaveActionLabel,
} from "@/config/profileReviewConfig";

const { openSidebar: openDashboardSidebar } = useDashboardShell();

const profile = reactive({ ...loadUser(), college: FIXED_COLLEGE });
const isEditing = ref(false);
const today = getProfileTodayString();
const originalProfileData = ref(null);
const savedProfileData = ref(null);
const {
  submitProfileReviewRequest,
  updateReviewRequestStatus,
  fetchProfileReviewRequests,
  hasPendingProfileReviewRequest,
} = useNotifications(profile);
const { settings: reviewSettings, fetchSettings: fetchReviewSettings } =
  useReviewSettings();
const { success: toastSuccess, error: toastError } = useToast();
const { uploadWithProgress } = useUploadProgress();

function uploadProfileAvatar(file) {
  return uploadWithProgress(file, uploadMedia);
}

const info = reactive(
  createProfileInfo({
    name: profile.displayName || profile.username || "",
    avatarUrl: profile.avatarUrl || "",
    studentNo: profile.studentNo || "",
    classYear: new Date().getFullYear(),
    className: profile.className || "",
  }),
);

const {
  leagueApplicationDisabled,
  leagueJoinDisabled,
  leagueNoDisabled,
  partyAppliedDisabled,
  applicationDateDisabled,
  activistDateDisabled,
  partyTrainingDisabled,
  developmentTargetDisabled,
  probationaryDisabled,
  fullMemberDisabled,
  sanitizePartyPayload,
} = useProfilePartyFields(info, isEditing);

const PROFILE_CHANGE_FIELDS = [
  { key: "fullName", label: "姓名", section: "学籍信息" },
  { key: "avatarUrl", label: "头像", section: "基础信息" },
  { key: "studentNo", label: "学号", section: "学籍信息" },
  { key: "classYear", label: "年级", section: "学籍信息" },
  { key: "classMajor", label: "专业", section: "学籍信息" },
  { key: "classNo", label: "班级", section: "学籍信息" },
  { key: "className", label: "班级名称", section: "学籍信息" },
  { key: "enrollmentDate", label: "入学时间", section: "学籍信息" },
  { key: "studentCategory", label: "学生类别", section: "学籍信息" },
  { key: "ethnicity", label: "民族", section: "联系方式" },
  { key: "politicalStatus", label: "政治面貌", section: "联系方式" },
  { key: "phone", label: "手机号码", section: "联系方式" },
  { key: "backupContact", label: "备用联系方式", section: "联系方式" },
  { key: "address", label: "家庭住址", section: "联系方式" },
  { key: "idType", label: "证件类型", section: "联系方式" },
  { key: "idNo", label: "证件号码", section: "联系方式" },
  { key: "birthDate", label: "出生年月", section: "联系方式" },
  { key: "nativePlace", label: "籍贯", section: "联系方式" },
  { key: "dormCampus", label: "宿舍校区", section: "住宿信息" },
  { key: "dormBuilding", label: "宿舍楼栋", section: "住宿信息" },
  { key: "dormRoom", label: "宿舍房间", section: "住宿信息" },
  { key: "offCampusLiving", label: "是否校外居住", section: "住宿信息" },
  { key: "offCampusAddress", label: "校外住址", section: "住宿信息" },
  { key: "classTeacher", label: "班主任", section: "学籍信息" },
  { key: "counselor", label: "辅导员", section: "学籍信息" },
  { key: "leagueJoined", label: "是否入团", section: "党团信息" },
  { key: "leagueNo", label: "团员编号", section: "党团信息" },
  { key: "leagueApplicationDate", label: "入团申请时间", section: "党团信息" },
  { key: "leagueJoinDate", label: "入团时间", section: "党团信息" },
  { key: "partyApplied", label: "是否申请入党", section: "党团信息" },
  { key: "applicationDate", label: "入党申请时间", section: "党团信息" },
  { key: "activistDate", label: "积极分子时间", section: "党团信息" },
  { key: "partyTrainingDate", label: "党校培训时间", section: "党团信息" },
  { key: "developmentTargetDate", label: "发展对象时间", section: "党团信息" },
  { key: "probationaryMemberDate", label: "预备党员时间", section: "党团信息" },
  { key: "fullMemberDate", label: "转正时间", section: "党团信息" },
  { key: "emergencyPhone", label: "紧急联系人电话", section: "家庭信息" },
  { key: "emergencyRelation", label: "与紧急联系人关系", section: "家庭信息" },
  { key: "isHk", label: "香港身份", section: "身份信息" },
  { key: "isMo", label: "澳门身份", section: "身份信息" },
  { key: "isTw", label: "台湾身份", section: "身份信息" },
  { key: "fatherName", label: "父亲姓名", section: "家庭信息" },
  { key: "fatherPhone", label: "父亲电话", section: "家庭信息" },
  { key: "fatherWorkUnit", label: "父亲工作单位", section: "家庭信息" },
  { key: "fatherTitle", label: "父亲职务", section: "家庭信息" },
  { key: "motherName", label: "母亲姓名", section: "家庭信息" },
  { key: "motherPhone", label: "母亲电话", section: "家庭信息" },
  { key: "motherWorkUnit", label: "母亲工作单位", section: "家庭信息" },
  { key: "motherTitle", label: "母亲职务", section: "家庭信息" },
];
const saveActionLabel = computed(() =>
  getProfileSaveActionLabel(
    reviewSettings.profileReviewEnabled,
    profile.role || "STUDENT",
  ),
);

const { educationItems, cadreItems } = createProfileExperienceRows();

const classMajorOptions = computed(
  () => majorOptionsByCategory[info.studentCategory] || [],
);

const {
  dormBuildingOptions,
  dormBuildingDisabled,
  dormRoomDisabled,
} = useProfileDormFields(info, isEditing);

useProfileCategoryWatch(info);

const { applyProfileResponse: applyProfileFromApi } = useProfileApplier({
  info,
  educationItems,
  cadreItems,
});

const { buildPdfStudentSnapshot, buildCurrentProfileState } = useProfileSnapshot({
  info,
  profile,
  educationItems,
  cadreItems,
  isEducationRowEmpty,
  isCadreRowEmpty,
  includeSpecialStudentFields: false,
});


function enterEdit() {
  if (hasPendingProfileReviewRequest.value) {
    toastError("个人信息正在审核，请等待审核结果或前往通知页取消申请");
    return;
  }
  originalProfileData.value = buildCurrentProfileState();
  isEditing.value = true;
}

function cancelEdit() {
  if (originalProfileData.value) {
    applyProfileResponse(originalProfileData.value, {
      syncSavedProfile: false,
    });
  }
  isEditing.value = false;
}

async function confirmEdit() {
  if (info.classYear && !info.classMajor) {
    toastError("请先选择专业");
    return;
  }
  if (info.classYear && !info.classNo) {
    toastError("请先选择班级");
    return;
  }
  await fetchReviewSettings().catch(() => {});
  const requiresReview = checkProfileSaveRequiresReview(
    reviewSettings.profileReviewEnabled,
    profile.role || "STUDENT",
  );
  const payload = buildProfileSavePayload({
    info,
    educationItems,
    cadreItems,
    sanitizePartyPayload,
    includeSpecialStudentFields: false,
  });
  const changes = buildProfileChanges(originalProfileData.value, payload);
  try {
    if (requiresReview) {
      const { data: requestData } = await submitProfileReviewRequest({
        actor: profile.username,
        payloadSnapshot: payload,
        changes,
      });
      await fetchProfileReviewRequests(true);
      if (
        reviewSettings.profileReviewAutoApprove &&
        requestData?.status === "approved"
      ) {
        const updatedProfile = await getStudentProfile();
        applyProfileResponse(updatedProfile.data);
        toastSuccess("个人信息已更新");
      } else {
        toastSuccess("已提交审核，请等待审核结果");
      }
      isEditing.value = false;
      return;
    }
    const { data } = await saveStudentProfile(payload);
    applyProfileResponse(data);
    toastSuccess("保存成功");
    isEditing.value = false;
  } catch {
    // 错误文案由 request 拦截器统一 toast
  }
}

function buildProfileChanges(previousState, nextState) {
  const changes = PROFILE_CHANGE_FIELDS.reduce((list, field) => {
    const before = stringifyProfileChangeValue(previousState?.[field.key]);
    const after = stringifyProfileChangeValue(nextState?.[field.key]);
    if (before === after) {
      return list;
    }
    list.push({
      section: field.section,
      label: field.label,
      before,
      after,
    });
    return list;
  }, []);

  const previousEducation = stringifyProfileCollection(
    previousState?.educationExperiences,
  );
  const nextEducation = stringifyProfileCollection(
    nextState?.educationExperiences,
  );
  if (previousEducation !== nextEducation) {
    changes.push({
      section: "教育经历",
      label: "教育经历",
      before: previousEducation,
      after: nextEducation,
    });
  }

  const previousCadre = stringifyProfileCollection(
    previousState?.cadreExperiences,
  );
  const nextCadre = stringifyProfileCollection(nextState?.cadreExperiences);
  if (previousCadre !== nextCadre) {
    changes.push({
      section: "学生干部经历",
      label: "学生干部经历",
      before: previousCadre,
      after: nextCadre,
    });
  }

  return changes;
}

function stringifyProfileCollection(items) {
  const list = Array.isArray(items) ? items.filter(Boolean) : [];
  if (!list.length) {
    return "-";
  }
  const firstItem = list[0] || {};
  if (
    "schoolName" in firstItem ||
    "educationLevel" in firstItem ||
    "witness" in firstItem
  ) {
    return list.map(formatEducationExperienceItem).filter(Boolean).join("\n");
  }
  if (
    "department" in firstItem ||
    "position" in firstItem ||
    "description" in firstItem
  ) {
    return list.map(formatCadreExperienceItem).filter(Boolean).join("\n");
  }
  return list
    .map((item) =>
      Object.entries(item)
        .map(([, value]) => stringifyProfileChangeValue(value))
        .filter((value) => value !== "-")
        .join(" / "),
    )
    .filter(Boolean)
    .join("\n");
}

function formatEducationExperienceItem(item, index) {
  const period = formatPeriodText(
    item?.startDate,
    item?.endDate,
    item?.isCurrent,
  );
  const schoolName = stringifyProfileChangeValue(item?.schoolName);
  const educationLevel = stringifyProfileChangeValue(item?.educationLevel);
  const witness = stringifyProfileChangeValue(item?.witness);
  return [
    `第${index + 1}条`,
    `时间：${period}`,
    `学校名称：${schoolName}`,
    `学历：${educationLevel}`,
    `证明人：${witness}`,
  ].join("\n");
}

function formatCadreExperienceItem(item, index) {
  const period = formatPeriodText(
    item?.startDate,
    item?.endDate,
    item?.isCurrent,
  );
  const department = stringifyProfileChangeValue(item?.department);
  const position = stringifyProfileChangeValue(item?.position);
  const description = stringifyProfileChangeValue(item?.description);
  return [
    `第${index + 1}条`,
    `起止时间：${period}`,
    `班级/社团部门：${department}`,
    `职位：${position}`,
    `职责说明：${description}`,
  ].join("\n");
}

function formatPeriodText(startDate, endDate, isCurrent) {
  const start = stringifyProfileChangeValue(startDate);
  if (isCurrent) {
    return `${start} 至今`;
  }
  const end = stringifyProfileChangeValue(endDate);
  return `${start} - ${end}`;
}

function stringifyProfileChangeValue(value) {
  if (typeof value === "boolean") {
    return value ? "是" : "否";
  }
  if (Array.isArray(value)) {
    return stringifyProfileCollection(value);
  }
  const text = String(value ?? "").trim();
  return text || "-";
}

function handleProfileApplied(data, options = {}) {
  if (!data) {
    return;
  }
  const { syncSavedProfile = true } = options;
  profile.displayName = data.displayName || profile.displayName;
  profile.username = data.username || profile.username;
  profile.avatarUrl = data.avatarUrl || profile.avatarUrl;
  profile.role = data.role || profile.role;
  profile.studentNo = data.studentNo || profile.studentNo;
  profile.className = data.className || profile.className;
  profile.college = FIXED_COLLEGE;
  if (syncSavedProfile) {
    savedProfileData.value = data;
    originalProfileData.value = data;
  }
  saveUser(profile);
}

function applyProfileResponse(data, options = {}) {
  applyProfileFromApi(data, {
    ...options,
    classYearFallback: "current-year",
    avatarUrlFallback: profile.avatarUrl || "",
    studentNoFallback: profile.studentNo || "",
    onApplied: handleProfileApplied,
  });
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
  localStorage.setItem("bdai_sc_user", JSON.stringify(user));
}

onMounted(async () => {
  try {
    await Promise.all([
      fetchReviewSettings().catch(() => {}),
      getStudentProfile().then(({ data }) => applyProfileResponse(data)),
    ]);
  } catch {
    // 错误文案由 request 拦截器统一 toast
  }
});
</script>

<style scoped>
@import "@/assets/styles/my-infos-view.css";

.float-btn-group {
  display: flex;
  gap: 10px;
  align-items: center;
}

.float-btn-enter-active,
.float-btn-leave-active {
  transition: opacity 200ms ease, transform 200ms cubic-bezier(0.22, 1, 0.36, 1);
}

.float-btn-enter-from {
  opacity: 0;
  transform: translateY(8px) scale(0.96);
}

.float-btn-leave-to {
  opacity: 0;
  transform: translateY(-8px) scale(0.96);
}

.class-num {
  width: 76px !important;
  flex-shrink: 0;
}

@media (max-width: 480px) {
  .contact-row {
    flex-direction: column;
    gap: 8px;
  }

  .contact-row > .info-input:first-child {
    width: 100%;
    flex: unset;
  }

  .contact-row > .info-input:last-child {
    width: 100%;
    flex: unset;
  }

  .id-row {
    flex-direction: column;
    gap: 8px;
  }

  .id-row > .info-input,
  .id-row > select.info-input {
    width: 100%;
    flex: unset;
  }
}

@media (max-width: 840px) {
  .info-actions {
    display: none;
  }
}

.capsule-ghost {
  height: 36px !important;
  padding: 0 12px !important;
  font-size: 13px !important;
}

.capsule-action-button {
  height: 36px !important;
  padding: 0 12px !important;
  font-size: 13px !important;
}
</style>
