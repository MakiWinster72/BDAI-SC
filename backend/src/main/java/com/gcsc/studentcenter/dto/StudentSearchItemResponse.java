package com.gcsc.studentcenter.dto;

public class StudentSearchItemResponse {
  private Long id;
  private String fullName;
  private String studentNo;
  private Integer classYear;
  private String classMajor;
  private String classNo;
  private String college;
  private Boolean isHk;
  private Boolean isMo;
  private Boolean isTw;
  private Boolean specialStudent;
  private String specialStudentType;
  private String avatarUrl;

  public StudentSearchItemResponse(
      Long id,
      String fullName,
      String studentNo,
      Integer classYear,
      String classMajor,
      String classNo,
      String college,
      Boolean isHk,
      Boolean isMo,
      Boolean isTw,
      Boolean specialStudent,
      String specialStudentType,
      String avatarUrl) {
    this.id = id;
    this.fullName = fullName;
    this.studentNo = studentNo;
    this.classYear = classYear;
    this.classMajor = classMajor;
    this.classNo = classNo;
    this.college = college;
    this.isHk = isHk;
    this.isMo = isMo;
    this.isTw = isTw;
    this.specialStudent = specialStudent;
    this.specialStudentType = specialStudentType;
    this.avatarUrl = avatarUrl;
  }

  public Long getId() {
    return id;
  }

  public String getFullName() {
    return fullName;
  }

  public String getStudentNo() {
    return studentNo;
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

  public String getCollege() {
    return college;
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

  public String getSpecialStudentType() {
    return specialStudentType;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }
}
