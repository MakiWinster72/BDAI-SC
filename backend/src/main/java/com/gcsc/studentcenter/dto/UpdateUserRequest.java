package com.gcsc.studentcenter.dto;

import com.gcsc.studentcenter.entity.UserRole;

public class UpdateUserRequest {

    private String username;
    private String password;
    private UserRole role;
    private String remark;
    private String assignedClasses;

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

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getAssignedClasses() {
        return assignedClasses;
    }

    public void setAssignedClasses(String assignedClasses) {
        this.assignedClasses = assignedClasses;
    }
}
