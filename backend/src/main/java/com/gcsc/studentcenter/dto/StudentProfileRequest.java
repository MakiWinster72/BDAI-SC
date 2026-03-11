package com.gcsc.studentcenter.dto;

import java.time.LocalDate;

public class StudentProfileRequest {
    private String fullName;
    private String avatarUrl;
    private String studentNo;
    private Integer classYear;
    private String classMajor;
    private String classNo;
    private String className;
    private String college;
    private String phone;
    private String address;
    private String idNo;
    private String nativePlace;
    private String leagueNo;
    private Boolean partyApplied;
    private Boolean notDeveloped;
    private LocalDate applicationDate;
    private LocalDate activistDate;
    private String emergencyPhone;
    private String emergencyRelation;

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
}
