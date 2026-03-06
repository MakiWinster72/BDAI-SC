package com.gcsc.studentcenter.dto;

public class AuthResponse {
    private final boolean success;
    private final String message;
    private final String username;
    private final String displayName;

    public AuthResponse(boolean success, String message, String username, String displayName) {
        this.success = success;
        this.message = message;
        this.username = username;
        this.displayName = displayName;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }
}
