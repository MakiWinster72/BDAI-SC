package com.gcsc.studentcenter.dto;

import java.time.LocalDate;
import java.util.List;

public class StudentProfileRequest {
  private String fullName;
  private String avatarUrl;
  private String studentNo;
  private Integer classYear;
  private String classMajor;
  private String classNo;
  private String className;
  private String college;
  private LocalDate enrollmentDate;
  private String studentCategory;
  private String ethnicity;
  private String politicalStatus;
  private String dormCampus;
  private String dormBuilding;
  private String dormRoom;
  private Boolean offCampusLiving;
  private String offCampusAddress;
  private String classTeacher;
  private String counselor;
  private String phone;
  private String backupContact;
  private String address;
  private String idType;
  private String idNo;
  private LocalDate birthDate;
  private String nativePlace;
  private String leagueNo;
  private LocalDate leagueApplicationDate;
  private LocalDate leagueJoinDate;
  private Boolean leagueJoined;
  private Boolean leagueDeveloping;
  private Boolean partyApplied;
  private Boolean notDeveloped;
  private LocalDate applicationDate;
  private LocalDate activistDate;
  private Boolean activistDeveloping;
  private LocalDate partyTrainingDate;
  private Boolean partyTrainingPending;
  private LocalDate developmentTargetDate;
  private Boolean developmentTargetDeveloping;
  private LocalDate probationaryMemberDate;
  private Boolean probationaryDeveloping;
  private LocalDate fullMemberDate;
  private Boolean fullMemberDeveloping;
  private String emergencyPhone;
  private String emergencyRelation;
  private Boolean isHk;
  private Boolean isMo;
  private Boolean isTw;
  private Boolean specialStudent;
  private String fatherName;
  private String fatherPhone;
  private String fatherWorkUnit;
  private String fatherTitle;
  private String motherName;
  private String motherPhone;
  private String motherWorkUnit;
  private String motherTitle;
  private String specialStudentType;
  private String specialStudentRemark;
  private List<EducationExperienceItem> educationExperiences;
  private List<CadreExperienceItem> cadreExperiences;

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getStudentNo() {
    return studentNo;
  }

  public void setStudentNo(String studentNo) {
    this.studentNo = studentNo;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public void setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
  }

  public Integer getClassYear() {
    return classYear;
  }

  public void setClassYear(Integer classYear) {
    this.classYear = classYear;
  }

  public String getClassMajor() {
    return classMajor;
  }

  public void setClassMajor(String classMajor) {
    this.classMajor = classMajor;
  }

  public String getClassNo() {
    return classNo;
  }

