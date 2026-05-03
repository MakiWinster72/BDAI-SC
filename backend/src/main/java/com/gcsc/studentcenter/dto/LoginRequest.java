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
}
