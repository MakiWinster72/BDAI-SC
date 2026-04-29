package com.gcsc.studentcenter.dto;

import java.time.LocalDate;
import java.util.List;

public class StudentProfileResponse {
    private Long id;
    private String username;
    private String displayName;
    private String studentNo;
    private String className;
    private String college;
    private String fullName;
    private String avatarUrl;
    private Integer classYear;
    private String classMajor;
    private String classNo;
    private LocalDate enrollmentDate;
    private String studentCategory;
    private String ethnicity;
    private String politicalStatus;
    private String phone;
    private String backupContact;
    private String address;
    private String idType;
    private String idNo;
    private LocalDate birthDate;
    private String nativePlace;
    private String dormCampus;
    private String dormBuilding;
    private String dormRoom;
    private Boolean offCampusLiving;
    private String offCampusAddress;
    private String classTeacher;
    private String counselor;
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

    public StudentProfileResponse(
        Long id,
        String username,
        String displayName,
        String studentNo,
        String className,
        String college,
        String fullName,
        String avatarUrl,
        Integer classYear,
        String classMajor,
        String classNo,
        LocalDate enrollmentDate,
        String studentCategory,
        String ethnicity,
        String politicalStatus,
        String phone,
        String backupContact,
        String address,
        String idType,
        String idNo,
        LocalDate birthDate,
        String nativePlace,
        String dormCampus,
        String dormBuilding,
        String dormRoom,
        Boolean offCampusLiving,
        String offCampusAddress,
        String classTeacher,
        String counselor,
        String leagueNo,
        LocalDate leagueApplicationDate,
        LocalDate leagueJoinDate,
        Boolean leagueJoined,
        Boolean leagueDeveloping,
        Boolean partyApplied,
        Boolean notDeveloped,
        LocalDate applicationDate,
        LocalDate activistDate,
        Boolean activistDeveloping,
        LocalDate partyTrainingDate,
        Boolean partyTrainingPending,
        LocalDate developmentTargetDate,
        Boolean developmentTargetDeveloping,
        LocalDate probationaryMemberDate,
        Boolean probationaryDeveloping,
        LocalDate fullMemberDate,
        Boolean fullMemberDeveloping,
        String emergencyPhone,
        String emergencyRelation,
        Boolean isHk,
        Boolean isMo,
        Boolean isTw,
        Boolean specialStudent,
        String fatherName,
        String fatherPhone,
        String fatherWorkUnit,
        String fatherTitle,
        String motherName,
        String motherPhone,
        String motherWorkUnit,
        String motherTitle,
        String specialStudentType,
        String specialStudentRemark,
        List<EducationExperienceItem> educationExperiences,
        List<CadreExperienceItem> cadreExperiences
    ) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.studentNo = studentNo;
        this.className = className;
        this.college = college;
        this.fullName = fullName;
        this.avatarUrl = avatarUrl;
        this.classYear = classYear;
        this.classMajor = classMajor;
        this.classNo = classNo;
        this.enrollmentDate = enrollmentDate;
        this.studentCategory = studentCategory;
        this.ethnicity = ethnicity;
        this.politicalStatus = politicalStatus;
        this.phone = phone;
        this.backupContact = backupContact;
        this.address = address;
        this.idType = idType;
        this.idNo = idNo;
        this.birthDate = birthDate;
        this.nativePlace = nativePlace;
        this.dormCampus = dormCampus;
        this.dormBuilding = dormBuilding;
        this.dormRoom = dormRoom;
        this.offCampusLiving = offCampusLiving;
        this.offCampusAddress = offCampusAddress;
        this.classTeacher = classTeacher;
        this.counselor = counselor;
        this.leagueNo = leagueNo;
        this.leagueApplicationDate = leagueApplicationDate;
        this.leagueJoinDate = leagueJoinDate;
        this.leagueJoined = leagueJoined;
        this.leagueDeveloping = leagueDeveloping;
        this.partyApplied = partyApplied;
        this.notDeveloped = notDeveloped;
        this.applicationDate = applicationDate;
        this.activistDate = activistDate;
        this.activistDeveloping = activistDeveloping;
        this.partyTrainingDate = partyTrainingDate;
        this.partyTrainingPending = partyTrainingPending;
        this.developmentTargetDate = developmentTargetDate;
        this.developmentTargetDeveloping = developmentTargetDeveloping;
        this.probationaryMemberDate = probationaryMemberDate;
        this.probationaryDeveloping = probationaryDeveloping;
        this.fullMemberDate = fullMemberDate;
        this.fullMemberDeveloping = fullMemberDeveloping;
        this.emergencyPhone = emergencyPhone;
        this.emergencyRelation = emergencyRelation;
        this.isHk = isHk;
        this.isMo = isMo;
        this.isTw = isTw;
        this.specialStudent = specialStudent;
        this.fatherName = fatherName;
        this.fatherPhone = fatherPhone;
        this.fatherWorkUnit = fatherWorkUnit;
        this.fatherTitle = fatherTitle;
        this.motherName = motherName;
        this.motherPhone = motherPhone;
        this.motherWorkUnit = motherWorkUnit;
        this.motherTitle = motherTitle;
        this.specialStudentType = specialStudentType;
        this.specialStudentRemark = specialStudentRemark;
        this.educationExperiences = educationExperiences;
        this.cadreExperiences = cadreExperiences;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public String getClassName() {
        return className;
    }

