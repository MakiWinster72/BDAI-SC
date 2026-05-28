import { FIXED_COLLEGE } from "@/constants/profileOptions";
import {
  buildCadrePayload,
  buildEducationPayload,
} from "@/composables/useProfileFormModel";
import {
  buildAddress,
  buildClassName,
  buildDormRoom,
} from "@/utils/profile";

export function buildProfileSavePayload({
  info,
  educationItems,
  cadreItems,
  sanitizePartyPayload,
  includeSpecialStudentFields = true,
}) {
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
    educationExperiences,
    cadreExperiences,
  };

  if (includeSpecialStudentFields) {
    payload.specialStudent = info.specialStudent;
    payload.specialStudentType = info.specialStudentType || "";
    payload.specialStudentRemark = info.specialStudentRemark || "";
  }

  if (info.offCampusLiving) {
    payload.dormCampus = null;
    payload.dormBuilding = null;
    payload.dormRoom = null;
  } else {
    payload.offCampusAddress = null;
  }

  sanitizePartyPayload(payload);
  return payload;
}
