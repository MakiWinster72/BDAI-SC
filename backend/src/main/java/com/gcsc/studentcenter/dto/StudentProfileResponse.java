package com.gcsc.studentcenter.dto;

import java.time.LocalDate;

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
    private String address;
    private String idNo;
    private String nativePlace;
    private String dormCampus;
    private String dormBuilding;
    private String dormRoom;
    private String classTeacher;
    private String counselor;
    private String leagueNo;
    private LocalDate leagueApplicationDate;
    private LocalDate leagueJoinDate;
    private Boolean partyApplied;
    private Boolean notDeveloped;
    private LocalDate applicationDate;
    private LocalDate activistDate;
    private LocalDate partyTrainingDate;
    private LocalDate developmentTargetDate;
    private LocalDate probationaryMemberDate;
    private LocalDate fullMemberDate;
    private String emergencyPhone;
    private String emergencyRelation;
    private Boolean hkMoTw;
    private Boolean specialStudent;
    private String fatherName;
    private String fatherPhone;
    private String fatherWorkUnit;
    private String fatherTitle;
    private String motherName;
    private String motherPhone;
    private String motherWorkUnit;
    private String motherTitle;

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
        String address,
        String idNo,
        String nativePlace,
        String dormCampus,
        String dormBuilding,
        String dormRoom,
        String classTeacher,
        String counselor,
        String leagueNo,
        LocalDate leagueApplicationDate,
        LocalDate leagueJoinDate,
        Boolean partyApplied,
        Boolean notDeveloped,
        LocalDate applicationDate,
        LocalDate activistDate,
        LocalDate partyTrainingDate,
        LocalDate developmentTargetDate,
        LocalDate probationaryMemberDate,
        LocalDate fullMemberDate,
        String emergencyPhone,
        String emergencyRelation,
        Boolean hkMoTw,
        Boolean specialStudent,
        String fatherName,
        String fatherPhone,
        String fatherWorkUnit,
        String fatherTitle,
        String motherName,
        String motherPhone,
        String motherWorkUnit,
        String motherTitle
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
        this.address = address;
        this.idNo = idNo;
        this.nativePlace = nativePlace;
        this.dormCampus = dormCampus;
        this.dormBuilding = dormBuilding;
        this.dormRoom = dormRoom;
        this.classTeacher = classTeacher;
        this.counselor = counselor;
        this.leagueNo = leagueNo;
        this.leagueApplicationDate = leagueApplicationDate;
        this.leagueJoinDate = leagueJoinDate;
        this.partyApplied = partyApplied;
        this.notDeveloped = notDeveloped;
        this.applicationDate = applicationDate;
        this.activistDate = activistDate;
        this.partyTrainingDate = partyTrainingDate;
        this.developmentTargetDate = developmentTargetDate;
        this.probationaryMemberDate = probationaryMemberDate;
        this.fullMemberDate = fullMemberDate;
        this.emergencyPhone = emergencyPhone;
        this.emergencyRelation = emergencyRelation;
        this.hkMoTw = hkMoTw;
        this.specialStudent = specialStudent;
        this.fatherName = fatherName;
        this.fatherPhone = fatherPhone;
        this.fatherWorkUnit = fatherWorkUnit;
        this.fatherTitle = fatherTitle;
        this.motherName = motherName;
        this.motherPhone = motherPhone;
        this.motherWorkUnit = motherWorkUnit;
        this.motherTitle = motherTitle;
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

    public String getAddress() {
        return address;
    }

    public String getIdNo() {
        return idNo;
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

    public LocalDate getPartyTrainingDate() {
        return partyTrainingDate;
    }

    public LocalDate getDevelopmentTargetDate() {
        return developmentTargetDate;
    }

    public LocalDate getProbationaryMemberDate() {
        return probationaryMemberDate;
    }

    public LocalDate getFullMemberDate() {
        return fullMemberDate;
    }

    public String getEmergencyPhone() {
        return emergencyPhone;
    }

    public String getEmergencyRelation() {
        return emergencyRelation;
    }

    public Boolean getHkMoTw() {
        return hkMoTw;
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
}
