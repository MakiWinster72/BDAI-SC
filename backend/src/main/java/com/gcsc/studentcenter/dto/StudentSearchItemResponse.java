package com.gcsc.studentcenter.dto;

public class StudentSearchItemResponse {
    private Long id;
    private String fullName;
    private String studentNo;
    private Integer classYear;
    private String classMajor;
    private String classNo;
    private String college;

    public StudentSearchItemResponse(
        Long id,
        String fullName,
        String studentNo,
        Integer classYear,
        String classMajor,
        String classNo,
        String college
    ) {
        this.id = id;
        this.fullName = fullName;
        this.studentNo = studentNo;
        this.classYear = classYear;
        this.classMajor = classMajor;
        this.classNo = classNo;
        this.college = college;
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
}
