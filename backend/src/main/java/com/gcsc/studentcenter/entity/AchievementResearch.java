package com.gcsc.studentcenter.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "achievement_researches")
public class AchievementResearch {

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

  @Column(name = "project_name", nullable = false, length = 255)
  private String projectName;

  @Column(name = "teacher_no", length = 64)
  private String teacherNo;

  @Column(name = "project_leader", length = 64)
  private String projectLeader;

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

  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  public String getTeacherNo() {
    return teacherNo;
  }

  public void setTeacherNo(String teacherNo) {
    this.teacherNo = teacherNo;
  }

  public String getProjectLeader() {
    return projectLeader;
  }

  public void setProjectLeader(String projectLeader) {
    this.projectLeader = projectLeader;
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
