import { FIXED_COLLEGE } from "@/constants/profileOptions";
import {
  normalizeCadreExperiences,
  normalizeEducationExperiences,
} from "@/composables/useProfileFormModel";
import {
  parseAddressToRegion,
  parseDormRoom,
} from "@/utils/profile";

export function applyProfileToInfo(info, data, options = {}) {
  if (!data) {
    return;
  }
  const { classYearFallback = "empty" } = options;
  const source = data;

  info.name = source.fullName || source.displayName || "";
  info.avatarUrl = source.avatarUrl || options.avatarUrlFallback || "";
  info.studentNo = source.studentNo || options.studentNoFallback || "";
  if (source.classYear != null && source.classYear !== "") {
    info.classYear = source.classYear;
  } else if (classYearFallback === "current-year") {
    info.classYear = new Date().getFullYear();
  } else {
    info.classYear = "";
  }
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
  info.isHk = Boolean(source.isHk);
  info.isMo = Boolean(source.isMo);
  info.isTw = Boolean(source.isTw);
  info.specialStudent = Boolean(source.specialStudent);
  info.specialStudentType = source.specialStudentType || "";
  info.specialStudentRemark = source.specialStudentRemark || "";
}

export function useProfileApplier({ info, educationItems, cadreItems }) {
  function applyEducationExperiences(rawItems) {
    educationItems.splice(
      0,
      educationItems.length,
      ...normalizeEducationExperiences(rawItems),
    );
  }

  function applyCadreExperiences(rawItems) {
    cadreItems.splice(
      0,
      cadreItems.length,
      ...normalizeCadreExperiences(rawItems),
    );
  }

  function applyProfileResponse(data, options = {}) {
    if (!data) {
      return;
    }
    applyProfileToInfo(info, data, options);
    applyEducationExperiences(data.educationExperiences);
    applyCadreExperiences(data.cadreExperiences);
    options.onApplied?.(data, options);
  }

  return {
    applyProfileResponse,
    applyEducationExperiences,
    applyCadreExperiences,
  };
}
