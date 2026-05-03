package com.gcsc.studentcenter.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class AppUser {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "display_name", nullable = false, length = 64)
  private String displayName;

  @Column(name = "username", nullable = false, unique = true, length = 32)
  private String username;

  @Column(name = "password_hash", nullable = false, length = 100)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false, length = 16)
  private UserRole role;

  @Column(name = "student_no", length = 32)
  private String studentNo;

  @Column(name = "class_name", length = 64)
  private String className;

  @Column(name = "college", length = 64)
  private String college;

  @Column(name = "avatar_url", length = 255)
  private String avatarUrl;

  @Column(name = "assigned_classes", length = 500)
  private String assignedClasses;

  @Column(name = "remark", length = 255)
  private String remark;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  public Long getId() {
    return id;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public UserRole getRole() {
    return role;
  }

  public void setRole(UserRole role) {
    this.role = role;
  }

  public String getStudentNo() {
    return studentNo;
  }

  public void setStudentNo(String studentNo) {
    this.studentNo = studentNo;
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

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public void setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
  }

  public String getAssignedClasses() {
    return assignedClasses;
  }

  public void setAssignedClasses(String assignedClasses) {
    this.assignedClasses = assignedClasses;
  }

  public String getRemark() {
    return remark;
  }

  public void setRemark(String remark) {
    this.remark = remark;
  }
}
