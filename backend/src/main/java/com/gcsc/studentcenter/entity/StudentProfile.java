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

    @Column(name = "party_applied")
    private Boolean partyApplied;

    @Column(name = "not_developed")
    private Boolean notDeveloped;

    @Column(name = "application_date")
    private LocalDate applicationDate;

    @Column(name = "activist_date")
    private LocalDate activistDate;

    @Column(name = "emergency_phone", length = 32)
    private String emergencyPhone;

    @Column(name = "emergency_relation", length = 32)
    private String emergencyRelation;

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
