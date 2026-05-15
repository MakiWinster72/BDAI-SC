package com.gcsc.studentcenter.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "achievement_san_san_xiang")
public class AchievementSanSanXiang {

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

  @Column(name = "college", length = 128)
  private String college;

  @Column(name = "team_name", length = 255)
  private String teamName;

  @Column(name = "project_name", nullable = false, length = 255)
  private String projectName;

  @Column(name = "service_category", length = 128)
  private String serviceCategory;

  @Column(name = "is_paired", length = 16)
  private String isPaired;

  @Column(name = "project_type", length = 64)
  private String projectType;

  @Column(name = "team_leader", length = 64)
  private String teamLeader;

  @Column(name = "team_members", length = 512)
  private String teamMembers;

  @Column(name = "team_size", length = 16)
  private String teamSize;

  @Column(name = "practice_days", length = 16)
  private String practiceDays;

  @Column(name = "instructor", length = 128)
  private String instructor;

  @Column(name = "project_level", length = 64)
  private String projectLevel;

  @Column(name = "final_level", length = 64)
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

  public String getCollege() {
    return college;
  }

  public void setCollege(String college) {
    this.college = college;
  }

  public String getTeamName() {
    return teamName;
  }

  public void setTeamName(String teamName) {
    this.teamName = teamName;
  }

  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  public String getServiceCategory() {
    return serviceCategory;
  }

  public void setServiceCategory(String serviceCategory) {
    this.serviceCategory = serviceCategory;
  }

  public String getIsPaired() {
    return isPaired;
  }

  public void setIsPaired(String isPaired) {
    this.isPaired = isPaired;
  }

  public String getProjectType() {
    return projectType;
  }

  public void setProjectType(String projectType) {
    this.projectType = projectType;
  }

  public String getTeamLeader() {
    return teamLeader;
  }

  public void setTeamLeader(String teamLeader) {
    this.teamLeader = teamLeader;
  }

  public String getTeamMembers() {
    return teamMembers;
  }

  public void setTeamMembers(String teamMembers) {
    this.teamMembers = teamMembers;
  }

  public String getTeamSize() {
    return teamSize;
  }

  public void setTeamSize(String teamSize) {
    this.teamSize = teamSize;
  }

  public String getPracticeDays() {
    return practiceDays;
  }

  public void setPracticeDays(String practiceDays) {
    this.practiceDays = practiceDays;
  }

  public String getInstructor() {
    return instructor;
  }

  public void setInstructor(String instructor) {
    this.instructor = instructor;
  }

  public String getProjectLevel() {
    return projectLevel;
  }

  public void setProjectLevel(String projectLevel) {
    this.projectLevel = projectLevel;
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