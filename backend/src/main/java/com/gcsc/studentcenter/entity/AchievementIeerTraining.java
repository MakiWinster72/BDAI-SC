package com.gcsc.studentcenter.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "achievement_ieer_trainings")
public class AchievementIeerTraining {

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

  @Column(name = "college_name", length = 128)
  private String collegeName;

  @Column(name = "project_name", nullable = false, length = 255)
  private String projectName;

  @Column(name = "project_type", length = 64)
  private String projectType;

  @Column(name = "project_leader", length = 64)
  private String projectLeader;

  @Column(name = "instructor_name", length = 128)
  private String instructorName;

  @Column(name = "recommended_level", length = 64)
  private String recommendedLevel;

  @Column(name = "is_key_area", length = 16)
  private String isKeyArea;

  @Column(name = "final_status", length = 32)
  private String finalStatus;

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

  public String getCollegeName() {
    return collegeName;
  }

  public void setCollegeName(String collegeName) {
    this.collegeName = collegeName;
  }

  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  public String getProjectType() {
    return projectType;
  }

  public void setProjectType(String projectType) {
    this.projectType = projectType;
  }

  public String getProjectLeader() {
    return projectLeader;
  }

  public void setProjectLeader(String projectLeader) {
    this.projectLeader = projectLeader;
  }

  public String getInstructorName() {
    return instructorName;
  }

  public void setInstructorName(String instructorName) {
    this.instructorName = instructorName;
  }

  public String getRecommendedLevel() {
    return recommendedLevel;
  }

  public void setRecommendedLevel(String recommendedLevel) {
    this.recommendedLevel = recommendedLevel;
  }

  public String getIsKeyArea() {
    return isKeyArea;
  }

  public void setIsKeyArea(String isKeyArea) {
    this.isKeyArea = isKeyArea;
  }

  public String getFinalStatus() {
    return finalStatus;
  }

  public void setFinalStatus(String finalStatus) {
    this.finalStatus = finalStatus;
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
