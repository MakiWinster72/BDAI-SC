package com.gcsc.studentcenter.dto;

public class UserProfileResponse {
    private final String username;
    private final String displayName;

    public UserProfileResponse(String username, String displayName) {
        this.username = username;
        this.displayName = displayName;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }
}
