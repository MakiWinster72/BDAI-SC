package com.gcsc.studentcenter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

  @NotBlank(message = "显示名称不能为空")
  @Size(min = 2, max = 64, message = "显示名称长度需在2-64之间")
  private String displayName;

  @NotBlank(message = "用户名不能为空")
  @Size(min = 4, max = 32, message = "用户名长度需在4-32之间")
  @Pattern(regexp = "^[a-zA-Z0-9_]{4,32}$", message = "用户名只能包含字母、数字、下划线")
  private String username;

  @NotBlank(message = "密码不能为空")
  @Size(min = 6, max = 32, message = "密码长度需在6-32之间")
  private String password;

  private String role;
  private String studentNo;
  private String className;
  private String college;

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

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
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
}
