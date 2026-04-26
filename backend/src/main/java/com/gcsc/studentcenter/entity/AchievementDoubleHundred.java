package com.gcsc.studentcenter.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "achievement_double_hundreds")
public class AchievementDoubleHundred {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id", nullable = false)
    private AppUser author;

    @Column(name = "student_no", length = 32)
    private String studentNo;

    @Column(name = "student_name", length = 64)
    private String studentName;

    @Column(name = "project_category", length = 64)
    private String projectCategory;

    @Column(name = "project_domain", length = 128)
    private String projectDomain;

    @Column(name = "project_name", nullable = false, length = 255)
    private String projectName;

    @Column(name = "project_leader", length = 64)
    private String projectLeader;

    @Column(name = "leader_student_no", length = 32)
    private String leaderStudentNo;

    @Column(name = "education_level", length = 32)
    private String educationLevel;

    @Column(name = "team_members", length = 255)
    private String teamMembers;

    @Column(name = "instructors", length = 255)
    private String instructors;

    @Column(name = "team_size", length = 16)
    private String teamSize;

    @Column(name = "planned_level", length = 32)
    private String plannedLevel;

    @Column(name = "college", length = 128)
    private String college;

    @Column(name = "final_level", length = 32)
    private String finalLevel;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "_image_urls", columnDefinition = "TEXT")
    private String imageUrls;

    @Column(name = "_attachments", columnDefinition = "TEXT")
    private String attachments;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public AppUser getAuthor() {
        return author;
    }

    public void setAuthor(AppUser author) {
        this.author = author;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getProjectCategory() {
        return projectCategory;
    }

    public void setProjectCategory(String projectCategory) {
        this.projectCategory = projectCategory;
    }

    public String getProjectDomain() {
        return projectDomain;
    }

    public void setProjectDomain(String projectDomain) {
        this.projectDomain = projectDomain;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectLeader() {
        return projectLeader;
    }

    public void setProjectLeader(String projectLeader) {
        this.projectLeader = projectLeader;
    }

    public String getLeaderStudentNo() {
        return leaderStudentNo;
    }

    public void setLeaderStudentNo(String leaderStudentNo) {
        this.leaderStudentNo = leaderStudentNo;
    }

    public String getEducationLevel() {
        return educationLevel;
    }

    public void setEducationLevel(String educationLevel) {
        this.educationLevel = educationLevel;
    }

    public String getTeamMembers() {
        return teamMembers;
    }

    public void setTeamMembers(String teamMembers) {
        this.teamMembers = teamMembers;
    }

    public String getInstructors() {
        return instructors;
    }

    public void setInstructors(String instructors) {
        this.instructors = instructors;
    }

    public String getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(String teamSize) {
        this.teamSize = teamSize;
    }

    public String getPlannedLevel() {
        return plannedLevel;
    }

    public void setPlannedLevel(String plannedLevel) {
        this.plannedLevel = plannedLevel;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public String getFinalLevel() {
        return finalLevel;
    }

    public void setFinalLevel(String finalLevel) {
        this.finalLevel = finalLevel;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(String imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
