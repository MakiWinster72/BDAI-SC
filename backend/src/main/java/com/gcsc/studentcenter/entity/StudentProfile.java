package com.gcsc.studentcenter.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "student_profiles")
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;

    @Column(name = "full_name", length = 64)
    private String fullName;

    @Column(name = "student_no", length = 32)
    private String studentNo;

    @Column(name = "class_year")
    private Integer classYear;

    @Column(name = "class_major", length = 64)
    private String classMajor;

    @Column(name = "class_no", length = 16)
    private String classNo;

    @Column(name = "class_name", length = 64)
    private String className;

    @Column(name = "college", length = 64)
    private String college;

    @Column(name = "enrollment_date")
    private LocalDate enrollmentDate;

    @Column(name = "student_category", length = 32)
    private String studentCategory;

    @Column(name = "ethnicity", length = 32)
    private String ethnicity;

    @Column(name = "political_status", length = 32)
    private String politicalStatus;

    @Column(name = "dorm_campus", length = 64)
    private String dormCampus;

    @Column(name = "dorm_building", length = 64)
    private String dormBuilding;

    @Column(name = "dorm_room", length = 64)
    private String dormRoom;

    @Column(name = "class_teacher", length = 64)
    private String classTeacher;

    @Column(name = "counselor", length = 64)
    private String counselor;

    @Column(name = "phone", length = 32)
    private String phone;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "id_no", length = 32)
    private String idNo;

    @Column(name = "native_place", length = 64)
    private String nativePlace;

    @Column(name = "league_no", length = 32)
    private String leagueNo;

    @Column(name = "league_application_date")
    private LocalDate leagueApplicationDate;

    @Column(name = "league_join_date")
    private LocalDate leagueJoinDate;

    @Column(name = "party_applied")
    private Boolean partyApplied;

    @Column(name = "not_developed")
    private Boolean notDeveloped;

    @Column(name = "application_date")
    private LocalDate applicationDate;

    @Column(name = "activist_date")
    private LocalDate activistDate;

    @Column(name = "party_training_date")
    private LocalDate partyTrainingDate;

    @Column(name = "development_target_date")
    private LocalDate developmentTargetDate;

    @Column(name = "probationary_member_date")
    private LocalDate probationaryMemberDate;

    @Column(name = "full_member_date")
    private LocalDate fullMemberDate;

    @Column(name = "emergency_phone", length = 32)
    private String emergencyPhone;

    @Column(name = "emergency_relation", length = 32)
    private String emergencyRelation;

    @Column(name = "is_hk_mo_tw")
    private Boolean hkMoTw;

    @Column(name = "is_special")
    private Boolean specialStudent;

    @Column(name = "father_name", length = 64)
    private String fatherName;

    @Column(name = "father_phone", length = 32)
    private String fatherPhone;

    @Column(name = "father_work_unit", length = 128)
    private String fatherWorkUnit;

    @Column(name = "father_title", length = 64)
    private String fatherTitle;

    @Column(name = "mother_name", length = 64)
    private String motherName;

    @Column(name = "mother_phone", length = 32)
    private String motherPhone;

    @Column(name = "mother_work_unit", length = 128)
    private String motherWorkUnit;

    @Column(name = "mother_title", length = 64)
    private String motherTitle;

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIdNo() {
        return idNo;
    }

    public void setIdNo(String idNo) {
        this.idNo = idNo;
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

    public LocalDate getPartyTrainingDate() {
        return partyTrainingDate;
    }

    public void setPartyTrainingDate(LocalDate partyTrainingDate) {
        this.partyTrainingDate = partyTrainingDate;
    }

    public LocalDate getDevelopmentTargetDate() {
        return developmentTargetDate;
    }

    public void setDevelopmentTargetDate(LocalDate developmentTargetDate) {
        this.developmentTargetDate = developmentTargetDate;
    }

    public LocalDate getProbationaryMemberDate() {
        return probationaryMemberDate;
    }

    public void setProbationaryMemberDate(LocalDate probationaryMemberDate) {
        this.probationaryMemberDate = probationaryMemberDate;
    }

    public LocalDate getFullMemberDate() {
        return fullMemberDate;
    }

    public void setFullMemberDate(LocalDate fullMemberDate) {
        this.fullMemberDate = fullMemberDate;
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

    public Boolean getHkMoTw() {
        return hkMoTw;
    }

    public void setHkMoTw(Boolean hkMoTw) {
        this.hkMoTw = hkMoTw;
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
}
