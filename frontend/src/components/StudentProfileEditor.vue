<script setup>
import { computed, reactive, ref, watch } from "vue";
import ExportPdfButton from "./ExportPdfButton.vue";
import ProfileFormHeroSection from "@/components/profile-form/ProfileFormHeroSection.vue";
import ProfileFormSchoolSection from "@/components/profile-form/ProfileFormSchoolSection.vue";
import ProfileFormIdentitySection from "@/components/profile-form/ProfileFormIdentitySection.vue";
import ProfileFormDormSection from "@/components/profile-form/ProfileFormDormSection.vue";
import ProfileFormPartySection from "@/components/profile-form/ProfileFormPartySection.vue";
import ProfileFormEducationSection from "@/components/profile-form/ProfileFormEducationSection.vue";
import ProfileFormCadreSection from "@/components/profile-form/ProfileFormCadreSection.vue";
import ProfileFormFamilySection from "@/components/profile-form/ProfileFormFamilySection.vue";
import ProfileFormSpecialStudentSection from "@/components/profile-form/ProfileFormSpecialStudentSection.vue";
import { uploadMedia } from "@/api/upload";
import { useProfileApplier } from "@/composables/useProfileApplier";
import { useProfileCategoryWatch } from "@/composables/useProfileCategoryWatch";
import { useProfileDormFields } from "@/composables/useProfileDormFields";
import {
  createProfileExperienceRows,
  createProfileInfo,
  isCadreRowEmpty,
  isEducationRowEmpty,
} from "@/composables/useProfileFormModel";
import { useProfilePartyFields } from "@/composables/useProfilePartyFields";
import { useProfileSnapshot } from "@/composables/useProfileSnapshot";
import { useToast } from "@/composables/useToast";
import { useStudentPdfExport } from "@/composables/useStudentPdfExport";
import {
  FIXED_COLLEGE,
  majorOptionsByCategory,
  studentCategoryOptions,
} from "@/constants/profileOptions";
import { getProfileTodayString } from "@/utils/profileFormDate";
import { buildProfileSavePayload } from "@/utils/profilePayload";

const props = defineProps({
  student: {
    type: Object,
    default: null,
  },
  resolveMediaUrl: {
    type: Function,
    required: true,
  },
  saveProfile: {
    type: Function,
    default: null,
  },
  canEdit: {
    type: Boolean,
    default: false,
  },
  showAchievements: {
    type: Boolean,
    default: false,
  },
  editing: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(["saved", "openAchievements", "start-edit", "cancel-edit"]);
const { success: toastSuccess, error: toastError } = useToast();
const { exportResumePdf } = useStudentPdfExport();

const info = reactive(createProfileInfo());
const isEditing = ref(false);
const saving = ref(false);
const today = getProfileTodayString();
const originalProfileData = ref(null);
const { educationItems, cadreItems } = createProfileExperienceRows();

const classMajorOptions = computed(
  () => majorOptionsByCategory[info.studentCategory] || [],
);

const { applyProfileResponse } = useProfileApplier({
  info,
  educationItems,
  cadreItems,
});

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

const {
  dormBuildingOptions,
  dormBuildingDisabled,
  dormRoomDisabled,
} = useProfileDormFields(info, isEditing);

useProfileCategoryWatch(info);

const emptyProfile = { displayName: "", username: "", studentNo: "" };
const { buildPdfStudentSnapshot, buildCurrentProfileState } = useProfileSnapshot({
  info,
  profile: emptyProfile,
  educationItems,
  cadreItems,
  isEducationRowEmpty,
  isCadreRowEmpty,
});

function uploadProfileAvatar(file) {
  return uploadMedia(file);
}

watch(
  () => props.student,
  (student) => {
    applyProfileResponse(student, { classYearFallback: "empty" });
    isEditing.value = false;
  },
  { immediate: true },
);

watch(
  () => props.editing,
  (editing) => {
    if (editing && !isEditing.value) {
      enterEdit();
    } else if (!editing && isEditing.value) {
      cancelEdit();
    }
  },
);

function enterEdit() {
  if (!props.canEdit) {
    return;
  }
  originalProfileData.value = buildCurrentProfileState();
  isEditing.value = true;
  emit("start-edit");
}

function cancelEdit() {
  if (originalProfileData.value) {
    applyProfileResponse(originalProfileData.value, { classYearFallback: "empty" });
  } else {
    applyProfileResponse(props.student, { classYearFallback: "empty" });
  }
  isEditing.value = false;
  emit("cancel-edit");
}

async function confirmEdit() {
  if (!props.saveProfile || saving.value) {
    return;
  }
  saving.value = true;
  const payload = buildProfileSavePayload({
    info,
    educationItems,
    cadreItems,
    sanitizePartyPayload,
  });
  const { data } = await props.saveProfile(payload).finally(() => {
    saving.value = false;
  });
  applyProfileResponse(data, { classYearFallback: "empty" });
  originalProfileData.value = data || null;
  isEditing.value = false;
  emit("saved", data);
}

async function triggerPdfExport() {
  const student = buildPdfStudentSnapshot();
  if (!student) {
    toastError("PDF 导出失败");
    return;
  }
  await exportResumePdf({
    student,
    resolveMediaUrl: props.resolveMediaUrl,
  })
    .then(() => toastSuccess("PDF 导出成功！"))
    .catch(() => toastError("PDF 导出失败"));
}

const avatarPlaceholder = computed(() =>
  info.name ? info.name.slice(0, 1) : "点击设置头像",
);

defineExpose({
  triggerSave: confirmEdit,
  cancelEdit,
  triggerPdfExport,
});
</script>

<template>
  <section
    class="info-shell student-profile-editor"
    :class="{ 'info-shell-editing': isEditing }"
  >
    <ProfileFormHeroSection
      :info="info"
      :is-editing="isEditing"
      :upload-file="uploadProfileAvatar"
      :avatar-placeholder="avatarPlaceholder"
    >
      <template #subtitle>
        学号：{{ info.studentNo || "未填写" }}
      </template>
      <template #actions>
        <button
          v-if="showAchievements && !isEditing"
          class="ghost-button"
          type="button"
          @click="emit('openAchievements')"
        >
          个人成就
        </button>
        <ExportPdfButton
          v-if="!isEditing"
          :get-student="buildPdfStudentSnapshot"
          :resolve-media-url="resolveMediaUrl"
          button-class="ghost-button"
          @export-complete="toastSuccess('PDF 导出成功！')"
          @export-error="toastError('PDF 导出失败')"
        />
        <button
          v-if="canEdit && !isEditing"
          class="ghost-button"
          type="button"
          @click="enterEdit"
        >
          编辑信息
        </button>
      </template>
    </ProfileFormHeroSection>

    <ProfileFormSchoolSection
      :info="info"
      :is-editing="isEditing"
      :today="today"
      :student-category-options="studentCategoryOptions"
      :class-major-options="classMajorOptions"
      student-no-editable
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

    <ProfileFormSpecialStudentSection :info="info" :is-editing="isEditing" />

    <transition name="edit-dock">
      <div v-if="isEditing" class="edit-dock">
        <div class="edit-dock-inner">
          <button class="ghost-button" type="button" @click="cancelEdit">
            取消
          </button>
          <button
            class="action-button"
            type="button"
            :disabled="saving"
            @click="confirmEdit"
          >
            {{ saving ? "保存中..." : "保存" }}
          </button>
        </div>
      </div>
    </transition>
  </section>
</template>
