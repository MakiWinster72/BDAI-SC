import { reactive } from "vue";
import { FIXED_COLLEGE } from "../constants/profileOptions";

export function createProfileInfo(overrides = {}) {
  return {
    name: "",
    avatarUrl: "",
    studentNo: "",
    classYear: "",
    classMajor: "",
    classNo: 1,
    className: "",
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
    backupContact: "",
    address: "",
    addressProvince: "",
    addressCity: "",
    addressCounty: "",
    addressDetail: "",
    offCampusProvince: "",
    offCampusCity: "",
    offCampusCounty: "",
    offCampusDetail: "",
    idType: "居民身份证",
    idNo: "",
    birthDate: "",
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
    isHk: false,
    isMo: false,
    isTw: false,
    specialStudent: false,
    specialStudentType: "",
    specialStudentRemark: "",
    fatherName: "",
    fatherPhone: "",
    fatherWorkUnit: "",
    fatherTitle: "",
    motherName: "",
    motherPhone: "",
    motherWorkUnit: "",
    motherTitle: "",
    ...overrides,
  };
}

export function createEducationItem() {
  return {
    startDate: "",
    endDate: "",
    schoolName: "",
    educationLevel: "",
    witness: "",
    isCurrent: false,
  };
}

export function createCadreItem() {
  return {
    startDate: "",
    endDate: "",
    department: "",
    position: "",
    description: "",
    isCurrent: false,
  };
}

export function createProfileExperienceRows(count = 5) {
  return {
    educationItems: reactive(Array.from({ length: count }, () => createEducationItem())),
    cadreItems: reactive(Array.from({ length: count }, () => createCadreItem())),
  };
}

export function isEducationRowEmpty(entry) {
  return (
    !entry.startDate &&
    !entry.endDate &&
    !entry.schoolName &&
    !entry.educationLevel &&
    !entry.witness &&
    !entry.isCurrent
  );
}

export function isCadreRowEmpty(entry) {
  return (
    !entry.startDate &&
    !entry.endDate &&
    !entry.department &&
    !entry.position &&
    !entry.description &&
    !entry.isCurrent
  );
}

export function normalizeEducationExperiences(rawItems) {
  const nextItems = Array.isArray(rawItems) ? rawItems : [];
  const normalized = nextItems.map((item) => ({
    startDate: item?.startDate || "",
    endDate: item?.isCurrent ? "" : item?.endDate || "",
    schoolName: item?.schoolName || "",
    educationLevel: item?.educationLevel || "",
    witness: item?.witness || "",
    isCurrent: Boolean(item?.isCurrent),
  }));
  const filtered = normalized.filter((item) => !isEducationRowEmpty(item));
  return filtered.length ? filtered : [createEducationItem()];
}

export function normalizeCadreExperiences(rawItems) {
  const nextItems = Array.isArray(rawItems) ? rawItems : [];
  const normalized = nextItems.map((item) => ({
    startDate: item?.startDate || "",
    endDate: item?.isCurrent ? "" : item?.endDate || "",
    department: item?.department || "",
    position: item?.position || "",
    description: item?.description || "",
    isCurrent: Boolean(item?.isCurrent),
  }));
  const filtered = normalized.filter((item) => !isCadreRowEmpty(item));
  return filtered.length ? filtered : [createCadreItem()];
}

export function buildEducationPayload(items) {
  return items
    .filter((item) => !isEducationRowEmpty(item))
    .map((item) => ({
      startDate: item.startDate,
      endDate: item.endDate,
      schoolName: item.schoolName,
      educationLevel: item.educationLevel,
      witness: item.witness,
      isCurrent: item.isCurrent,
    }));
}

export function buildCadrePayload(items) {
  return items
    .filter((item) => !isCadreRowEmpty(item))
    .map((item) => ({
      startDate: item.startDate,
      endDate: item.endDate,
      department: item.department,
      position: item.position,
      description: item.description,
      isCurrent: item.isCurrent,
    }));
}
