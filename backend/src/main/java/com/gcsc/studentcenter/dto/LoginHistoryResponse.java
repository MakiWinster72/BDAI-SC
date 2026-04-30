package com.gcsc.studentcenter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class LoginHistoryResponse {

    private final String ipAddress;
    private final String deviceName;
    private final String browser;
    private final String os;
    private final LocalDateTime loginTime;

    public LoginHistoryResponse(
        @JsonProperty("ipAddress") String ipAddress,
        @JsonProperty("deviceName") String deviceName,
        @JsonProperty("browser") String browser,
        @JsonProperty("os") String os,
        @JsonProperty("loginTime") LocalDateTime loginTime
    ) {
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
