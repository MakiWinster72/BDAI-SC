package com.gcsc.studentcenter.dto;

import java.time.LocalDateTime;

public class LoginHistoryResponse {

    private final String ipAddress;
    private final String deviceName;
    private final LocalDateTime loginTime;

    public LoginHistoryResponse(
        String ipAddress,
        String deviceName,
        LocalDateTime loginTime
    ) {
        this.ipAddress = ipAddress;
        this.deviceName = deviceName;
        this.loginTime = loginTime;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }
}
