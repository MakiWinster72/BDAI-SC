package com.gcsc.studentcenter.dto;

public class AuthResponse {
    private final boolean success;
    private final String message;
    private final String username;
    private final String displayName;
    private final String token;
    private final String tokenType;

    public AuthResponse(boolean success, String message, String username, String displayName, String token) {
        this.success = success;
        this.message = message;
        this.username = username;
        this.displayName = displayName;
        this.token = token;
        this.tokenType = "Bearer";
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

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }
}
