package com.gcsc.studentcenter.dto;

import java.time.LocalDateTime;

public class LastLoginInfo {
  private final String ipAddress;
  private final String deviceName;
  private final String browser;
  private final String os;
  private final LocalDateTime loginTime;

  public LastLoginInfo(
      String ipAddress,
      String deviceName,
      String browser,
      String os,
      LocalDateTime loginTime) {
    this.ipAddress = ipAddress;
    this.deviceName = deviceName;
    this.browser = browser;
    this.os = os;
    this.loginTime = loginTime;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public String getDeviceName() {
    return deviceName;
  }

  public String getBrowser() {
    return browser;
  }

  public String getOs() {
    return os;
  }

  public LocalDateTime getLoginTime() {
    return loginTime;
  }
}
