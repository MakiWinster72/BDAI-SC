<script setup>
import { computed, reactive, ref, watch } from "vue";
import { regionData, codeToText } from "element-china-area-data";
import ExportPdfButton from "./ExportPdfButton.vue";
import { uploadMedia } from "../api/upload";
import { useToast } from "../composables/useToast";
import {
  buildClassName,
  buildDormRoom,
  buildAddress,
  parseAddressToRegion,
  parseDormRoom,
} from "../utils/profile";

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
});

const emit = defineEmits(["saved", "openAchievements"]);
const { success: toastSuccess, error: toastError } = useToast();

const FIXED_COLLEGE = "大数据与人工智能学院";

const info = reactive(createEmptyInfo());
const isEditing = ref(false);
const saving = ref(false);
const avatarInput = ref(null);
const workUnitHintOpen = ref(false);
const today = getTodayString();
const originalProfileData = ref(null);

const classYearOptions = Array.from({ length: 19 }, (_, index) => 2022 + index);
const majorOptionsByCategory = {
  本科生: [
    "计算机科学与技术",
    "计算机科学与技术（实验区）",
    "计算机科学与技术(中外联合培养项目班)",
    "2025计算机科学与技术（中外联合培养项目班未赴国外学习）",
    "软件工程",
    "人工智能",
    "电子商务",
    "电子商务（大数据决策分析）",
    "大数据管理与应用",
    "大数据管理与应用（佛山校区全学段）",
    "大数据管理与应用（数字治理）",
  ],
  研究生: [
    "管理科学与工程",
    "技术经济及管理",
    "智能科学与技术",
    "计算机技术",
    "图书情报",
  ],
};
const studentCategoryOptions = ["本科生", "研究生"];
const politicalStatusOptions = ["群众", "共青团员", "中共预备党员", "中共党员"];
const idTypeOptions = [
  "居民身份证",
  "台湾居民来往大陆通行证",
  "港澳居民来往内地通行证",
  "普通护照",
  "台湾居民居住证",
  "港澳居民居住证",
  "外国人永久居留身份证",
  "外国护照",
];
const dormCampusOptions = ["佛山校区", "广州校区"];

const specialStudentTypeOptions = [
  { value: "", label: "无" },
  { value: "HIGH_CARE", label: "高关怀" },
  { value: "ECONOMIC_SPECIAL", label: "经济困难>特殊困难" },
  { value: "ECONOMIC_DIFFICULT", label: "经济困难>困难" },
  { value: "ECONOMIC_GENERAL", label: "经济困难>一般困难" },
  { value: "DISABILITY", label: "残疾" },
  { value: "ORPHAN", label: "孤儿" },
  { value: "ACADEMIC_DIFFICULTY", label: "学业困难" },
];

const educationItems = reactive(Array.from({ length: 5 }, () => createEducationItem()));
const cadreItems = reactive(Array.from({ length: 5 }, () => createCadreItem()));

