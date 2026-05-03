package com.gcsc.studentcenter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequest {

  @NotBlank(message = "旧密码不能为空")
  private String oldPassword;

  @NotBlank(message = "新密码不能为空")
  @Size(min = 6, max = 32, message = "新密码长度必须在6-32个字符之间")
  private String newPassword;

  public String getOldPassword() {
    return oldPassword;
  }

  public void setOldPassword(String oldPassword) {
    this.oldPassword = oldPassword;
  }

  public String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }
}
