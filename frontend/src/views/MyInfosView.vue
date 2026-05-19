<template>
  <main class="dashboard-right">
    <header class="feed-header">
      <h1 class="feed-title">我的信息</h1>
    </header>

    <section class="info-shell" :class="{ 'info-shell-editing': isEditing }">
      <MyInfoHeroSection
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

      <MyInfoSchoolSection
        :info="info"
        :is-editing="isEditing"
        :today="today"
        :student-category-options="studentCategoryOptions"
        :class-major-options="classMajorOptions"
      />

      <MyInfoIdentitySection
        :info="info"
        :is-editing="isEditing"
        :today="today"
      />

      <MyInfoDormSection
        :info="info"
        :is-editing="isEditing"
        :dorm-building-options="dormBuildingOptions"
        :dorm-building-disabled="dormBuildingDisabled"
        :dorm-room-disabled="dormRoomDisabled"
      />

      <MyInfoPartySection
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

      <MyInfoEducationSection
        :items="educationItems"
        :is-editing="isEditing"
        :today="today"
      />

      <MyInfoCadreSection
        :items="cadreItems"
        :is-editing="isEditing"
        :today="today"
      />

      <MyInfoFamilySection :info="info" :is-editing="isEditing" />

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
import { reactive, computed, ref, onMounted, watch } from "vue";
import ExportPdfButton from "@/components/ExportPdfButton.vue";
import MobileCapsule from "@/components/MobileCapsule.vue";
import MyInfoHeroSection from "@/components/my-info/MyInfoHeroSection.vue";
import MyInfoIdentitySection from "@/components/my-info/MyInfoIdentitySection.vue";
import MyInfoDormSection from "@/components/my-info/MyInfoDormSection.vue";
import MyInfoSchoolSection from "@/components/my-info/MyInfoSchoolSection.vue";
import MyInfoPartySection from "@/components/my-info/MyInfoPartySection.vue";
import MyInfoEducationSection from "@/components/my-info/MyInfoEducationSection.vue";
import MyInfoCadreSection from "@/components/my-info/MyInfoCadreSection.vue";
import MyInfoFamilySection from "@/components/my-info/MyInfoFamilySection.vue";
import {
  FIXED_COLLEGE,
  majorOptionsByCategory,
  studentCategoryOptions,
} from "@/constants/profileOptions";
import { getStudentProfile, saveStudentProfile } from "@/api/profile";
import { uploadMedia } from "@/api/upload";
import { useUploadProgress } from "@/composables/useUploadProgress";
import {
  buildCadrePayload,
  buildEducationPayload,
  createProfileExperienceRows,
  createProfileInfo,
  isCadreRowEmpty,
  isEducationRowEmpty,
  normalizeCadreExperiences,
  normalizeEducationExperiences,
} from "@/composables/useProfileFormModel";
import { resolveMediaUrl } from "@/utils/media";
import {
  buildClassName,
  buildDormRoom,
  buildAddress,
  parseAddressToRegion,
  parseDormRoom,
} from "@/utils/profile";
import { useDashboardShell } from "@/composables/useDashboardShell";
import { useNotifications } from "@/composables/useNotifications";
import { useReviewSettings } from "@/composables/useReviewSettings";
import { useProfilePartyFields } from "@/composables/useProfilePartyFields";
import { useToast } from "@/composables/useToast";
import { loadUser } from "@/utils/userStorage";

const { openSidebar: openDashboardSidebar } = useDashboardShell();

