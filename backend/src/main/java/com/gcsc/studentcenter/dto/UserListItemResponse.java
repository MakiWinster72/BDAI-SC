package com.gcsc.studentcenter.dto;

import com.gcsc.studentcenter.entity.AppUser;
import com.gcsc.studentcenter.entity.UserRole;

import java.time.LocalDateTime;

public class UserListItemResponse {

  private Long id;
  private String username;
  private String displayName;
  private UserRole role;
  private String studentNo;
  private String className;
  private String college;
  private String avatarUrl;
  private LocalDateTime createdAt;
  private String assignedClasses;
  private String remark;

  public UserListItemResponse() {
  }

  public UserListItemResponse(AppUser user) {
    this.id = user.getId();
    this.username = user.getUsername();
    this.displayName = user.getDisplayName();
    this.role = user.getRole();
    this.studentNo = user.getStudentNo();
    this.className = user.getClassName();
    this.college = user.getCollege();
    this.avatarUrl = user.getAvatarUrl();
    this.createdAt = user.getCreatedAt();
    this.assignedClasses = user.getAssignedClasses();
    this.remark = user.getRemark();
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

  public UserRole getRole() {
    return role;
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

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public String getAssignedClasses() {
    return assignedClasses;
  }

  public String getRemark() {
    return remark;
  }
}