const classMajorOptions = computed(() => {
  return majorOptionsByCategory[info.studentCategory] || [];
});
const addressProvinceOptions = computed(() =>
  regionData.map((item) => ({ value: item.value, label: item.label })),
);
const addressCityOptions = computed(() => {
  const province = regionData.find((item) => item.value === info.addressProvince);
  return province?.children || [];
});
const addressCountyOptions = computed(() => {
  const province = regionData.find((item) => item.value === info.addressProvince);
  const city = province?.children?.find((entry) => entry.value === info.addressCity);
  return city?.children || [];
});
const offCampusCityOptions = computed(() => {
  const province = regionData.find((item) => item.value === info.offCampusProvince);
  return province?.children || [];
});
const offCampusCountyOptions = computed(() => {
  const province = regionData.find((item) => item.value === info.offCampusProvince);
  const city = province?.children?.find((entry) => entry.value === info.offCampusCity);
  return city?.children || [];
});
const idNoMaxLength = computed(() => {
  switch (info.idType) {
    case "居民身份证":
      return 18;
    case "台湾居民来往大陆通行证":
    case "港澳居民来往内地通行证":
      return 9;
    case "普通护照":
      return 9;
    case "台湾居民居住证":
    case "港澳居民居住证":
      return 10;
    case "外国人永久居留身份证":
      return 15;
    case "外国护照":
      return 20;
    default:
      return 32;
  }
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
const hasEducationCurrent = computed(() =>
  educationItems.some((entry) => entry.isCurrent),
);
const currentEducationIndex = computed(() =>
  educationItems.findIndex((entry) => entry.isCurrent),
);
const hasCadreCurrent = computed(() =>
  cadreItems.some((entry) => entry.isCurrent),
);
const currentCadreIndex = computed(() =>
  cadreItems.findIndex((entry) => entry.isCurrent),
);
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

watch(
  () => props.student,
  (student) => {
    applyProfileResponse(student);
    isEditing.value = false;
  },
  { immediate: true },
);

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

watch(
  () => info.addressProvince,
  () => {
    if (!info.addressProvince) {
      info.addressCity = "";
      info.addressCounty = "";
      return;
    }
    if (!addressCityOptions.value.some((item) => item.value === info.addressCity)) {
      info.addressCity = "";
    }
    if (!addressCountyOptions.value.some((item) => item.value === info.addressCounty)) {
      info.addressCounty = "";
    }
  },
);

watch(
  () => info.addressCity,
  () => {
    if (!info.addressCity) {
      info.addressCounty = "";
      return;
    }
    if (!addressCountyOptions.value.some((item) => item.value === info.addressCounty)) {
      info.addressCounty = "";
    }
  },
);

watch(
  () => info.offCampusProvince,
  () => {
    if (!info.offCampusProvince) {
      info.offCampusCity = "";
      info.offCampusCounty = "";
      return;
    }
    if (!offCampusCityOptions.value.some((item) => item.value === info.offCampusCity)) {
      info.offCampusCity = "";
    }
    if (!offCampusCountyOptions.value.some((item) => item.value === info.offCampusCounty)) {
      info.offCampusCounty = "";
    }
  },
);

watch(
  () => info.offCampusCity,
  () => {
    if (!info.offCampusCity) {
      info.offCampusCounty = "";
      return;
    }
    if (!offCampusCountyOptions.value.some((item) => item.value === info.offCampusCounty)) {
      info.offCampusCounty = "";
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

function createEmptyInfo() {
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
    fatherName: "",
    fatherPhone: "",
    fatherWorkUnit: "",
    fatherTitle: "",
    motherName: "",
    motherPhone: "",
    motherWorkUnit: "",
    motherTitle: "",
    specialStudentType: "",
    specialStudentRemark: "",
  };
}

function getTodayString() {
  const now = new Date();
  const offsetMs = now.getTimezoneOffset() * 60 * 1000;
  return new Date(now.getTime() - offsetMs).toISOString().slice(0, 10);
}

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

function createCadreItem() {
  return {
    startDate: "",
    endDate: "",
    department: "",
    position: "",
    description: "",
    isCurrent: false,
  };
}

function isEducationRowEmpty(entry) {
  return (
    !entry.startDate &&
    !entry.endDate &&
    !entry.schoolName &&
    !entry.educationLevel &&
    !entry.witness &&
    !entry.isCurrent
  );
}

function isCadreRowEmpty(entry) {
  return (
    !entry.startDate &&
    !entry.endDate &&
    !entry.department &&
    !entry.position &&
    !entry.description &&
    !entry.isCurrent
  );
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

function clearCadreRowsAfter(index) {
  cadreItems.slice(index + 1).forEach((entry) => {
    entry.startDate = "";
    entry.endDate = "";
    entry.department = "";
    entry.position = "";
    entry.description = "";
    entry.isCurrent = false;
  });
}

function pruneEducationRowsAfter(index) {
  if (educationItems.length <= index + 1) {
    return;
  }
  const kept = educationItems.slice(0, index + 1);
  educationItems.slice(index + 1).forEach((entry) => {
    if (!isEducationRowEmpty(entry)) {
      kept.push(entry);
    }
  });
  if (kept.length !== educationItems.length) {
    educationItems.splice(0, educationItems.length, ...kept);
  }
}

function pruneCadreRowsAfter(index) {
  if (cadreItems.length <= index + 1) {
    return;
  }
  const kept = cadreItems.slice(0, index + 1);
  cadreItems.slice(index + 1).forEach((entry) => {
    if (!isCadreRowEmpty(entry)) {
      kept.push(entry);
    }
  });
  if (kept.length !== cadreItems.length) {
    cadreItems.splice(0, cadreItems.length, ...kept);
  }
}

function isEducationCurrentDisabled(item) {
  if (item.isCurrent) {
    return false;
  }
  return hasEducationCurrent.value;
}

function isCadreCurrentDisabled(item) {
  if (item.isCurrent) {
    return false;
  }
  return hasCadreCurrent.value;
}

function isEducationRowDisabled(index) {
  if (!isEditing.value) {
    return true;
  }
  const currentIndex = currentEducationIndex.value;
  return currentIndex !== -1 && index > currentIndex;
}

function isCadreRowDisabled(index) {
  if (!isEditing.value) {
    return true;
  }
  const currentIndex = currentCadreIndex.value;
  return currentIndex !== -1 && index > currentIndex;
}

function handleEducationCurrentChange(item, index) {
  if (!item.isCurrent) {
    return;
  }
  item.endDate = "";
  clearEducationRowsAfter(index);
  pruneEducationRowsAfter(index);
}

function handleCadreCurrentChange(item, index) {
  if (!item.isCurrent) {
    return;
  }
  item.endDate = "";
  clearCadreRowsAfter(index);
  pruneCadreRowsAfter(index);
}

function addEducationRow() {
  educationItems.push(createEducationItem());
}

function removeEducationRow() {
  if (educationItems.length <= 1) {
    return;
  }
  educationItems.pop();
}

function addCadreRow() {
  cadreItems.push(createCadreItem());
}

function removeCadreRow() {
  if (cadreItems.length <= 1) {
    return;
  }
  cadreItems.pop();
}

function handleDigitsInput(field, maxLength, event) {
  const raw = event.target.value || "";
  const next = raw.replace(/\D/g, "").slice(0, maxLength);
  info[field] = next;
}

function handleIdNoInput(event) {
  const raw = (event.target.value || "").toUpperCase();
  const maxLen = idNoMaxLength.value;
  if (info.idType === "居民身份证") {
    const cleaned = raw.replace(/[^0-9X]/g, "");
    const digits = cleaned.replace(/X/g, "").slice(0, 17);
    if (raw.endsWith("X")) {
      info.idNo = `${digits}X`.slice(0, maxLen);
      return;
    }
    info.idNo = digits;
    return;
  }
  if (
    info.idType === "台湾居民来往大陆通行证" ||
    info.idType === "港澳居民来往内地通行证"
  ) {
    const cleaned = raw.replace(/[^0-9A-Za-z]/g, "").toUpperCase();
    const digits = cleaned.replace(/[A-Z]/g, "").slice(0, 8);
    const letter = cleaned.slice(8, 9).replace(/[^A-Z]/g, "");
    info.idNo = `${digits}${letter}`.slice(0, maxLen);
    return;
  }
  if (info.idType === "普通护照" || info.idType === "外国护照") {
    info.idNo = raw.replace(/[^0-9A-Za-z]/g, "").toUpperCase().slice(0, maxLen);
    return;
  }
  info.idNo = raw.replace(/\D/g, "").slice(0, maxLen);
}

function triggerAvatarUpload() {
  if (!isEditing.value) {
    return;
  }
  avatarInput.value?.click();
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
  if (!props.canEdit) {
    return;
  }
  originalProfileData.value = buildCurrentProfileState();
  isEditing.value = true;
}

function cancelEdit() {
  if (originalProfileData.value) {
    applyProfileResponse(originalProfileData.value);
  } else {
    applyProfileResponse(props.student);
  }
  isEditing.value = false;
}

async function confirmEdit() {
  if (!props.saveProfile || saving.value) {
    return;
  }
  saving.value = true;
  try {
    const payload = buildSavePayload();
    const { data } = await props.saveProfile(payload);
    applyProfileResponse(data);
    originalProfileData.value = data || null;
    isEditing.value = false;
    emit("saved", data);
  } catch (err) {
    console.error(err);
  } finally {
    saving.value = false;
  }
}

function buildSavePayload() {
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
  const dormRoom = buildDormRoom(info.dormFloor, info.dormRoomNo, info.dormRoom);
  const educationExperiences = educationItems
    .filter((item) => !isEducationRowEmpty(item))
    .map((item) => ({
      startDate: item.startDate,
      endDate: item.endDate,
      schoolName: item.schoolName,
      educationLevel: item.educationLevel,
      witness: item.witness,
      isCurrent: item.isCurrent,
    }));
  const cadreExperiences = cadreItems
    .filter((item) => !isCadreRowEmpty(item))
    .map((item) => ({
      startDate: item.startDate,
      endDate: item.endDate,
      department: item.department,
      position: item.position,
      description: item.description,
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
    specialStudentType: info.specialStudentType,
    specialStudentRemark: info.specialStudentRemark,
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
  if (!info.leagueJoined) {
    payload.leagueJoined = false;
    payload.leagueApplicationDate = null;
    payload.leagueJoinDate = null;
    payload.leagueDeveloping = false;
    payload.leagueNo = null;
    payload.partyApplied = false;
    payload.applicationDate = null;
    payload.activistDate = null;
    payload.activistDeveloping = false;
    payload.partyTrainingDate = null;
    payload.partyTrainingPending = false;
    payload.developmentTargetDate = null;
    payload.developmentTargetDeveloping = false;
    payload.probationaryMemberDate = null;
    payload.probationaryDeveloping = false;
    payload.fullMemberDate = null;
    payload.fullMemberDeveloping = false;
  } else if (!info.partyApplied) {
    payload.partyApplied = false;
    payload.applicationDate = null;
    payload.activistDate = null;
    payload.activistDeveloping = false;
    payload.partyTrainingDate = null;
    payload.partyTrainingPending = false;
    payload.developmentTargetDate = null;
    payload.developmentTargetDeveloping = false;
    payload.probationaryMemberDate = null;
    payload.probationaryDeveloping = false;
    payload.fullMemberDate = null;
    payload.fullMemberDeveloping = false;
  }

  return payload;
}

function buildPdfStudentSnapshot() {
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
  const educationExperiences = educationItems
    .filter((item) => !isEducationRowEmpty(item))
    .map((item) => ({
      startDate: item.startDate,
      endDate: item.endDate,
      schoolName: item.schoolName,
      educationLevel: item.educationLevel,
      witness: item.witness,
      isCurrent: item.isCurrent,
    }));
  const cadreExperiences = cadreItems
    .filter((item) => !isCadreRowEmpty(item))
    .map((item) => ({
      startDate: item.startDate,
      endDate: item.endDate,
      department: item.department,
      position: item.position,
      description: item.description,
      isCurrent: item.isCurrent,
    }));

  return {
    fullName: info.name,
    avatarUrl: info.avatarUrl,
    studentNo: info.studentNo,
    classYear: info.classYear,
    classMajor: info.classMajor,
    classNo: info.classNo,
    className: buildClassName(info.classYear, info.classMajor, info.classNo, info.className),
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
    dormRoom: buildDormRoom(info.dormFloor, info.dormRoomNo, info.dormRoom),
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
    educationExperiences,
    cadreExperiences,
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
    className: buildClassName(info.classYear, info.classMajor, info.classNo, info.className),
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
    educationExperiences: educationItems.map((item) => ({ ...item })),
    cadreExperiences: cadreItems.map((item) => ({ ...item })),
  };
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
  const filtered = normalized.filter((item) => !isEducationRowEmpty(item));
  if (!filtered.length) {
    filtered.push(createEducationItem());
  }
  educationItems.splice(0, educationItems.length, ...filtered);
}

function applyCadreExperiences(rawItems) {
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
  if (!filtered.length) {
    filtered.push(createCadreItem());
  }
  cadreItems.splice(0, cadreItems.length, ...filtered);
}

function applyProfileResponse(data) {
  const source = data || {};
  info.name = source.fullName || source.displayName || "";
  info.avatarUrl = source.avatarUrl || "";
  info.studentNo = source.studentNo || "";
  info.classYear = source.classYear || "";
  info.classMajor = source.classMajor || "";
  info.classNo = source.classNo ?? 1;
  info.className = source.className || "";
  info.college = source.college || FIXED_COLLEGE;
  info.enrollmentDate = source.enrollmentDate || "";
  info.studentCategory = source.studentCategory || "";
  info.ethnicity = source.ethnicity || "";
  info.politicalStatus = source.politicalStatus || "";
  info.dormCampus = source.dormCampus || "";
  info.dormBuilding = source.dormBuilding || "";
  info.dormRoom = source.dormRoom || "";
  const parsedDormRoom = parseDormRoom(info.dormRoom);
  info.dormFloor = parsedDormRoom.floor;
  info.dormRoomNo = parsedDormRoom.roomNo;
  info.offCampusLiving = Boolean(source.offCampusLiving);
  info.offCampusAddress = source.offCampusAddress || "";
  const parsedOffCampusAddress = parseAddressToRegion(info.offCampusAddress);
  info.offCampusProvince = parsedOffCampusAddress.province;
  info.offCampusCity = parsedOffCampusAddress.city;
  info.offCampusCounty = parsedOffCampusAddress.county;
  info.offCampusDetail = parsedOffCampusAddress.detail;
  info.classTeacher = source.classTeacher || "";
  info.counselor = source.counselor || "";
  info.phone = source.phone || "";
  info.backupContact = source.backupContact || "";
  info.address = source.address || "";
  const parsedAddress = parseAddressToRegion(info.address);
  info.addressProvince = parsedAddress.province;
  info.addressCity = parsedAddress.city;
  info.addressCounty = parsedAddress.county;
  info.addressDetail = parsedAddress.detail;
  info.idType = source.idType || "居民身份证";
  info.idNo = source.idNo || "";
  info.birthDate = source.birthDate || "";
  info.nativePlace = source.nativePlace || "";
  info.leagueNo = source.leagueNo || "";
  info.leagueApplicationDate = source.leagueApplicationDate || "";
  info.leagueJoinDate = source.leagueJoinDate || "";
  info.leagueJoined = Boolean(source.leagueJoined);
  info.leagueDeveloping = Boolean(source.leagueDeveloping);
  info.partyApplied = Boolean(source.partyApplied);
  info.notDeveloped = Boolean(source.notDeveloped);
  info.applicationDate = source.applicationDate || "";
  info.activistDate = source.activistDate || "";
  info.activistDeveloping = Boolean(source.activistDeveloping);
  info.partyTrainingDate = source.partyTrainingDate || "";
  info.partyTrainingPending = Boolean(source.partyTrainingPending);
  info.developmentTargetDate = source.developmentTargetDate || "";
  info.developmentTargetDeveloping = Boolean(source.developmentTargetDeveloping);
  info.probationaryMemberDate = source.probationaryMemberDate || "";
  info.probationaryDeveloping = Boolean(source.probationaryDeveloping);
  info.fullMemberDate = source.fullMemberDate || "";
  info.fullMemberDeveloping = Boolean(source.fullMemberDeveloping);
  info.emergencyPhone = source.emergencyPhone || "";
  info.emergencyRelation = source.emergencyRelation || "";
  info.fatherName = source.fatherName || "";
  info.fatherPhone = source.fatherPhone || "";
  info.fatherWorkUnit = source.fatherWorkUnit || "";
  info.fatherTitle = source.fatherTitle || "";
  info.motherName = source.motherName || "";
  info.motherPhone = source.motherPhone || "";
  info.motherWorkUnit = source.motherWorkUnit || "";
  info.motherTitle = source.motherTitle || "";
  info.specialStudentType = source.specialStudentType || "";
  info.specialStudentRemark = source.specialStudentRemark || "";
  applyEducationExperiences(source.educationExperiences);
  applyCadreExperiences(source.cadreExperiences);
}
</script>

<template>
  <section
    class="info-shell student-profile-editor"
    :class="{ 'info-shell-editing': isEditing }"
  >
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
        <span v-else>{{ info.name ? info.name.slice(0, 1) : "点击设置头像" }}</span>
        <input
          ref="avatarInput"
          type="file"
          accept="image/*"
          hidden
          @change="onAvatarChange"
        />
      </button>
      <div class="info-hero-text">
        <div class="info-hero-title">基础信息</div>
        <div class="info-hero-subtitle">
          学号：{{ info.studentNo || "未填写" }}
        </div>
      </div>
      <div class="info-actions">
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
          <select v-model="info.classYear" class="info-input" :disabled="!isEditing">
            <option disabled value="">选择年级</option>
            <option v-for="year in classYearOptions" :key="year" :value="year">
              {{ year }}
            </option>
          </select>
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
        <label class="field-card field-full">
          <span class="info-label">班级</span>
          <div class="class-inline">
            <select
              v-model="info.classMajor"
              class="info-input"
              :disabled="
                !isEditing ||
                !info.studentCategory ||
                !classMajorOptions.length
              "
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
              :disabled="!isEditing || info.studentCategory === '研究生'"
            />
            <span class="class-text">班</span>
          </div>
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
        <label class="field-card field-full">
          <span class="info-label">入学时间</span>
          <input
            v-model="info.enrollmentDate"
            class="info-input"
            type="date"
            lang="zh-CN"
            :max="today"
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
        <label class="field-card field-full">
          <span class="info-label">手机号码 / 备用联系方式</span>
          <div class="class-inline">
            <input
              v-model="info.phone"
              class="info-input"
              type="tel"
              placeholder="手机号"
              maxlength="11"
              inputmode="numeric"
              :disabled="!isEditing"
              @input="handleDigitsInput('phone', 11, $event)"
            />
            <span class="class-text">/</span>
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
          <div class="class-inline id-type-inline">
            <select v-model="info.idType" class="info-input" :disabled="!isEditing">
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
        </label>
        <label class="field-card">
          <span class="info-label">出生年月</span>
          <input
            v-model="info.birthDate"
            class="info-input"
            type="date"
            lang="zh-CN"
            :max="today"
            :disabled="!isEditing"
          />
        </label>
        <label class="field-card">
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

    <div class="info-card">
      <div class="info-section-title">住宿信息</div>
      <div class="info-form-grid">
        <div class="field-card field-full">
          <span class="info-label">是否在外居住</span>
          <div class="info-inline">
            <label class="info-choice">
              <input v-model="info.offCampusLiving" type="radio" :value="true" :disabled="!isEditing" />
              是
            </label>
            <label class="info-choice">
              <input v-model="info.offCampusLiving" type="radio" :value="false" :disabled="!isEditing" />
              否
            </label>
          </div>
        </div>
        <label class="field-card field-full" v-if="info.offCampusLiving">
          <span class="info-label">外居住详细地址</span>
          <div class="info-inline address-inline">
            <select
              v-model="info.offCampusProvince"
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
        <label class="field-card" v-if="!info.offCampusLiving">
          <span class="info-label">住宿校区</span>
          <select
            v-model="info.dormCampus"
            class="info-input"
            :disabled="!isEditing || info.offCampusLiving"
          >
            <option disabled value="">选择住宿校区</option>
            <option v-for="item in dormCampusOptions" :key="item" :value="item">
              {{ item }}
            </option>
          </select>
        </label>
        <label class="field-card" v-if="!info.offCampusLiving">
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
        <label class="field-card field-full" v-if="!info.offCampusLiving">
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
              <input v-model="info.leagueJoined" type="radio" :value="true" :disabled="!isEditing" />
              是
            </label>
            <label class="info-choice">
              <input v-model="info.leagueJoined" type="radio" :value="false" :disabled="!isEditing" />
              否
            </label>
          </div>
        </div>
        <template v-if="info.leagueJoined">
          <label class="field-card field-full">
            <span class="info-label">提交入团申请书时间</span>
            <input
              v-model="info.leagueApplicationDate"
              class="info-input"
              type="date"
              lang="zh-CN"
              :max="today"
              :disabled="leagueApplicationDisabled"
            />
          </label>
          <label class="field-card field-full">
            <span class="info-label">入团时间</span>
            <div class="info-inline">
              <input
                v-model="info.leagueJoinDate"
                class="info-input"
                type="date"
                lang="zh-CN"
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
              <input
                v-model="info.applicationDate"
                class="info-input"
                type="date"
                lang="zh-CN"
                :max="today"
                :disabled="applicationDateDisabled"
              />
            </label>
            <label class="field-card field-full">
              <span class="info-label">确定积极分子时间</span>
              <div class="info-inline">
                <input
                  v-model="info.activistDate"
                  class="info-input"
                  type="date"
                  lang="zh-CN"
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
                <input
                  v-model="info.partyTrainingDate"
                  class="info-input"
                  type="date"
                  lang="zh-CN"
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
                <input
                  v-model="info.developmentTargetDate"
                  class="info-input"
                  type="date"
                  lang="zh-CN"
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
                <input
                  v-model="info.probationaryMemberDate"
                  class="info-input"
                  type="date"
                  lang="zh-CN"
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
                <input
                  v-model="info.fullMemberDate"
                  class="info-input"
                  type="date"
                  lang="zh-CN"
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

    <div class="info-card">
      <div class="info-section-title">教育经历</div>
      <div class="info-hint">从小学开始填</div>
      <div class="record-list-wrap">
        <transition-group
          name="education-row"
          tag="div"
          class="record-list"
        >
          <section
            v-for="(item, index) in educationItems"
            :key="`edu-${index}`"
            class="record-row"
          >
            <div class="record-grid record-grid-edu">
              <div class="record-field record-field-time">
                <span class="info-label">时间段</span>
                <div class="record-period">
                  <input
                    v-model="item.startDate"
                    class="info-input"
                    type="date"
                    lang="zh-CN"
                    :max="today"
                    :disabled="isEducationRowDisabled(index)"
                  />
                  <span class="record-sep">至</span>
                  <input
                    v-model="item.endDate"
                    class="info-input"
                    type="date"
                    lang="zh-CN"
                    :max="today"
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
              </div>
              <label class="record-field record-field-school">
                <span class="info-label">学校名称</span>
                <input
                  v-model="item.schoolName"
                  class="info-input"
                  type="text"
                  placeholder="学校名称"
                  :disabled="isEducationRowDisabled(index)"
                />
              </label>
              <label class="record-field record-field-level">
                <span class="info-label">学历</span>
                <input
                  v-model="item.educationLevel"
                  class="info-input"
                  type="text"
                  placeholder="学历"
                  :disabled="isEducationRowDisabled(index)"
                />
              </label>
              <label class="record-field record-field-witness">
                <span class="info-label">证明人</span>
                <input
                  v-model="item.witness"
                  class="info-input"
                  type="text"
                  placeholder="证明人"
                  :disabled="isEducationRowDisabled(index)"
                />
              </label>
            </div>
          </section>
        </transition-group>
        <div class="record-controls">
          <button
            class="record-ctl"
            type="button"
            :disabled="!isEditing"
            aria-label="增加一行"
            @click="addEducationRow"
          >
            +
          </button>
          <button
            class="record-ctl"
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

    <div class="info-card">
      <div class="info-section-title">学生干部经历</div>
      <div class="record-list-wrap">
        <transition-group
          name="education-row"
          tag="div"
          class="record-list"
        >
          <section
            v-for="(item, index) in cadreItems"
            :key="`cadre-${index}`"
            class="record-row"
          >
            <div class="record-grid record-grid-cdr">
              <div class="record-field record-field-time">
                <span class="info-label">时间段</span>
                <div class="record-period">
                  <input
                    v-model="item.startDate"
                    class="info-input"
                    type="date"
                    lang="zh-CN"
                    :max="today"
                    :disabled="isCadreRowDisabled(index)"
                  />
                  <span class="record-sep">至</span>
                  <input
                    v-model="item.endDate"
                    class="info-input"
                    type="date"
                    lang="zh-CN"
                    :max="today"
                    :disabled="isCadreRowDisabled(index) || item.isCurrent"
                  />
                  <label class="info-choice info-choice-muted">
                    <input
                      v-model="item.isCurrent"
                      type="checkbox"
                      :disabled="isCadreRowDisabled(index) || isCadreCurrentDisabled(item)"
                      @change="handleCadreCurrentChange(item, index)"
                    />
                    至今
                  </label>
                </div>
              </div>
              <label class="record-field record-field-dept">
                <span class="info-label">社团部门/班级</span>
                <input
                  v-model="item.department"
                  class="info-input"
                  type="text"
                  placeholder="部门/班级"
                  :disabled="isCadreRowDisabled(index)"
                />
              </label>
              <label class="record-field record-field-pos">
                <span class="info-label">职位</span>
                <input
                  v-model="item.position"
                  class="info-input"
                  type="text"
                  placeholder="职位"
                  :disabled="isCadreRowDisabled(index)"
                />
              </label>
              <label class="record-field record-field-desc">
                <span class="info-label">描述</span>
                <textarea
                  v-model="item.description"
                  class="info-input"
                  rows="2"
                  placeholder="简述你在该职位的职责/成就"
                  :disabled="isCadreRowDisabled(index)"
                ></textarea>
              </label>
            </div>
          </section>
        </transition-group>
        <div class="record-controls">
          <button
            class="record-ctl"
            type="button"
            :disabled="!isEditing"
            aria-label="增加一行"
            @click="addCadreRow"
          >
            +
          </button>
          <button
            class="record-ctl"
            type="button"
            :disabled="!isEditing || cadreItems.length <= 1"
            aria-label="减少一行"
            @click="removeCadreRow"
          >
            −
          </button>
        </div>
      </div>
    </div>

    <div class="info-card">
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

    <div class="info-card">
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

    <div class="info-card">
      <div class="info-section-title">特殊学生类型</div>
      <div class="info-form-grid">
        <label class="field-card">
          <span class="info-label">特殊学生类别</span>
          <select v-model="info.specialStudentType" class="info-input" :disabled="!isEditing" style="max-width: 200px;">
            <option
              v-for="opt in specialStudentTypeOptions"
              :key="opt.value"
              :value="opt.value"
            >
              {{ opt.label }}
            </option>
          </select>
        </label>
        <label class="field-card field-full special-remark-field">
          <span class="info-label">备注</span>
          <textarea
            v-model="info.specialStudentRemark"
            class="info-input remark-textarea"
            rows="2"
            placeholder="请输入备注"
            :disabled="!isEditing"
          ></textarea>
        </label>
      </div>
    </div>

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

<style scoped>
.special-remark-field {
  margin-top: 4px;
}

.remark-textarea {
  height: auto;
  min-height: 60px;
  padding: 8px 12px;
  line-height: 1.4;
  font-size: 13px;
  resize: vertical;
}
</style>