const profile = reactive({ ...loadUser(), college: FIXED_COLLEGE });
const isEditing = ref(false);
const today = getTodayString();
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
  { key: "specialStudent", label: "特殊学生", section: "身份信息" },
  { key: "specialStudentType", label: "特殊学生类型", section: "身份信息" },
  { key: "specialStudentRemark", label: "特殊学生备注", section: "身份信息" },
  { key: "fatherName", label: "父亲姓名", section: "家庭信息" },
  { key: "fatherPhone", label: "父亲电话", section: "家庭信息" },
  { key: "fatherWorkUnit", label: "父亲工作单位", section: "家庭信息" },
  { key: "fatherTitle", label: "父亲职务", section: "家庭信息" },
  { key: "motherName", label: "母亲姓名", section: "家庭信息" },
  { key: "motherPhone", label: "母亲电话", section: "家庭信息" },
  { key: "motherWorkUnit", label: "母亲工作单位", section: "家庭信息" },
  { key: "motherTitle", label: "母亲职务", section: "家庭信息" },
];
const hasSavedProfileBefore = computed(() =>
  Boolean(savedProfileData.value?.id),
);
const isReviewer = computed(
  () => profile.role === "ADMIN" || profile.role === "TEACHER",
);
const saveActionLabel = computed(() => {
  if (isReviewer.value) return "保存";
  return hasSavedProfileBefore.value && reviewSettings.profileReviewEnabled
    ? "请求审核"
    : "保存";
});

const dormBuildingOptions = computed(() => {
  if (info.dormCampus === "佛山校区") {
    return [
      ...Array.from({ length: 21 }, (_, index) => {
        const label = `${index + 1}号楼`;
        return { label, value: label };
      }),
      { label: "有为9栋", value: "有为9栋" },
      { label: "有为21栋", value: "有为21栋" },
      {
        label: "教师公寓（请选择校外居住）",
        value: "教师公寓",
        disabled: true,
      },
    ];
  }
  if (info.dormCampus === "广州校区") {
    return [
      ...Array.from({ length: 16 }, (_, index) => {
        const label = `${index + 17}号楼`;
        return { label, value: label };
      }),
      { label: "凌云楼", value: "凌云楼" },
      { label: "揽月楼", value: "揽月楼" },
      { label: "丽枫酒店", value: "丽枫酒店" },
    ];
  }
  return [];
});
const { educationItems, cadreItems } = createProfileExperienceRows();

function getTodayString() {
  const now = new Date();
  const offsetMs = now.getTimezoneOffset() * 60 * 1000;
  return new Date(now.getTime() - offsetMs).toISOString().slice(0, 10);
}

const classMajorOptions = computed(() => {
  return majorOptionsByCategory[info.studentCategory] || [];
});

const dormBuildingDisabled = computed(
  () => !isEditing.value || info.offCampusLiving || !info.dormCampus,
);
const dormRoomDisabled = computed(
  () => dormBuildingDisabled.value || !info.dormBuilding,
);


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
  const requiresReview =
    hasSavedProfileBefore.value &&
    reviewSettings.profileReviewEnabled &&
    !isReviewer.value;
  const className = buildClassName(
    info.classYear,
    info.classMajor,
    info.classNo,
    info.className,
  );
  const address = buildAddress(
    info.addressProvince,
    info.addressCity,
    info.addressCounty,
    info.addressDetail,
    info.address,
  );
  const offCampusAddress = buildAddress(
    info.offCampusProvince,
    info.offCampusCity,
    info.offCampusCounty,
    info.offCampusDetail,
    info.offCampusAddress,
  );
  const dormRoom = buildDormRoom(
    info.dormFloor,
    info.dormRoomNo,
    info.dormRoom,
  );
  const educationExperiences = buildEducationPayload(educationItems);
  const cadreExperiences = buildCadrePayload(cadreItems);
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
    offCampusAddress,
    classTeacher: info.classTeacher,
    counselor: info.counselor,
    phone: info.phone,
    backupContact: info.backupContact,
    address,
    idType: info.idType,
    idNo: info.idNo,
    birthDate: info.birthDate || null,
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
    isHk: info.isHk,
    isMo: info.isMo,
    isTw: info.isTw,
    specialStudent: info.specialStudent,
    specialStudentType: info.specialStudentType || "",
    specialStudentRemark: info.specialStudentRemark || "",
    educationExperiences,
    cadreExperiences,
  };
  if (info.offCampusLiving) {
    payload.dormCampus = null;
    payload.dormBuilding = null;
    payload.dormRoom = null;
  } else {
    payload.offCampusAddress = null;
  }
  sanitizePartyPayload(payload);
  const changes = buildProfileChanges(originalProfileData.value, payload);
  try {
    if (requiresReview) {
      const { data: requestData } = await submitProfileReviewRequest({
        actor: profile.value?.username,
        payloadSnapshot: payload,
        changes,
      });
      if (
        reviewSettings.profileReviewAutoApprove &&
        requestData?.status === "approved"
      ) {
        await fetchProfileReviewRequests(true);
        const updatedProfile = await getStudentProfile();
        applyProfileResponse(updatedProfile.data);
      }
      isEditing.value = false;
      return;
    }
    const { data } = await saveStudentProfile(payload);
    applyProfileResponse(data);
    isEditing.value = false;
  } catch (err) {
    console.error(err);
  }
}

