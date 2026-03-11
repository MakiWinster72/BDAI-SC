package com.gcsc.studentcenter.dto;

public class UserProfileResponse {
    private final String username;
    private final String displayName;
    private final String role;
    private final String studentNo;
    private final String className;
    private final String college;
    private final String avatarUrl;

    public UserProfileResponse(
        String username,
        String displayName,
        String role,
        String studentNo,
        String className,
        String college,
        String avatarUrl
    ) {
        this.username = username;
        this.displayName = displayName;
        this.role = role;
        this.studentNo = studentNo;
        this.className = className;
        this.college = college;
        this.avatarUrl = avatarUrl;
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
}
