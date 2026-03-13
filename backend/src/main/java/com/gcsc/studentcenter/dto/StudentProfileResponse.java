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
    private Boolean hkMoTw;
    private Boolean specialStudent;

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
        String phone,
        String address,
        String idNo,
        String nativePlace,
        String leagueNo,
        Boolean partyApplied,
        Boolean notDeveloped,
        LocalDate applicationDate,
        LocalDate activistDate,
        String emergencyPhone,
        String emergencyRelation,
        Boolean hkMoTw,
        Boolean specialStudent
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
        this.phone = phone;
        this.address = address;
        this.idNo = idNo;
        this.nativePlace = nativePlace;
        this.leagueNo = leagueNo;
        this.partyApplied = partyApplied;
        this.notDeveloped = notDeveloped;
        this.applicationDate = applicationDate;
        this.activistDate = activistDate;
        this.emergencyPhone = emergencyPhone;
        this.emergencyRelation = emergencyRelation;
        this.hkMoTw = hkMoTw;
        this.specialStudent = specialStudent;
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

    public String getLeagueNo() {
        return leagueNo;
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
}
