package com.gcsc.studentcenter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class LoginRequest {

  @NotBlank(message = "用户名不能为空")
  @Size(min = 4, max = 32, message = "用户名长度需在4-32之间")
  @Pattern(regexp = "^[a-zA-Z0-9_]{4,32}$", message = "用户名只能包含字母、数字、下划线")
  private String username;

  @NotBlank(message = "密码不能为空")
  @Size(min = 6, max = 32, message = "密码长度需在6-32之间")
  private String password;

  @NotBlank(message = "验证码标识不能为空")
  private String captchaId;

  @NotBlank(message = "验证码不能为空")
  @Size(min = 4, max = 4, message = "验证码长度不正确")
  private String captchaCode;

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

  public String getCaptchaId() {
    return captchaId;
  }

  public void setCaptchaId(String captchaId) {
    this.captchaId = captchaId;
  }

  public String getCaptchaCode() {
    return captchaCode;
  }

  public void setCaptchaCode(String captchaCode) {
    this.captchaCode = captchaCode;
  }
}
