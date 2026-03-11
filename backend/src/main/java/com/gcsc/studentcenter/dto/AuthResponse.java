package com.gcsc.studentcenter.dto;

public class AuthResponse {
    private final boolean success;
    private final String message;
    private final String username;
    private final String displayName;
    private final String role;
    private final String studentNo;
    private final String className;
    private final String college;
    private final String avatarUrl;
    private final String token;
    private final String tokenType;

    public AuthResponse(
        boolean success,
        String message,
        String username,
        String displayName,
        String role,
        String studentNo,
        String className,
        String college,
        String avatarUrl,
        String token
    ) {
        this.success = success;
        this.message = message;
        this.username = username;
        this.displayName = displayName;
        this.role = role;
        this.studentNo = studentNo;
        this.className = className;
        this.college = college;
        this.avatarUrl = avatarUrl;
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

    public String getRole() {
        return role;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public String getClassName() {
        return className;
    }

    public String getCollege() {
        return college;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }
}
