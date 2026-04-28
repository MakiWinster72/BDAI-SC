import { buildClassName, buildDormRoom, buildAddress } from "../utils/profile";

export function useProfileSnapshot({
  info,
  profile,
  educationItems,
  cadreItems,
  isEducationRowEmpty,
  isCadreRowEmpty,
}) {
  function buildEducationSnapshot() {
    return educationItems
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

  function buildCadreSnapshot() {
    return cadreItems
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
      educationExperiences: buildEducationSnapshot(),
      cadreExperiences: buildCadreSnapshot(),
      avatarUrl: info.avatarUrl,
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
      educationExperiences: educationItems
        .filter((item) => !isEducationRowEmpty(item))
        .map((item) => ({ ...item })),
      cadreExperiences: cadreItems
        .filter((item) => !isCadreRowEmpty(item))
        .map((item) => ({ ...item })),
    };
  }

  return { buildPdfStudentSnapshot, buildCurrentProfileState };
}