    public String getCollege() {
        return college;
    }

    public String getFullName() {
        return fullName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public Integer getClassYear() {
        return classYear;
    }

    public String getClassMajor() {
        return classMajor;
    }

    public String getClassNo() {
        return classNo;
    }

    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }

    public String getStudentCategory() {
        return studentCategory;
    }

    public String getEthnicity() {
        return ethnicity;
    }

    public String getPoliticalStatus() {
        return politicalStatus;
    }

    public String getPhone() {
        return phone;
    }

    public String getBackupContact() {
        return backupContact;
    }

    public String getAddress() {
        return address;
    }

    public String getIdNo() {
        return idNo;
    }

    public String getIdType() {
        return idType;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getNativePlace() {
        return nativePlace;
    }

    public String getDormCampus() {
        return dormCampus;
    }

    public String getDormBuilding() {
        return dormBuilding;
    }

    public String getDormRoom() {
        return dormRoom;
    }

    public Boolean getOffCampusLiving() {
        return offCampusLiving;
    }

    public String getOffCampusAddress() {
        return offCampusAddress;
    }

    public String getClassTeacher() {
        return classTeacher;
    }

    public String getCounselor() {
        return counselor;
    }

    public String getLeagueNo() {
        return leagueNo;
    }

    public LocalDate getLeagueApplicationDate() {
        return leagueApplicationDate;
    }

    public LocalDate getLeagueJoinDate() {
        return leagueJoinDate;
    }

    public Boolean getLeagueJoined() {
        return leagueJoined;
    }

    public Boolean getLeagueDeveloping() {
        return leagueDeveloping;
    }

    public Boolean getPartyApplied() {
        return partyApplied;
    }

    public Boolean getNotDeveloped() {
        return notDeveloped;
    }

    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    public LocalDate getActivistDate() {
        return activistDate;
    }

    public Boolean getActivistDeveloping() {
        return activistDeveloping;
    }

    public LocalDate getPartyTrainingDate() {
        return partyTrainingDate;
    }

    public Boolean getPartyTrainingPending() {
        return partyTrainingPending;
    }

    public LocalDate getDevelopmentTargetDate() {
        return developmentTargetDate;
    }

    public Boolean getDevelopmentTargetDeveloping() {
        return developmentTargetDeveloping;
    }

    public LocalDate getProbationaryMemberDate() {
        return probationaryMemberDate;
    }

    public Boolean getProbationaryDeveloping() {
        return probationaryDeveloping;
    }

    public LocalDate getFullMemberDate() {
        return fullMemberDate;
    }

    public Boolean getFullMemberDeveloping() {
        return fullMemberDeveloping;
    }

    public String getEmergencyPhone() {
        return emergencyPhone;
    }

    public String getEmergencyRelation() {
        return emergencyRelation;
    }

    public Boolean getIsHk() {
        return isHk;
    }

    public Boolean getIsMo() {
        return isMo;
    }

    public Boolean getIsTw() {
        return isTw;
    }

    public Boolean getSpecialStudent() {
        return specialStudent;
    }

    public String getFatherName() {
        return fatherName;
    }

    public String getFatherPhone() {
        return fatherPhone;
    }

    public String getFatherWorkUnit() {
        return fatherWorkUnit;
    }

    public String getFatherTitle() {
        return fatherTitle;
    }

    public String getMotherName() {
        return motherName;
    }

    public String getMotherPhone() {
        return motherPhone;
    }

    public String getMotherWorkUnit() {
        return motherWorkUnit;
    }

    public String getMotherTitle() {
        return motherTitle;
    }

    public String getSpecialStudentType() {
        return specialStudentType;
    }

    public String getSpecialStudentRemark() {
        return specialStudentRemark;
    }

    public List<EducationExperienceItem> getEducationExperiences() {
        return educationExperiences;
    }

    public List<CadreExperienceItem> getCadreExperiences() {
        return cadreExperiences;
    }
}