function buildPdfStudentSnapshot() {
  const studentName =
    info.name || profile.displayName || profile.username || "";
  const studentNo = info.studentNo || profile.studentNo || "";
  const className = buildClassName(
    info.classYear,
    info.classMajor,
    info.classNo,
    info.className,
  );
  const addressText = buildAddress(
    info.addressProvince,
    info.addressCity,
    info.addressCounty,
    info.addressDetail,
    info.address,
  );
  const offCampusAddress = buildAddress(
    info.offCampusProvince,
    info.offCampusCity,
    info.offCampusCounty,
    info.offCampusDetail,
    info.offCampusAddress,
  );
  const educationExperiences = buildEducationPayload(educationItems);
  const cadreExperiences = buildCadrePayload(cadreItems);
  return {
    fullName: studentName,
    studentNo,
    classYear: info.classYear,
    classMajor: info.classMajor,
    classNo: info.classNo,
    className,
    college: info.college,
    enrollmentDate: info.enrollmentDate,
    studentCategory: info.studentCategory,
    classTeacher: info.classTeacher,
    counselor: info.counselor,
    ethnicity: info.ethnicity,
    politicalStatus: info.politicalStatus,
    phone: info.phone,
    backupContact: info.backupContact,
    idType: info.idType,
    idNo: info.idNo,
    birthDate: info.birthDate,
    nativePlace: info.nativePlace,
    address: addressText,
    dormCampus: info.dormCampus,
    dormBuilding: info.dormBuilding,
    dormRoom: info.dormRoom,
    offCampusLiving: info.offCampusLiving,
    offCampusAddress,
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
    leagueNo: info.leagueNo,
    leagueApplicationDate: info.leagueApplicationDate,
    leagueJoinDate: info.leagueJoinDate,
    leagueJoined: info.leagueJoined,
    leagueDeveloping: info.leagueDeveloping,
    partyApplied: info.partyApplied,
    notDeveloped: info.notDeveloped,
    applicationDate: info.applicationDate,
    activistDate: info.activistDate,
    activistDeveloping: info.activistDeveloping,
    partyTrainingDate: info.partyTrainingDate,
    partyTrainingPending: info.partyTrainingPending,
    developmentTargetDate: info.developmentTargetDate,
    developmentTargetDeveloping: info.developmentTargetDeveloping,
    probationaryMemberDate: info.probationaryMemberDate,
    probationaryDeveloping: info.probationaryDeveloping,
    fullMemberDate: info.fullMemberDate,
    fullMemberDeveloping: info.fullMemberDeveloping,
    isHk: info.isHk,
    isMo: info.isMo,
    isTw: info.isTw,
    specialStudent: info.specialStudent,
    specialStudentType: info.specialStudentType,
    specialStudentRemark: info.specialStudentRemark,
    educationExperiences,
    cadreExperiences,
    avatarUrl: profile.avatarUrl,
  };
}