  public void setClassNo(String classNo) {
    this.classNo = classNo;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public String getCollege() {
    return college;
  }

  public void setCollege(String college) {
    this.college = college;
  }

  public LocalDate getEnrollmentDate() {
    return enrollmentDate;
  }

  public void setEnrollmentDate(LocalDate enrollmentDate) {
    this.enrollmentDate = enrollmentDate;
  }

  public String getStudentCategory() {
    return studentCategory;
  }

  public void setStudentCategory(String studentCategory) {
    this.studentCategory = studentCategory;
  }

  public String getEthnicity() {
    return ethnicity;
  }

  public void setEthnicity(String ethnicity) {
    this.ethnicity = ethnicity;
  }

  public String getPoliticalStatus() {
    return politicalStatus;
  }

  public void setPoliticalStatus(String politicalStatus) {
    this.politicalStatus = politicalStatus;
  }

  public String getDormCampus() {
    return dormCampus;
  }

  public void setDormCampus(String dormCampus) {
    this.dormCampus = dormCampus;
  }

  public String getDormBuilding() {
    return dormBuilding;
  }

  public void setDormBuilding(String dormBuilding) {
    this.dormBuilding = dormBuilding;
  }

  public String getDormRoom() {
    return dormRoom;
  }

  public void setDormRoom(String dormRoom) {
    this.dormRoom = dormRoom;
  }

  public Boolean getOffCampusLiving() {
    return offCampusLiving;
  }

  public void setOffCampusLiving(Boolean offCampusLiving) {
    this.offCampusLiving = offCampusLiving;
  }

  public String getOffCampusAddress() {
    return offCampusAddress;
  }

  public void setOffCampusAddress(String offCampusAddress) {
    this.offCampusAddress = offCampusAddress;
  }

  public String getClassTeacher() {
    return classTeacher;
  }

  public void setClassTeacher(String classTeacher) {
    this.classTeacher = classTeacher;
  }

  public String getCounselor() {
    return counselor;
  }

  public void setCounselor(String counselor) {
    this.counselor = counselor;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getBackupContact() {
    return backupContact;
  }

  public void setBackupContact(String backupContact) {
    this.backupContact = backupContact;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getIdType() {
    return idType;
  }

  public void setIdType(String idType) {
    this.idType = idType;
  }

  public String getIdNo() {
    return idNo;
  }

  public void setIdNo(String idNo) {
    this.idNo = idNo;
  }

  public LocalDate getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(LocalDate birthDate) {
    this.birthDate = birthDate;
  }

  public String getNativePlace() {
    return nativePlace;
  }

  public void setNativePlace(String nativePlace) {
    this.nativePlace = nativePlace;
  }

  public String getLeagueNo() {
    return leagueNo;
  }

  public void setLeagueNo(String leagueNo) {
    this.leagueNo = leagueNo;
  }

  public LocalDate getLeagueApplicationDate() {
    return leagueApplicationDate;
  }

  public void setLeagueApplicationDate(LocalDate leagueApplicationDate) {
    this.leagueApplicationDate = leagueApplicationDate;
  }

  public LocalDate getLeagueJoinDate() {
    return leagueJoinDate;
  }

  public void setLeagueJoinDate(LocalDate leagueJoinDate) {
    this.leagueJoinDate = leagueJoinDate;
  }

  public Boolean getLeagueJoined() {
    return leagueJoined;
  }

  public void setLeagueJoined(Boolean leagueJoined) {
    this.leagueJoined = leagueJoined;
  }

  public Boolean getLeagueDeveloping() {
    return leagueDeveloping;
  }

  public void setLeagueDeveloping(Boolean leagueDeveloping) {
    this.leagueDeveloping = leagueDeveloping;
  }

  public Boolean getPartyApplied() {
    return partyApplied;
  }

  public void setPartyApplied(Boolean partyApplied) {
    this.partyApplied = partyApplied;
  }

  public Boolean getNotDeveloped() {
    return notDeveloped;
  }

  public void setNotDeveloped(Boolean notDeveloped) {
    this.notDeveloped = notDeveloped;
  }

  public LocalDate getApplicationDate() {
    return applicationDate;
  }

  public void setApplicationDate(LocalDate applicationDate) {
    this.applicationDate = applicationDate;
  }

  public LocalDate getActivistDate() {
    return activistDate;
  }

  public void setActivistDate(LocalDate activistDate) {
    this.activistDate = activistDate;
  }

  public Boolean getActivistDeveloping() {
    return activistDeveloping;
  }

  public void setActivistDeveloping(Boolean activistDeveloping) {
    this.activistDeveloping = activistDeveloping;
  }

  public LocalDate getPartyTrainingDate() {
    return partyTrainingDate;
  }

  public void setPartyTrainingDate(LocalDate partyTrainingDate) {
    this.partyTrainingDate = partyTrainingDate;
  }

  public Boolean getPartyTrainingPending() {
    return partyTrainingPending;
  }

  public void setPartyTrainingPending(Boolean partyTrainingPending) {
    this.partyTrainingPending = partyTrainingPending;
  }

  public LocalDate getDevelopmentTargetDate() {
    return developmentTargetDate;
  }

  public void setDevelopmentTargetDate(LocalDate developmentTargetDate) {
    this.developmentTargetDate = developmentTargetDate;
  }

  public Boolean getDevelopmentTargetDeveloping() {
    return developmentTargetDeveloping;
  }

  public void setDevelopmentTargetDeveloping(Boolean developmentTargetDeveloping) {
    this.developmentTargetDeveloping = developmentTargetDeveloping;
  }

  public LocalDate getProbationaryMemberDate() {
    return probationaryMemberDate;
  }

  public void setProbationaryMemberDate(LocalDate probationaryMemberDate) {
    this.probationaryMemberDate = probationaryMemberDate;
  }

  public Boolean getProbationaryDeveloping() {
    return probationaryDeveloping;
  }

  public void setProbationaryDeveloping(Boolean probationaryDeveloping) {
    this.probationaryDeveloping = probationaryDeveloping;
  }

  public LocalDate getFullMemberDate() {
    return fullMemberDate;
  }

  public void setFullMemberDate(LocalDate fullMemberDate) {
    this.fullMemberDate = fullMemberDate;
  }

  public Boolean getFullMemberDeveloping() {
    return fullMemberDeveloping;
  }

  public void setFullMemberDeveloping(Boolean fullMemberDeveloping) {
    this.fullMemberDeveloping = fullMemberDeveloping;
  }

  public String getEmergencyPhone() {
    return emergencyPhone;
  }

  public void setEmergencyPhone(String emergencyPhone) {
    this.emergencyPhone = emergencyPhone;
  }

  public String getEmergencyRelation() {
    return emergencyRelation;
  }

  public void setEmergencyRelation(String emergencyRelation) {
    this.emergencyRelation = emergencyRelation;
  }

  public Boolean getIsHk() {
    return isHk;
  }

  public void setIsHk(Boolean isHk) {
    this.isHk = isHk;
  }

  public Boolean getIsMo() {
    return isMo;
  }

  public void setIsMo(Boolean isMo) {
    this.isMo = isMo;
  }

  public Boolean getIsTw() {
    return isTw;
  }

  public void setIsTw(Boolean isTw) {
    this.isTw = isTw;
  }

  public Boolean getSpecialStudent() {
    return specialStudent;
  }

  public void setSpecialStudent(Boolean specialStudent) {
    this.specialStudent = specialStudent;
  }

  public String getFatherName() {
    return fatherName;
  }

  public void setFatherName(String fatherName) {
    this.fatherName = fatherName;
  }

  public String getFatherPhone() {
    return fatherPhone;
  }

  public void setFatherPhone(String fatherPhone) {
    this.fatherPhone = fatherPhone;
  }

  public String getFatherWorkUnit() {
    return fatherWorkUnit;
  }

  public void setFatherWorkUnit(String fatherWorkUnit) {
    this.fatherWorkUnit = fatherWorkUnit;
  }

  public String getFatherTitle() {
    return fatherTitle;
  }

  public void setFatherTitle(String fatherTitle) {
    this.fatherTitle = fatherTitle;
  }

  public String getMotherName() {
    return motherName;
  }

  public void setMotherName(String motherName) {
    this.motherName = motherName;
  }

  public String getMotherPhone() {
    return motherPhone;
  }

  public void setMotherPhone(String motherPhone) {
    this.motherPhone = motherPhone;
  }

  public String getMotherWorkUnit() {
    return motherWorkUnit;
  }

  public void setMotherWorkUnit(String motherWorkUnit) {
    this.motherWorkUnit = motherWorkUnit;
  }

  public String getMotherTitle() {
    return motherTitle;
  }

  public void setMotherTitle(String motherTitle) {
    this.motherTitle = motherTitle;
  }

  public String getSpecialStudentType() {
    return specialStudentType;
  }

  public void setSpecialStudentType(String specialStudentType) {
    this.specialStudentType = specialStudentType;
  }

  public String getSpecialStudentRemark() {
    return specialStudentRemark;
  }

  public void setSpecialStudentRemark(String specialStudentRemark) {
    this.specialStudentRemark = specialStudentRemark;
  }

  public List<EducationExperienceItem> getEducationExperiences() {
    return educationExperiences;
  }

  public void setEducationExperiences(List<EducationExperienceItem> educationExperiences) {
    this.educationExperiences = educationExperiences;
  }

  public List<CadreExperienceItem> getCadreExperiences() {
    return cadreExperiences;
  }

  public void setCadreExperiences(List<CadreExperienceItem> cadreExperiences) {
    this.cadreExperiences = cadreExperiences;
  }
}