function buildCurrentProfileState() {
  return {
    fullName: info.name,
    avatarUrl: info.avatarUrl,
    studentNo: info.studentNo,
    classYear: info.classYear || null,
    classMajor: info.classMajor,
    classNo: info.classNo,
    className: buildClassName(
      info.classYear,
      info.classMajor,
      info.classNo,
      info.className,
    ),
    college: info.college,
    enrollmentDate: info.enrollmentDate || null,
    studentCategory: info.studentCategory,
    ethnicity: info.ethnicity,
    politicalStatus: info.politicalStatus,
    dormCampus: info.dormCampus,
    dormBuilding: info.dormBuilding,
    dormRoom: buildDormRoom(info.dormFloor, info.dormRoomNo, info.dormRoom),
    offCampusLiving: info.offCampusLiving,
    offCampusAddress: buildAddress(
      info.offCampusProvince,
      info.offCampusCity,
      info.offCampusCounty,
      info.offCampusDetail,
      info.offCampusAddress,
    ),
    classTeacher: info.classTeacher,
    counselor: info.counselor,
    phone: info.phone,
    backupContact: info.backupContact,
    address: buildAddress(
      info.addressProvince,
      info.addressCity,
      info.addressCounty,
      info.addressDetail,
      info.address,
    ),
    idType: info.idType,
    idNo: info.idNo,
    birthDate: info.birthDate || null,
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
    isHk: info.isHk,
    isMo: info.isMo,
    isTw: info.isTw,
    specialStudent: info.specialStudent,
    specialStudentType: info.specialStudentType,
    specialStudentRemark: info.specialStudentRemark,
    educationExperiences: buildEducationPayload(educationItems),
    cadreExperiences: buildCadrePayload(cadreItems),
  };
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

function applyProfileResponse(data, options = {}) {
  if (!data) {
    return;
  }
  const { syncSavedProfile = true } = options;
  info.name = data.fullName || data.displayName || "";
  info.avatarUrl = data.avatarUrl || profile.avatarUrl || "";
  info.studentNo = data.studentNo || profile.studentNo || "";
  info.classYear = data.classYear || new Date().getFullYear();
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
  const parsedOffCampusAddress = parseAddressToRegion(info.offCampusAddress);
  info.offCampusProvince = parsedOffCampusAddress.province;
  info.offCampusCity = parsedOffCampusAddress.city;
  info.offCampusCounty = parsedOffCampusAddress.county;
  info.offCampusDetail = parsedOffCampusAddress.detail;
  info.classTeacher = data.classTeacher || "";
  info.counselor = data.counselor || "";
  info.phone = data.phone || "";
  info.backupContact = data.backupContact || "";
  info.address = data.address || "";
  const parsedAddress = parseAddressToRegion(info.address);
  info.addressProvince = parsedAddress.province;
  info.addressCity = parsedAddress.city;
  info.addressCounty = parsedAddress.county;
  info.addressDetail = parsedAddress.detail;
  info.idType = data.idType || "居民身份证";
  info.idNo = data.idNo || "";
  info.birthDate = data.birthDate || "";
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
  info.isHk = Boolean(data.isHk);
  info.isMo = Boolean(data.isMo);
  info.isTw = Boolean(data.isTw);
  info.specialStudent = Boolean(data.specialStudent);
  info.specialStudentType = data.specialStudentType || "";
  info.specialStudentRemark = data.specialStudentRemark || "";
  applyEducationExperiences(data.educationExperiences);
  applyCadreExperiences(data.cadreExperiences);

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

function applyEducationExperiences(rawItems) {
  educationItems.splice(
    0,
    educationItems.length,
    ...normalizeEducationExperiences(rawItems),
  );
}

function applyCadreExperiences(rawItems) {
  cadreItems.splice(0, cadreItems.length, ...normalizeCadreExperiences(rawItems));
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
  fetchReviewSettings().catch(() => {});
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
      info.offCampusProvince = "";
      info.offCampusCity = "";
      info.offCampusCounty = "";
      info.offCampusDetail = "";
    }
  },
);

watch(
  () => info.studentCategory,
  (category) => {
    if (!majorOptionsByCategory[category]) {
      info.classMajor = "";
      return;
    }
    if (!majorOptionsByCategory[category].includes(info.classMajor)) {
      info.classMajor = "";
    }
    if (category === "研究生") {
      info.classNo = 1;
    }
  },
);

watch(
  () => info.dormCampus,
  () => {
    if (!info.dormCampus) {
      info.dormBuilding = "";
      return;
    }
    const exists = dormBuildingOptions.value.some(
      (item) => item.value === info.dormBuilding && !item.disabled,
    );
    if (!exists) {
      info.dormBuilding = "";
    }
  },
);


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
